package util;

import java.io.InputStream;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Properties;

public class VersionUtils {

    private static String versao = "N/A";
    private static String buildFormatado = "N/A";

    static {
        try (InputStream is = VersionUtils.class.getClassLoader().getResourceAsStream("app.properties")) {
            if (is != null) {
                Properties props = new Properties();
                props.load(is);

                versao = props.getProperty("app.version");
                String rawDate = props.getProperty("app.build.time");

                // Converte de UTC (do Maven) para o Horário de Brasília
                try {
                    Instant instant = Instant.parse(rawDate);
                    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy '| Hr:' HH:mm:ss")
                                                                   .withZone(ZoneId.of("America/Sao_Paulo"));
                    buildFormatado = formatter.format(instant);
                } catch (Exception e) {
                    // Caso a data não esteja no formato ISO, usa o valor original
                    buildFormatado = rawDate;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static String getVersao() { return versao; }
    public static String getBuildFormatado() { return buildFormatado; }
}