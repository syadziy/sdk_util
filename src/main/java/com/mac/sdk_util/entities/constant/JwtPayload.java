package com.mac.sdk_util.entities.constant;

import lombok.Getter;

@Getter
public enum JwtPayload {

    EXP("exp"),
    IAT("iat"),
    JTI("jti"),
    ISS("iss"),
    AUD("aud"),
    SUB("sub"),
    TYP("typ"),
    AZP("azp"),
    ACR("acr"),
    SID("sid"),
    NAME("name"),
    SCOPE("scope"),
    GROUPS("groups"),
    EMAIL("email"),
    ROLES("roles"),
    GIVEN_NAME("given_name"),
    FAMILY_NAME("family_name"),
    SESSION_STATE("session_state"),
    ALLOWED_ORIGINS("allowed-origins"),
    REALM_ACCESS("realm_access"),
    RESOURCE_ACCESS("resource_access"),
    EMAIL_VERIFIED("email_verified"),
    PREFERRED_USERNAME("preferred_username");

    private final String claimKey;

    JwtPayload(String claimKey) {
        this.claimKey = claimKey;
    }
}
