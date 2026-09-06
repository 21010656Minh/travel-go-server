package api.v2.travel_social_network_server.utilities.enums;

import java.util.Arrays;

public enum PrivacyTypeEnum {
    PUBLIC("PUBLIC"),
    FRIEND("FRIEND"),
    PRIVATE("PRIVATE");

    private final String value;

    PrivacyTypeEnum(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    public static PrivacyTypeEnum fromString(String text) {
        if (text == null) return PUBLIC; // Fallback nghĩa là giá trị dự phòng hoặc giải pháp thay thế khi dữ liệu đầu vào không hợp lệ hoặc bị thiếu.
        return Arrays.stream(PrivacyTypeEnum.values())
                .filter(p -> p.value.equalsIgnoreCase(text.trim()))
                .findFirst()
                .orElse(PUBLIC);
    }
}
