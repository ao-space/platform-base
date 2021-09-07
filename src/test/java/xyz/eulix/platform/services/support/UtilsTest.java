package xyz.eulix.platform.services.support;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class UtilsTest {

  @Test
  void createUnifiedRandomCharacters() {
    final String characters = Utils.createUnifiedRandomCharacters(12);
    assertEquals(12, characters.length());
  }
}