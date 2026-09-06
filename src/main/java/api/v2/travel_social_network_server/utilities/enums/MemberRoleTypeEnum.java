package api.v2.travel_social_network_server.utilities.enums;

import java.util.Arrays;

public enum MemberRoleTypeEnum {
    OWNER("OWNER"),
    ADMIN("ADMIN"),
    MODERATOR("MODERATOR"),
    MEMBER("MEMBER");

    private final String value;

    MemberRoleTypeEnum(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    public static MemberRoleTypeEnum fromString(String text) {
        return Arrays.stream(MemberRoleTypeEnum.values())
                .filter(role -> role.value.equalsIgnoreCase(text))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("No GroupMemberRoleEnum with text: " + text));
    }
}

