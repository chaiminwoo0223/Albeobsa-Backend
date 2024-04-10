package skhu.jijijig.domain.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.time.LocalDateTime;

@Builder
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class CrawlingDTO {
    @Schema(description = "텍스트 데이터", example = "지지직")
    private String text;

    @Schema(description = "제목", example = "비타민 A, B, C, D, E, F")
    private String title;

    @Schema(description = "이름", example = "chaiminwoo0223")
    private String name;

    @Schema(description = "이미지", example = "image URL")
    private String image;

    @Schema(description = "링크", example = "https://link.com")
    private String link;

    @Schema(description = "등록일", example = "2024-03-30 15:00:00")
    private String createdDateTime;

    @Schema(description = "조회수", example = "10")
    private Integer view;

    @Schema(description = "추천수", example = "10")
    private Integer recommendCnt;

    @Schema(description = "댓글수", example = "10")
    private Integer commentCnt;
}