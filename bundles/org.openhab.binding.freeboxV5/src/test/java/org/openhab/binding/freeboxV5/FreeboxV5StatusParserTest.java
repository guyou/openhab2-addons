package org.openhab.binding.freeboxV5;

import java.io.IOException;
import java.io.InputStream;

import org.junit.Assert;
import org.junit.Test;
import org.openhab.binding.freeboxv5.model.FreeboxV5Status;
import org.openhab.binding.freeboxv5.model.UpDownValue;
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

        // Phone
        Assert.assertTrue(status.phone.on);
        Assert.assertTrue(status.phone.hang_up);
        Assert.assertFalse(status.phone.ringing);

        // ADSL
        Assert.assertEquals("Showtime", status.adsl_state);
        Assert.assertEquals("ADSL2+", status.adsl_protocol);
        Assert.assertEquals("Interleaved", status.adsl_mode);

        Assert.assertEquals(new UpDownValue<>(15286, 1093), status.adsl_atm);
        Assert.assertEquals(new UpDownValue<>(6.0, 7.4), status.adsl_noise_margin);
        Assert.assertEquals(new UpDownValue<>(32.5, 19.0), status.adsl_attenuation);
        Assert.assertEquals(new UpDownValue<>(836528, 782458), status.adsl_fec);
        Assert.assertEquals(new UpDownValue<>(521506, 0), status.adsl_crc);
        Assert.assertEquals(new UpDownValue<>(2244, 124383), status.adsl_hec);

        // Wifi
        Assert.assertTrue(status.wifi_state);
        Assert.assertEquals("Ralink RT2880", status.wifi_model);
        Assert.assertEquals(9, status.wifi_channel);
        Assert.assertTrue(status.wifi_net_state);
        Assert.assertEquals("BSSID", status.wifi_ssid);
        Assert.assertEquals("WPA", status.wifi_type);
        Assert.assertTrue(status.wifi_freewifi);
        Assert.assertTrue(status.wifi_freewifi_secure);

    }
}