package skhu.jijijig.domain.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import skhu.jijijig.domain.model.Crawling;

@Builder
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class CrawlingDTO {
    @Schema(description = "제목", example = "비타민 A, B, C, D, E, F")
    private String title;

    @Schema(description = "분류", example = "식품/건강")
    private String category;

    @Schema(description = "이름", example = "chaiminwoo0223")
    private String name;

    @Schema(description = "등록일", example = "2024-03-30 15:00:00")
    private String createdDate;

    @Schema(description = "링크", example = "https://link.com")
    private String link;

    @Schema(description = "이미지", example = "image URL")
    private String image;

    @Schema(description = "조회수", example = "10")
    private String view;

    @Schema(description = "댓글수", example = "10")
    private String commentCnt;

    @Schema(description = "추천수", example = "10")
    private String likeCnt;

    @Schema(description = "비추천수", example = "0")
    private String dislikeCnt;

    @Schema(description = "딜 종료 여부", example = "종료")
    private String soldOut;

    public static CrawlingDTO of(String title, String category, String name, String createdDate, String link, String image, String view, String commentCnt, String likeCnt, String dislikeCnt, String soldOut) {
        return CrawlingDTO.builder()
                .title(title)
                .category(category)
                .name(name)
                .createdDate(createdDate)
                .link(link)
                .image(image)
                .view(view)
                .commentCnt(commentCnt)
                .likeCnt(likeCnt)
                .dislikeCnt(dislikeCnt)
                .soldOut(soldOut)
                .build();
    }

    public static CrawlingDTO of(Crawling crawling) {
        return CrawlingDTO.builder()
                .title(crawling.getTitle())
                .category(crawling.getCategory())
                .name(crawling.getName())
                .createdDate(crawling.getCreatedDate())
                .link(crawling.getLink())
                .image(crawling.getImage())
                .view(crawling.getView())
                .commentCnt(crawling.getCommentCnt())
                .likeCnt(crawling.getLikeCnt())
                .dislikeCnt(crawling.getDislikeCnt())
                .soldOut(crawling.getSoldOut())
                .build();
    }
}