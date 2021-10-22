package xyz.eulix.platform.services.mgtboard.dto;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.NoSuchElementException;

public class AppTypeEnumTest {

    @Test
    public void testHelloEndpoint() {
        Assertions.assertEquals(AppTypeEnum.IOS, AppTypeEnum.fromValue("ios"));
        Assertions.assertThrows(NoSuchElementException.class, () -> AppTypeEnum.fromValue("ios2"));
    }
}
