package skhu.jijijig.domain.model;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.security.access.AccessDeniedException;
import skhu.jijijig.domain.dto.CommentDTO;
import skhu.jijijig.repository.CommentRepository;

import java.util.List;
import java.util.stream.Collectors;

@Entity
@Builder
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PROTECTED)
public class Comment extends BaseEntity {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "comment_id")
    private Long id;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String content;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    @JsonBackReference
    private Member member; // 작성자

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "board_id", nullable = false)
    @JsonBackReference
    private Board board; // 댓글이 달린 게시판

    @OneToMany(mappedBy = "comment", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonManagedReference
    private List<Report> reports; // 댓글에 대한 신고 목록


    public Comment(Board board, Member member, String content) {
        this.board = board;
        this.member = member;
        this.content = content;
    }

    public void updateContent(String content) {
        this.content = content;
    }

    public void deleteCommentIfAuthorized(Member member, CommentRepository commentRepository) {
        if (this.member.equals(member) || member.getRole().equals(Role.ADMIN)) {
            commentRepository.delete(this);
        } else {
            throw new AccessDeniedException("댓글 삭제 권한이 없습니다.");
        }
    }

    public static List<CommentDTO> toDTOsForBoard(Board board, CommentRepository commentRepository) {
        List<Comment> comments = commentRepository.findByBoardId(board.getId());
        return comments.stream()
                .map(CommentDTO::fromEntity)
                .collect(Collectors.toList());
    }
}