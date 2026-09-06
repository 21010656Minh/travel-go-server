package api.v2.travel_social_network_server.utilities.enums;

import java.util.Arrays;

public enum ConversationTypeEnum {
    PRIVATE("PRIVATE"),
    GROUP("GROUP");

    private final String value;

    ConversationTypeEnum(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    public static ConversationTypeEnum fromString(String value) {
        return Arrays.stream(ConversationTypeEnum.values())
                .filter(e -> e.getValue().equals(value)).findFirst()
                .orElseThrow(() -> new IllegalArgumentException("No groupChatEnum with value: " + value));
    }

}
