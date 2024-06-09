package skhu.jijijig.service;

import lombok.RequiredArgsConstructor;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.springframework.stereotype.Service;
import skhu.jijijig.repository.crawling.CrawlingRepository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
public class ParsingService {
    private final CrawlingRepository crawlingRepository;

    public boolean parseOpen(WebElement row, String OPEN) {
        return row.findElements(By.cssSelector(OPEN)).isEmpty();
    }

    public String[] parseTitle(WebElement row, String TITLE) {
        String fullTitle = row.findElement(By.cssSelector(TITLE)).getText();
        String subLabel = Optional.of(Pattern.compile("\\[(.*?)]").matcher(fullTitle))
                .filter(Matcher::find)
                .map(matcher -> matcher.group(1))
                .orElse(" ");
        String title = fullTitle.replaceAll("\\[.*?]", "").trim();
        return new String[] { subLabel, title }; // 대괄호 및 그 안의 내용을 제거한 제목을 저장
    }

    public String parseName(WebElement row, String NAME) {
        return Optional.of(row.findElement(By.cssSelector(NAME)).getText()).orElse("No name");
    }

    public String parseLink(WebElement row, String TITLE) {
        return row.findElement(By.cssSelector(TITLE)).getAttribute("href");
    }

    public String parseImage(WebElement row, String label, String IMAGE) {
        return switch (label) {
            case "루리웹" -> "https://img.ruliweb.com/img/2016/common/ruliweb_bi.png";
            case "쿨엔조이" -> "https://coolenjoy.net/theme/BS4-Basic/storage/image/logo-test.svg";
            default -> Optional.of(row.findElement(By.cssSelector(IMAGE)).getAttribute("src"))
                    .map(src -> src.startsWith("//") ? "https:" + src : src)
                    .orElse("No image");
        };
    }

    public String parseDateTime(WebElement row, String label, String DATETIME) {
        String dateTime = row.findElement(By.cssSelector(DATETIME)).getText();
        LocalDate today = LocalDate.now();
        LocalDateTime now = LocalDateTime.now();
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("uuuu-MM-dd");
        DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("uuuu-MM-dd HH:mm:ss");
        if (label.startsWith("뽐뿌")) {
            return parsePpomppuDateTime(label, dateTime, today, dateFormatter);
        } else if (label.startsWith("루리웹")) {
            return parseRuliwebDateTime(label, dateTime, today, dateFormatter);
        } else if (label.startsWith("어미새")) {
            return parseEomisaeDateTime(label, dateTime, today, now, dateFormatter, timeFormatter);
        } else if (label.startsWith("쿨엔조이")) {
            return parseCoolenjoyDateTime(label, dateTime, today, dateFormatter);
        } else if (label.startsWith("퀘사이존")) {
            return parseQuasarzoneDateTime(label, dateTime, today, dateFormatter);
        }
        return today.format(timeFormatter);
    }

    public int parseViews(WebElement row, String label, String VIEWS) {
        if (label.startsWith("퀘사이존")) {
            return Optional.ofNullable(row.findElement(By.cssSelector(VIEWS)).getText())
                    .map(v -> v.endsWith("k") ? (int)(Double.parseDouble(v.replace("k", "")) * 1000) : Integer.parseInt(v))
                    .orElse(0);
        }
        return parseInteger(row.findElement(By.cssSelector(VIEWS)).getText());
    }

    public int[] parseRecommendCnts(WebElement row, String RECOMMENDCNTS) {
        String cnts = row.findElements(By.cssSelector(RECOMMENDCNTS)).stream()
                .findFirst()
                .map(td -> td.getText().replaceAll("\\D+", "-"))  // 숫자가 아닌 모든 문자를 '-'로 대체
                .orElse("0-0");  // 값이 없다면 "0,0"을 반환
        String[] parts = cnts.split("-");
        int recommendCnt = parts.length > 0 ? parseInteger(parts[0]) : 0;  // 첫 번째 숫자를 추천수로 파싱, 없으면 0
        int unrecommendCnt = parts.length > 1 ? parseInteger(parts[1]) : 0;  // 두 번째 숫자를 비추천수로 파싱, 없으면 0
        return new int[] {recommendCnt, unrecommendCnt};
    }

