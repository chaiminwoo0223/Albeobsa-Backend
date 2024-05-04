package skhu.jijijig.domain.model;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.security.access.AccessDeniedException;
import skhu.jijijig.domain.dto.BoardDTO;
import skhu.jijijig.repository.BoardRepository;
import skhu.jijijig.repository.CommentRepository;
import skhu.jijijig.repository.HeartRepository;

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
    private int commentCnt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    @JsonBackReference
    private Member member;

    @OneToMany(mappedBy = "board", orphanRemoval = true)
    @JsonManagedReference
    private List<Comment> comments;

    @OneToMany(mappedBy = "board", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonManagedReference
    private List<Report> reports; // 게시글에 대한 신고 목록

    @OneToMany(mappedBy = "board", orphanRemoval = true)
    @JsonManagedReference
    private List<Heart> hearts;

    public static Board createNewBoard(BoardDTO boardDTO, Member member) {
        return Board.builder()
                .title(boardDTO.getTitle())
                .content(boardDTO.getContent())
                .member(member)
                .heartCnt(0)
                .commentCnt(0)
                .build();
    }

    public void updateDetails(String title, String content) {
        this.title = title;
        this.content = content;
    }

    public void deleteBoardIfAuthorized(Member member, BoardRepository boardRepository, CommentRepository commentRepository, HeartRepository heartRepository) {
        if (this.member.isAuthorizedToDelete(member)) {
            commentRepository.deleteByBoardId(this.id); // 이 게시글에 대한 모든 댓글 삭제
            heartRepository.deleteAll(hearts); // 이 게시글에 대한 모든 좋아요 삭제
            boardRepository.delete(this); // 게시글 삭제
        } else {
            throw new AccessDeniedException("게시글 삭제 권한이 없습니다.");
        }
    }

    public void attachComment(Member member, String content, CommentRepository commentRepository) {
        Comment comment = new Comment(this, member, content);
        commentRepository.save(comment);
    }

    public void attachHeart(Member member, HeartRepository heartRepository) {
        if (!heartRepository.existsByBoardAndMember(this, member)) {
            Heart heart = new Heart(this, member);
            heartRepository.save(heart);
            this.heartCnt += 1;
        }
    }

    public void detachHeart(Member member, HeartRepository heartRepository) {
        heartRepository.findByBoardAndMember(this, member).ifPresent(heart -> {
            heartRepository.delete(heart);
            this.heartCnt = Math.max(0, this.heartCnt - 1); // 음수 방지
        });
    }
}