package skhu.jijijig.domain.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import skhu.jijijig.domain.model.Reason;
import skhu.jijijig.domain.model.Report;

@Builder
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ReportDTO {
    @Schema(description = "신고 ID", example = "1")
    private Long id; // 신고 ID 추가

    @Schema(description = "댓글 ID", example = "3")
    private Long commentId; // 댓글 ID 추가

    @Schema(description = "신고자 ID", example = "5")
    private Long reporterId; // 신고자 ID 추가

    @Schema(description = "신고 이유", example = "잘못된 정보")
    private Reason reason;

    public static ReportDTO fromEntity(Report report) {
        return new ReportDTO(
                report.getId(),
                report.getComment().getId(),
                report.getReporter().getId(),
                report.getReason()
        );
    }
}