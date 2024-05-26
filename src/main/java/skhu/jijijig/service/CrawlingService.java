package skhu.jijijig.service;

import io.github.bonigarcia.wdm.WebDriverManager;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;

import java.time.Duration;
import java.util.Optional;
import java.util.concurrent.*;
import java.util.List;
import java.util.ArrayList;
import java.util.stream.Collectors;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.By;
import skhu.jijijig.domain.dto.CrawlingDTO;
import skhu.jijijig.domain.Crawling;
import skhu.jijijig.exception.CrawlingProcessException;
import skhu.jijijig.repository.crawling.CrawlingRepository;

@Service
@RequiredArgsConstructor
public class CrawlingService {
    private final ParsingService parsingService;
    private final CrawlingRepository crawlingRepository;

    private ScheduledExecutorService executor = Executors.newScheduledThreadPool(10);

    @Scheduled(fixedRate = 3600000) // 1시간마다 실행
    public void scheduleCrawlingTasks() {
        submitCrawlingTask("https://www.ppomppu.co.kr/zboard/zboard.php?id=ppomppu", "뽐뿌(국내게시판)", "tbody > tr.baseList.bbs_new1");
        submitCrawlingTask("https://www.ppomppu.co.kr/zboard/zboard.php?id=ppomppu4", "뽐뿌(해외게시판)", "tbody > tr.baseList.bbs_new1");
        submitCrawlingTask("https://eomisae.co.kr/rt", "어미새", "div.card_el.n_ntc.clear");
        submitCrawlingTask("https://bbs.ruliweb.com/news/board/1020", "루리웹", "tr.table_body.blocktarget");
        submitCrawlingTask("https://coolenjoy.net/bbs/jirum", "쿨엔조이", "li.d-md-table-row.px-3.py-2.p-md-0.text-md-center.text-muted.border-bottom");
    }

    @Scheduled(fixedRate = 3600000) // 1시간마다 실행
    public void manageThreads() {
        System.out.println("Managing threads...");
        if (executor.isShutdown() || executor.isTerminated()) {
            restartExecutor();
        }
        System.gc(); // 가비지 컬렉션 강제 실행
    }

    @Transactional(readOnly = true)
    public Page<CrawlingDTO> searchCrawling(String keyword, Pageable pageable) {
        try {
            Page<Crawling> crawlings = crawlingRepository.searchAllByKeyword(keyword, pageable);
            return crawlings.map(CrawlingDTO::fromEntity);
        } catch (Exception e) {
            throw new CrawlingProcessException("크롤링 검색 중 오류 발생" + e.getMessage());
        }

    }

    @Transactional(readOnly = true)
    public List<CrawlingDTO> getTop10CrawlingsByRanking() {
        try {
            List<Crawling> crawlings = crawlingRepository.findTop10ByRanking();
            return crawlings.stream()
                    .map(CrawlingDTO::fromEntity)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            throw new CrawlingProcessException("크롤링 랭킹 검색 중 오류 발생" + e.getMessage());
        }
    }

    @Transactional(readOnly = true)
    public List<CrawlingDTO> getAllCrawlingsSortedByDateTime(Pageable pageable) {
        try {
            Page<Crawling> crawlings = crawlingRepository.findAllSortedByDateTime(pageable);
            return crawlings.stream()
                    .map(CrawlingDTO::fromEntity)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            throw new CrawlingProcessException("크롤링 날짜순 검색 중 오류 발생" + e.getMessage());
        }
    }

    @Transactional(readOnly = true)
    public List<CrawlingDTO> getCrawlingsSortedByLabelAndDateTime(String label, Pageable pageable) {
        try {
            Page<Crawling> crawlings = crawlingRepository.findAllSortedByDateTimeByLabel(label, pageable);
            return crawlings.stream()
                    .map(CrawlingDTO::fromEntity)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            throw new CrawlingProcessException("크롤링 라벨 및 날짜순 검색 중 오류 발생" + e.getMessage());
        }
    }

