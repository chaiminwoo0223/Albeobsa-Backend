package skhu.jijijig.domain.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import skhu.jijijig.domain.model.Crawling;

@Builder
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class CrawlingDTO {
    @Schema(description = "라벨", example = "지지직")
    private String label;

    @Schema(description = "제목", example = "비타민 A, B, C, D, E, F")
    private String title;

    @Schema(description = "이름", example = "chaiminwoo0223")
    private String name;

    @Schema(description = "이미지 URL", example = "image URL")
    private String imageURL;

    @Schema(description = "링크", example = "https://link.com")
    private String link;

    @Schema(description = "등록일, 등록시간", example = "2024-03-30 or 19:30")
    private String createdDateTime;

    @Schema(description = "조회수", example = "10")
    private int views;

    @Schema(description = "추천수", example = "10")
    private int recommendCnt;

    @Schema(description = "비추천수", example = "2")
    private int unrecommendCnt;

    @Schema(description = "댓글수", example = "10")
    private int commentCnt;

    public static CrawlingDTO fromEntity(Crawling crawling) {
        return CrawlingDTO.builder()
                .label(crawling.getLabel())
                .title(crawling.getTitle())
                .name(crawling.getName())
                .imageURL(crawling.getImageURL())
                .link(crawling.getLink())
                .createdDateTime(crawling.getCreatedDateTime())
                .views(crawling.getViews())
                .recommendCnt(crawling.getRecommendCnt())
                .unrecommendCnt(crawling.getUnrecommendCnt())
                .commentCnt(crawling.getCommentCnt())
                .build();
    }
}