package skhu.jijijig.domain;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Builder
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PROTECTED)
public class Report extends BaseEntity {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "report_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "board_id")
    private Board board; // 신고 대상 게시글

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "comment_id")
    private Comment comment; // 신고 대상 댓글

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member reporter; // 신고자

    @Column(columnDefinition = "TEXT")
    private Reason reason; // 신고 이유

    public static Report createReportForBoard(Board board, Member reporter, Reason reason) {
        return Report.builder()
                .board(board)
                .reporter(reporter)
                .reason(reason)
                .build();
    }

    public static Report createReportForComment(Comment comment, Member reporter, Reason reason) {
        return Report.builder()
                .comment(comment)
                .reporter(reporter)
                .reason(reason)
                .build();
    }
}