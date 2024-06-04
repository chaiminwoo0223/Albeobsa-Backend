package skhu.jijijig.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import skhu.jijijig.domain.dto.ReportDTO;
import skhu.jijijig.domain.Reason;
import skhu.jijijig.service.ReportService;

import java.security.Principal;
import java.util.List;

@Tag(name = "Report API", description = "신고 관련 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/reports")
public class ReportController {
    private final ReportService reportService;

    @Operation(summary = "게시글별 신고 내역 조회", description = "특정 게시글에 대한 모든 신고 내역을 조회합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "신고 내역 조회 성공"),
            @ApiResponse(responseCode = "404", description = "게시글을 찾을 수 없음")
    })
    @GetMapping("/{boardId}")
    public ResponseEntity<List<ReportDTO>> findAllReportsForBoard(@PathVariable Long boardId) {
        List<ReportDTO> reports = reportService.findAllReportsForBoard(boardId);
        return ResponseEntity.ok(reports);
    }

    @Operation(summary = "게시글 신고", description = "게시글을 신고합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "게시글 신고 성공"),
            @ApiResponse(responseCode = "400", description = "잘못된 요청"),
            @ApiResponse(responseCode = "404", description = "게시글 또는 신고자 정보를 찾을 수 없음")
    })
    @PostMapping("/{boardId}")
    public ResponseEntity<ReportDTO> reportBoard(Principal principal, @PathVariable Long boardId, @RequestParam Reason reason) {
        Long reporterId = Long.parseLong(principal.getName());
        ReportDTO reportDTO = reportService.reportBoard(boardId, reporterId, reason);
        return new ResponseEntity<>(reportDTO, HttpStatus.CREATED);
    }

    @Operation(summary = "댓글별 신고 내역 조회", description = "특정 댓글에 대한 모든 신고 내역을 조회합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "신고 내역 조회 성공"),
            @ApiResponse(responseCode = "404", description = "댓글을 찾을 수 없음")
    })
    @GetMapping("/comments/{commentId}")
    public ResponseEntity<List<ReportDTO>> findAllReportsForComment(@PathVariable Long commentId) {
        List<ReportDTO> reports = reportService.findAllReportsForComment(commentId);
        return ResponseEntity.ok(reports);
    }

    @Operation(summary = "댓글 신고", description = "댓글을 신고합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "댓글 신고 성공"),
            @ApiResponse(responseCode = "400", description = "잘못된 요청"),
            @ApiResponse(responseCode = "404", description = "댓글 또는 신고자 정보를 찾을 수 없음")
    })
    @PostMapping("/comments/{commentId}")
    public ResponseEntity<ReportDTO> reportComment(Principal principal, @PathVariable Long commentId, @RequestParam Reason reason) {
        Long reporterId = Long.parseLong(principal.getName());
        ReportDTO reportDTO = reportService.reportComment(commentId, reporterId, reason);
        return new ResponseEntity<>(reportDTO, HttpStatus.CREATED);
    }
}