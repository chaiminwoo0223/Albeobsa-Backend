package skhu.jijijig.domain;

import lombok.Getter;

@Getter
public enum Reason {
    INAPPROPRIATE_CONTENT("부적절한 내용"),
    MISINFORMATION("잘못된 정보"),
    SPAM("스팸"),
    OTHER("기타");

    private final String description;

    Reason(String description) {
        this.description = description;
    }
}