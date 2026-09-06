package api.v2.travel_social_network_server.utilities.enums;

import java.util.Arrays;

public enum NotificationTypeEnum {
    NEW_POST("NEW_POST"),
    POST_LIKE("POST_LIKE"),
    POST_COMMENT("POST_COMMENT"),
    POST_SHARE("POST_SHARE"),
    FRIEND_REQUEST("FRIEND_REQUEST"),
    FRIEND_ACCEPTED("FRIEND_ACCEPTED"),
    GROUP_INVITE("GROUP_INVITE"),
    GROUP_JOIN_REQUEST("GROUP_JOIN_REQUEST"),
    GROUP_JOIN_ACCEPTED("GROUP_JOIN_ACCEPTED"),
    CHAT_MESSAGE("CHAT_MESSAGE"),
    MENTION("MENTION"),
    SYSTEM("SYSTEM");

    private final String value;

    NotificationTypeEnum(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    public static NotificationTypeEnum fromString(String text) {
        return Arrays.stream(NotificationTypeEnum.values())
                .filter(type -> type.value.equalsIgnoreCase(text))
                .findFirst()
                .orElseThrow(() ->
                        new IllegalArgumentException("No NotificationTypeEnum with text: " + text)
                );
    }
}
