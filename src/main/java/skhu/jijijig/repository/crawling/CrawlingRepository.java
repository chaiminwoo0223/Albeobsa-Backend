package skhu.jijijig.repository.crawling;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import skhu.jijijig.domain.Crawling;

import java.util.Optional;

@Repository
public interface CrawlingRepository extends JpaRepository<Crawling, Long>, CrawlingRepositoryCustom {
    Optional<Crawling> findByLink(String link);
}