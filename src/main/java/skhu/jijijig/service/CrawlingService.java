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

    // 뽐뿌(국내게시판)
    public void crawlingPpomppuDomestic() {
        crawlingWebSite("https://www.ppomppu.co.kr/zboard/zboard.php?id=ppomppu");
    }

    // 뽐뿌(해외게시판)
    public void crawinglPpomppuOverseas() {
        crawlingWebSite("https://www.ppomppu.co.kr/zboard/zboard.php?id=ppomppu4");
    }

    // 루리웹(예판 핫딜 뽐뿌 게시판)
    public void crawlingRuliweb() {
        crawlingWebSite("https://bbs.ruliweb.com/news/board/1020");
    }

    // 쿨엔조이(지름/알뜰정보 페이지)
    public void crawlingCoolenjoy() {
        crawlingWebSite("https://coolenjoy.net/bbs/jirum");
    }

    // 퀘사이존(핫딜게시판)
    public void crawlingQuasarzone() {
        crawlingWebSite("https://quasarzone.com/bbs/qb_saleinfo");
    }

    // 범용 크롤링 메소드
    private void crawlingWebSite(String url) {
        System.setProperty("webdriver.chrome.driver", chromedriver);
        WebDriver driver = new ChromeDriver(getChromeOptions());
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
        driver.get(url);
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.tagName("body")));
        WebElement body = driver.findElement(By.tagName("body"));
        String bodyContent = body.getText();
        Crawling crawling = Crawling.builder().text(bodyContent).build(); // 크롤링된 내용을 데이터베이스에 저장
        crawlingRepository.save(crawling);
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