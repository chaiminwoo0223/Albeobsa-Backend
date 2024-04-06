package skhu.jijijig.service;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import skhu.jijijig.domain.dto.BoardDTO;
import skhu.jijijig.domain.model.*;
import skhu.jijijig.domain.repository.BoardRepository;
import skhu.jijijig.domain.repository.CommentRepository;
import skhu.jijijig.domain.repository.HeartRepository;
import skhu.jijijig.domain.repository.MemberRepository;

@Service
@RequiredArgsConstructor
public class BoardService {
    private final MemberRepository memberRepository;
    private final BoardRepository boardRepository;
    private final CommentRepository commentRepository;
    private final HeartRepository heartRepository;

    @Transactional
    public Page<BoardDTO> getBoards(Pageable pageable) {
        return boardRepository.findAll(pageable)
                .map(board -> BoardDTO.fromEntity(board, Comment.toDTOsForBoard(board, commentRepository)));
    }

    @Transactional
    public BoardDTO getBoard(Long boardId) {
        Board board = boardRepository.findById(boardId)
                .orElseThrow(() -> new EntityNotFoundException("Board를 찾을 수 없습니다."));
        return BoardDTO.fromEntity(board, Comment.toDTOsForBoard(board, commentRepository));
    }

    @Transactional
    public Long addBoard(BoardDTO boardDTO, Long memberId) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new EntityNotFoundException("Member를 찾을 수 없습니다."));
        Board board = Board.createNewBoard(boardDTO, member);
        return boardRepository.save(board).getId();
    }

    @Transactional
    public void removeBoard(Long boardId, Long memberId) {
        Board board = boardRepository.findById(boardId)
                .orElseThrow(() -> new EntityNotFoundException("Board를 찾을 수 없습니다."));
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new EntityNotFoundException("Member를 찾을 수 없습니다."));
        board.deleteBoardIfAuthorized(member, boardRepository, commentRepository, heartRepository);
    }
}