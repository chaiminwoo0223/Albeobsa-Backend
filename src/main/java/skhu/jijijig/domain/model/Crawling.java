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
    private String label;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String image;

    @Column(nullable = false)
    private String link;

    @Column(nullable = false)
    private String dateTime;

    @Column(nullable = false)
    private int views;

    @Column(nullable = false)
    private int recommendCnt;

    @Column(nullable = false)
    private int unrecommendCnt;

    @Column(nullable = false)
    private int commentCnt;

    public static Crawling of(String label, String title, String name, String image, String link, String dateTime, int views, int recommendCnt, int unrecommendCnt, int commentCnt) {
        return Crawling.builder()
                .label(label)
                .title(title)
                .name(name)
                .image(image)
                .link(link)
                .dateTime(dateTime)
                .views(views)
                .recommendCnt(recommendCnt)
                .unrecommendCnt(unrecommendCnt)
                .commentCnt(commentCnt)
                .build();
    }
}