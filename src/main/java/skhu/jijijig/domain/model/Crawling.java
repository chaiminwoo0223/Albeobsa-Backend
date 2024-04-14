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

    @Column(columnDefinition = "TEXT")
    private String text; // 크롤링한 텍스트 데이터를 저장

    private String title;

    private String name;

    private String imageURL;

    private String link;

    private String createdDate;

    private int views;

    private int recommendCnt;

    private int commentCnt;

    public static Crawling of(String title, String name, String imageURL, int views, int recommendCnt, int commentCnt, String createdDate, String link) {
        return Crawling.builder()
                .title(title)
                .name(name)
                .imageURL(imageURL)
                .views(views)
                .recommendCnt(recommendCnt)
                .commentCnt(commentCnt)
                .createdDate(createdDate)
                .link(link)
                .build();
    }
}