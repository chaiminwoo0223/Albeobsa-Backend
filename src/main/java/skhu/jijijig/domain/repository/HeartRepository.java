package skhu.jijijig.domain.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import skhu.jijijig.domain.model.Board;
import skhu.jijijig.domain.model.Heart;
import skhu.jijijig.domain.model.Member;

import java.util.Optional;

@Repository
public interface HeartRepository extends JpaRepository<Heart, Long> {
    boolean existsByBoardAndMember(Board board, Member member);

    Optional<Heart> findByBoardAndMember(Board board, Member member);
}