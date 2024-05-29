package skhu.jijijig.service;

import io.github.bonigarcia.wdm.WebDriverManager;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.springframework.context.ApplicationContext;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;

import java.time.Duration;
import java.util.Optional;
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
    private final ApplicationContext applicationContext;

    @Scheduled(fixedRate = 3600000) // 1시간마다 실행
    public void scheduleCrawlingTasks() {
        applicationContext.getBean(CrawlingService.class).performCrawlingForPpomppuDomestic();
        applicationContext.getBean(CrawlingService.class).performCrawlingForPpomppuOverseas();
        applicationContext.getBean(CrawlingService.class).performCrawlingForEomisae();
        applicationContext.getBean(CrawlingService.class).performCrawlingForRuliweb();
        applicationContext.getBean(CrawlingService.class).performCrawlingForCoolenjoy();
        applicationContext.getBean(CrawlingService.class).performCrawlingForQuasarzone();
    }

    @Transactional
    @Async
    public void performCrawlingForPpomppuDomestic() {
        System.out.println("뽐뿌(국내게시판)");
        crawlWebsite("https://www.ppomppu.co.kr/zboard/zboard.php?id=ppomppu", "뽐뿌(국내게시판)", "tbody > tr.baseList.bbs_new1");
    }

    @Transactional
    @Async
    public void performCrawlingForPpomppuOverseas() {
        System.out.println("뽐뿌(해외게시판)");
        crawlWebsite("https://www.ppomppu.co.kr/zboard/zboard.php?id=ppomppu4", "뽐뿌(해외게시판)", "tbody > tr.baseList.bbs_new1");
    }

    @Transactional
    @Async
    public void performCrawlingForEomisae() {
        System.out.println("어미새");
        crawlWebsite("https://eomisae.co.kr/rt", "어미새", "div.card_el.n_ntc.clear");
    }

    @Transactional
    @Async
    public void performCrawlingForRuliweb() {
        System.out.println("루리웹");
        crawlWebsite("https://bbs.ruliweb.com/news/board/1020", "루리웹", "tr.table_body.blocktarget");
    }

    @Transactional
    @Async
    public void performCrawlingForCoolenjoy() {
        System.out.println("쿨엔조이");
        crawlWebsite("https://coolenjoy.net/bbs/jirum", "쿨엔조이", "li.d-md-table-row.px-3.py-2.p-md-0.text-md-center.text-muted.border-bottom");
    }

    @Transactional
    @Async
    public void performCrawlingForQuasarzone() {
        System.out.println("퀘사이존");
        crawlWebsite("https://quasarzone.com/bbs/qb_saleinfo", "퀘사이존", "div.market-info-list");
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
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(30));
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
                int views = parsingService.parseViews(row, label, selectors[5]);
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
        String[] selectors = getSelectorsByLabel(label);
        int start = getStartByLabel(label);
        int minus = getMinusByLabel(label);
        return new ArrayList<>(extractCrawling(rows, label, start, minus, selectors));
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

    private String[] getSelectorsByLabel(String label) {
        if (label.startsWith("뽐뿌")) {
            return parsingService.getPpomppuSelectors();
        } else if (label.startsWith("어미새")) {
            return parsingService.getEomisaeSelectors();
        } else if (label.startsWith("루리웹")) {
            return parsingService.getRuliwebSelectors();
        } else if (label.startsWith("쿨엔조이")) {
            return parsingService.getCoolenjoySelectors();
        } else if (label.startsWith("퀘사이존")) {
            return parsingService.getQuasarzoneSelectors();
        }
        return new String[0]; // 기본값으로 빈 배열 반환
    }

    private int getStartByLabel(String label) {
        if (label.startsWith("루리웹")) {
            return 4;
        }
        return 0; // 기본값
    }

    private int getMinusByLabel(String label) {
        if (label.startsWith("뽐뿌")) {
            return 4;
        }
        return 0; // 기본값
    }

    private WebDriver setupChromeDriver() {
        ChromeOptions options = new ChromeOptions();
        options.addArguments("--headless", "--disable-gpu", "--no-sandbox", "--disable-dev-shm-usage", "--disable-extensions", "--disable-popup-blocking", "--start-maximized", "--window-size=1920,1080", "user-agent=Mozilla/5.0...");
        WebDriverManager.chromedriver().setup();
        return new ChromeDriver(options);
    }
}