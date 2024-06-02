package skhu.jijijig.domain.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class LoginResponseDTO {
    @Schema(description = "토큰 정보")
    private TokenDTO tokens;

    @Schema(description = "사용자 정보")
    private MemberDTO memberDTO;
}