package xyz.eulix.platform.services.mgtboard.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.Arrays;

@AllArgsConstructor
public enum ArticleStateEnum {
    DRAFT(0, "草稿"),
    PUBLISHED(1, "已发布")
    ;

    @Setter
    @Getter
    private Integer state;

    @Setter @Getter
    private String desc;

    public static ArticleStateEnum fromValue(Integer value) {
        return Arrays.stream(values()).filter(state -> {
            if (state.getState().equals(value)) {
                return true;
            }
            return false;
        }).findFirst().orElseThrow();
    }
}
