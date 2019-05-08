package org.openhab.binding.freeboxv5.parser;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.openhab.binding.freeboxv5.model.FreeboxV5Status;
import org.openhab.binding.freeboxv5.model.UpDownValue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FreeboxV5StatusParser {

    private final Logger logger = LoggerFactory.getLogger(FreeboxV5StatusParser.class);

    private final DurationParser durationParser = new DurationParser();

    private final Pattern atmPattern = Pattern.compile("(\\d+) kb/s +(\\d+) kb/s");

    private final Pattern dbPattern = Pattern.compile("(\\d+\\.\\d+) dB +(\\d+\\.\\d+) dB");

    private final Pattern intPattern = Pattern.compile("(\\d+) +(\\d+)");

    public enum Context {
        SYSTEM("Informations générales"),
        PHONE("Téléphone"),
        ADSL("Adsl"),
        WIFI("Wifi");

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
            if (line.startsWith("===") || line.startsWith("---") || line.startsWith("___")) {
                // Ignore ruler lines
                continue;
            }
            if (line.startsWith(" ")) {
                logger.trace("Processing " + line);

                // Not a title
                if (Context.SYSTEM.equals(context)) {
                    if (line.contains("Modèle")) {
                        String model = line.substring("Modèle".length() + 2);
                        model = model.trim();
                        result.model = model;
                    } else if (line.contains("Version du firmware")) {
                        String fwversion = line.substring("Version du firmware".length() + 2);
                        fwversion = fwversion.trim();
                        result.fwversion = fwversion;
                    } else if (line.contains("Temps depuis la mise en route")) {
                        result.uptime = durationParser.match(line);
                    }
                }
                if (Context.PHONE.equals(context)) {
                    if (line.contains("Etat  ")) {
                        result.phone.on = line.contains("Ok");
                    } else if (line.contains("Etat du combiné")) {
                        result.phone.hang_up = line.contains("Raccroché");
                    } else if (line.contains("Sonnerie")) {
                        result.phone.ringing = !line.contains("Inactive");
                    }
                }
                if (Context.ADSL.equals(context)) {
                    if (line.startsWith("  Etat  ")) {
                        result.adsl_state = line.substring("Etat  ".length() + 2).trim();
                    } else if (line.contains("Protocole")) {
                        result.adsl_protocol = line.substring("Protocole".length() + 2).trim();
                    } else if (line.contains("Mode")) {
                        result.adsl_mode = line.substring("Mode".length() + 2).trim();
                    } else if (line.contains("Débit ATM")) {
                        Matcher matcher = atmPattern.matcher(line);
                        if (!matcher.find()) {
                            throw new IOException("Failed to parse ATM in " + line);
                        }
                        result.adsl_atm = new UpDownValue<Integer>(Integer.parseInt(matcher.group(1)),
                                Integer.parseInt(matcher.group(2)));
                    } else if (line.contains("Marge de bruit")) {
                        Matcher matcher = dbPattern.matcher(line);
                        if (!matcher.find()) {
                            throw new IOException("Failed to parse noise margin in " + line);
                        }
                        result.adsl_noise_margin = new UpDownValue<Double>(Double.parseDouble(matcher.group(1)),
                                Double.parseDouble(matcher.group(2)));
                    } else if (line.contains("Atténuation")) {
                        Matcher matcher = dbPattern.matcher(line);
                        if (!matcher.find()) {
                            throw new IOException("Failed to parse attenuation in " + line);
                        }
                        result.adsl_attenuation = new UpDownValue<Double>(Double.parseDouble(matcher.group(1)),
                                Double.parseDouble(matcher.group(2)));
                    } else if (line.contains("FEC")) {
                        Matcher matcher = intPattern.matcher(line);
                        if (!matcher.find()) {
                            throw new IOException("Failed to parse FEC in " + line);
                        }
                        result.adsl_fec = new UpDownValue<Integer>(Integer.parseInt(matcher.group(1)),
                                Integer.parseInt(matcher.group(2)));
                    } else if (line.contains("CRC")) {
                        Matcher matcher = intPattern.matcher(line);
                        if (!matcher.find()) {
                            throw new IOException("Failed to parse CRC in " + line);
                        }
                        result.adsl_crc = new UpDownValue<Integer>(Integer.parseInt(matcher.group(1)),
                                Integer.parseInt(matcher.group(2)));
                    } else if (line.contains("HEC")) {
                        Matcher matcher = intPattern.matcher(line);
                        if (!matcher.find()) {
                            throw new IOException("Failed to parse HEC in " + line);
                        }
                        result.adsl_hec = new UpDownValue<Integer>(Integer.parseInt(matcher.group(1)),
                                Integer.parseInt(matcher.group(2)));
                    }
                }
                if (Context.WIFI.equals(context)) {
                    if (line.startsWith("  Etat  ")) {
                        result.wifi_state = line.contains("Ok");
                    } else if (line.contains("Modèle")) {
                        result.wifi_model = line.substring("Modèle".length() + 2).trim();
                    } else if (line.contains("Canal")) {
                        result.wifi_channel = Integer.parseInt(line.substring("Canal".length() + 2).trim());
                    } else if (line.contains("État du réseau")) {
                        result.wifi_net_state = line.contains("Activé");
                    } else if (line.contains("Ssid")) {
                        result.wifi_ssid = line.substring("Ssid".length() + 2).trim();
                    } else if (line.contains("Type de clé")) {
                        result.wifi_type = line.substring("Type de clé".length() + 2).trim();
                    } else if (line.contains("FreeWifi Secure")) {
                        result.wifi_freewifi_secure = line.contains("Actif");
                    } else if (line.contains("FreeWifi")) {
                        result.wifi_freewifi = line.contains("Actif");
                    }
                }
            } else {
                // A title
                Context next = Context.match(line);
                if (null != next) {
                    logger.trace("Switching context from {} to {}", context, next);
                    // New context
                    context = next;
                } else {
                    logger.warn("Unknown context: {}", line);
                    context = null;
                }
            }
        }
        reader.close();

        return result;
    }
}