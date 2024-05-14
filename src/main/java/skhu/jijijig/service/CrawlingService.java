package skhu.jijijig.service;

import io.github.bonigarcia.wdm.WebDriverManager;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.springframework.context.ApplicationContext;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.CompletableFuture;
import java.util.List;
import java.util.ArrayList;
import java.util.stream.Collectors;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.By;
import skhu.jijijig.domain.dto.CrawlingDTO;
import skhu.jijijig.domain.model.Crawling;
import skhu.jijijig.repository.CrawlingRepository;

@Service
@RequiredArgsConstructor
public class CrawlingService {
    private final CrawlingRepository crawlingRepository;
    private final ApplicationContext applicationContext;
    private final ExecutorService executor = Executors.newFixedThreadPool(10);

    @Scheduled(fixedRate = 600000) // 10분마다 실행
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
    public void performCrawlingForPpomppuDomestic() {
        CompletableFuture.supplyAsync(() -> crawlWebsite("https://www.ppomppu.co.kr/zboard/zboard.php?id=ppomppu", "뽐뿌(국내게시판)", "tbody > tr.baseList.bbs_new1"), executor);
    }

    @Transactional
    @Async
    public void performCrawlingForPpomppuOverseas() {
        CompletableFuture.supplyAsync(() -> crawlWebsite("https://www.ppomppu.co.kr/zboard/zboard.php?id=ppomppu4", "뽐뿌(해외게시판)", "tbody > tr.baseList.bbs_new1"), executor);
    }

    @Transactional
    @Async
    public void performCrawlingForQuasarzone() {
        CompletableFuture.supplyAsync(() -> crawlWebsite("https://quasarzone.com/bbs/qb_saleinfo", "퀘사이존", "tbody > tr"), executor);
    }

    @Transactional
    @Async
    public void performCrawlingForEomisae() {
        CompletableFuture.supplyAsync(() -> crawlWebsite("https://eomisae.co.kr/rt", "어미새", "div.card_el.n_ntc.clear"), executor);
    }

    @Transactional
    @Async
    public void performCrawlingForRuliweb() {
        CompletableFuture.supplyAsync(() -> crawlWebsite("https://bbs.ruliweb.com/news/board/1020", "루리웹", "tr.table_body.blocktarget"), executor);
    }

    @Transactional
    @Async
    public void performCrawlingForCoolenjoy() {
        CompletableFuture.supplyAsync(() -> crawlWebsite("https://coolenjoy.net/bbs/jirum", "쿨엔조이", "li.d-md-table-row.px-3.py-2.p-md-0.text-md-center.text-muted.border-bottom"), executor);
    }

