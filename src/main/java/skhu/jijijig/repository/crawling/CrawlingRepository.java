package skhu.jijijig.repository.crawling;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import skhu.jijijig.domain.model.Crawling;

import java.util.Optional;

@Repository
public interface CrawlingRepository extends JpaRepository<Crawling, Long>, CrawlingRepositoryCustom {
    Optional<Crawling> findByLink(String link);

    Page<Crawling> findAllByOrderByDateTimeDesc(Pageable pageable);

    Page<Crawling> findByLabelOrderByDateTimeDesc(String label, Pageable pageable);
}