package skhu.jijijig.repository.crawling;

import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.support.PageableExecutionUtils;
import org.springframework.stereotype.Repository;
import skhu.jijijig.domain.model.Crawling;
import skhu.jijijig.domain.model.QCrawling;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class CrawlingRepositoryImpl implements CrawlingRepositoryCustom {
    private final JPAQueryFactory queryFactory;

    @Override
    public Page<Crawling> searchCrawlingWithPagination(String keyword, Pageable pageable) {
        QCrawling crawling = QCrawling.crawling;

        List<Crawling> searchResult = queryFactory.selectFrom(crawling)
                .where(keywordContainsInLabelOrTitle(keyword))
                .orderBy(crawling.dateTime.desc())
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        JPAQuery<Long> countQuery = queryFactory
                .select(crawling.count())
                .from(crawling)
                .where(keywordContainsInLabelOrTitle(keyword));

        return PageableExecutionUtils.getPage(searchResult, pageable, countQuery::fetchOne);
    }

    @Override
    public List<Crawling> findTop10ByRecommendAndComment() {
        QCrawling crawling = QCrawling.crawling;

        return queryFactory.selectFrom(crawling)
                .orderBy(crawling.recommendCnt.desc(), crawling.unrecommendCnt.asc(), crawling.commentCnt.desc())
                .limit(10)
                .fetch();
    }

    private BooleanExpression keywordContainsInLabelOrTitle(String keyword) {
        return keyword != null ? QCrawling.crawling.label.containsIgnoreCase(keyword)
                .or(QCrawling.crawling.title.containsIgnoreCase(keyword)) : null;
    }
}