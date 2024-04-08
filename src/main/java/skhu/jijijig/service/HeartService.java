package skhu.jijijig.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import skhu.jijijig.domain.model.Board;
import skhu.jijijig.domain.model.Member;
import skhu.jijijig.domain.repository.BoardRepository;
import skhu.jijijig.domain.repository.HeartRepository;
import skhu.jijijig.domain.repository.MemberRepository;
import skhu.jijijig.exception.ResourceNotFoundException;

@Service
@RequiredArgsConstructor
public class HeartService {
    private final MemberRepository memberRepository;
    private final BoardRepository boardRepository;
    private final HeartRepository heartRepository;

    @Transactional
    public void addHeart(Long boardId, Long memberId) {
        Board board = boardRepository.findById(boardId)
                .orElseThrow(() -> new ResourceNotFoundException("해당 ID의 게시글을 찾을 수 없습니다: " + boardId));
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new ResourceNotFoundException("해당 ID의 회원을 찾을 수 없습니다: " + memberId));
        board.attachHeart(member, heartRepository);
        boardRepository.save(board);
    }

    @Transactional
    public void removeHeart(Long boardId, Long memberId) {
        Board board = boardRepository.findById(boardId)
                .orElseThrow(() -> new ResourceNotFoundException("해당 ID의 게시글을 찾을 수 없습니다: " + boardId));
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new ResourceNotFoundException("해당 ID의 회원을 찾을 수 없습니다: " + memberId));
        board.detachHeart(member, heartRepository);
        boardRepository.save(board);
    }
}