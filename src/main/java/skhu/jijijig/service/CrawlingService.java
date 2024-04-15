package skhu.jijijig.service;

import lombok.RequiredArgsConstructor;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import skhu.jijijig.domain.model.Crawling;
import skhu.jijijig.repository.CrawlingRepository;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Service
public class CrawlingService {
    private final CrawlingRepository crawlingRepository;

    @Value("${CHROME_DRIVER}")
    private String chromedriver;

    // 뽐뿌(국내게시판)
    @Transactional
    public void crawlingPpomppuDomestic() {
        crawlingPpomppu("https://www.ppomppu.co.kr/zboard/zboard.php?id=ppomppu");
    }

    // 뽐뿌(해외게시판)
    @Transactional
    public void crawlingPpomppuOverseas() {
        crawlingPpomppu("https://www.ppomppu.co.kr/zboard/zboard.php?id=ppomppu4");
    }

    // 루리웹(예판 핫딜 뽐뿌 게시판)
    @Transactional
    public void crawlingRuliweb() {
        String cssSelectors = "tr.table_body.best.inside.blocktarget.other, tr.table_body.best.inside.blocktarget, tr.table_body.blocktarget";
        crawlingWebSite("https://bbs.ruliweb.com/news/board/1020", cssSelectors);
    }

    // 쿨엔조이(지름/알뜰정보 페이지)
    @Transactional
    public void crawlingCoolenjoy() {
        crawlingWebSite("https://coolenjoy.net/bbs/jirum", "ul.na-table.d-md-table.w-100");
    }

    // 퀘사이존(핫딜게시판)
    @Transactional
    public void crawlingQuasarzone() {
        crawlingWebSite("https://quasarzone.com/bbs/qb_saleinfo", "tbody > tr");
    }

    // 어미새(기타정보)
    @Transactional
    public void crawlingEomisae() {
        crawlingWebSite("https://eomisae.co.kr/rt", "div._bd.cf.clear");
    }

    // 뽐뿌 크롤링 메소드
    private void crawlingPpomppu(String url) {
        System.setProperty("webdriver.chrome.driver", chromedriver);
        WebDriver driver = new ChromeDriver(getChromeOptions());
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
        try {
            driver.get(url);
            List<WebElement> rows = wait.until(ExpectedConditions.visibilityOfAllElementsLocatedBy(By.cssSelector("tr.baseList.bbs_new1")));
            int totalRows = rows.size();
            List<Crawling> crawlings = new ArrayList<>();
            for (int i = 0; i < totalRows - 4; i++) {
                // 외부 정보 수집
                WebElement row = rows.get(i);
                String title = row.findElement(By.cssSelector("a.baseList-title")).getText();
                String name = row.findElement(By.cssSelector("a.baseList-name")).getText();
                String imageURL = Optional.ofNullable(row.findElement(By.cssSelector("a.baseList-thumb img")).getAttribute("src"))
                        .map(src -> src.startsWith("//") ? "https:" + src : src)
                        .orElse("No Image");
                int views = Optional.ofNullable(row.findElement(By.cssSelector("td.baseList-space.baseList-views")).getText())
                        .filter(s -> !s.isEmpty())
                        .map(Integer::parseInt)
                        .orElse(0);
                int recommendCnt = Optional.ofNullable(row.findElement(By.cssSelector("td.baseList-space.baseList-rec")).getText())
                        .map(s -> s.split(" - ")[0].trim())
                        .filter(s -> !s.isEmpty())
                        .map(Integer::parseInt)
                        .orElse(0);
                int commentCnt = row.findElements(By.cssSelector("span.baseList-c"))
                        .stream()
                        .findFirst()
                        .map(e -> Integer.parseInt(e.getText().replaceAll("[()]", "")))
                        .orElse(0);
                String link = row.findElement(By.cssSelector("a.baseList-title")).getAttribute("href");
                // 내부 페이지로 이동
                String href = row.findElement(By.cssSelector("a.baseList-title")).getAttribute("href");
                driver.get(href);
                WebElement detailContent = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("div.sub-top-contents-box")));
                String createdDate = detailContent.getText().split("등록일:")[1].trim().split("\\s")[0];
                // Build
                Crawling crawling = Crawling.of(title, name, imageURL, views, recommendCnt, commentCnt, createdDate, link);
                crawlings.add(crawling);
                driver.navigate().back();
                rows = wait.until(ExpectedConditions.visibilityOfAllElementsLocatedBy(By.cssSelector("tr.baseList.bbs_new1")));
            }
            crawlingRepository.saveAll(crawlings);
        } catch (Exception e) {
            System.err.println("크롤링 중 오류 발생: " + e.getMessage());
        } finally {
            driver.quit();
        }
    }

    // 공통 크롤링 메소드
    private void crawlingWebSite(String url, String cssSelector) {
        System.setProperty("webdriver.chrome.driver", chromedriver);
        WebDriver driver = new ChromeDriver(getChromeOptions());
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
        try {
            driver.get(url);
            wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(cssSelector)));
            List<WebElement> elements = driver.findElements(By.cssSelector(cssSelector));
            String elementsText = elements.stream().map(WebElement::getText).collect(Collectors.joining("\n"));
            Crawling crawling = Crawling.builder().text(elementsText).build();
            crawlingRepository.save(crawling);
        } finally {
            driver.quit();
        }
    }

    // ChromeOptions 설정 메소드
    private ChromeOptions getChromeOptions() {
        return new ChromeOptions()
                .addArguments("--headless") // GUI 없는 환경에서 실행
                .addArguments("--disable-gpu") // GPU 가속 비활성화
                .addArguments("--no-sandbox") // 샌드박스 모드 비활성화, Docker에서 필수
                .addArguments("--disable-dev-shm-usage") // 컨테이너 환경에서 공유 메모리 사용량 최적화
                .addArguments("--disable-extensions") // 확장 프로그램 비활성화
                .addArguments("--disable-popup-blocking") // 팝업 차단 해제
                .addArguments("--start-maximized") // 브라우저 최대 크기로 시작
                .addArguments("user-agent=Mozilla/5.0..."); // 사용자 에이전트 설정
    }
}