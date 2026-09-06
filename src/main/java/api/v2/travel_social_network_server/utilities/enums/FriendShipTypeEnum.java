package api.v2.travel_social_network_server.utilities.enums;

import java.util.Arrays;

public enum FriendShipTypeEnum {
    PENDING("PENDING"),
    ACCEPTED("ACCEPTED"),
    BLOCKED("BLOCKED");

    private final String value;

    FriendShipTypeEnum(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    public static FriendShipTypeEnum fromString(String text) {
        return Arrays.stream(FriendShipTypeEnum.values())
                .filter(status -> status.value.equalsIgnoreCase(text))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("No FriendShipEnum with text: " + text));
    }
}
