package skhu.jijijig.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import skhu.jijijig.domain.Board;

@Repository
public interface BoardRepository extends JpaRepository<Board, Long> {
}