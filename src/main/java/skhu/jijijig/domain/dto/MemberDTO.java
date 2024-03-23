package skhu.jijijig.domain.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class MemberDTO {

    @Builder
    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Signup {
        @Schema(description = "이름")
        private String name;

        @Schema(description = "이메일")
        private String email;

        @Schema(description = "비밀번호")
        private String password;

        @Schema(description = "사진")
        private String picture;
    }

    @Builder
    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Login {
        @Schema(description = "id")
        private Long id;

        @Schema(description = "이메일")
        private String email;
    }
}