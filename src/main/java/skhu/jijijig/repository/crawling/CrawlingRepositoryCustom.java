package skhu.jijijig.repository.crawling;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import skhu.jijijig.domain.model.Crawling;

import java.util.List;

public interface CrawlingRepositoryCustom {
    Page<Crawling> searchCrawlingWithPagination(String keyword, Pageable pageable);

    List<Crawling> findTop10ByRecommendAndComment();
}