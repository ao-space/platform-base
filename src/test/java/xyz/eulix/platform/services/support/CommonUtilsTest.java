package xyz.eulix.platform.services.support;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class CommonUtilsTest {

  @Test
  void createUnifiedRandomCharacters() {
    final String characters = CommonUtils.createUnifiedRandomCharacters(12);
    assertEquals(12, characters.length());
  }
}