package api.v2.travel_social_network_server.utilities.enums;

import java.util.Arrays;

public enum StatusTypeEnum {
    ACTIVE("ACTIVE"),
    INACTIVE("INACTIVE")
;
    private final String value;

    StatusTypeEnum(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    public static StatusTypeEnum fromValue(String value) {
        return Arrays.stream(StatusTypeEnum.values())
                .filter(e -> e.getValue()
                        .equals(value)).findFirst()
                .orElseThrow(() -> new IllegalArgumentException("No ProviderEnum with text: " + value));
    }
}

