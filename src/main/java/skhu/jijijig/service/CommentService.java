package skhu.jijijig.service;

import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import skhu.jijijig.domain.model.Board;
import skhu.jijijig.domain.model.Comment;
import skhu.jijijig.domain.model.Member;
import skhu.jijijig.domain.repository.BoardRepository;
import skhu.jijijig.domain.repository.CommentRepository;
import skhu.jijijig.domain.repository.MemberRepository;
import skhu.jijijig.exception.ResourceNotFoundException;

@Service
@RequiredArgsConstructor
public class CommentService {
    private final MemberRepository memberRepository;
    private final BoardRepository boardRepository;
    private final CommentRepository commentRepository;

    @Transactional
    public void addComment(Long boardId, Long memberId, String content) {
        Board board = boardRepository.findById(boardId)
                .orElseThrow(() -> new ResourceNotFoundException("해당 ID의 게시글을 찾을 수 없습니다: " + boardId));
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new ResourceNotFoundException("해당 ID의 회원을 찾을 수 없습니다: " + memberId));
        board.attachComment(member, content, commentRepository);
    }

    @Transactional
    public void editComment(Long boardId, Long commentId, Long memberId, String content) {
        Comment comment = commentRepository.findByIdAndBoardId(commentId, boardId)
                .orElseThrow(() -> new ResourceNotFoundException("해당 ID의 댓글을 찾을 수 없습니다: " + commentId));
        if (!comment.getMember().getId().equals(memberId)) {
            throw new AccessDeniedException("댓글 수정 권한이 없습니다.");
        }
        comment.updateContent(content);
        commentRepository.save(comment);
    }

    @Transactional
    public void removeComment(Long boardId, Long commentId, Long memberId) {
        Comment comment = commentRepository.findByIdAndBoardId(commentId, boardId)
                .orElseThrow(() -> new ResourceNotFoundException("해당 ID의 게시글을 찾을 수 없습니다: " + boardId));
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new ResourceNotFoundException("해당 ID의 회원을 찾을 수 없습니다: " + memberId));
        comment.deleteCommentIfAuthorized(member, commentRepository);
    }
}