package api.v2.travel_social_network_server.utilities.enums;

import java.util.Arrays;

public enum ProviderTypeEnum {
    LOCAL("LOCAL"),
    GOOGLE("GOOGLE"),
    FACEBOOK("FACEBOOK"),
    TWITTER("TWITTER");

    private final String value;

    ProviderTypeEnum(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    public static ProviderTypeEnum fromString(String text) {
        return Arrays.stream(ProviderTypeEnum.values())
                .filter(provider -> provider.value.equalsIgnoreCase(text))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("No ProviderEnum with text: " + text));
    }
}