    @Transactional(readOnly = true)
    public List<CrawlingDTO> getTop10CrawlingsByRanking() {
        List<Crawling> crawlings = crawlingRepository.findTop10ByOrderByRecommendCntDescUnrecommendCntAscCommentCntDesc();
        return crawlings.stream()
                .map(CrawlingDTO::fromEntity)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<CrawlingDTO> getAllCrawlingsSortedByDateTime(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Crawling> crawlings = crawlingRepository.findAllByOrderByDateTimeDesc(pageable);
        return crawlings.stream()
                .map(CrawlingDTO::fromEntity)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<CrawlingDTO> getCrawlingsSortedByLabelAndDateTime(String label, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Crawling> crawlings = crawlingRepository.findByLabelOrderByDateTimeDesc(label, pageable);
        return crawlings.stream()
                .map(CrawlingDTO::fromEntity)
                .collect(Collectors.toList());
    }

    private List<Crawling> crawlWebsite(String url, String label, String ROWS) {
        WebDriver driver = setupChromeDriver();
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(60));
        List<Crawling> crawlings = new ArrayList<>();
        try {
            driver.get(url);
            List<WebElement> rows = wait.until(ExpectedConditions.visibilityOfAllElementsLocatedBy(By.cssSelector(ROWS)));
            crawlings.addAll(handleCrawlingByLabel(label, rows));
            crawlingRepository.saveAll(crawlings);
        } catch (Exception e) {
            System.err.println("크롤링 도중 오류 발생: " + e.getMessage());
        } finally {
            driver.quit();
        }
        return crawlings;
    }

    private List<Crawling> extractPpomppu(List<WebElement> rows, String label) {
        List<Crawling> crawlings = new ArrayList<>();
        for (int i = 0; i < rows.size() - 4; i++) {
            WebElement row = rows.get(i);
            boolean open = row.findElements(By.cssSelector("img[src*='/zboard/skin/DQ_Revolution_BBS_New1/end_icon.PNG']")).isEmpty();
            try {
                String title = row.findElement(By.cssSelector("a.baseList-title")).getText();
                String name = Optional.of(row.findElement(By.cssSelector("a.baseList-name")).getText()).orElse("No name");
                String image = Optional.of(row.findElement(By.cssSelector("a.baseList-thumb img")).getAttribute("src"))
                        .map(src -> src.startsWith("//") ? "https:" + src : src).orElse("No image");
                String link = row.findElement(By.cssSelector("a.baseList-title")).getAttribute("href");
                String dateTime = parseDateTime(row.findElement(By.cssSelector("time.baseList-time")).getText(), label);
                int views = parseInteger(row.findElement(By.cssSelector("td.baseList-space.baseList-views")).getText());
                String[] voteCnts = row.findElements(By.cssSelector("td.baseList-space.baseList-rec")).stream()
                        .findFirst()
                        .map(td -> td.getText().split(" - "))
                        .orElse(new String[]{"0", "0"});
                int recommendCnt = parseInteger(voteCnts.length > 0 ? voteCnts[0] : "0");
                int unrecommendCnt = parseInteger(voteCnts.length > 1 ? voteCnts[1] : "0");
                int commentCnt = row.findElements(By.cssSelector("span.baseList-c")).stream()
                        .findFirst()
                        .map(element -> parseInteger(element.getText()))
                        .orElse(0);
                Crawling crawling = Crawling.of(label, title, name, image, link, dateTime, views, recommendCnt, unrecommendCnt, commentCnt, open);
                updateOrCreateCrawling(crawling, open);
            } catch (Exception e) {
                System.err.println("뽐뿌 데이터 추출 실패: " + e.getMessage());
            }
        }
        return crawlings;
    }


    private List<Crawling> extractQuasarzone(List<WebElement> rows, String label) {
        List<Crawling> crawlings = new ArrayList<>();
        for (WebElement row : rows) {
            boolean open = row.findElements(By.cssSelector("span.label.done")).isEmpty();
            try {
                String title = row.findElement(By.cssSelector("a.subject-link")).getText();
                String name = Optional.of(row.findElement(By.cssSelector("div.user-nick-text")).getText()).orElse("No name");
                String image = Optional.of(row.findElement(By.cssSelector("a.thumb img")).getAttribute("src"))
                        .map(src -> src.startsWith("//") ? "https:" + src : src).orElse("No image");
                String link = row.findElement(By.cssSelector("a.subject-link")).getAttribute("href");
                String dateTime = parseDateTime(row.findElement(By.cssSelector("span.date")).getText(), label);
                int views = Optional.ofNullable(row.findElement(By.cssSelector("span.count")).getText())
                        .map(s -> s.endsWith("k") ? (int)(Double.parseDouble(s.replace("k", "")) * 1000) : Integer.parseInt(s))
                        .orElse(0);
                int commentCnt = row.findElements(By.cssSelector("span.ctn-count")).stream()
                        .findFirst()
                        .map(element -> parseInteger(element.getText()))
                        .orElse(0);
                Crawling crawling = Crawling.of(label, title, name, image, link, dateTime, views, 0, 0, commentCnt, open);
                updateOrCreateCrawling(crawling, open);
            } catch (Exception e) {
                System.err.println("퀘사이존 데이터 추출 실패: " + e.getMessage());
            }
        }
        return crawlings;
    }

    private List<Crawling> extractEomisae(List<WebElement> rows, String label) {
        List<Crawling> crawlings = new ArrayList<>();
        for (WebElement row : rows) {
            boolean open = true;
            try {
                String title = row.findElement(By.cssSelector("h3 a.pjax")).getText();
                String name = Optional.of(row.findElement(By.cssSelector("div.info")).getText()).orElse("No name");
                String image = Optional.of(row.findElement(By.cssSelector("img.tmb")).getAttribute("src"))
                        .map(src -> src.startsWith("//") ? "https:" + src : src).orElse("No image");
                String link = row.findElement(By.cssSelector("a.pjax.hx")).getAttribute("href");
                String dateTime = parseDateTime(row.findElement(By.cssSelector("p > span:nth-child(2)")).getText(), label);
                int views = parseInteger(row.findElement(By.cssSelector("span.fr:nth-child(1)")).getText());
                int recommendCnt = row.findElements(By.cssSelector("span.fr:nth-child(3)")).stream()
                        .findFirst()
                        .map(td -> td.getText().split(" - ")[0])
                        .map(this::parseInteger)
                        .orElse(0);
                int commentCnt = row.findElements(By.cssSelector("span.fr:nth-child(2)")).stream()
                        .findFirst()
                        .map(element -> parseInteger(element.getText()))
                        .orElse(0);
                Crawling crawling = Crawling.of(label, title, name, image, link, dateTime, views, recommendCnt, 0, commentCnt, open);
                updateOrCreateCrawling(crawling, open);
            } catch (Exception e) {
                System.err.println("어미새 데이터 추출 실패: " + e.getMessage());
            }
        }
        return crawlings;
    }

    private List<Crawling> extractRuliweb(List<WebElement> rows, String label) {
        List<Crawling> crawlings = new ArrayList<>();
        for (int i = 4; i < rows.size(); i++) {
            WebElement row = rows.get(i);
            boolean open = true;
            try {
                String title = row.findElement(By.cssSelector("a.deco")).getText();
                String name = Optional.of(row.findElement(By.cssSelector("td.writer.text_over")).getText()).orElse("No name");
                String link = row.findElement(By.cssSelector("a.deco")).getAttribute("href");
                String dateTime = parseDateTime(row.findElement(By.cssSelector("td.time")).getText(), label);
                int views = parseInteger(row.findElement(By.cssSelector("td.hit")).getText());
                int recommendCnt = row.findElements(By.cssSelector("td.recomd")).stream()
                        .findFirst()
                        .map(td -> td.getText().split(" - ")[0])
                        .map(this::parseInteger)
                        .orElse(0);
                int commentCnt = row.findElements(By.cssSelector("a.num_reply span.num")).stream()
                        .findFirst()
                        .map(element -> parseInteger(element.getText()))
                        .orElse(0);
                Crawling crawling = Crawling.of(label, title, name, "https://img.ruliweb.com/img/2016/common/ruliweb_bi.png", link, dateTime, views, recommendCnt, 0, commentCnt, open);
                updateOrCreateCrawling(crawling, open);
            } catch (Exception e) {
                System.err.println("루리웹 데이터 추출 실패: " + e.getMessage());
            }
        }
        return crawlings;
    }

    private List<Crawling> extractCoolenjoy(List<WebElement> rows, String label) {
        List<Crawling> crawlings = new ArrayList<>();
        for (WebElement row : rows) {
            boolean open = row.findElements(By.cssSelector(".fa-lock")).isEmpty();
            try {
                String title = Optional.ofNullable(row.findElement(By.cssSelector("div.na-item")).getText())
                        .map(t -> t.split("\\n")[0])
                        .orElse("");
                String name = Optional.ofNullable(row.findElement(By.cssSelector("a.sv_member")).getText()).orElse("No name");
                String link = row.findElement(By.cssSelector("div.na-item a")).getAttribute("href");
                String dateTime = parseDateTime(row.findElement(By.cssSelector("div.float-left.float-md-none.d-md-table-cell.nw-6.nw-md-auto.f-sm.font-weight-normal.py-md-2.pr-md-1")).getText(), label);
                int views = parseInteger(row.findElement(By.cssSelector("div.float-left.float-md-none.d-md-table-cell.nw-4.nw-md-auto.f-sm.font-weight-normal.py-md-2.pr-md-1")).getText());
                int recommendCnt = row.findElements(By.cssSelector("span.rank-icon_vote")).stream()
                        .findFirst()
                        .map(td -> td.getText().split(" - ")[0])
                        .map(this::parseInteger)
                        .orElse(0);
                int commentCnt = row.findElements(By.cssSelector("span.count-plus")).stream()
                        .findFirst()
                        .map(element -> parseInteger(element.getText()))
                        .orElse(0);
                Crawling crawling = Crawling.of(label, title, name, "https://coolenjoy.net/theme/BS4-Basic/storage/image/logo-test.svg", link, dateTime, views, recommendCnt, 0, commentCnt, open);
                updateOrCreateCrawling(crawling, open);
            } catch (Exception e) {
                System.err.println("쿨엔조이 데이터 추출 실패: " + e.getMessage());
            }
        }
        return crawlings;
    }

    private List<Crawling> handleCrawlingByLabel(String label, List<WebElement> rows) {
        List<Crawling> crawlings = new ArrayList<>();
        if (label.startsWith("뽐뿌")) {
            crawlings.addAll(extractPpomppu(rows, label));
        } else if (label.startsWith("퀘사이존")) {
            crawlings.addAll(extractQuasarzone(rows, label));
        } else if (label.startsWith("어미새")) {
            crawlings.addAll(extractEomisae(rows, label));
        } else if (label.startsWith("루리웹")) {
            crawlings.addAll(extractRuliweb(rows, label));
        } else if (label.startsWith("쿨엔조이")) {
            crawlings.addAll(extractCoolenjoy(rows, label));
        }
        return crawlings;
    }

    private String parseDateTime(String dateTime, String label) {
        LocalDate today = LocalDate.now();
        LocalDateTime now = LocalDateTime.now();
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("uuuu-MM-dd");
        DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("uuuu-MM-dd HH:mm:ss");
        if (label.startsWith("뽐뿌")) {
            if (dateTime.contains(":")) { // 시간 포맷이 들어오면 (예: "20:18:16")
                return today.format(dateFormatter) + " " + dateTime;
            } else if (dateTime.contains("/")) { // 날짜 포맷이 들어오면 (예: "24/05/11")
                String[] parts = dateTime.split("/");
                LocalDate date = LocalDate.of(today.getYear(), Integer.parseInt(parts[1]), Integer.parseInt(parts[2]));
                return date.format(dateFormatter) + " 00:00:00";
            }
        } else if (label.startsWith("퀘사이존") || label.startsWith("루리웹")) {
            if (dateTime.contains(":")) { // 시간 포맷 (예: "11:53")
                return today.format(dateFormatter) + " " + dateTime + ":00"; // "2024-05-12 11:53:00"
            } else if (dateTime.contains("-")) { // 날짜 포맷 (예: "05-11")
                String[] parts = dateTime.split("-");
                LocalDate date = LocalDate.of(today.getYear(), Integer.parseInt(parts[0]), Integer.parseInt(parts[1]));
                return date.format(dateFormatter) + " 00:00:00"; // "2024-05-11 00:00:00"
            } else if (dateTime.contains(".")) {
                String[] parts = dateTime.split("\\.");
                LocalDate date = LocalDate.of(Integer.parseInt(parts[0]), Integer.parseInt(parts[1]), Integer.parseInt(parts[2]));
                return date.format(dateFormatter) + " 00:00:00";
            }
        } else if (label.startsWith("어미새")) {
            if (dateTime.contains(".")) {
                String[] parts = dateTime.split("\\.");
                LocalDate date = LocalDate.of(today.getYear(), Integer.parseInt(parts[1]), Integer.parseInt(parts[2]));
                if (date.isEqual(today)) { // 날짜가 오늘 날짜인 경우
                    return now.format(timeFormatter);
                } else { // 어제 날짜 또는 그 이전 날짜인 경우
                    return date.format(dateFormatter) + " 00:00:00";
                }
            }
        } else if (label.startsWith("쿨엔조이")) {
            dateTime = dateTime.replaceAll("등록일\\s+", ""); // "등록일"과 모든 공백(공백, 탭, 개행 포함) 제거
            if (dateTime.contains(":")) { // 시간 포맷 (예: "11:00")
                return today.format(dateFormatter) + " " + dateTime + ":00"; // "2024-05-12 11:00:00"
            } else if (dateTime.contains(".")) { // 날짜 포맷 (예: "05.11")
                String[] parts = dateTime.split("\\.");
                LocalDate date = LocalDate.of(today.getYear(), Integer.parseInt(parts[0]), Integer.parseInt(parts[1]));
                return date.format(dateFormatter) + " 00:00:00"; // "2024-05-11 00:00:00"
            }
        }
        return today.format(timeFormatter);
    }

    private int parseInteger(String text) {
        try {
            return Integer.parseInt(text.replaceAll("\\D+", ""));
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    private void updateOrCreateCrawling(Crawling crawling, boolean open) {
        Optional<Crawling> existing = crawlingRepository.findByLink(crawling.getLink());
        if (existing.isPresent()) {
            Crawling existingCrawling = existing.get();
            if (existingCrawling.isOpen() != open) {
                existingCrawling.updateOpen(open);
                crawlingRepository.save(existingCrawling);
            }
        } else {
            crawlingRepository.save(crawling);
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