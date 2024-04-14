package skhu.jijijig.domain.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

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

    @Schema(description = "이미지 URL", example = "image URL")
    private String imageURL;

    @Schema(description = "링크", example = "https://link.com")
    private String link;

    @Schema(description = "등록일", example = "2024-03-30")
    private String createdDate;

    @Schema(description = "조회수", example = "10")
    private int views;

    @Schema(description = "추천수", example = "10")
    private int recommendCnt;

    @Schema(description = "댓글수", example = "10")
    private int commentCnt;
}