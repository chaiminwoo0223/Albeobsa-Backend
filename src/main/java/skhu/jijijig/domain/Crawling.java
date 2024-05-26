package skhu.jijijig.domain;

import jakarta.persistence.*;
import lombok.*;

import java.util.Objects;

@Entity
@Builder(toBuilder = true)
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

    @Column(nullable = false)
    private boolean open;

    public static Crawling of(String label, String title, String name, String image, String link, String dateTime, int views, int recommendCnt, int unrecommendCnt, int commentCnt, boolean open) {
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
                .open(open)
                .build();
    }

    public boolean isDifferent(Crawling other) {
        return !Objects.equals(this.label, other.label) ||
                !Objects.equals(this.title, other.title) ||
                !Objects.equals(this.name, other.name) ||
                !Objects.equals(this.image, other.image) ||
                this.views != other.views ||
                this.recommendCnt != other.recommendCnt ||
                this.unrecommendCnt != other.unrecommendCnt ||
                this.commentCnt != other.commentCnt ||
                this.open != other.open;
    }
}