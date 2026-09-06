package api.v2.travel_social_network_server.responses;

import lombok.*;
import java.time.Instant;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Response<T> {
    private boolean success;
    private String path;
    private String message;
    private T data;
    private Object errors;
    private Instant timestamp;

    public static <T> Response<T> success(T data, String path, String message) {
        return Response.<T>builder()
                .success(true)
                .path(path)
                .message(message)
                .data(data)
                .errors(null)
                .timestamp(Instant.now())
                .build();
    }

    public static Response<Object> error(String path ,String message, Object errors) {
        return Response.builder()
                .success(false)
                .path(path)
                .message(message)
                .data(null)
                .errors(errors)
                .timestamp(Instant.now())
                .build();
    }
}
