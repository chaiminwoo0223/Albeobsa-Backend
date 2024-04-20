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

    @Column(nullable = true)
    private String name;

    @Column(nullable = false)
    private String imageURL;

    @Column(nullable = false)
    private String link;

    @Column(nullable = false)
    private String createdDate;

    @Column(nullable = false)
    private int views;

    @Column(nullable = false)
    private int recommendCnt;

    @Column(nullable = false)
    private int commentCnt;

    public static Crawling of(String label, String title, String name, String imageURL, int views, int recommendCnt, int commentCnt, String createdDate, String link) {
        return Crawling.builder()
                .label(label)
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