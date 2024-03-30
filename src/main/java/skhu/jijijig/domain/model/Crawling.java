package skhu.jijijig.domain.model;

import jakarta.persistence.*;
import lombok.*;
import skhu.jijijig.domain.dto.CrawlingDTO;

@Entity
@Builder
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PROTECTED)
public class Crawling {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "crawling_id")
    private Long id;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false)
    private String category;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String createdDate;

    @Column(nullable = false)
    private String link;

    @Column(nullable = false)
    private String image;

    @Column(nullable = false)
    private String view;

    @Column(nullable = false)
    private String commentCnt;

    @Column(nullable = true)
    private String likeCnt;

    @Column(nullable = true)
    private String dislikeCnt;

    @Column(nullable = true)
    private String soldOut;

    public static Crawling fromDTO(CrawlingDTO dto) {
        return Crawling.builder()
                .title(dto.getTitle())
                .category(dto.getCategory())
                .name(dto.getName())
                .createdDate(dto.getCreatedDate())
                .link(dto.getLink())
                .image(dto.getImage())
                .view(dto.getView())
                .commentCnt(dto.getCommentCnt())
                .likeCnt(dto.getLikeCnt())
                .dislikeCnt(dto.getDislikeCnt())
                .soldOut(dto.getSoldOut())
                .build();
    }
}