    public int parseCommentCnt(WebElement row, String COMMENTCNT) {
        return row.findElements(By.cssSelector(COMMENTCNT)).stream()
                .findFirst()
                .map(element -> parseInteger(element.getText()))
                .orElse(0);
    }

    public String[] getPpomppuSelectors() {
        String OPEN = "img[src*='/zboard/skin/DQ_Revolution_BBS_New1/end_icon.PNG']";
        String TITLE = "a.baseList-title";
        String NAME = "a.baseList-name";
        String IMAGE = "a.baseList-thumb img";
        String DATETIME = "time.baseList-time";
        String VIEWS = "td.baseList-space.baseList-views";
        String RECOMMENDCNTS = "td.baseList-space.baseList-rec";
        String COMMENTCNT = "span.baseList-c";
        return new String[] {OPEN, TITLE, NAME, IMAGE, DATETIME, VIEWS, RECOMMENDCNTS, COMMENTCNT};
    }

    public String[] getEomisaeSelectors() {
        String OPEN = "No selector";
        String TITLE = "h3 a.pjax";
        String NAME = "div.info";
        String IMAGE = "img.tmb";
        String DATETIME = "p > span:nth-child(2)";
        String VIEWS = "span.fr:nth-child(1)";
        String RECOMMENDCNTS = "span.fr:nth-child(3)";
        String COMMENTCNT = "span.fr:nth-child(1)";
        return new String[] {OPEN, TITLE, NAME, IMAGE, DATETIME, VIEWS, RECOMMENDCNTS, COMMENTCNT};
    }

    public String[] getRuliwebSelectors() {
        String OPEN = "No selector";
        String TITLE = "a.deco";
        String NAME = "td.writer.text_over";
        String IMAGE = "No selector";
        String DATETIME = "td.time";
        String VIEWS = "td.hit";
        String RECOMMENDCNTS = "td.recomd";
        String COMMENTCNT = "a.num_reply span.num";
        return new String[] {OPEN, TITLE, NAME, IMAGE, DATETIME, VIEWS, RECOMMENDCNTS, COMMENTCNT};
    }

    public String[] getCoolenjoySelectors() {
        String OPEN = ".fa-lock";
        String TITLE = "a.na-subject";
        String NAME = "a.sv_member";
        String IMAGE = "No selector";
        String DATETIME = "div.float-left.float-md-none.d-md-table-cell.nw-6.nw-md-auto.f-sm.font-weight-normal.py-md-2.pr-md-1";
        String VIEWS = "div.float-left.float-md-none.d-md-table-cell.nw-4.nw-md-auto.f-sm.font-weight-normal.py-md-2.pr-md-1";
        String RECOMMENDCNTS = "span.rank-icon_vote";
        String COMMENTCNT = "span.count-plus";
        return new String[] {OPEN, TITLE, NAME, IMAGE, DATETIME, VIEWS, RECOMMENDCNTS, COMMENTCNT};
    }

    public String[] getQuasarzoneSelectors() {
        String OPEN = "span.label.done";
        String TITLE = "a.subject-link";
        String NAME = "span.user-nick-wrap.nick.d-inline-block";
        String IMAGE = "a.thumb img";
        String DATETIME = "span.date";
        String VIEWS = "span.count";
        String RECOMMENDCNTS = "No selector";
        String COMMENTCNT = "span.ctn-count";
        return new String[] {OPEN, TITLE, NAME, IMAGE, DATETIME, VIEWS, RECOMMENDCNTS, COMMENTCNT};
    }

