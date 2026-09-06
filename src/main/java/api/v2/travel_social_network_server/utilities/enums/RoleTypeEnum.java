package api.v2.travel_social_network_server.utilities.enums;

import java.util.Arrays;

public enum RoleTypeEnum {
    ADMIN("ADMIN"),
    USER("USER");

    private final String value;

    RoleTypeEnum(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    public static RoleTypeEnum fromString(String text) {
        return Arrays.stream(RoleTypeEnum.values())
                .filter(role -> role.value.equalsIgnoreCase(text))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("No RoleEnum with text: " + text));
    }
}
