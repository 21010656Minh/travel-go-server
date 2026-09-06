package api.v2.travel_social_network_server.utilities.enums;

import java.util.Arrays;

public enum PostTypeEnum {
    NORMAL("NORMAL"),
    AVATAR_UPDATE("AVATAR_UPDATE"),
    COVER_UPDATE("COVER_UPDATE");

    private final String value;

    PostTypeEnum(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    public static PostTypeEnum fromString(String text) {
        if (text == null || text.trim().isEmpty()) {
            return NORMAL;
        }
        return Arrays.stream(PostTypeEnum.values())
                .filter(type -> type.value.equalsIgnoreCase(text.trim()))
                .findFirst()
                .orElse(NORMAL);
    }
}
