package api.v2.travel_social_network_server.exceptions;

public class ResourceAlreadyExistedException extends RuntimeException {
    public ResourceAlreadyExistedException(String message) {
        super(message);
    }
}