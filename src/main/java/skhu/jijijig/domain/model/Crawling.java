package skhu.jijijig.domain.model;

import jakarta.persistence.*;
import lombok.*;

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
    private Integer view;

    @Column(nullable = false)
    private Integer commentCnt;

    @Column(nullable = true)
    private Integer likeCnt;

    @Column(nullable = true)
    private Integer dislikeCnt;

    @Column(nullable = true)
    private String soldOut;
}