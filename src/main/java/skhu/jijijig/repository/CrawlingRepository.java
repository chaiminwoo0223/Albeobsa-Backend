package skhu.jijijig.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import skhu.jijijig.domain.model.Crawling;

@Repository
public interface CrawlingRepository extends JpaRepository<Crawling, Long> {
}