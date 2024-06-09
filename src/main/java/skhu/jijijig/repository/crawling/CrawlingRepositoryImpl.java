package skhu.jijijig.repository.crawling;

import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.support.PageableExecutionUtils;
import org.springframework.stereotype.Repository;
import skhu.jijijig.domain.Crawling;
import skhu.jijijig.domain.QCrawling;

import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class CrawlingRepositoryImpl implements CrawlingRepositoryCustom {
    private final JPAQueryFactory queryFactory;

    @Override
    public Page<Crawling> searchAllByKeyword(String keyword, Pageable pageable) {
        QCrawling crawling = QCrawling.crawling;

        List<Crawling> result = queryFactory.selectFrom(crawling)
                .where(keywordContainsInLabelOrTitle(keyword))
                .orderBy(crawling.dateTime.desc())
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        JPAQuery<Long> countQuery = queryFactory
                .select(crawling.count())
                .from(crawling)
                .where(keywordContainsInLabelOrTitle(keyword));

        return PageableExecutionUtils.getPage(result, pageable, countQuery::fetchOne);
    }

    @Override
    public List<Crawling> findTop10ByRanking() {
        QCrawling crawling = QCrawling.crawling;

        return queryFactory.selectFrom(crawling)
                .orderBy(crawling.recommendCnt.desc(), crawling.unrecommendCnt.asc(), crawling.commentCnt.desc())
                .limit(10)
                .fetch();
    }

    @Override
    public Page<Crawling> findAllSortedByDateTime(Pageable pageable) {
        QCrawling crawling = QCrawling.crawling;

        List<Crawling> result = queryFactory.selectFrom(crawling)
                .orderBy(crawling.dateTime.desc())
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        JPAQuery<Long> countQuery = queryFactory
                .select(crawling.count())
                .from(crawling);

        return PageableExecutionUtils.getPage(result, pageable, countQuery::fetchOne);
    }

    @Override
    public Page<Crawling> findAllSortedByDateTimeByLabel(String label, Pageable pageable) {
        QCrawling crawling = QCrawling.crawling;

        List<Crawling> result = queryFactory.selectFrom(crawling)
                .where(crawling.label.eq(label))
                .orderBy(crawling.dateTime.desc())
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        JPAQuery<Long> countQuery = queryFactory
                .select(crawling.count())
                .from(crawling)
                .where(crawling.label.eq(label));

        return PageableExecutionUtils.getPage(result, pageable, countQuery::fetchOne);
    }

    @Override
    public Optional<String> findDateTimeByLabel(String label) {
        QCrawling crawling = QCrawling.crawling;

        String dateTime = queryFactory.select(crawling.dateTime)
                .from(crawling)
                .where(crawling.label.eq(label))
                .fetchFirst();

        return Optional.ofNullable(dateTime);
    }

    private BooleanExpression keywordContainsInLabelOrTitle(String keyword) {
        return keyword != null ? QCrawling.crawling.label.containsIgnoreCase(keyword)
                .or(QCrawling.crawling.title.containsIgnoreCase(keyword)) : null;
    }
}