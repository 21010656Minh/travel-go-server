package api.v2.travel_social_network_server.utilities.enums;

import java.util.Arrays;

public enum TripStatusEnum {
    PLANNING("PLANNING"),
    CONFIRMED("CONFIRMED"),
    ONGOING("ONGOING"),
    COMPLETED("COMPLETED"),
    CANCELLED("CANCELLED");

    private final String value;

    TripStatusEnum(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    public static TripStatusEnum fromValue(String value) {
        return Arrays.stream(TripStatusEnum.values())
                .filter(e -> e.getValue().equals(value))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("No TripStatusEnum with value: " + value));
    }
}
