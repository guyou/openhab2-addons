package org.openhab.binding.freeboxV5;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.openhab.binding.freeboxv5.parser.DurationParser;

public class DurationParserTest {

    DurationParser parser;

    @Before
    public void setup() {
        parser = new DurationParser();
    }

    @Test
    public void testZero() {
        long duration = parser.match("0 jours, 0 heures, 0 minutes");
        Assert.assertEquals(0, duration);
    }

    @Test
    public void testOne() {
        long duration = parser.match("1 jours, 1 heures, 1 minutes");
        Assert.assertEquals(24 * 60 + 60 + 1, duration);
    }

    @Test
    public void testOneWithNoise() {
        long duration = parser.match("du bruit avant 1 jours, 1 heures, 1 minutes et apres");
        Assert.assertEquals(24 * 60 + 60 + 1, duration);
    }
}
