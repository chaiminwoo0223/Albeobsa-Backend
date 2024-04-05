package skhu.jijijig.service;

import lombok.RequiredArgsConstructor;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import skhu.jijijig.domain.model.Crawling;
import skhu.jijijig.domain.repository.CrawlingRepository;

import java.time.Duration;

@RequiredArgsConstructor
@Service
public class CrawlingService {
    private final CrawlingRepository crawlingRepository;

    @Value("${CHROME_DRIVER}")
    private String chromedriver;

    private WebDriver createWebDriver() {
        System.setProperty("webdriver.chrome.driver", chromedriver);
        ChromeOptions options = new ChromeOptions().addArguments("--headless", "--disable-gpu", "user-agent=Mozilla/5.0...");
        return new ChromeDriver(options);
    }

    // 뽐뿌(국내게시판)
    public void crawlPpomppuDomestic() {
        WebDriver driver = createWebDriver();
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
        driver.get("https://www.ppomppu.co.kr/zboard/zboard.php?id=ppomppu");
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.tagName("body")));
        WebElement body = driver.findElement(By.tagName("body"));
        String bodyContent = body.getText();
        Crawling crawling = Crawling.builder().text(bodyContent).build(); // 크롤링된 내용을 데이터베이스에 저장
        crawlingRepository.save(crawling);
    }

    // 뽐뿌(해외게시판)
    public void crawlPpomppuOverseas() {
        WebDriver driver = createWebDriver();
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
        driver.get("https://www.ppomppu.co.kr/zboard/zboard.php?id=ppomppu4");
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.tagName("body")));
        WebElement body = driver.findElement(By.tagName("body"));
        String bodyContent = body.getText();
        Crawling crawling = Crawling.builder().text(bodyContent).build(); // 크롤링된 내용을 데이터베이스에 저장
        crawlingRepository.save(crawling);
    }
}