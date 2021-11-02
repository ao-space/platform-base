package xyz.eulix.platform.services.support;

import org.locationtech.jts.util.CollectionUtil;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Collection;
import java.util.Random;
import java.util.UUID;

/**
 * 提供一些全局可以访问的工具方法。
 */
public final class CommonUtils {
  private CommonUtils() {
    // construction guards
  }

  private final static Random random = new java.security.SecureRandom();

  /**
   * 缺省的日期显示格式： yyyy-MM-dd
   */
  public static final String DEFAULT_DATE_FORMAT = "yyyy-MM-dd";

  /**
   * 用做单号
   */
  public static final String DEFAULT_DATE_FORMAT_ORDER = "yyyyMMdd";

  public static String createUnifiedRandomCharacters(int length) {
    int startChar = '0';
    int endChar = 'z';

    return random.ints(startChar, endChar + 1)
        .filter(i -> (i <= 57 || i >= 65) && (i <= 90 || i >= 97))
        .limit(length)
        .collect(StringBuilder::new, StringBuilder::appendCodePoint, StringBuilder::append)
        .toString();
  }

  /**
   * 获得一个UUID
   *
   * @return String UUID
   */
  public static String getUUID(){
    String uuid = UUID.randomUUID().toString();
    //去掉“-”符号
    return uuid.replaceAll("-", "");
  }

  /**
   * 获取年月日(当天)
   *
   * @return 年月日
   */
  public static String getDay() {
    LocalDate date = LocalDate.now();
    DateTimeFormatter fmt = DateTimeFormatter.ofPattern(DEFAULT_DATE_FORMAT);
    return date.format(fmt);
  }

  /**
   * 获取年月日(当天),订单格式
   *
   * @return 年月日
   */
  public static String getDayOrderFormat() {
    LocalDate date = LocalDate.now();
    DateTimeFormatter fmt = DateTimeFormatter.ofPattern(DEFAULT_DATE_FORMAT_ORDER);
    return date.format(fmt);
  }

  public static boolean isNullOrEmpty(String value) {
    return value == null || value.isEmpty();
  }

  public static <T> boolean isNullOrEmpty(Collection<T> list) {
    return list == null || list.isEmpty();
  }
}
