package api.v2.travel_social_network_server.utilities.enums;

import java.util.Arrays;

public enum GenderTypeEnum {
    MALE("MALE"),
    FEMALE("FEMALE"),
    OTHER("OTHER");

    private final String value;

    GenderTypeEnum(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    public static GenderTypeEnum fromString(String text) {
        if (text == null) return OTHER; // fallback khi null
        return Arrays.stream(GenderTypeEnum.values())
                .filter(g -> g.value.equalsIgnoreCase(text.trim()))
                .findFirst()
                .orElse(OTHER);
    }
}
