package api.v2.travel_social_network_server.utilities.enums;

import java.util.Arrays;

public enum BlogStatusEnum {
    DRAFT("DRAFT"),
    PUBLISHED("PUBLISHED"),
    ARCHIVED("ARCHIVED"),
    PENDING("PENDING");

    private final String value;

    BlogStatusEnum(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    public static BlogStatusEnum fromValue(String value) {
        return Arrays.stream(BlogStatusEnum.values())
                .filter(e -> e.getValue().equals(value))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("No BlogStatusEnum with value: " + value));
    }
}
