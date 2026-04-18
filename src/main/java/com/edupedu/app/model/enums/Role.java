package com.edupedu.app.model.enums;

public enum Role {
    ROLE_ADMIN("ROLE_ADMIN"),
    ROLE_UNIVERSITY_ADMIN("ROLE_UNIVERSITY_ADMIN"),
    ROLE_STUDENT("ROLE_STUDENT"),
    ROLE_TEACHER("ROLE_TEACHER"),
    ROLE_ACCOUNTANT("ROLE_ACCOUNTANT"),
    ROLE_GUEST("ROLE_GUEST");

    private final String name;

    Role(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public static Role fromName(String name) {
        for (Role role : Role.values()) {
            if (role.name.equals(name)) {
                return role;
            }
        }
        throw new IllegalArgumentException("Invalid role name: " + name);
    }
}
