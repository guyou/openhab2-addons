package org.openhab.binding.freeboxV5;

import java.io.IOException;
import java.io.InputStream;

import org.junit.Assert;
import org.junit.Test;
import org.openhab.binding.freeboxv5.model.FreeboxV5Status;
import org.openhab.binding.freeboxv5.parser.FreeboxV5StatusParser;

public class FreeboxV5StatusParserTest {

    @Test
    public void test() throws IOException {
        InputStream is = this.getClass().getResourceAsStream("/fbx_info.txt");
        FreeboxV5StatusParser parser = new FreeboxV5StatusParser();
        FreeboxV5Status status = parser.parse(is);

        Assert.assertNotNull(status);
        System.out.println(status);

        Assert.assertEquals("1.5.21", status.fwversion);

        Assert.assertEquals(261 * 24 * 60 + 12 * 60 + 48, status.uptime);
    }
}