    private int parseInteger(String text) {
        try {
            return Integer.parseInt(text.replaceAll("\\D+", ""));
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    private String parsePpomppuDateTime(String label, String dateTime, LocalDate today, DateTimeFormatter dateFormatter) {
        if (dateTime.contains(":")) {
            return today.format(dateFormatter) + " " + dateTime;
        } else if (dateTime.contains("/")) {
            String[] parts = dateTime.split("/");
            LocalDate date = LocalDate.of(2000 + Integer.parseInt(parts[0]), Integer.parseInt(parts[1]), Integer.parseInt(parts[2]));
            return crawlingRepository.findDateTimeByLabel(label).orElse(date.format(dateFormatter) + " 00:00:00");
        }
        return today.format(dateFormatter) + " 00:00:00";
    }

    private String parseRuliwebDateTime(String label, String dateTime, LocalDate today, DateTimeFormatter dateFormatter) {
        if (dateTime.contains(":")) {
            return today.format(dateFormatter) + " " + dateTime + ":00";
        } else if (dateTime.contains(".")) {
            String[] parts = dateTime.split("\\.");
            LocalDate date = LocalDate.of(Integer.parseInt(parts[0]), Integer.parseInt(parts[1]), Integer.parseInt(parts[2]));
            return crawlingRepository.findDateTimeByLabel(label).orElse(date.format(dateFormatter) + " 00:00:00");
        }
        return today.format(dateFormatter) + " 00:00:00";
    }

    private String parseEomisaeDateTime(String label, String dateTime, LocalDate today, LocalDateTime now, DateTimeFormatter dateFormatter, DateTimeFormatter timeFormatter) {
        if (dateTime.contains(".")) {
            String[] parts = dateTime.split("\\.");
            LocalDate date = LocalDate.of(2000 + Integer.parseInt(parts[0]), Integer.parseInt(parts[1]), Integer.parseInt(parts[2]));
            if (date.isEqual(today)) {
                return now.format(timeFormatter);
            } else {
                return crawlingRepository.findDateTimeByLabel(label).orElse(date.format(dateFormatter) + " 00:00:00");
            }
        }
        return today.format(dateFormatter) + " 00:00:00";
    }

    private String parseCoolenjoyDateTime(String label, String dateTime, LocalDate today, DateTimeFormatter dateFormatter) {
        dateTime = dateTime.replaceAll("등록일\\s+", "");
        if (dateTime.contains(":")) {
            return today.format(dateFormatter) + " " + dateTime + ":00";
        }
        if (dateTime.contains(".")) {
            String[] parts = dateTime.split("\\.");
            int year = today.getYear();
            int month = Integer.parseInt(parts[0]);
            int day = Integer.parseInt(parts[1]);
            if (month > 12) {
                year = 2000 + month;
                month = Integer.parseInt(parts[1]);
                day = Integer.parseInt(parts[2]);
            }
            LocalDate date = LocalDate.of(year, month, day);
            return crawlingRepository.findDateTimeByLabel(label).orElse(date.format(dateFormatter) + " 00:00:00");
        }
        return today.format(dateFormatter) + " 00:00:00";
    }

    private String parseQuasarzoneDateTime(String label, String dateTime, LocalDate today, DateTimeFormatter dateFormatter) {
        if (dateTime.contains(":")) {
            return today.format(dateFormatter) + " " + dateTime + ":00";
        }
        if (dateTime.contains("-")) {
            String[] parts = dateTime.split("-");
            int year = today.getYear();
            int month = Integer.parseInt(parts[0]);
            int day = Integer.parseInt(parts[1]);
            if (month == 12 && today.getMonthValue() == 1) {
                year -= 1;
            }
            LocalDate date = LocalDate.of(year, month, day);
            return crawlingRepository.findDateTimeByLabel(label).orElse(date.format(dateFormatter) + " 00:00:00");
        }
        return today.format(dateFormatter) + " 00:00:00";
    }
}