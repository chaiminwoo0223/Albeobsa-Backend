package skhu.jijijig.service;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import skhu.jijijig.domain.model.Board;
import skhu.jijijig.domain.model.Comment;
import skhu.jijijig.domain.model.Member;
import skhu.jijijig.domain.repository.BoardRepository;
import skhu.jijijig.domain.repository.CommentRepository;
import skhu.jijijig.domain.repository.MemberRepository;

@Service
@RequiredArgsConstructor
public class CommentService {
    private final MemberRepository memberRepository;
    private final BoardRepository boardRepository;
    private final CommentRepository commentRepository;

    @Transactional
    public void addComment(Long boardId, Long memberId, String content) {
        Board board = boardRepository.findById(boardId)
                .orElseThrow(() -> new EntityNotFoundException("Board를 찾을 수 없습니다."));
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new EntityNotFoundException("Member를 찾을 수 없습니다."));
        board.attachComment(member, content, commentRepository);
    }

    @Transactional
    public void removeComment(Long boardId, Long commentId, Long memberId) {
        Comment comment = commentRepository.findByIdAndBoardId(commentId, boardId)
                .orElseThrow(() -> new EntityNotFoundException("Comment를 찾을 수 없습니다."));
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new EntityNotFoundException("Member를 찾을 수 없습니다."));
        comment.deleteCommentIfAuthorized(member, commentRepository);
    }
}