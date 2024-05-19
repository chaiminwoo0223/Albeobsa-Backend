package skhu.jijijig.repository.crawling;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import skhu.jijijig.domain.model.Crawling;

public interface CrawlingRepositoryCustom {
    Page<Crawling> searchCrawlingWithPagination(String keyword, Pageable pageable);
}