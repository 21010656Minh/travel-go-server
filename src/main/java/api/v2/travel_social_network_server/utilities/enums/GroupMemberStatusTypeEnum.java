package api.v2.travel_social_network_server.utilities.enums;

import java.util.Arrays;

public enum GroupMemberStatusTypeEnum {
    PENDING("PENDING"),
    APPROVED("APPROVED");

    private final String value;

    GroupMemberStatusTypeEnum(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    public static GroupMemberStatusTypeEnum fromString(String text) {
        return Arrays.stream(GroupMemberStatusTypeEnum.values())
                .filter(status -> status.value.equalsIgnoreCase(text))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("No GroupMemberStatusEnum with text: " + text));
    }
}

