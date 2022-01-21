package xyz.eulix.platform.services.mgtboard.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.Arrays;

@AllArgsConstructor
public enum QuestionnaireStateEnum {
    HAS_END("has_end", "已结束", 0),
    IN_PROCESS("in_process", "待反馈", 1),
    COMPLETED("completed", "已反馈", 2),
    NOT_START("not_start", "未开始", 3),
    ;

    @Setter @Getter
    private String name;

    @Setter @Getter
    private String desc;

    @Setter @Getter
    private Integer order;

    public static QuestionnaireStateEnum fromValue(String value) {
        return Arrays.stream(values()).filter(appType -> appType.getName().equals(value))
                .findFirst()
                .orElseThrow();
    }
}
