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
import java.util.List;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Service
public class CrawlingService {
    private final CrawlingRepository crawlingRepository;

    @Value("${CHROME_DRIVER}")
    private String chromedriver;

    // 뽐뿌(국내게시판)
    @Transactional
    public void crawlingPpomppu() {
        System.setProperty("webdriver.chrome.driver", chromedriver);
        WebDriver driver = new ChromeDriver(getChromeOptions());
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
        try {
            driver.get("https://www.ppomppu.co.kr/zboard/zboard.php?id=ppomppu");
            wait.until(ExpectedConditions.visibilityOfAllElementsLocatedBy(By.cssSelector("tr.baseList.bbs_new1")));
            List<WebElement> rows = driver.findElements(By.cssSelector("tr.baseList.bbs_new1"));
            int totalRows = rows.size();
            List<Crawling> crawlings = rows.stream()
                    .limit(totalRows - 4) // 마지막 네 개 행을 제외
                    .map(row -> {
                        // 제목
                        String title = row.findElement(By.cssSelector("a.baseList-title")).getText();
                        // 이름
                        String name = row.findElements(By.cssSelector("td")).get(1).getText();
                        // 이미지 URL
                        String imageURL = row.findElement(By.cssSelector("a.baseList-thumb img")).getAttribute("src");
                        imageURL = imageURL.startsWith("//") ? "https:" + imageURL : imageURL;
                        // 조회수
                        String viewsText = row.findElement(By.cssSelector("td.baseList-space.baseList-views")).getText();
                        Integer views = viewsText.isEmpty() ? 0 : Integer.parseInt(viewsText);
                        // 추천수
                        String recText = row.findElement(By.cssSelector("td.baseList-space.baseList-rec")).getText();
                        Integer recommendCnt = recText.isEmpty() ? 0 : Integer.parseInt(recText.split(" - ")[0].trim());
                        // 댓글수
                        Integer commentCnt = row.findElements(By.cssSelector("span.baseList-c"))
                                .stream()
                                .findFirst()
                                .map(webElement -> Integer.parseInt(webElement.getText().replace("(", "").replace(")", "")))
                                .orElse(0);
                        return Crawling.builder()
                                .title(title)
                                .name(name)
                                .imageURL(imageURL)
                                .views(views)
                                .recommendCnt(recommendCnt)
                                .commentCnt(commentCnt)
                                .text(row.getText()) // 전체 텍스트
                                .build();
                    }).collect(Collectors.toList());
            crawlingRepository.saveAll(crawlings);
        } finally {
            driver.quit();
        }
    }

    // 뽐뿌(해외게시판)
    @Transactional
    public void crawlingPpomppu4() {
        System.setProperty("webdriver.chrome.driver", chromedriver);
        WebDriver driver = new ChromeDriver(getChromeOptions());
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10)); // 대기 시간을 길게 설정
        try {
            driver.get("https://www.ppomppu.co.kr/zboard/zboard.php?id=ppomppu4");
            List<WebElement> rows = wait.until(ExpectedConditions.visibilityOfAllElementsLocatedBy(By.cssSelector("tr.baseList.bbs_new1")));

            for (int i = 0; i < rows.size(); i++) {
                String title = ""; // title 초기화를 try 블록 밖에서 선언
                try {
                    // 페이지 뷰가 변경될 때마다 원소를 새로 찾음
                    rows = driver.findElements(By.cssSelector("tr.baseList.bbs_new1"));
                    WebElement row = rows.get(i);

                    title = row.findElement(By.cssSelector("a.baseList-title")).getText();
                    String href = row.findElement(By.cssSelector("a.baseList-title")).getAttribute("href");
                    System.out.println(href);

                    driver.get(href);
                    WebElement detailContent = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("div.sub-top-contents-box")));
                    String detailedText = detailContent.getText();

                    // 데이터베이스에 저장
                    Crawling crawling = Crawling.builder()
                            .title(title)
                            .text(detailedText)
                            .build();
                    crawlingRepository.save(crawling);

                    driver.navigate().back();
                    wait.until(ExpectedConditions.visibilityOfAllElementsLocatedBy(By.cssSelector("tr.baseList.bbs_new1")));
                } catch (StaleElementReferenceException e) {
                    System.err.println("Stale element reference: 페이지 요소가 변경되었습니다. 다시 시도합니다. (" + title + ")");
                    i--; // 재시도를 위해 인덱스 감소
                }
            }
        } finally {
            driver.quit();
        }
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