package xyz.eulix.platform.services.mgtboard.service;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ProposalServiceTest {

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
}
