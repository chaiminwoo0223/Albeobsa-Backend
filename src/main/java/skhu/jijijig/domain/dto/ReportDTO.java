package skhu.jijijig.domain.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import skhu.jijijig.domain.Reason;
import skhu.jijijig.domain.Report;

@Builder
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ReportDTO {
    @Schema(description = "신고 ID", example = "1")
    private Long id; // 신고 ID 추가

    @Schema(description = "게시글 ID", example = "2")
    private Long boardId;

    @Schema(description = "댓글 ID", example = "3")
    private Long commentId;

    @Schema(description = "신고자 ID", example = "5")
    private Long reporterId;

    @Schema(description = "신고 이유", example = "잘못된 정보")
    private Reason reason;

    public static ReportDTO fromEntity(Report report) {
        return ReportDTO.builder()
                .id(report.getId())
                .boardId(report.getBoard() != null ? report.getBoard().getId() : null)
                .commentId(report.getComment() != null ? report.getComment().getId() : null)
                .reporterId(report.getReporter().getId())
                .reason(report.getReason())
                .build();
    }
}