package api.v2.travel_social_network_server.dtos.auth;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@ToString
public class GoogleUserInfoDto {
    private String id;
    private String email;
    private String name;
    private String givenName;
    private String familyName;
    private String picture;
}
