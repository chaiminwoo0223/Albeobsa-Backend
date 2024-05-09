package skhu.jijijig.service;

import io.github.bonigarcia.wdm.WebDriverManager;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.springframework.context.ApplicationContext;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;

import java.time.Duration;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.CompletableFuture;
import java.util.List;
import java.util.ArrayList;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.By;
import skhu.jijijig.domain.model.Crawling;
import skhu.jijijig.repository.CrawlingRepository;

@Service
@RequiredArgsConstructor
public class CrawlingService {
    private final CrawlingRepository crawlingRepository;
    private final ApplicationContext applicationContext;
    private final ExecutorService executor = Executors.newFixedThreadPool(10);

    @Scheduled(fixedRate = 300000) // 5분마다 실행
    public void scheduleCrawlingTasks() {
        applicationContext.getBean(CrawlingService.class).performCrawlingForPpomppuDomestic();
        applicationContext.getBean(CrawlingService.class).performCrawlingForPpomppuOverseas();
    }

    @Transactional
    @Async
    public CompletableFuture<List<Crawling>> performCrawlingForPpomppuDomestic() {
        return CompletableFuture.supplyAsync(() -> crawlWebsite("https://www.ppomppu.co.kr/zboard/zboard.php?id=ppomppu", "뽐뿌(국내게시판)"), executor);
    }

    @Transactional
    @Async
    public CompletableFuture<List<Crawling>> performCrawlingForPpomppuOverseas() {
        return CompletableFuture.supplyAsync(() -> crawlWebsite("https://www.ppomppu.co.kr/zboard/zboard.php?id=ppomppu4", "뽐뿌(해외게시판)"), executor);
    }

    private List<Crawling> crawlWebsite(String url, String label) {
        WebDriver driver = setupChromeDriver();
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(1));
        List<Crawling> crawlings = new ArrayList<>();
        try {
            driver.get(url);
            List<WebElement> rows = wait.until(ExpectedConditions.visibilityOfAllElementsLocatedBy(By.cssSelector("tr.baseList.bbs_new1")));
            for (WebElement row : rows) {
                Crawling crawling = extractDataFromRow(row, label);
                if (crawling != null) {
                    crawlings.add(crawling);
                }
            }
            crawlingRepository.saveAll(crawlings);
        } catch (Exception e) {
            System.err.println("크롤링 도중 오류 발생: " + e.getMessage());
        } finally {
            driver.quit();
        }
        return crawlings;
    }

    private Crawling extractDataFromRow(WebElement row, String label) {
        try {
            String title = row.findElement(By.cssSelector("a.baseList-title")).getText();
            String name = Optional.ofNullable(row.findElement(By.cssSelector("a.baseList-name")).getText()).orElse("No name");
            String imageURL = Optional.ofNullable(row.findElement(By.cssSelector("a.baseList-thumb img")).getAttribute("src"))
                    .map(src -> src.startsWith("//") ? "https:" + src : src).orElse("No image");
            int views = parseInteger(row.findElement(By.cssSelector("td.baseList-space.baseList-views")).getText());
            int recommendCnt = parseInteger(row.findElement(By.cssSelector("td.baseList-space.baseList-rec")).getText().split(" - ")[0]);
            int commentCnt = parseInteger(row.findElements(By.cssSelector("span.baseList-c")).stream().findFirst().orElseThrow().getText());
            int unrecommendCnt = 0;
            String link = row.findElement(By.cssSelector("a.baseList-title")).getAttribute("href");
            String createdDateTime = row.findElement(By.cssSelector("time.baseList-time")).getText();
            return Crawling.of(label, title, name, imageURL, link, createdDateTime, views, recommendCnt, unrecommendCnt, commentCnt);
        } catch (Exception e) {
            return null;
        }
    }

    private int parseInteger(String text) {
        try {
            return Integer.parseInt(text.replaceAll("\\D+", ""));
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    private WebDriver setupChromeDriver() {
        ChromeOptions options = new ChromeOptions();
        options.addArguments("--headless", "--disable-gpu", "--no-sandbox", "--disable-dev-shm-usage", "--disable-extensions", "--disable-popup-blocking", "--start-maximized", "--window-size=1920,1080", "user-agent=Mozilla/5.0...", "--disable-infobars", "--disable-browser-side-navigation", "--disable-setuid-sandbox");
        options.setCapability("goog:loggingPrefs", java.util.Collections.singletonMap("browser", "ALL"));
        WebDriverManager.chromedriver().setup();
        return new ChromeDriver(options);
    }
}