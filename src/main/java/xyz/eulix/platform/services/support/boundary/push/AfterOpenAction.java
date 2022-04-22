package xyz.eulix.platform.services.support.boundary.push;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.Arrays;

@AllArgsConstructor
public enum AfterOpenAction {
    GO_APP("go_app", "打开应用"),
    GO_URL("go_url", "跳转到URL"),
    GO_ACTIVITY("go_activity", "打开特定的activity"),
    GO_CUSTOM("go_custom", "用户自定义内容"),
    ;

    @Setter @Getter
    private String name;

    @Setter @Getter
    private String desc;

    public static AfterOpenAction fromValue(String name) {
        return Arrays.stream(values()).filter(value -> {
            if (value.getName().equals(name)) {
                return true;
            }
            return false;
        }).findFirst().orElseThrow();
    }
}
