package skhu.jijijig.domain.model;

import jakarta.persistence.*;
import lombok.*;
import skhu.jijijig.domain.dto.CrawlingDTO;

import java.time.LocalDateTime;

@Entity
@Builder
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PROTECTED)
public class Crawling {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "crawling_id")
    private Long id;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String text; // 크롤링한 텍스트 데이터를 저장

    @Column(nullable = false)
    private String title;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String image;

    @Column(nullable = false)
    private String link;

    @Column(nullable = false)
    private String createdDateTime;

    @Column(nullable = false)
    private Integer view;

    @Column(nullable = false)
    private Integer recommendCnt;

    @Column(nullable = false)
    private Integer commentCnt;
}