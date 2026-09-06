package api.v2.travel_social_network_server.utilities.enums;

import java.util.Arrays;

public enum ActivityTypeEnum {
    VISIT("VISIT"),
    MEAL("MEAL"),
    ACCOMMODATION("ACCOMMODATION"),
    TRANSPORT("TRANSPORT"),
    OTHER("OTHER");

    private final String value;

    ActivityTypeEnum(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    public static ActivityTypeEnum fromValue(String value) {
        return Arrays.stream(ActivityTypeEnum.values())
                .filter(e -> e.getValue().equals(value))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("No ActivityTypeEnum with value: " + value));
    }
}
