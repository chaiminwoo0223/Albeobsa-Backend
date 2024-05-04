package skhu.jijijig.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import skhu.jijijig.domain.model.Report;

import java.util.List;

@Repository
public interface ReportRepository extends JpaRepository<Report, Long> {
    List<Report> findByBoardId(Long boardId);

    List<Report> findByCommentId(Long commentId);
}