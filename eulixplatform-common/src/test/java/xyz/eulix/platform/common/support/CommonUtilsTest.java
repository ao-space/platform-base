package xyz.eulix.platform.common.support;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class CommonUtilsTest {

  @Test
  void createUnifiedRandomCharacters() {
    final String characters = CommonUtils.createUnifiedRandomCharacters(12);
    assertEquals(12, characters.length());
  }
}