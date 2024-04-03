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

    @Column(nullable = false, columnDefinition = "TEXT")
    private String text; // 크롤링한 텍스트 데이터를 저장
}