package skhu.jijijig.repository.crawling;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import skhu.jijijig.domain.Crawling;

import java.util.List;

public interface CrawlingRepositoryCustom {
    Page<Crawling> searchAllByKeyword(String keyword, Pageable pageable);

    List<Crawling> findTop10ByRanking();

    Page<Crawling> findAllSortedByDateTime(Pageable pageable);

    Page<Crawling> findAllSortedByDateTimeByLabel(String label, Pageable pageable);
}