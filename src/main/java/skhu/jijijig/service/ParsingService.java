package skhu.jijijig.service;

import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Optional;

@Service
public class ParsingService {
    public boolean parseOpen(WebElement row, String OPEN) {
        return OPEN.equals("open") || row.findElements(By.cssSelector(OPEN)).isEmpty();
    }

    public String parseTitle(WebElement row, String TITLE) {
        return row.findElement(By.cssSelector(TITLE)).getText();
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
            default -> Optional.ofNullable(row.findElement(By.cssSelector(IMAGE)).getAttribute("src"))
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
            if (dateTime.contains(":")) { // 시간 포맷이 들어오면 (예: "20:18:16")
                return today.format(dateFormatter) + " " + dateTime;
            } else if (dateTime.contains("/")) { // 날짜 포맷이 들어오면 (예: "24/05/11")
                String[] parts = dateTime.split("/");
                LocalDate date = LocalDate.of(today.getYear(), Integer.parseInt(parts[1]), Integer.parseInt(parts[2]));
                return date.format(dateFormatter) + " 00:00:00";
            }
        } else if (label.startsWith("루리웹")) {
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

    public int parseViews(WebElement row, String VIEWS) {
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

    public int parseInteger(String text) {
        try {
            return Integer.parseInt(text.replaceAll("\\D+", ""));
        } catch (NumberFormatException e) {
            return 0;
        }
    }
}