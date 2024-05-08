package skhu.jijijig.service;

import io.github.bonigarcia.wdm.WebDriverManager;
import lombok.RequiredArgsConstructor;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.*;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import skhu.jijijig.domain.model.Crawling;
import skhu.jijijig.repository.CrawlingRepository;

import java.text.SimpleDateFormat;
import java.time.Duration;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CrawlingService {
    private final CrawlingRepository crawlingRepository;
    private final ExecutorService executor = Executors.newFixedThreadPool(10);  // 동시에 처리할 수 있는 최대 크롤링 작업 수

    private static final Calendar TODAY_CALENDAR = Calendar.getInstance();
    private static final String TODAY_DATE = new SimpleDateFormat("yyyy-MM-dd").format(TODAY_CALENDAR.getTime());

    // 자동 크롤링 설정: 매 분마다 실행
    @Scheduled(fixedRate = 60000)
    public void scheduleCrawlingTasks() {
        crawlingPpomppuDomestic();
        crawlingPpomppuOverseas();
    }

    public CompletableFuture<List<Crawling>> crawlingPpomppuDomestic() {
        return CompletableFuture.supplyAsync(() -> performcrawlingAsync("https://www.ppomppu.co.kr/zboard/zboard.php?id=ppomppu", "뽐뿌(국내게시판)"), executor);
    }

    public CompletableFuture<List<Crawling>> crawlingPpomppuOverseas() {
        return CompletableFuture.supplyAsync(() -> performcrawlingAsync("https://www.ppomppu.co.kr/zboard/zboard.php?id=ppomppu4", "뽐뿌(해외게시판)"), executor);
    }

    private List<Crawling> performcrawlingAsync(String url, String label) {
        WebDriver driver = getChromeDriver();
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(1));
        List<Crawling> crawlings = new ArrayList<>();
        try {
            driver.get(url);
            List<WebElement> rows = wait.until(ExpectedConditions.visibilityOfAllElementsLocatedBy(By.cssSelector("tr.baseList.bbs_new1")));
            for (WebElement row : rows) {
                Crawling crawling = extractCrawlingData(row, label);
                if (crawling != null) crawlings.add(crawling);
            }
            crawlingRepository.saveAll(crawlings);
        } catch (Exception e) {
            System.err.println("크롤링 중 오류 발생: " + e.getMessage());  // 한국어로 에러 메시지 표시
        } finally {
            driver.quit();
        }
        return crawlings;
    }

    private Crawling extractCrawlingData(WebElement row, String label) {
        try {
            String title = row.findElement(By.cssSelector("a.baseList-title")).getText();
            String name = Optional.ofNullable(row.findElement(By.cssSelector("a.baseList-name")).getText()).orElse("");
            String imageURL = Optional.ofNullable(row.findElement(By.cssSelector("a.baseList-thumb img")).getAttribute("src"))
                    .map(src -> src.startsWith("//") ? "https:" + src : src).orElse("이미지 없음");
            int views = parseInt(row.findElement(By.cssSelector("td.baseList-space.baseList-views")).getText());
            int recommendCnt = parseInt(row.findElement(By.cssSelector("td.baseList-space.baseList-rec")).getText().split(" - ")[0]);
            int commentCnt = parseInt(row.findElements(By.cssSelector("span.baseList-c")).stream().findFirst().orElseThrow().getText());
            String link = row.findElement(By.cssSelector("a.baseList-title")).getAttribute("href");
            String createdDate = parseDate(row.findElement(By.cssSelector("time.baseList-time")).getText());
            return Crawling.of(label, title, name, imageURL, views, recommendCnt, commentCnt, createdDate, link);
        } catch (Exception e) {
            return null;  // 실패한 크롤링 작업은 null 반환
        }
    }

    private int parseInt(String text) {
        try {
            return Integer.parseInt(text.replaceAll("\\D+", ""));
        } catch (NumberFormatException e) {
            return 0;  // 숫자 변환 실패 시 0 반환
        }
    }

    private String parseDate(String date) {
        if (date.contains(":")) {
            return TODAY_DATE;
        } else {
            String[] parts = date.split("/");
            int month = Integer.parseInt(parts[1]);
            int day = Integer.parseInt(parts[2]);
            int year = TODAY_CALENDAR.get(Calendar.YEAR) + (month > TODAY_CALENDAR.get(Calendar.MONTH) + 1 ? -1 : 0);
            return String.format("%d-%02d-%02d", year, month, day);
        }
    }

    private WebDriver getChromeDriver() {
        ChromeOptions options = new ChromeOptions();
        options.addArguments("--headless", "--disable-gpu", "--no-sandbox", "--disable-dev-shm-usage", "--disable-extensions", "--disable-popup-blocking", "--start-maximized", "--window-size=1920,1080", "user-agent=Mozilla/5.0...", "--disable-infobars", "--disable-browser-side-navigation", "--disable-setuid-sandbox");
        options.setCapability("goog:loggingPrefs", java.util.Collections.singletonMap("browser", "ALL"));
        WebDriverManager.chromedriver().setup();
        return new ChromeDriver(options);
    }
}