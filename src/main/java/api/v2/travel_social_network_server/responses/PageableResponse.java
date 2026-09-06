package api.v2.travel_social_network_server.responses;

import lombok.*;

import java.util.List;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class PageableResponse<T> {
    private List<T> content;
    private int pageNumber;
    private int pageSize;
    private long totalPages;
    private long totalElements;
    private boolean last;
    private boolean first;
}
