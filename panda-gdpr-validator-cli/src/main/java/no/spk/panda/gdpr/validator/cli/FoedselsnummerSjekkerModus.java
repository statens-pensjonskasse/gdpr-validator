package no.spk.panda.gdpr.validator.cli;

import static java.util.Objects.requireNonNull;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.regex.MatchResult;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class FoedselsnummerSjekkerModus {

    private static final Pattern POTENSIELL_FØDSELSNUMMER_REGEX = Pattern.compile("(\\d{11})");

    private final String bane;
    private final List<String> filtyper;
    private final boolean skalFiltrerePåFiletternavn;
    private final List<Resultat> resultater;

    public FoedselsnummerSjekkerModus(final String bane, final List<String> filtyper) {
        this.bane = requireNonNull(bane, "bane var påkrevd, men var null");
        this.filtyper = requireNonNull(filtyper, "filtyper var påkrevd, men var null");
        skalFiltrerePåFiletternavn = filtyper.size() > 0;
        resultater = new ArrayList<>();
    }

    public Integer sjekk() throws FileNotFoundException {
        final File fil = new File(bane);

        if (!fil.exists()) {
            throw new FileNotFoundException("Filen/katalogen eksisterer ikke.");
        } else if (fil.isFile()) {
            sjekkEnkeltfil(fil);
        } else if (fil.isDirectory()) {
            sjekkKatalog(bane);
        }

        return 0;
    }

    private void sjekkEnkeltfil(final File fil) throws FileNotFoundException {
        if (skalFiltrerePåFiletternavn && !filtyper.contains(finnFiletternavn(fil))) {
            return;
        }

        resultater.addAll(
                FoedselsnummerSjekker.sjekk(
                        new Scanner(fil)
                                .findAll(POTENSIELL_FØDSELSNUMMER_REGEX)
                                .map(MatchResult::group)
                                .collect(Collectors.toUnmodifiableList()),
                        fil.getAbsolutePath()
                )
        );

        resultater
                .forEach(r ->
                        System.out.format(
                                "Fødselsnummer: %s, fil: %s\n",
                                r.fødselsnummer(), r.filbane()
                        )
                );
    }

    private void sjekkKatalog(final String bane) {

    }

    private String finnFiletternavn(final File fil) {
        return fil.getAbsolutePath().substring(fil.getAbsolutePath().lastIndexOf(".") + 1);
    }
}
