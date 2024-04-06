package skhu.jijijig.service;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import skhu.jijijig.domain.model.Board;
import skhu.jijijig.domain.model.Member;
import skhu.jijijig.domain.repository.BoardRepository;
import skhu.jijijig.domain.repository.HeartRepository;
import skhu.jijijig.domain.repository.MemberRepository;

@Service
@RequiredArgsConstructor
public class HeartService {
    private final MemberRepository memberRepository;
    private final BoardRepository boardRepository;
    private final HeartRepository heartRepository;

    @Transactional
    public void addHeart(Long boardId, Long memberId) {
        Board board = boardRepository.findById(boardId)
                .orElseThrow(() -> new EntityNotFoundException("Board를 찾을 수 없습니다."));
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new EntityNotFoundException("Member를 찾을 수 없습니다."));
        board.addHeart(member, heartRepository);
        boardRepository.save(board);
    }

    @Transactional
    public void removeHeart(Long boardId, Long memberId) {
        Board board = boardRepository.findById(boardId)
                .orElseThrow(() -> new EntityNotFoundException("Board를 찾을 수 없습니다."));
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new EntityNotFoundException("Member를 찾을 수 없습니다."));
        board.removeHeart(member, heartRepository);
        boardRepository.save(board);
    }
}