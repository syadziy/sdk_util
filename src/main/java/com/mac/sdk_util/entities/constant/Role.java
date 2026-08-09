package com.mac.sdk_util.entities.constant;

import lombok.Getter;

@Getter
public class Role {

    public static final String TENANT_UPDATE = "hasAuthority('PERM_tenant:update')";

    public static final String USER_CREATE = "hasAuthority('PERM_user:create')";
    public static final String USER_VIEW = "hasAuthority('PERM_user:view')";

    public static final String ROLE_ASSIGN = "hasAuthority('PERM_role:assign')";
    public static final String ROLE_CREATE = "hasAuthority('PERM_role:create')";
    public static final String ROLE_VIEW = "hasAuthority('PERM_role:view')";
    public static final String ROLE_EDIT = "hasAuthority('PERM_role:edit')";

    public static final String PERMISSION_CREATE = "hasAuthority('PERM_permission:create')";
    public static final String PERMISSION_VIEW = "hasAuthority('PERM_permission:view')";

    public static final String ALERT_READ_RECIPIENTS =
            "hasAuthority('PERM_alert:read-recipients')";
    public static final String ALERT_MANAGE_RECIPIENTS =
            "hasAuthority('PERM_alert:manage-recipients')";
    public static final String ALERT_READ_NOTIFICATIONS =
            "hasAuthority('PERM_alert:read-notifications')";

    public static final String AUDIT_READ = "hasAuthority('PERM_audit:read')";

    public static final String SCHEDULER_READ = "hasAuthority('PERM_scheduler:read')";
    public static final String SCHEDULER_MANAGE = "hasAuthority('PERM_scheduler:manage')";

    private Role() {}
}
