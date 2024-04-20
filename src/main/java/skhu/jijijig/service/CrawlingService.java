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

import java.text.SimpleDateFormat;
import java.time.Duration;
import java.util.*;

@RequiredArgsConstructor
@Service
public class CrawlingService {
    private final CrawlingRepository crawlingRepository;

    @Value("${CHROME_DRIVER}")
    private String chromedriver;

    // 뽐뿌(국내게시판)
    @Transactional
    public List<Crawling> crawlingPpomppuDomestic() {
        System.setProperty("webdriver.chrome.driver", chromedriver);
        WebDriver driver = new ChromeDriver(getChromeOptions());
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(1));
        List<Crawling> crawlings = new ArrayList<>();
        try {
            driver.get("https://www.ppomppu.co.kr/zboard/zboard.php?id=ppomppu");
            List<WebElement> rows = wait.until(ExpectedConditions.visibilityOfAllElementsLocatedBy(By.cssSelector("tr.baseList.bbs_new1")));
            for (int i = 0; i < rows.size() - 4; i++) {
                // 외부 정보 수집
                WebElement row = rows.get(i);
                String title = row.findElement(By.cssSelector("a.baseList-title")).getText();
                String name = Optional.ofNullable(row.findElement(By.cssSelector("a.baseList-name")).getText())
                        .filter(s -> !s.isEmpty())
                        .orElse(null);
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
                        .map(s -> Integer.parseInt(s.getText().replaceAll("[()]", "")))
                        .orElse(0);
                String link = row.findElement(By.cssSelector("a.baseList-title")).getAttribute("href");
                // 내부 페이지로 이동
                driver.get(link);
                WebElement detailContent = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("div.sub-top-contents-box")));
                String createdDate = detailContent.getText().split("등록일:")[1].trim().split("\\s")[0];
                // Build
                Crawling crawling = Crawling.of("뽐뿌(국내게시판)", title, name, imageURL, views, recommendCnt, commentCnt, createdDate, link);
                crawlings.add(crawling);
                driver.navigate().back();
                rows = wait.until(ExpectedConditions.visibilityOfAllElementsLocatedBy(By.cssSelector("tr.baseList.bbs_new1")));
            }
            crawlingRepository.saveAll(crawlings);
            return crawlings;
        } catch (Exception e) {
            System.err.println("크롤링 중 오류 발생: " + e.getMessage());
        } finally {
            driver.quit();
        }
        return crawlings;
    }

    // 뽐뿌(해외게시판)
    @Transactional
    public List<Crawling> crawlingPpomppuOverseas() {
        System.setProperty("webdriver.chrome.driver", chromedriver);
        WebDriver driver = new ChromeDriver(getChromeOptions());
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(1));
        List<Crawling> crawlings = new ArrayList<>();
        try {
            driver.get("https://www.ppomppu.co.kr/zboard/zboard.php?id=ppomppu4");
            List<WebElement> rows = wait.until(ExpectedConditions.visibilityOfAllElementsLocatedBy(By.cssSelector("tr.baseList.bbs_new1")));
            for (int i = 0; i < rows.size() - 3; i++) {
                // 외부 정보 수집
                WebElement row = rows.get(i);
                String title = row.findElement(By.cssSelector("a.baseList-title")).getText();
                String name = Optional.ofNullable(row.findElement(By.cssSelector("a.baseList-name")).getText())
                        .filter(s -> !s.isEmpty())
                        .orElse(null);
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
                        .map(s -> Integer.parseInt(s.getText().replaceAll("[()]", "")))
                        .orElse(0);
                String link = row.findElement(By.cssSelector("a.baseList-title")).getAttribute("href");
                // 내부 페이지로 이동
                driver.get(link);
                WebElement detailContent = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("div.sub-top-contents-box")));
                String createdDate = detailContent.getText().split("등록일:")[1].trim().split("\\s")[0];
                // Build
                Crawling crawling = Crawling.of("뽐뿌(해외게시판)", title, name, imageURL, views, recommendCnt, commentCnt, createdDate, link);
                crawlings.add(crawling);
                driver.navigate().back();
                rows = wait.until(ExpectedConditions.visibilityOfAllElementsLocatedBy(By.cssSelector("tr.baseList.bbs_new1")));
            }
            crawlingRepository.saveAll(crawlings);
            return crawlings;
        } catch (Exception e) {
            System.err.println("크롤링 중 오류 발생: " + e.getMessage());
        } finally {
            driver.quit();
        }
        return crawlings;
    }

    // 루리웹(예판 핫딜 뽐뿌 게시판)
    @Transactional
    public List<Crawling> crawlingRuliweb() {
        System.setProperty("webdriver.chrome.driver", chromedriver);
        WebDriver driver = new ChromeDriver(getChromeOptions());
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(1));
        List<Crawling> crawlings = new ArrayList<>();
        try {
            driver.get("https://bbs.ruliweb.com/news/board/1020");
            List<WebElement> rows = wait.until(ExpectedConditions.visibilityOfAllElementsLocatedBy(By.cssSelector("tr.table_body.blocktarget")));
            for (int i = 4; i < rows.size(); i++) {
                // 외부 정보 수집
                WebElement row = rows.get(i);
                String title = row.findElement(By.cssSelector("a.deco")).getText();
                String name = row.findElement(By.cssSelector("td.writer.text_over")).getText();
                int views = Optional.ofNullable(row.findElement(By.cssSelector("td.hit")).getText())
                        .filter(s -> !s.isEmpty())
                        .map(Integer::parseInt)
                        .orElse(0);
                int recommendCnt = Optional.ofNullable(row.findElement(By.cssSelector("td.recomd")).getText())
                        .filter(s -> !s.isEmpty())
                        .map(Integer::parseInt)
                        .orElse(0);
                int commentCnt = row.findElements(By.cssSelector("a.num_reply span.num"))
                        .stream()
                        .findFirst()
                        .map(s -> Integer.parseInt(s.getText()))
                        .orElse(0);
                String link = row.findElement(By.cssSelector("a.deco")).getAttribute("href");
                // 내부 페이지로 이동
                driver.get(link);
                wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("div.view_content.autolink")));
                String imageURL = driver.findElements(By.cssSelector("a.img_load img")).stream()
                        .findFirst()
                        .map(e -> e.getAttribute("src").startsWith("//") ? "https:" + e.getAttribute("src") : e.getAttribute("src"))
                        .orElse("No Image");
                String createdDate = driver.findElement(By.cssSelector("span.regdate")).getText().split(" ")[0].replace('.', '-');
                // build
                Crawling crawling = Crawling.of("루리웹", title, name, imageURL, views, recommendCnt, commentCnt, createdDate, link);
                crawlings.add(crawling);
                driver.navigate().back();
                rows = wait.until(ExpectedConditions.visibilityOfAllElementsLocatedBy(By.cssSelector("tr.table_body.blocktarget")));
            }
            crawlingRepository.saveAll(crawlings);
            return crawlings;
        } catch (Exception e) {
            System.err.println("크롤링 중 오류 발생: " + e.getMessage());
        } finally {
            driver.quit();
        }
        return crawlings;
    }

    // 쿨엔조이(지름/알뜰정보 페이지)
    @Transactional
    public List<Crawling> crawlingCoolenjoy() {
        System.setProperty("webdriver.chrome.driver", chromedriver);
        WebDriver driver = new ChromeDriver(getChromeOptions());
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(1));
        List<Crawling> crawlings = new ArrayList<>();
        try {
            driver.get("https://coolenjoy.net/bbs/jirum");
            List<WebElement> rows = wait.until(ExpectedConditions.visibilityOfAllElementsLocatedBy(By.cssSelector("li.d-md-table-row.px-3.py-2.p-md-0.text-md-center.text-muted.border-bottom")));
            for (int i = 0; i < rows.size(); i++) {
                // 외부 정보 수집
                WebElement row = rows.get(i);
                if (!row.findElements(By.cssSelector(".fa-lock")).isEmpty()) continue; // 잠긴 게시물 건너뛰기
                String title = Optional.ofNullable(row.findElement(By.cssSelector("div.na-item")).getText())
                        .map(t -> t.split("\\n")[0])
                        .orElse("");
                String name = row.findElement(By.cssSelector("a.sv_member")).getText();
                int views = Optional.ofNullable(row.findElement(By.cssSelector("div.float-left.float-md-none.d-md-table-cell.nw-4.nw-md-auto.f-sm.font-weight-normal.py-md-2.pr-md-1")).getText())
                        .map(s -> s.replaceAll("[^0-9]", ""))
                        .filter(s -> !s.isEmpty())
                        .map(Integer::parseInt)
                        .orElse(0);
                int recommendCnt = Optional.ofNullable(row.findElement(By.cssSelector("span.rank-icon_vote")).getText())
                        .filter(s -> !s.isEmpty())
                        .map(Integer::parseInt)
                        .orElse(0);
                int commentCnt = row.findElements(By.cssSelector("span.count-plus"))
                        .stream()
                        .findFirst()
                        .map(s -> Integer.parseInt(s.getText().replaceAll("[\\[\\]]", ""))) // 괄호 제거 후 파싱
                        .orElse(0);
                String link = row.findElement(By.cssSelector("div.na-item a")).getAttribute("href");
                // 내부 페이지로 이동
                driver.get(link);
                wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("article.mb-4")));
                String imageURL = driver.findElements(By.cssSelector("img.fr-fic")).stream()
                        .findFirst()
                        .or(() -> driver.findElements(By.cssSelector("a.view_image img")).stream().findFirst())
                        .map(s -> s.getAttribute("src").startsWith("//") ? "https:" + s.getAttribute("src") : s.getAttribute("src"))
                        .orElse("No Image");
                String createdDate = driver.findElement(By.cssSelector("time.f-xs")).getText().split(" ")[0].replace('.', '-');
                // build
                Crawling crawling = Crawling.of("쿨엔조이", title, name, imageURL, views, recommendCnt, commentCnt, createdDate, link);
                crawlings.add(crawling);
                driver.navigate().back();
                rows = wait.until(ExpectedConditions.visibilityOfAllElementsLocatedBy(By.cssSelector("li.d-md-table-row.px-3.py-2.p-md-0.text-md-center.text-muted.border-bottom")));
            }
            crawlingRepository.saveAll(crawlings);
            return crawlings;
        } catch (Exception e) {
            System.err.println("크롤링 중 오류 발생: " + e.getMessage());
        } finally {
            driver.quit();
        }
        return crawlings;
    }

    // 퀘사이존(핫딜게시판)
    @Transactional
    public List<Crawling> crawlingQuasarzone() {
        System.setProperty("webdriver.chrome.driver", chromedriver);
        WebDriver driver = new ChromeDriver(getChromeOptions());
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(1));
        List<Crawling> crawlings = new ArrayList<>();
        Calendar todayCalendar = Calendar.getInstance();
        String todayDate = new SimpleDateFormat("yyyy-MM-dd").format(todayCalendar.getTime());
        try {
            driver.get("https://quasarzone.com/bbs/qb_saleinfo");
            List<WebElement> rows = wait.until(ExpectedConditions.visibilityOfAllElementsLocatedBy(By.cssSelector("tbody > tr")));
            for (WebElement row : rows) {
                // 외부 정보 수집
                if (!row.findElements(By.cssSelector(".fa-lock")).isEmpty()) continue;
                String title = row.findElement(By.cssSelector("a.subject-link")).getText().split(" ")[0];
                String name = row.findElement(By.cssSelector("div.user-nick-text")).getText();
                String imageURL = Optional.ofNullable(row.findElement(By.cssSelector("a.thumb img")).getAttribute("src")).orElse("No Image");
                int views = Optional.ofNullable(row.findElement(By.cssSelector("span.count")).getText())
                        .map(s -> s.endsWith("k") ? (int)(Double.parseDouble(s.replace("k", "")) * 1000) : Integer.parseInt(s))
                        .orElse(0);
                int commentCnt = row.findElements(By.cssSelector("span.ctn-count")).stream().findFirst().map(e -> Integer.parseInt(e.getText())).orElse(0);
                String link = row.findElement(By.cssSelector("a.subject-link")).getAttribute("href");
                String createdDate = row.findElement(By.cssSelector("span.date")).getText().split(" ")[0].replace('.', '-');
                if (createdDate.contains(":")) {
                    createdDate = todayDate;
                } else {
                    int currentMonth = todayCalendar.get(Calendar.MONTH) + 1;
                    int createdMonth = Integer.parseInt(createdDate.split("-")[0]);
                    createdDate = (todayCalendar.get(Calendar.YEAR) + (createdMonth > currentMonth ? -1 : 0)) + "-" + createdDate;
                }
                // Build
                Crawling crawling = Crawling.of("퀘사이존", title, name, imageURL, views, -1, commentCnt, createdDate, link);
                crawlings.add(crawling);
            }
            crawlingRepository.saveAll(crawlings);
            return crawlings;
        } catch (Exception e) {
            System.err.println("크롤링 중 오류 발생: " + e.getMessage());
        } finally {
            driver.quit();
        }
        return crawlings;
    }

    // 어미새(기타정보)
    @Transactional
    public List<Crawling> crawlingEomisae() {
        System.setProperty("webdriver.chrome.driver", chromedriver);
        WebDriver driver = new ChromeDriver(getChromeOptions());
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(1));
        List<Crawling> crawlings = new ArrayList<>();
        try {
            driver.get("https://eomisae.co.kr/rt");
            List<WebElement> rows = wait.until(ExpectedConditions.visibilityOfAllElementsLocatedBy(By.cssSelector("div.card_el.n_ntc.clear")));
            for (WebElement row : rows) {
                // 외부 정보 수집
                String title = row.findElement(By.cssSelector("h3 a.pjax")).getText();
                String name = row.findElement(By.cssSelector("div.info")).getText();
                String imageURL = Optional.ofNullable(row.findElement(By.cssSelector("img.tmb")).getAttribute("src"))
                        .map(src -> src.startsWith("//") ? "https:" + src : src)
                        .orElse("No Image");
                int views = Optional.ofNullable(row.findElement(By.cssSelector("span.fr:nth-child(1)")).getText())
                        .filter(s -> !s.isEmpty())
                        .map(Integer::parseInt)
                        .orElse(0);
                int recommendCnt = Optional.ofNullable(row.findElement(By.cssSelector("span.fr:nth-child(3)")).getText())
                        .filter(s -> !s.isEmpty())
                        .map(Integer::parseInt)
                        .orElse(0);
                int commentCnt = Optional.ofNullable(row.findElement(By.cssSelector("span.fr:nth-child(2)")).getText())
                        .filter(s -> !s.isEmpty())
                        .map(Integer::parseInt)
                        .orElse(0);
                String link = row.findElement(By.cssSelector("a.pjax.hx")).getAttribute("href");
                String createdDate = "20" + driver.findElement(By.cssSelector("p > span:nth-child(2)")).getText().replace('.', '-');
                // Build
                Crawling crawling = Crawling.of("어미새", title, name, imageURL, views, recommendCnt, commentCnt, createdDate, link);
                crawlings.add(crawling);
            }
            crawlingRepository.saveAll(crawlings);
            return crawlings;
        } catch (Exception e) {
            System.err.println("크롤링 중 오류 발생: " + e.getMessage());
        } finally {
            driver.quit();
        }
        return crawlings;
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