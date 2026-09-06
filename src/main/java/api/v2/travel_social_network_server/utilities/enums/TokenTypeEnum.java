package api.v2.travel_social_network_server.utilities.enums;

import java.util.Arrays;

public enum TokenTypeEnum {
    ACCESS("access"),
    REFRESH("refresh");

    private final String value;

    TokenTypeEnum(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    public static TokenTypeEnum fromValue(String value) {
        return Arrays.stream(TokenTypeEnum.values())
                .filter(e -> e.getValue().equalsIgnoreCase(value))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Invalid token type: " + value));
    }

    @Override
    public String toString() {
        return this.value;
    }
}
