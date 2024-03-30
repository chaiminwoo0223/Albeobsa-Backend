package skhu.jijijig.service;

import lombok.RequiredArgsConstructor;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import skhu.jijijig.domain.dto.CrawlingDTO;
import skhu.jijijig.domain.model.Crawling;
import skhu.jijijig.domain.repository.CrawlingRepository;

import java.time.Duration;
import java.util.List;

@RequiredArgsConstructor
@Service
public class CrawlingService {
    private final CrawlingRepository crawlingRepository;

    @Scheduled(fixedRate = 3600000)
    public void crawlSites() {
        System.setProperty("webdriver.chrome.driver", "./driver/chromedriver");
        ChromeOptions options = new ChromeOptions();
        options.addArguments("--headless");
        options.addArguments("--disable-gpu");
        options.addArguments("user-agent=Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/58.0.3029.110 Safari/537.3");
        WebDriver driver = new ChromeDriver(options);
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10)); // 최대 10초까지 대기
        try {
            crawlPpomppuDomestic(driver, wait);
            crawlPpomppuOverseas(driver, wait);
        } finally {
            driver.quit();
        }
    }

    // 뽐뿌(국내게시판)
    private void crawlPpomppuDomestic(WebDriver driver, WebDriverWait wait) {
        driver.get("https://www.ppomppu.co.kr/zboard/zboard.php?id=ppomppu");
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.tagName("body")));
        String[] classNames = {"common-list0", "common-list1", "list0", "list1"};
        for (String className : classNames) {
            List<WebElement> posts = driver.findElements(By.className(className));
            for (WebElement post : posts) {
                String name = post.findElement(By.cssSelector(".list_name")).getText();
                String title = post.findElement(By.cssSelector(".list_title")).getText();
                String commentCnt = post.findElement(By.cssSelector(".list_comment2")).getText();
                String createdDate = post.findElement(By.cssSelector(".eng.list_vspace")).getAttribute("title");
                List<WebElement> views = driver.findElements(By.cssSelector(".eng.list_vspace"));
                WebElement lastViewElement = views.get(views.size() - 1);
                String view = lastViewElement.getText();
                String image = post.findElement(By.cssSelector(".thumb_border")).getAttribute("src");
                String soldOut = "진행중";
                List<WebElement> endIcons = driver.findElements(By.cssSelector("img[src='https://www.ppomppu.co.kr/zboard/skin/DQ_Revolution_BBS_New1/end_icon.PNG']"));
                if (!endIcons.isEmpty()) {
                    soldOut = "종료";
                }
                try {
                    WebElement linkElement = post.findElement(By.tagName("a"));
                    String link = linkElement.getAttribute("href");
                    driver.get(link); // 상세 페이지로 이동
                    wait.until(ExpectedConditions.visibilityOfElementLocated(By.tagName("body")));
                    String category = driver.findElement(By.cssSelector(".view_cate")).getText();
                    String likeCnt = driver.findElement(By.cssSelector(".top_vote_item")).getText();
                    CrawlingDTO crawlingDTO = CrawlingDTO.of(title, category, name, createdDate, link, image, view, commentCnt, likeCnt, soldOut);
                    saveCrawlingData(crawlingDTO);
                    driver.navigate().back();
                    wait.until(ExpectedConditions.visibilityOfElementLocated(By.tagName("body")));
                } catch (Exception e) {
                    System.out.println("상세 페이지에서 정보 추출 중 오류 발생: " + e.getMessage());
                    driver.navigate().back();
                }
            }
        }
    }

    // 뽐뿌(해외게시판)
    private void crawlPpomppuOverseas(WebDriver driver, WebDriverWait wait) {
        driver.get("https://www.ppomppu.co.kr/zboard/zboard.php?id=ppomppu4");
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.tagName("body")));
        String[] classNames = {"common-list0", "common-list1", "list0", "list1"};
        for (String className : classNames) {
            List<WebElement> posts = driver.findElements(By.className(className));
            for (WebElement post : posts) {
                String name = post.findElement(By.cssSelector(".list_name")).getText();
                String title = post.findElement(By.cssSelector(".list_title")).getText();
                String commentCnt = post.findElement(By.cssSelector(".list_comment2")).getText();
                String createdDate = post.findElement(By.cssSelector(".eng.list_vspace")).getAttribute("title");
                List<WebElement> views = driver.findElements(By.cssSelector(".eng.list_vspace"));
                WebElement lastViewElement = views.get(views.size() - 1);
                String view = lastViewElement.getText();
                String image = post.findElement(By.cssSelector(".thumb_border")).getAttribute("src");
                String soldOut = "진행중";
                List<WebElement> endIcons = driver.findElements(By.cssSelector("img[src='https://www.ppomppu.co.kr/zboard/skin/DQ_Revolution_BBS_New1/end_icon.PNG']"));
                if (!endIcons.isEmpty()) {
                    soldOut = "종료";
                }
                try {
                    WebElement linkElement = post.findElement(By.tagName("a"));
                    String link = linkElement.getAttribute("href");
                    driver.get(link); // 상세 페이지로 이동
                    wait.until(ExpectedConditions.visibilityOfElementLocated(By.tagName("body")));
                    String category = driver.findElement(By.cssSelector(".view_cate")).getText();
                    String likeCnt = driver.findElement(By.cssSelector(".top_vote_item")).getText();
                    CrawlingDTO crawlingDTO = CrawlingDTO.of(title, category, name, createdDate, link, image, view, commentCnt, likeCnt, soldOut);
                    saveCrawlingData(crawlingDTO);
                    driver.navigate().back();
                    wait.until(ExpectedConditions.visibilityOfElementLocated(By.tagName("body")));
                } catch (Exception e) {
                    System.out.println("상세 페이지에서 정보 추출 중 오류 발생: " + e.getMessage());
                    driver.navigate().back();
                }
            }
        }
    }

    // 크롤링 데이터 저장
    private void saveCrawlingData(CrawlingDTO crawlingDTO) {
        Crawling crawling = Crawling.fromDTO(crawlingDTO);
        crawlingRepository.save(crawling);
    }
}