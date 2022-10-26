package xyz.eulix.platform.common.support;

import org.jboss.logging.Logger;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.Collection;
import java.util.Date;
import java.util.Random;
import java.util.UUID;

/**
 * 提供一些全局可以访问的工具方法。
 */
public final class CommonUtils {
    private static final Logger LOG = Logger.getLogger("app.log");

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

    public static final String LOCAL_DATE_TIME = "yyyy-MM-dd HH:mm:ss";

    public static String createUnifiedRandomCharacters(int length) {
        int startChar = '0';
        int endChar = 'z';

        return random.ints(startChar, endChar + 1)
                .filter(i -> (i <= 57 || i >= 65) && (i <= 90 || i >= 97))
                .limit(length)
                .collect(StringBuilder::new, StringBuilder::appendCodePoint, StringBuilder::append)
                .toString();
    }

    public static String randomDigestAndLetters(int length) {
        int startChar = '0';
        int endChar = 'z';

        return random.ints(startChar, endChar + 1)
                .filter(i -> i <= 57 || i >= 97)
                .limit(length)
                .collect(StringBuilder::new, StringBuilder::appendCodePoint, StringBuilder::append)
                .toString();
    }

    public static String randomLetters(int length) {
        int startChar = 'a';
        int endChar = 'z';

        return random.ints(startChar, endChar + 1)
                .limit(length)
                .collect(StringBuilder::new, StringBuilder::appendCodePoint, StringBuilder::append)
                .toString();
    }

    public static String unifiedRandomHexCharters(int length) {
        int startChar = '0';
        int endChar = 'f';
        return random.ints(startChar, endChar + 1)
                .filter(i -> (i <= 57 || i >97) )
                .limit(length)
                .collect(StringBuilder::new, StringBuilder::appendCodePoint, StringBuilder::append)
                .toString();
    }

    /**
     * 获得一个UUID
     *
     * @return String UUID
     */
    public static String getUUID() {
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

    public static boolean isNull(Object object) {
        return object == null;
    }

    public static boolean isNotNull(Object object) {
        return !isNull(object);
    }

    /**
     * 将DateTime格式时间转换成OffsetDateTime格式时间(时区采用当前时区)，如
     * 输入：2021-10-10 11:30:30
     * 输出：2021-10-10T11:30:30+08:00
     *
     * @param dateTime DateTime格式时间
     * @return OffsetDateTime格式时间
     */
    public static OffsetDateTime dateTimeToOffsetDateTime(String dateTime) {
        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern(LOCAL_DATE_TIME);
        LocalDateTime localDateTime = LocalDateTime.parse(dateTime, dateTimeFormatter);
        ZoneOffset defaultZoneOffset = OffsetDateTime.now().getOffset();
        return OffsetDateTime.of(localDateTime, defaultZoneOffset);
    }

    /**
     * 将OffsetDateTime格式时间转换成DateTime格式时间(时区采用当前时区)，如
     * 输入：2021-11-08T15:32:29+08:00
     * 输出：2021-11-08 15:32:29
     *
     * @param offsetDateTime OffsetDateTime格式时间
     * @return DateTime格式时间
     */
    public static String offsetDateTimeToDateTime(OffsetDateTime offsetDateTime) {
        Date date = Date.from(offsetDateTime.toInstant());
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(LOCAL_DATE_TIME);
        return simpleDateFormat.format(date);
    }

    public static Boolean isLocalDateTimeFormat(String localDateTime) {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(LOCAL_DATE_TIME);
        try {
            simpleDateFormat.parse(localDateTime);
        } catch (ParseException e) {
            LOG.debugv("date format error");
            return false;
        }
        return true;
    }
}
