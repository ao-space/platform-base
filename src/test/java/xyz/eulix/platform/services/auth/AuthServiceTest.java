package xyz.eulix.platform.services.auth;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import xyz.eulix.platform.services.appmgt.dto.AppTypeEnum;

import java.util.NoSuchElementException;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class AuthServiceTest {

    @Test
    public void testUUID() {
        // 生成uuid
        String uuid = UUID.randomUUID().toString();
        Pattern pattern = Pattern.compile( "[a-zA-Z0-9-]{36}");
        Matcher matcher = pattern.matcher(uuid);
        Assertions.assertTrue(matcher.matches());
    }
}
