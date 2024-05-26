package skhu.jijijig.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import skhu.jijijig.domain.*;
import skhu.jijijig.domain.dto.ReportDTO;
import skhu.jijijig.exception.ResourceNotFoundException;
import skhu.jijijig.repository.BoardRepository;
import skhu.jijijig.repository.CommentRepository;
import skhu.jijijig.repository.MemberRepository;
import skhu.jijijig.repository.ReportRepository;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ReportService {
    private final MemberRepository memberRepository;
    private final BoardRepository boardRepository;
    private final CommentRepository commentRepository;
    private final ReportRepository reportRepository;

    @Transactional
    public ReportDTO reportBoard(Long boardId, Long reporterId, Reason reason) {
        Board board = boardRepository.findById(boardId)
                .orElseThrow(() -> new ResourceNotFoundException("해당 ID의 게시글을 찾을 수 없습니다: " + boardId));
        Member reporter = memberRepository.findById(reporterId)
                .orElseThrow(() -> new ResourceNotFoundException("해당 ID의 회원을 찾을 수 없습니다: " + reporterId));
        Report report = Report.createReportForBoard(board, reporter, reason);
        reportRepository.save(report);
        return ReportDTO.fromEntity(report);
    }

    @Transactional
    public ReportDTO reportComment(Long commentId, Long reporterId, Reason reason) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new ResourceNotFoundException("해당 ID의 댓글을 찾을 수 없습니다: " + commentId));
        Member reporter = memberRepository.findById(reporterId)
                .orElseThrow(() -> new ResourceNotFoundException("해당 ID의 회원을 찾을 수 없습니다: " + reporterId));
        Report report = Report.createReportForComment(comment, reporter, reason);
        reportRepository.save(report);
        return ReportDTO.fromEntity(report);
    }

    @Transactional(readOnly = true)
    public List<ReportDTO> findAllReportsForBoard(Long boardId) {
        List<Report> reports = reportRepository.findByBoardId(boardId);
        return reports.stream()
                .map(ReportDTO::fromEntity)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<ReportDTO> findAllReportsForComment(Long commentId) {
        List<Report> reports = reportRepository.findByCommentId(commentId);
        return reports.stream()
                .map(ReportDTO::fromEntity)
                .collect(Collectors.toList());
    }
}