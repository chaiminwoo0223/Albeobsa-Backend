package skhu.jijijig.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import skhu.jijijig.domain.dto.BoardDTO;
import skhu.jijijig.domain.model.*;
import skhu.jijijig.repository.BoardRepository;
import skhu.jijijig.repository.CommentRepository;
import skhu.jijijig.repository.HeartRepository;
import skhu.jijijig.repository.MemberRepository;
import skhu.jijijig.exception.ResourceNotFoundException;

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
                .orElseThrow(() -> new ResourceNotFoundException("해당 ID의 게시글을 찾을 수 없습니다: " + boardId));
        return BoardDTO.fromEntity(board, Comment.toDTOsForBoard(board, commentRepository));
    }

    @Transactional
    public Long addBoard(BoardDTO boardDTO, Long memberId) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new ResourceNotFoundException("해당 ID의 회원을 찾을 수 없습니다: " + memberId));
        Board board = Board.createNewBoard(boardDTO, member);
        return boardRepository.save(board).getId();
    }

    @Transactional
    public void editBoard(Long boardId, BoardDTO boardDTO, Long memberId) {
        Board board = boardRepository.findById(boardId)
                .orElseThrow(() -> new ResourceNotFoundException("해당 ID의 게시글을 찾을 수 없습니다: " + boardId));
        if (!board.getMember().getId().equals(memberId)) {
            throw new AccessDeniedException("게시글 수정 권한이 없습니다.");
        }
        board.updateDetails(boardDTO.getTitle(), boardDTO.getContent());
        boardRepository.save(board);
    }

    @Transactional
    public void removeBoard(Long boardId, Long memberId) {
        Board board = boardRepository.findById(boardId)
                .orElseThrow(() -> new ResourceNotFoundException("해당 ID의 게시글을 찾을 수 없습니다: " + boardId));
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new ResourceNotFoundException("해당 ID의 회원을 찾을 수 없습니다: " + memberId));
        board.deleteBoardIfAuthorized(member, boardRepository, commentRepository, heartRepository);
    }
}