package api.v2.travel_social_network_server.utilities.enums;

import java.util.Arrays;

public enum MediaTypeEnum {
    VIDEO("VIDEO"),
    IMAGE("IMAGE");

    private final String value;

    MediaTypeEnum(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    public static MediaTypeEnum fromString(String text) {
        return Arrays.stream(MediaTypeEnum.values())
                .filter(type -> text.toLowerCase().contains(type.value.toLowerCase()))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("No MediaTypeEnum with text: " + text));
    }
}
