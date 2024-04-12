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

    private String text; // 크롤링한 텍스트 데이터를 저장

    @Column(nullable = false)
    private String title;

    private String name;

    @Column(nullable = false)
    private String imageURL;

    private String link;

    private String createdDateTime;

    @Column(nullable = false)
    private Integer views;

    @Column(nullable = false)
    private Integer recommendCnt;

    @Column(nullable = false)
    private Integer commentCnt;
}