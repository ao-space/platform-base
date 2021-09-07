package xyz.eulix.platform.services.support;

import java.util.Random;

/**
 * 提供一些全局可以访问的工具方法。
 */
public final class Utils {
  private Utils() {
    // construction guards
  }

  private final static Random random = new java.security.SecureRandom();

  public static String createUnifiedRandomCharacters(int length) {
    int startChar = '0';
    int endChar = 'z';

    return random.ints(startChar, endChar + 1)
        .filter(i -> (i <= 57 || i >= 65) && (i <= 90 || i >= 97))
        .limit(length)
        .collect(StringBuilder::new, StringBuilder::appendCodePoint, StringBuilder::append)
        .toString();
  }
}
