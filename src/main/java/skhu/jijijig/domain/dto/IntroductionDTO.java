package skhu.jijijig.domain.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

@Builder
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class IntroductionDTO {
    @Schema(description = "이름", example = "최민우")
    private String name;

    @Schema(description = "이메일", example = "chaiminwoo0223@gmail.com")
    private String email;

    @Schema(description = "자기소개", example = "안녕하세요. 저는 최민우입니다.")
    private String selfIntroduction;
}