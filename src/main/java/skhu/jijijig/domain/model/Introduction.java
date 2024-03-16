package skhu.jijijig.domain.model;

import jakarta.persistence.*;
import lombok.*;
import skhu.jijijig.domain.dto.IntroductionDTO;

@Entity
@Builder
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class Introduction {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "introduction_id")
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String email;

    @Column(nullable = false)
    private String introduction;

    // DTO로부터 엔티티 객체를 생성하는 정적 팩토리 메소드
    public static Introduction fromDTO(IntroductionDTO introductionDTO) {
        return Introduction.builder()
                .name(introductionDTO.getName())
                .email(introductionDTO.getEmail())
                .introduction(introductionDTO.getIntroduction())
                .build();
    }
}