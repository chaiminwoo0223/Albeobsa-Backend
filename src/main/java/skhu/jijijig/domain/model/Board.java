package skhu.jijijig.domain.model;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.*;
import skhu.jijijig.domain.repository.HeartRepository;

import java.util.List;

@Entity
@Builder
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PROTECTED)
public class Board extends BaseEntity {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "board_id")
    private Long id;

    @Column(nullable = false)
    private String title;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String content;

    @Column(nullable = false)
    private int heartCnt;

    @Column(nullable = false)
    private int CommentCnt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    @JsonBackReference
    private Member member;

    @OneToMany(mappedBy = "board", orphanRemoval = true)
    @JsonManagedReference
    private List<Comment> comments;

    @OneToMany(mappedBy = "board", orphanRemoval = true)
    @JsonManagedReference
    private List<Heart> hearts;

    public void addHeart(Member member, HeartRepository heartRepository) {
        if (!heartRepository.existsByBoardAndMember(this, member)) {
            Heart heart = new Heart(this, member);
            heartRepository.save(heart);
            this.heartCnt += 1;
        }
    }

    public void removeHeart(Member member, HeartRepository heartRepository) {
        heartRepository.findByBoardAndMember(this, member).ifPresent(heart -> {
            heartRepository.delete(heart);
            this.heartCnt = Math.max(0, this.heartCnt - 1); // 음수 방지
        });
    }
}