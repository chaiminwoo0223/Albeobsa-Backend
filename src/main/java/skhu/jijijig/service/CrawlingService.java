package skhu.jijijig.service;

import io.github.bonigarcia.wdm.WebDriverManager;
import lombok.RequiredArgsConstructor;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.*;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import skhu.jijijig.domain.model.Crawling;
import skhu.jijijig.repository.CrawlingRepository;

import java.text.SimpleDateFormat;
import java.time.Duration;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import java.util.List;
import java.util.concurrent.*;

@Service
@RequiredArgsConstructor
public class CrawlingService {
    private final CrawlingRepository crawlingRepository;
    private final ExecutorService executor = Executors.newFixedThreadPool(10);

    private static final Calendar TODAY_CALENDAR = Calendar.getInstance();
    private static final String TODAY_DATE = new SimpleDateFormat("yyyy-MM-dd").format(TODAY_CALENDAR.getTime());

    @Transactional
    public CompletableFuture<List<Crawling>> crawlingPpomppuDomestic() {
        return CompletableFuture.supplyAsync(() -> crawlingAsync("https://www.ppomppu.co.kr/zboard/zboard.php?id=ppomppu", "뽐뿌(국내게시판)"), executor);
    }

    @Transactional
    public CompletableFuture<List<Crawling>> crawlingPpomppuOverseas() {
        return CompletableFuture.supplyAsync(() -> crawlingAsync("https://www.ppomppu.co.kr/zboard/zboard.php?id=ppomppu4", "뽐뿌(해외게시판)"), executor);
    }

    private List<Crawling> crawlingAsync(String url, String label) {
        WebDriver driver = getChromeDriver();
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(1));
        List<Crawling> crawlings = new ArrayList<>();
        try {
            driver.get(url);
            List<WebElement> rows = wait.until(ExpectedConditions.visibilityOfAllElementsLocatedBy(By.cssSelector("tr.baseList.bbs_new1")));
            rows.stream().limit(rows.size() - 4).forEach(row -> crawlings.add(extractCrawlingData(row, label)));
            crawlingRepository.saveAll(crawlings);
        } catch (Exception e) {
            System.err.println("크롤링 중 오류 발생: " + e.getMessage());
        } finally {
            driver.quit();
        }
        return crawlings;
    }

    private Crawling extractCrawlingData(WebElement row, String label) {
        String title = row.findElement(By.cssSelector("a.baseList-title")).getText();
        String name = Optional.ofNullable(row.findElement(By.cssSelector("a.baseList-name")).getText()).orElse("");
        String imageURL = Optional.ofNullable(row.findElement(By.cssSelector("a.baseList-thumb img")).getAttribute("src"))
                .map(src -> src.startsWith("//") ? "https:" + src : src).orElse("No Image");
        int views = parseInt(row.findElement(By.cssSelector("td.baseList-space.baseList-views")).getText());
        int recommendCnt = parseInt(row.findElement(By.cssSelector("td.baseList-space.baseList-rec")).getText().split(" - ")[0]);
        int commentCnt = parseInt(row.findElements(By.cssSelector("span.baseList-c")).stream().findFirst().orElseThrow().getText());
        String link = row.findElement(By.cssSelector("a.baseList-title")).getAttribute("href");
        String createdDate = parseDate(row.findElement(By.cssSelector("time.baseList-time")).getText());
        return Crawling.of(label, title, name, imageURL, views, recommendCnt, commentCnt, createdDate, link);
    }

    private int parseInt(String text) {
        try {
            return Integer.parseInt(text.replaceAll("[^\\d]", ""));
        } catch (NumberFormatException e) {
            return 0;
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
        options.addArguments("--headless"); // GUI 없는 환경에서 실행
        options.addArguments("--disable-gpu"); // GPU 가속 비활성화
        options.addArguments("--no-sandbox"); // 샌드박스 모드 비활성화, Docker에서 필수
        options.addArguments("--disable-dev-shm-usage"); // 컨테이너 환경에서 공유 메모리 사용량 최적화
        options.addArguments("--disable-extensions"); // 확장 프로그램 비활성화
        options.addArguments("--disable-popup-blocking"); // 팝업 차단 해제
        options.addArguments("--start-maximized"); // 브라우저 최대 크기로 시작
        options.addArguments("--window-size=1920,1080"); // 창 크기 명시적 설정
        options.addArguments("user-agent=Mozilla/5.0..."); // 사용자 에이전트 설정
        options.addArguments("--disable-infobars"); // 정보 바 비활성화
        options.addArguments("--disable-browser-side-navigation"); // 브라우저 사이드 네비게이션 비활성화
        options.addArguments("--disable-setuid-sandbox"); // 프로세스 간 샌드박스 모드 비활성화
        options.setCapability("goog:loggingPrefs", java.util.Collections.singletonMap("browser", "ALL")); // 로깅 레벨 설정 (에러 발생 시 로그를 확인할 수 있도록)
        WebDriverManager.chromedriver().setup(); // WebDriverManager를 이용한 ChromeDriver 설정
        return new ChromeDriver(options);
    }
}