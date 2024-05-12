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
        applicationContext.getBean(CrawlingService.class).performCrawlingForQuasarzone();
        applicationContext.getBean(CrawlingService.class).performCrawlingForEomisae();
        applicationContext.getBean(CrawlingService.class).performCrawlingForRuliweb();
        applicationContext.getBean(CrawlingService.class).performCrawlingForCoolenjoy();
    }

    @Transactional
    @Async
    public CompletableFuture<List<Crawling>> performCrawlingForPpomppuDomestic() {
        return CompletableFuture.supplyAsync(() -> crawlWebsite("https://www.ppomppu.co.kr/zboard/zboard.php?id=ppomppu", "뽐뿌(국내게시판)", "tbody > tr.baseList.bbs_new1"), executor);
    }

    @Transactional
    @Async
    public CompletableFuture<List<Crawling>> performCrawlingForPpomppuOverseas() {
        return CompletableFuture.supplyAsync(() -> crawlWebsite("https://www.ppomppu.co.kr/zboard/zboard.php?id=ppomppu4", "뽐뿌(해외게시판)", "tbody > tr.baseList.bbs_new1"), executor);
    }

    @Transactional
    @Async
    public CompletableFuture<List<Crawling>> performCrawlingForQuasarzone() {
        return CompletableFuture.supplyAsync(() -> crawlWebsite("https://quasarzone.com/bbs/qb_saleinfo", "퀘사이존", "tbody > tr"), executor);
    }

    @Transactional
    @Async
    public CompletableFuture<List<Crawling>> performCrawlingForEomisae() {
        return CompletableFuture.supplyAsync(() -> crawlWebsite("https://eomisae.co.kr/rt", "어미새", "div.card_el.n_ntc.clear"), executor);
    }

    @Transactional
    @Async
    public CompletableFuture<List<Crawling>> performCrawlingForRuliweb() {
        return CompletableFuture.supplyAsync(() -> crawlWebsite("https://bbs.ruliweb.com/news/board/1020", "루리웹", "tr.table_body.blocktarget"), executor);
    }

    @Transactional
    @Async
    public CompletableFuture<List<Crawling>> performCrawlingForCoolenjoy() {
        return CompletableFuture.supplyAsync(() -> crawlWebsite("https://coolenjoy.net/bbs/jirum", "쿨엔조이", "li.d-md-table-row.px-3.py-2.p-md-0.text-md-center.text-muted.border-bottom"), executor);
    }

    private List<Crawling> crawlWebsite(String url, String label, String ROWS) {
        WebDriver driver = setupChromeDriver();
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(1));
        List<Crawling> crawlings = new ArrayList<>();
        try {
            driver.get(url);
            List<WebElement> rows = wait.until(ExpectedConditions.visibilityOfAllElementsLocatedBy(By.cssSelector(ROWS)));
            for (WebElement row : rows) {
                if (label.startsWith("뽐뿌")) {
                    Crawling crawling = extractPpomppu(row, label);
                    if (crawling != null) {
                        crawlings.add(crawling);
                    }
                } else if (label.startsWith("퀘사이존")) {
                    if (!row.findElements(By.cssSelector(".fa-lock")).isEmpty()) continue;
                    Crawling crawling = extractQuasarzone(row, label);
                    if (crawling != null) {
                        crawlings.add(crawling);
                    }
                } else if (label.startsWith("어미새")) {
                    Crawling crawling = extractEomisae(row, label);
                    if (crawling != null) {
                        crawlings.add(crawling);
                    }
                } else if (label.startsWith("루리웹")) {
                    Crawling crawling = extractRuliweb(row, label);
                    if (crawling != null) {
                        crawlings.add(crawling);
                    }
                } else if (label.startsWith("쿨엔조이")) {
                    if (!row.findElements(By.cssSelector(".fa-lock")).isEmpty()) continue;
                    Crawling crawling = extractCoolenjoy(row, label);
                    if (crawling != null) {
                        crawlings.add(crawling);
                    }
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

    private Crawling extractPpomppu(WebElement row, String label) {
        try {
            String title = row.findElement(By.cssSelector("a.baseList-title")).getText();
            String name = Optional.ofNullable(row.findElement(By.cssSelector("a.baseList-name")).getText()).orElse("No name");
            String image = Optional.ofNullable(row.findElement(By.cssSelector("a.baseList-thumb img")).getAttribute("src"))
                    .map(src -> src.startsWith("//") ? "https:" + src : src).orElse("No image");
            String link = row.findElement(By.cssSelector("a.baseList-title")).getAttribute("href");
            String createdDateTime = row.findElement(By.cssSelector("time.baseList-time")).getText();
            int views = parseInteger(row.findElement(By.cssSelector("td.baseList-space.baseList-views")).getText());
            int recommendCnt = parseInteger(row.findElement(By.cssSelector("td.baseList-space.baseList-rec")).getText().split(" - ")[0]);
            int unrecommendCnt = parseInteger(row.findElement(By.cssSelector("td.baseList-space.baseList-rec")).getText().split(" - ")[1]);
            int commentCnt = parseInteger(row.findElements(By.cssSelector("span.baseList-c")).stream().findFirst().orElseThrow().getText());
            return Crawling.of(label, title, name, image, link, createdDateTime, views, recommendCnt, unrecommendCnt, commentCnt);
        } catch (Exception e) {
            return null;
        }
    }

    private Crawling extractQuasarzone(WebElement row, String label) {
        try {
            String title = row.findElement(By.cssSelector("a.subject-link")).getText();
            String name = Optional.ofNullable(row.findElement(By.cssSelector("div.user-nick-text")).getText()).orElse("No name");
            String image = Optional.ofNullable(row.findElement(By.cssSelector("a.thumb img")).getAttribute("src"))
                    .map(src -> src.startsWith("//") ? "https:" + src : src).orElse("No image");
            String link = row.findElement(By.cssSelector("a.subject-link")).getAttribute("href");
            String createdDateTime = row.findElement(By.cssSelector("span.date")).getText();
            int views = Optional.ofNullable(row.findElement(By.cssSelector("span.count")).getText())
                    .map(s -> s.endsWith("k") ? (int)(Double.parseDouble(s.replace("k", "")) * 1000) : Integer.parseInt(s))
                    .orElse(0);
            int recommendCnt = 0;
            int unrecommendCnt = 0;
            int commentCnt = parseInteger(row.findElements(By.cssSelector("span.ctn-count")).stream().findFirst().orElseThrow().getText());
            return Crawling.of(label, title, name, image, link, createdDateTime, views, recommendCnt, unrecommendCnt, commentCnt);
        } catch (Exception e) {
            return null;
        }
    }

    private Crawling extractEomisae(WebElement row, String label) {
        try {
            String title = row.findElement(By.cssSelector("h3 a.pjax")).getText();
            String name = Optional.ofNullable(row.findElement(By.cssSelector("div.info")).getText()).orElse("No name");
            String image = Optional.ofNullable(row.findElement(By.cssSelector("img.tmb")).getAttribute("src"))
                    .map(src -> src.startsWith("//") ? "https:" + src : src).orElse("No image");
            String link = row.findElement(By.cssSelector("a.pjax.hx")).getAttribute("href");
            String createdDateTime = row.findElement(By.cssSelector("p > span:nth-child(2)")).getText();
            int views = parseInteger(row.findElement(By.cssSelector("span.fr:nth-child(1)")).getText());
            int recommendCnt = parseInteger(row.findElement(By.cssSelector("span.fr:nth-child(3)")).getText().split(" - ")[0]);
            int unrecommendCnt = 0;
            int commentCnt = parseInteger(row.findElements(By.cssSelector("span.fr:nth-child(2)")).stream().findFirst().orElseThrow().getText());
            return Crawling.of(label, title, name, image, link, createdDateTime, views, recommendCnt, unrecommendCnt, commentCnt);
        } catch (Exception e) {
            return null;
        }
    }

    // 내부 페이지로 이동 없음
    private Crawling extractRuliweb(WebElement row, String label) {
        try {
            String title = row.findElement(By.cssSelector("a.deco")).getText();
            String name = Optional.ofNullable(row.findElement(By.cssSelector("td.writer.text_over")).getText()).orElse("No name");
            String image = "No image";
            String link = row.findElement(By.cssSelector("a.deco")).getAttribute("href");
            String createdDateTime = row.findElement(By.cssSelector("td.time")).getText();
            int views = parseInteger(row.findElement(By.cssSelector("td.hit")).getText());
            int recommendCnt = parseInteger(row.findElement(By.cssSelector("td.recomd")).getText().split(" - ")[0]);
            int unrecommendCnt = 0;
            int commentCnt = parseInteger(row.findElements(By.cssSelector("a.num_reply span.num")).stream().findFirst().orElseThrow().getText());
            return Crawling.of(label, title, name, image, link, createdDateTime, views, recommendCnt, unrecommendCnt, commentCnt);
        } catch (Exception e) {
            return null;
        }
    }

    // 내부 페이지로 이동 없음
    private Crawling extractCoolenjoy(WebElement row, String label) {
        try {
            String title = row.findElement(By.cssSelector("div.na-item")).getText();
            String name = Optional.ofNullable(row.findElement(By.cssSelector("a.sv_member")).getText()).orElse("No name");
            String image = "No image";
            String link = row.findElement(By.cssSelector("div.na-item a")).getAttribute("href");
            String createdDateTime = row.findElement(By.cssSelector("div.float-left.float-md-none.d-md-table-cell.nw-6.nw-md-auto.f-sm.font-weight-normal.py-md-2.pr-md-1")).getText();
            int views = parseInteger(row.findElement(By.cssSelector("div.float-left.float-md-none.d-md-table-cell.nw-4.nw-md-auto.f-sm.font-weight-normal.py-md-2.pr-md-1")).getText());
            int recommendCnt = parseInteger(row.findElement(By.cssSelector("span.rank-icon_vote")).getText().split(" - ")[0]);
            int unrecommendCnt = 0;
            int commentCnt = parseInteger(row.findElements(By.cssSelector("span.count-plus")).stream().findFirst().orElseThrow().getText());
            return Crawling.of(label, title, name, image, link, createdDateTime, views, recommendCnt, unrecommendCnt, commentCnt);
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