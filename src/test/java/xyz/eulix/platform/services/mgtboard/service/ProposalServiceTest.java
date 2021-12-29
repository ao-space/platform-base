package xyz.eulix.platform.services.mgtboard.service;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ProposalServiceTest {
    /**
     * 用做单号
     */
    public static final String DEFAULT_DATE_FORMAT_ORDER = "yyyyMMdd";

    @Test
    public void testProposalReqToEntity() {
        List<String> stringList = new ArrayList<>();
        stringList.add("1");
        stringList.add("2");
        stringList.add("3");
        Assertions.assertEquals(stringList.toString(), "[1, 2, 3]");

        String join = String.join(",", stringList);
        Assertions.assertEquals(join, "1,2,3");

        String [] strings = join.split(",");
        Assertions.assertEquals(Arrays.asList(strings), stringList);

        List<String> stringList2 = new ArrayList<>();
        String join2 = String.join(",", stringList2);
        System.out.println(join2);

        String [] strings3 = "".split(",");
        System.out.println(Arrays.asList(strings3));
    }

    @Test
    public void testLocalDate() {
        LocalDate localDate = LocalDate.now();
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern(DEFAULT_DATE_FORMAT_ORDER );
        System.out.println(localDate.format(fmt));
    }

    @Test
    public void testIsLowerVersion() {
        String curV = "1.0.0-alpha";
        String targetV = "1.0.0";
        PkgMgtService pkgMgtService = new PkgMgtService();
        Assertions.assertTrue(pkgMgtService.isLowerVersion(curV, targetV));
    }
}