    private void crawlWebsite(String url, String label, String ROWS) {
        WebDriver driver = setupChromeDriver();
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
        List<Crawling> crawlings = new ArrayList<>();
        try {
            driver.get(url);
            List<WebElement> rows = wait.until(ExpectedConditions.visibilityOfAllElementsLocatedBy(By.cssSelector(ROWS)));
            crawlings.addAll(handleCrawlingByLabel(label, rows));
            crawlingRepository.saveAll(crawlings);
        } catch (Exception e) {
            throw new CrawlingProcessException("크롤링 중 오류 발생: " + e.getMessage());
        } finally {
            driver.quit();
        }
    }

    private List<Crawling> extractCrawling(List<WebElement> rows, String label, int start, int minus, String[] selectors) {
        List<Crawling> crawlings = new ArrayList<>();
        for (int i = start; i < rows.size() - minus; i++) {
            WebElement row = rows.get(i);
            boolean open = parsingService.parseOpen(row, selectors[0]);
            try {
                String title = parsingService.parseTitle(row, selectors[1]);
                String name = parsingService.parseName(row, selectors[2]);
                String image = parsingService.parseImage(row, label, selectors[3]);
                String link = parsingService.parseLink(row, selectors[1]);
                String dateTime = parsingService.parseDateTime(row, label, selectors[4]);
                int views = parsingService.parseViews(row, selectors[5]);
                int[] recommendCnts = parsingService.parseRecommendCnts(row, selectors[6]);
                int commentCnt = parsingService.parseCommentCnt(row, selectors[7]);
                Crawling crawling = Crawling.of(label, title, name, image, link, dateTime, views, recommendCnts[0], recommendCnts[1], commentCnt, open);
                createOrUpdateCrawling(crawling);
            } catch (Exception e) {
                throw new CrawlingProcessException("데이터 추출 중 오류 발생: " + e.getMessage());
            }
        }
        return crawlings;
    }

    private List<Crawling> handleCrawlingByLabel(String label, List<WebElement> rows) {
        List<Crawling> crawlings = new ArrayList<>();
        if (label.startsWith("뽐뿌")) {
            crawlings.addAll(extractCrawling(rows, label, 0, 4, parsingService.getPpomppuSelectors()));
        } else if (label.startsWith("어미새")) {
            crawlings.addAll(extractCrawling(rows, label, 0, 0, parsingService.getEomisaeSelectors()));
        } else if (label.startsWith("루리웹")) {
            crawlings.addAll(extractCrawling(rows, label, 4, 0, parsingService.getRuliwebSelectors()));
        } else if (label.startsWith("쿨엔조이")) {
            crawlings.addAll(extractCrawling(rows, label, 0, 0, parsingService.getCoolenjoySelectors()));
        }
        return crawlings;
    }

    private void createOrUpdateCrawling(Crawling crawling) {
        Optional<Crawling> existing = crawlingRepository.findByLink(crawling.getLink());
        if (existing.isPresent()) {
            Crawling existingCrawling = existing.get();
            if (existingCrawling.isDifferent(crawling)) {
                crawlingRepository.deleteById(existingCrawling.getId());
                crawlingRepository.save(crawling);
            }
        } else {
            crawlingRepository.save(crawling);
        }
    }

    private void submitCrawlingTask(String url, String label, String rows) {
        executor.submit(() -> crawlWebsite(url, label, rows));
    }

    private synchronized void restartExecutor() {
        if (!executor.isShutdown()) {
            executor.shutdown();
            try {
                if (!executor.awaitTermination(300, TimeUnit.SECONDS)) {
                    executor.shutdownNow();
                }
            } catch (InterruptedException e) {
                executor.shutdownNow(); // InterruptedException 발생 시 즉시 종료
                Thread.currentThread().interrupt(); // 현재 스레드에 대한 interrupt 상태를 설정하여 예외를 적절히 처리
            }
        }
        executor = Executors.newScheduledThreadPool(60);
    }

    private WebDriver setupChromeDriver() {
        ChromeOptions options = new ChromeOptions();
        options.addArguments("--headless", "--disable-gpu", "--no-sandbox", "--disable-dev-shm-usage", "--disable-extensions", "--disable-popup-blocking", "--start-maximized", "--window-size=1920,1080", "user-agent=Mozilla/5.0...");
        WebDriverManager.chromedriver().setup();
        return new ChromeDriver(options);
    }
}