package org.openhab.binding.freeboxv5.parser;

import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DurationParser {

    final Pattern pattern = Pattern.compile("(\\d+) jours?, (\\d+) heures?, (\\d+) minutes?");

    public long match(final String line) throws IOException {
        long duration = 0;
        Matcher matcher = pattern.matcher(line);
        if (!matcher.find()) {
            throw new IOException("No matching found on " + line);
        }
        // Days
        duration += Integer.valueOf(matcher.group(1)) * 24 * 60;
        // Hours
        duration += Integer.valueOf(matcher.group(2)) * 60;
        // Minutes
        duration += Integer.valueOf(matcher.group(3));

        return duration;
    }
}
