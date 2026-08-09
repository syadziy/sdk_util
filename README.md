# sdk-util

Shared Java SDK untuk menstandarkan response REST, exception handling, structured logging,
trace ID, Spring Security OAuth2/JWT, OpenAPI, timezone, serta utility umum antar-service.

## Compatibility

| Component | Version |
| --- | --- |
| Java | 21 |
| Spring Boot BOM | 4.1.0 |
| Springdoc OpenAPI | 2.8.0 |
| Artifact | `com.mac:sdk-util:1.0.0` |

## Installation

Tambahkan dependency berikut pada service Spring Boot:

```xml
<dependency>
    <groupId>com.mac</groupId>
    <artifactId>sdk-util</artifactId>
    <version>1.0.0</version>
</dependency>
```

Untuk development lokal, build dan install artifact terlebih dahulu:

```bash
mvn clean install
```

Service REST yang memakai global exception handler harus memiliki Spring MVC dan Jakarta
Validation, umumnya melalui:

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-web</artifactId>
</dependency>
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-validation</artifactId>
</dependency>
```

Logging method berbasis AOP membutuhkan `spring-boot-starter-aop` pada service consumer.

## Features

| Feature | Default | Aktivasi utama |
| --- | --- | --- |
| Standard response | Tersedia | Gunakan `ResponseHelper` atau `ResponsePagingHelper` |
| Global REST exception handler | Aktif | Aplikasi servlet dan Spring MVC tersedia |
| ECS structured logging | Aktif | `sdk.logging.structured.enabled=true` |
| HTTP trace ID/MDC filter | Aktif | Structured logging dan filter aktif |
| Service method AOP logging | Tidak efektif sampai package diisi | Isi `sdk.logging.aspect.packages` |
| OAuth2/JWT web security | Aktif | `sdk.security.enabled=true` |
| Method security | Aktif | `sdk.security.method-security-enabled=true` |
| CORS | Aktif dan permisif | `sdk.security.cors.enabled=true` |
| OpenAPI/Swagger UI | Aktif | `sdk.openapi.enabled=true` |
| Application timezone | Aktif | `sdk.timezone`, lalu fallback lainnya |

Semua auto-configuration didaftarkan melalui
`META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports`; consumer tidak
perlu menambahkan `@ComponentScan` untuk package `com.mac.sdk_util`.

Expression `@PreAuthorize` lintas service tersedia sebagai constant pada
`com.mac.sdk_util.entities.constant.Role`. Controller harus menggunakan constant tersebut agar nama
authority konsisten, misalnya `@PreAuthorize(Role.AUDIT_READ)` atau
`@PreAuthorize(Role.SCHEDULER_MANAGE)`.

Response helper diimpor dari `com.mac.sdk_util.helper.ResponseHelper` dan
`com.mac.sdk_util.helper.ResponsePagingHelper`.

## Package structure

```text
com/mac/sdk_util/
├── config/       # Spring Boot auto-configuration dan properties
├── entities/     # Shared constants, response DTO, dan paging DTO
├── exception/    # Shared exceptions
├── helper/       # ResponseHelper dan ResponsePagingHelper
├── openapi/      # OpenAPI conditions dan environment defaults
├── securities/   # JWT converter, servlet responses, dan security customization SPI
├── utils/        # Structured logging, MDC, date, string, dan query utilities
└── web/          # GlobalExceptionHandler
```

`helper` digunakan khusus untuk membangun standard REST response dan paging response. Utility umum
yang tidak membentuk response tetap berada di `utils`.

## Quick start configuration

Contoh konfigurasi service yang menggunakan fitur utama:

```yaml
sdk:
  timezone: UTC

  web:
    exception-handler:
      enabled: true

  logging:
    structured:
      enabled: true
      format: ecs
      auto-configure-format: true
      trace-header: X-Correlation-Id
      trace-response-header: true
      filter:
        enabled: true
        order: -100
    aspect:
      enabled: true
      packages: com.mac.orders.service,com.mac.orders.repository

  security:
    enabled: true
    csrf-disabled: true
    jwt-issuer-uri: http://localhost:9005
    session-creation-policy: STATELESS
    path-prefix: /orders
    permit-all-paths:
      - /actuator/health/**
      - /v3/api-docs/**
      - /swagger-ui/**
      - /error
    method-security-enabled: true
    cors:
      enabled: false

  openapi:
    enabled: true
    title: Orders API
    description: REST API for order management
    version: 1.0.0

jwt:
  auth:
    converter:
      principle-attribute: preferred_username
      resource-id: orders
```

## Standard REST response

`ResponseDTO<T>` menghasilkan kontrak JSON berikut:

```json
{
  "code": "RC-200",
  "message": "success",
  "data": {},
  "errors": [],
  "paging": {
    "limit": 20,
    "offset": 0,
    "total_record": 100
  }
}
```

`paging` tidak ditulis ke JSON ketika nilainya `null`. Field lain mengikuti konfigurasi Jackson
service consumer.

### ResponseHelper

Source helper ini dikelompokkan dalam direktori `helper` dan menghasilkan
`ResponseEntity<ResponseDTO<T>>` dengan status serta response code yang konsisten.

| Method | HTTP status | Response code |
| --- | --- | --- |
| `httpOK()` / `httpOK(body)` | 200 | `RC-200` |
| `httpCreated(body, location)` | 201 | `RC-201` |
| `httpAccepted(body)` | 202 | `RC-202` |
| `httpBadRequest(...)` | 400 | `RC-400` |
| `httpUnauthorized()` | 401 | `RC-401` |
| `httpForbidden()` | 403 | `RC-403` |
| `httpNotFound(...)` | 404 | `RC-404` |
| `httpConflict(body)` | 409 | `RC-409` |
| `httpInternalServerError()` | 500 | `RC-500` |
| `httpServiceUnavailable()` | 503 | `RC-503` |

Contoh controller:

```java
@PostMapping("/orders")
public ResponseEntity<ResponseDTO<CreateOrderResponse>> create(
        @Valid @RequestBody CreateOrderRequest request) {
    CreateOrderResponse result = orderService.create(request);
    URI location = URI.create("/api/v1/orders/" + result.id());
    return ResponseHelper.httpCreated(result, location);
}
```

### ResponsePagingHelper

Gunakan `ResponsePagingHelper.httpOK(body, paging)` untuk endpoint list:

```java
PagingDTO paging = new PagingDTO(20, 0, 125);
return ResponsePagingHelper.httpOK(items, paging);
```

Helper ini juga menyediakan `httpNotFound()`, `httpMethodNotAllowed()`, dan `httpBadRequest()`.

## Global REST exception handler

Pada aplikasi servlet, SDK otomatis mendaftarkan `GlobalExceptionHandler`. Semua response error
menggunakan `ResponseDTO` dan message standar berbahasa Inggris.

| Exception | HTTP status | Perilaku |
| --- | --- | --- |
| `ResourceNotFoundException` | 404 | Menampilkan message exception atau `Resource not found` |
| `MethodArgumentNotValidException` | 400 | Menghasilkan daftar `field: validation message` |
| `HttpMessageNotReadableException` | 400 | Menghasilkan `requestBody: invalid or unreadable JSON` |
| `MethodArgumentTypeMismatchException` | 400 | Menjelaskan parameter dan tipe yang diharapkan |
| `ConstraintViolationException` | 400 | Menghasilkan daftar constraint violation terurut |
| `IllegalArgumentException` | 400 | Menampilkan message exception atau `Invalid input` |
| Exception lainnya | 500 | Menyembunyikan detail internal dan menulis structured error log |

Gunakan exception bersama untuk resource yang tidak ditemukan:

```java
import com.mac.sdk_util.exception.ResourceNotFoundException;

Order order = repository.findById(id)
        .orElseThrow(() -> new ResourceNotFoundException("Order was not found"));
```

Handler SDK memiliki `Ordered.LOWEST_PRECEDENCE`. Handler domain milik service harus menggunakan
precedence lebih tinggi:

```java
@RestControllerAdvice
@Order(Ordered.HIGHEST_PRECEDENCE)
public class OrderExceptionHandler {

    @ExceptionHandler(OrderStateException.class)
    ResponseEntity<ResponseDTO<Void>> handleOrderState(OrderStateException exception) {
        return ResponseHelper.httpConflict(null);
    }
}
```

Untuk mengganti seluruh kebijakan exception HTTP:

```yaml
sdk:
  web:
    exception-handler:
      enabled: false
```

Global REST handler hanya berlaku pada request Spring MVC. Kafka listener, scheduler,
`CompletableFuture`, executor, dan virtual thread harus memasang error handler pada boundary
masing-masing.

## Structured logging and trace ID

SDK mengaktifkan format ECS untuk console dan file melalui default berikut, kecuali service sudah
menentukan nilainya sendiri:

```yaml
logging:
  structured:
    format:
      console: ecs
      file: ecs
    ecs:
      service:
        environment: ${spring.profiles.active:default}
```

Format tersebut dikontrol oleh:

| Property | Default | Keterangan |
| --- | --- | --- |
| `sdk.logging.structured.enabled` | `true` | Mengaktifkan structured logging SDK |
| `sdk.logging.structured.format` | `ecs` | Format Spring Boot structured logging |
| `sdk.logging.structured.auto-configure-format` | `true` | Mengisi default format console/file |
| `sdk.logging.structured.trace-header` | `X-Correlation-Id` | Header trace masuk dan keluar |
| `sdk.logging.structured.trace-response-header` | `true` | Menulis trace ID ke response |
| `sdk.logging.structured.filter.enabled` | `true` | Mengaktifkan filter MDC HTTP |
| `sdk.logging.structured.filter.order` | `-100` | Urutan servlet filter |

Untuk setiap request HTTP, `MdcLoggingFilter`:

1. Membaca trace ID dari header yang dikonfigurasi.
2. Membuat UUID bila header kosong.
3. Menaruh `trace.id` dan `event.dataset=http` ke MDC.
4. Mengembalikan trace ID pada response jika diaktifkan.
5. Membersihkan MDC setelah request selesai.

### Manual structured log

Gunakan nama field ECS dari `LogFields` bila tersedia:

```java
private static final Logger LOG = LoggerFactory.getLogger(OrderService.class);

StructuredLog.info(LOG, "Order created", Map.of(
        LogFields.EVENT_ACTION, "createOrder",
        LogFields.EVENT_OUTCOME, LogFields.OUTCOME_SUCCESS,
        LogFields.EVENT_DATASET, "orders",
        "order.id", orderId));
```

Tersedia method `debug`, `info`, `warn`, `error`, dan overload `error` dengan `Throwable`.

### MDC on asynchronous work

MDC tidak otomatis berpindah ke executor, scheduler, Kafka listener, `CompletableFuture`, atau
virtual thread. Copy context sebelum task dibuat, kemudian jalankan task melalui `withMdc`:

```java
Map<String, String> taskContext = StructuredLog.copyMdc();

executor.submit(() -> StructuredLog.withMdc(taskContext, () -> {
    StructuredLog.info(LOG, "Async order processing started", Map.of(
            LogFields.EVENT_ACTION, "processOrder",
            "order.id", orderId));
}));
```

Untuk event tanpa HTTP request, buat trace ID sendiri:

```java
StructuredLog.withMdc(
        Map.of(
                LogFields.TRACE_ID, UUID.randomUUID().toString(),
                LogFields.EVENT_DATASET, "orders.scheduler"),
        this::runScheduledJob);
```

`withMdc` mengganti seluruh MDC pada thread selama action berjalan, lalu memulihkan context lama di
blok `finally`. Tambahkan field yang ingin dipertahankan ke map sebelum memanggilnya.

## Automatic method logging with AOP

Isi package service atau repository yang akan diintercept:

```yaml
sdk:
  logging:
    aspect:
      enabled: true
      packages: com.mac.orders.service,com.mac.orders.repository
```

Package otomatis mencakup seluruh subpackage. Pola `com.mac.orders.service.*` dan
`com.mac.orders.service..*` juga diterima.

Setiap method menghasilkan field:

- `event.dataset=service`
- `event.action`
- `event.outcome=success|failure`
- `event.duration` dalam milidetik
- `class`, `method`, dan `layer=service`

Jika method gagal, exception tetap dilempar kembali setelah dicatat. Advisor tidak dibuat ketika
`sdk.logging.aspect.packages` kosong atau `sdk.logging.aspect.enabled=false`.

Panduan logging lebih rinci tersedia di
[`docs/guides/structured-logging-consumer-tutorial.md`](docs/guides/structured-logging-consumer-tutorial.md).

## OAuth2 resource server and JWT

Security servlet aktif secara default. Bila service tidak menyediakan `SecurityFilterChain`, SDK
membuat chain stateless dengan perilaku berikut:

- CSRF dinonaktifkan secara default.
- Request cache dinonaktifkan.
- Semua request selain public paths harus authenticated.
- Request `OPTIONS /**` diizinkan.
- Response 401 dan 403 menggunakan kontrak `ResponseDTO`.
- JWT decoder dibuat dari issuer URI jika issuer dikonfigurasi.
- Service dapat menyediakan `SecurityFilterChain` sendiri; auto-configuration SDK akan mundur.

Issuer dapat memakai salah satu property berikut. Property SDK memiliki prioritas:

```yaml
sdk:
  security:
    jwt-issuer-uri: http://localhost:9005
```

atau:

```yaml
spring:
  security:
    oauth2:
      resourceserver:
        jwt:
          issuer-uri: http://localhost:9005
```

Tanpa issuer, SDK tidak membuat `JwtDecoder` dan konfigurasi resource server JWT tidak dipasang.
Security chain tetap mengharuskan authentication kecuali service melakukan custom configuration.

### JWT authorities

`JwtAuthConverter` menggabungkan:

- Scope authorities standar Spring Security.
- Roles top-level `roles` dari `usermanagement` menjadi `ROLE_*`.
- Permissions top-level `permissions` dari `usermanagement` menjadi `PERM_*`.
- Roles dari `realm_access.roles`.
- Roles dari `resource_access.{resource-id}.roles`.

Konfigurasi consumer `usermanagement`:

```yaml
sdk:
  security:
    jwt-issuer-uri: http://localhost:9005
jwt:
  auth:
    converter:
      principle-attribute: username
```

Issuer harus menyediakan discovery metadata dan `jwks_uri`. `usermanagement` menyediakan
`/.well-known/openid-configuration` dan `/oauth2/jwks`; private signing key tidak pernah dibagikan
ke consumer.

Role otomatis mendapat prefix `ROLE_`, sehingga dapat digunakan dengan `hasRole(...)`:

```java
@PreAuthorize("hasRole('TENANT_OWNER')")
public void createOrder() {
    // ...
}
```

Permission otomatis mendapat prefix `PERM_` dan tetap mempertahankan separator titik dua:

```java
@PreAuthorize("hasAuthority('PERM_orders:create')")
public void createOrder() {
    // ...
}
```

Konfigurasi converter:

| Property | Default | Keterangan |
| --- | --- | --- |
| `jwt.auth.converter.principle-attribute` | `preferred_username` | Claim untuk principal name |
| `jwt.auth.converter.resource-id` | `sdk` | Client ID pada `resource_access` |

Nama property `principle-attribute` mengikuti nama field yang saat ini tersedia pada SDK.

### Public paths and path prefix

`sdk.security.permit-all-paths` dipasang melalui `WebSecurityCustomizer.ignoring`, sehingga path
tersebut melewati Spring Security filter chain sepenuhnya. Daftar canonical dimiliki oleh SDK;
consumer tidak boleh menduplikasi atau menggantinya melalui application YAML.

Default public paths:

```text
/health
/actuator/**
/actuator/health/**
/actuator/info/**
/actuator/metrics/**
/v3/api-docs
/v3/api-docs/**
/v3/api-docs/swagger-config
/swagger-ui.html
/swagger-ui/**
/webjars/**
/api-docs/**
/ws/alerts
/internal/**
/error
```

Endpoint bisnis seperti `/api/v1/**` tidak termasuk public path dan wajib melewati JWT filter serta
method security. `/ws/alerts` hanya membuka HTTP upgrade handshake; autentikasi JWT dan permission
`alert:read-notifications` tetap diverifikasi pada frame STOMP `CONNECT` oleh `centralized_alert`.
`/internal/**` harus dibatasi oleh internal ingress, network policy, firewall, atau service mesh
karena nama path bukan mekanisme keamanan. `usermanagement` memakai `SecurityFilterChain` sendiri,
sedangkan `api_gateway` bersifat reactive dan tidak menggunakan security auto-configuration SDK.

Jika `sdk.security.path-prefix=/orders`, SDK mendaftarkan versi prefixed dan unprefixed dari setiap
public path. Contoh: `/actuator/**` dan `/orders/actuator/**`.

### Customize HttpSecurity

Service dapat menambahkan customizer tanpa mengganti seluruh chain:

```java
@Bean
HttpSecurityCustomizer orderHttpSecurityCustomizer() {
    return http -> http.authorizeHttpRequests(authorize -> authorize
            .requestMatchers("/api/v1/public/**").permitAll()
            .requestMatchers("/api/v1/admin/**").hasRole("admin"));
}
```

Customizer dijalankan sebelum rule fallback `anyRequest().authenticated()`.

### Disable security features

```yaml
sdk:
  security:
    enabled: false
    method-security-enabled: false
    cors:
      enabled: false
```

`method-security-enabled` dan `cors.enabled` dikondisikan secara independen. Karena itu, set
ketiganya ke `false` ketika ingin menonaktifkan seluruh fitur security SDK.

## CORS

CORS filter aktif secara default dengan konfigurasi:

- Origin pattern: `*`
- Method: `*`
- Header: `*`
- Credentials: `false`
- Filter order: `Ordered.HIGHEST_PRECEDENCE`

Konfigurasi ini nyaman untuk development tetapi terlalu luas untuk banyak environment production.
Nonaktifkan filter SDK dan sediakan policy service sendiri bila origin perlu dibatasi:

```yaml
sdk:
  security:
    cors:
      enabled: false
```

## OpenAPI and Swagger UI

Jika Springdoc tersedia, SDK membuat bean `OpenAPI` default dengan bearer JWT scheme bernama
`bearerAuth`.

```yaml
sdk:
  openapi:
    enabled: true
    title: Orders API
    description: REST API for order management
    version: 1.0.0
```

| Property | Default |
| --- | --- |
| `sdk.openapi.enabled` | `true` |
| `sdk.openapi.title` | `SDK API` |
| `sdk.openapi.description` | Deskripsi REST SDK |
| `sdk.openapi.version` | `1.0.0` |

SDK hanya membuat bean ketika belum ada bean `OpenAPI`, sehingga service dapat menggantinya.

Default Springdoc yang diisi bila service belum menentukan property sendiri:

- `springdoc.api-docs.path=/v3/api-docs`
- `springdoc.swagger-ui.path=/swagger-ui.html`
- `springdoc.swagger-ui.config-url=/v3/api-docs/swagger-config`
- `springdoc.swagger-ui.url=/v3/api-docs`

Jika `sdk.security.path-prefix` diisi dan `server.servlet.context-path` kosong, URL publik Swagger
menggunakan prefix. SDK memasang forward/redirect filter agar endpoint docs tetap dapat diakses.
Jika servlet context path tersedia, context path tersebut yang menangani prefix aplikasi.

Untuk mematikan OpenAPI beserta default Springdoc dari SDK:

```yaml
sdk:
  openapi:
    enabled: false
```

## Timezone and date utility

SDK membuat bean `ZoneId`, mengonfigurasi `DateUtil`, dan menetapkan default JVM timezone dengan
urutan resolusi:

1. `sdk.timezone`
2. `spring.jackson.time-zone`
3. `UTC`

```yaml
sdk:
  timezone: UTC
```

Penggunaan:

```java
LocalDateTime now = DateUtil.getDateTimeNow();
LocalDateTime utcNow = DateUtil.getDateTimeNow(ZoneId.of("UTC"));
String formatted = DateUtil.getDateTimeString(now);
ZoneId applicationZone = DateUtil.getApplicationZone();
```

Format default `getDateTimeString` adalah `yyyy-MM-dd'T'HH:mm:ss.SSS`.

Perhatian: konfigurasi ini memanggil `TimeZone.setDefault`, sehingga memengaruhi seluruh JVM.

## Other utilities and constants

### StringUtil

- `toMap(object)` mengubah object menjadi map dengan key `snake_case` dan dukungan Java Time.
- `capitalizeFirstLetter(value)` mengubah huruf pertama menjadi kapital.
- `convertSnakeCaseToCamel(value)` mengubah `snake_case` menjadi `snakeCase`.
- Menyediakan default zone dan formatter tanggal.

Method string mengasumsikan input valid dan non-empty. Validasi input dilakukan oleh caller.

### QueryUtil

Menyediakan constant operator untuk query builder:

- Field: `=`, `!=`, `>`, `>=`, `<`, `<=`, `IN`, `NOT IN`, `BETWEEN`, `ILIKE`, `NOT ILIKE`.
- Group: `AND`, `OR`.
- Data type: `TEXT`, `NUMERIC`, `BOOLEAN`, `TIMESTAMP`.
- Modifier: `DEFAULT`, `COUNT`, `COUNT_UNIQUE`, `SUM`, `AVERAGE`.

Class ini hanya menyediakan constant; SDK tidak membangun SQL atau melakukan sanitasi query.

### Shared constants

- `Status` dan `StatusCode` untuk standard response code.
- `LogFields` untuk nama field structured logging.
- `JwtPayload` untuk nama claim JWT.
- `Role` untuk expression method security yang sudah didefinisikan.
- `TableName` untuk nama tabel bersama yang sudah didefinisikan.

## Complete property reference

| Property | Default | Scope |
| --- | --- | --- |
| `sdk.timezone` | empty; fallback ke Jackson/UTC | Timezone |
| `sdk.web.exception-handler.enabled` | `true` | REST exception handler |
| `sdk.logging.structured.enabled` | `true` | Structured logging |
| `sdk.logging.structured.format` | `ecs` | Console/file log format |
| `sdk.logging.structured.auto-configure-format` | `true` | Set default Spring Boot logging format |
| `sdk.logging.structured.trace-header` | `X-Correlation-Id` | HTTP trace header |
| `sdk.logging.structured.trace-response-header` | `true` | Return trace response header |
| `sdk.logging.structured.filter.enabled` | `true` | MDC servlet filter |
| `sdk.logging.structured.filter.order` | `-100` | MDC filter order |
| `sdk.logging.aspect.enabled` | `true` | AOP method logging switch |
| `sdk.logging.aspect.packages` | empty | Comma-separated target packages |
| `sdk.security.enabled` | `true` | Web security and JWT SDK |
| `sdk.security.csrf-disabled` | `true` | Disable CSRF |
| `sdk.security.jwt-issuer-uri` | empty | JWT issuer |
| `sdk.security.path-prefix` | empty | Prefix public paths/OpenAPI |
| `sdk.security.permit-all-paths` | lihat daftar default | Paths ignored by security |
| `sdk.security.session-creation-policy` | `STATELESS` | Session policy |
| `sdk.security.method-security-enabled` | `true` | Enable `@PreAuthorize` etc. |
| `sdk.security.cors.enabled` | `true` | Permissive CORS filter |
| `jwt.auth.converter.principle-attribute` | `preferred_username` | Principal claim |
| `jwt.auth.converter.resource-id` | `sdk` | JWT resource client ID |
| `sdk.openapi.enabled` | `true` | OpenAPI auto-configuration |
| `sdk.openapi.title` | `SDK API` | API title |
| `sdk.openapi.description` | SDK REST description | API description |
| `sdk.openapi.version` | `1.0.0` | API version |

## Build and test

```bash
mvn test
mvn clean verify
mvn clean install
```

`mvn clean verify` membuat laporan JaCoCo di `target/site/jacoco/index.html` dan menggagalkan build
bila line coverage production behavior kurang dari 90%. DTO, enum, generated code, dan property
holder yang hanya menyimpan konfigurasi tidak dimasukkan dalam gate tersebut.

Jalankan `mvn clean install` sebelum menguji service consumer lokal agar Maven menggunakan artifact
SDK terbaru, bukan versi lama yang sudah tersimpan pada local repository.
