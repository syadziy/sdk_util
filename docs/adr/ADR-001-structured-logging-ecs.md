# ADR-001: Structured Logging ECS

## Status

Accepted

## Context

Microservices (`boilerplate`, `alert`) memakai **sdk-util** sebagai shared library. Logging saat ini:

- AOP service logging menghasilkan **plain text** (`ClassName:Function:methodName START: ... ELAPSED: ... ms`)
- Tidak ada standar correlation ID / trace ID antar request
- Tidak ada format JSON yang konsisten untuk log aggregation (Elastic Stack, dll.)
- Consumer memakai **Log4j2** + Spring Boot **3.4.0** yang sudah mendukung structured logging native (ECS, Logstash, GELF)

Kebutuhan: structured logging terstandarisasi di sdk-util yang otomatis aktif di semua consumer tanpa duplikasi kode.

## Decision

Implementasi structured logging berbasis **Elastic Common Schema (ECS)** di sdk-util dengan pendekatan berikut:

```
┌─────────────────────────────────────────────────────────────┐
│                     sdk-util (SLF4J only)                   │
├─────────────────────────────────────────────────────────────┤
│  LogFields          → konstanta field ECS                │
│  StructuredLog      → SLF4J fluent API + MDC helper      │
│  MdcLoggingFilter   → trace.id dari HTTP header          │
│  ServiceLoggingAdvice → AOP log dengan key-value fields  │
│  EnvironmentPostProcessor → default logging.structured.*=ecs│
└─────────────────────────────────────────────────────────────┘
                              │
                              ▼
┌─────────────────────────────────────────────────────────────┐
│              Consumer (Log4j2 + Spring Boot 3.4)            │
│  logging.structured.format.console=ecs  → JSON output       │
└─────────────────────────────────────────────────────────────┘
```

1. **Tidak menambah dependency Log4j2/Logback** di sdk-util — tetap SLF4J-only; binding dari consumer.
2. **`StructuredLog`** menggunakan SLF4J 2.x fluent API (`addKeyValue()`) dan MDC agar field otomatis masuk ke JSON ECS.
3. **`MdcLoggingFilter`** membaca `X-Correlation-Id` (konfigurable), generate UUID jika kosong, set `trace.id` di MDC.
4. **`StructuredLoggingEnvironmentPostProcessor`** set default `logging.structured.format.*=ecs` jika consumer belum override.
5. **Upgrade AOP advice** ke structured fields; fallback plain text jika `sdk.logging.structured.enabled=false`.
6. **Tidak log argument method** untuk menghindari kebocoran PII.

## Consequences

### Positif

- Semua consumer otomatis dapat ECS JSON dengan minimal konfigurasi
- Correlation ID konsisten via HTTP header + MDC
- Developer bisa pakai `StructuredLog` + `LogFields` untuk custom log terstandarisasi
- Backward compatible: disable via `sdk.logging.structured.enabled=false`

### Trade-offs

- Output JSON ECS hanya aktif jika consumer **tidak override** logging config dengan `log4j2.xml` custom yang tidak support `StructuredLogLayout`
- Consumer dengan custom `log4j2.xml` perlu migrasi ke `log4j2-spring.xml` + `StructuredLogLayout` atau hapus custom config
- Field ECS custom (`class`, `method`) tidak termasuk ECS core schema — ditambahkan sebagai extension fields

### Migrasi Consumer

```properties
# application-prod.properties (minimal)
sdk.logging.structured.enabled=true
sdk.logging.structured.auto-configure-format=true
```

Untuk Log4j2 custom config, gunakan:

```xml
<StructuredLogLayout format="${sys:CONSOLE_LOG_STRUCTURED_FORMAT}"
                     charset="${sys:CONSOLE_LOG_CHARSET}"/>
```

## Related

- [Spring Boot 3.4 Structured Logging](https://docs.spring.io/spring-boot/3.4/reference/features/logging.html#features.logging.structured)
- [Elastic Common Schema](https://www.elastic.co/guide/en/ecs/current/index.html)
- AGENTS.md — section `sdk.logging.structured.*`
