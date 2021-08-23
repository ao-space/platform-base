package xyz.eulix.platform.services.appmgt.dto;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class AppTypeEnumTest {

    @Test
    public void testHelloEndpoint() {
        Assertions.assertEquals(AppTypeEnum.IOS, AppTypeEnum.fromValue("ios"));
        Assertions.assertNull(AppTypeEnum.fromValue("ios2"));
    }
}
