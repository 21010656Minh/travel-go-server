package api.v2.travel_social_network_server.enums;

public enum AiRequestType {
    GENERATE("generate"); 
    private final String value;

    AiRequestType(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    public static AiRequestType fromValue(String value) {
        if (value == null) {
            return null;
        }
        for (AiRequestType type : AiRequestType.values()) {
            if (type.value.equalsIgnoreCase(value)) {
                return type;
            }
        }
        return null;
    }
}
