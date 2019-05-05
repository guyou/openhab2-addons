package org.openhab.binding.freeboxv5.parser;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.openhab.binding.freeboxv5.model.FreeboxV5Status;

public class FreeboxV5StatusParser {

    private final DurationParser durationParser = new DurationParser();

    public enum Context {
        SYSTEM("Informations générales"),
        PHONE("Téléphone"),
        ADSL("Adsl");

        final String title;

        Context(String title) {
            this.title = title;
        }

        public static Context match(String line) {
            Context result = null;

            for (Context current : values()) {
                if (line.startsWith(current.title)) {
                    result = current;
                }
            }

            return result;
        }
    }

    public FreeboxV5Status parse(InputStream input) throws IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(input));
        String line;
        Context context = null;
        FreeboxV5Status result = new FreeboxV5Status();
        while ((line = reader.readLine()) != null) {
            if (line.trim().isEmpty()) {
                // Ignore empty lines
                continue;
            }
            if (line.startsWith(" ")) {
                // Not a title
                if (line.contains("Version du firmware")) {
                    String fwversion = line.substring("Version du firmware".length() + 2);
                    fwversion = fwversion.trim();
                    result.fwversion = fwversion;
                }
                if (line.contains("Temps depuis la mise en route")) {
                    result.uptime = durationParser.match(line);
                }
                if (Context.PHONE.equals(context) && line.contains("Etat  ")) {
                    result.phone.on = line.contains("Ok");
                }
                if (Context.PHONE.equals(context) && line.contains("Sonnerie")) {
                    result.phone.ringing = !line.contains("Inactive");
                }
            } else {
                // A title
                Context next = Context.match(line);
                if (null != next) {
                    // New context
                    context = next;
                }
            }
        }
        reader.close();

        return result;
    }
}