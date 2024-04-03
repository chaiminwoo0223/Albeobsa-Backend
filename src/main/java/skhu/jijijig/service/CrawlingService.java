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

    public String crawlNaverBodyContent() {
        WebDriver driver = createWebDriver();
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
        driver.get("https://www.naver.com");
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.tagName("body")));
        WebElement body = driver.findElement(By.tagName("body"));
        return body.getText();
    }
    
    public void saveNaverBodyContent() {
        String bodyContent = crawlNaverBodyContent();
        Crawling crawling = Crawling.builder().text(bodyContent).build();
        crawlingRepository.save(crawling);
    }
}