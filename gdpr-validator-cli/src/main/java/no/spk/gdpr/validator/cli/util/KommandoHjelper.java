package no.spk.gdpr.validator.cli.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

public class KommandoHjelper {

    public static KommandoHjelper kommandoHjelper() {
        return new KommandoHjelper();
    }

    @SuppressWarnings("UnusedReturnValue")
    public Stream<String> kjoerKommando(final List<String> programOgArgumenter) {
        try {
            final Process process = new ProcessBuilder(programOgArgumenter).start();

            final InputStream is = process.getInputStream();
            final InputStreamReader isr = new InputStreamReader(is);
            final BufferedReader br = new BufferedReader(isr);

            String line;
            final List<String> output = new ArrayList<>();
            while ((line = br.readLine()) != null) {
                output.add(line);
            }

            br.close();

            try {
                process.waitFor();
            } catch (InterruptedException e) {
                throw new RuntimeException(
                        String.format(
                                "Ble avbrutt når den ventet på prosess som blir kjørt! Kjørt kommando: %s",
                                String.join(" ", programOgArgumenter)
                        )
                );
            }

            if (process.exitValue() != 0) {
                throw new RuntimeException(
                        String.format(
                                "Kjøring av kommando feilet med kode %s. Kommando som ble kjørt: %s",
                                process.exitValue(),
                                String.join(" ", programOgArgumenter)
                        )
                );
            }

            return output.stream();
        } catch (IOException e) {
            throw new RuntimeException(
                    String.format(
                            "Kjøring av kommando feilet. Kommando som ble kjørt: %s",
                            String.join(" ", programOgArgumenter)
                    ), e
            );
        }
    }
}
