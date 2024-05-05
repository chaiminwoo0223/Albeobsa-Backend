package skhu.jijijig.domain.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

@Data
@Builder
@Getter
@AllArgsConstructor
public class TokenDTO {
    @Schema(description = "토큰 타입")
    private String grantType;

    @Schema(description = "액세스 토큰")
    private String accessToken;

    @Schema(description = "리프레시 토큰")
    private String refreshToken;

    public static TokenDTO of(String accessToken, String refreshToken) {
        return TokenDTO.builder()
                .grantType("bearer")
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .build();
    }
}