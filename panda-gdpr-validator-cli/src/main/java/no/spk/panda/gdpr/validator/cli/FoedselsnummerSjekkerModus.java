package no.spk.panda.gdpr.validator.cli;

import static java.util.Objects.requireNonNull;
import static no.spk.panda.gdpr.validator.cli.Resultat.resultat;
import static no.spk.panda.gdpr.validator.fnr.Foedselsnummer.foedslesnummer;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Scanner;
import java.util.regex.MatchResult;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import no.spk.panda.gdpr.validator.fnr.ValidatorParametere;

public class FoedselsnummerSjekkerModus {

    private final Pattern fødselsnummerRegex;
    private final ValidatorParametere validatorParametere;

    private final String bane;
    private final List<String> filtyper;
    private final boolean skalFiltrerePåFiletternavn;
    private final List<Resultat> resultater;

    public FoedselsnummerSjekkerModus(
            final String bane,
            final List<String> filtyper
    ) {
        this(bane, filtyper, ValidatorParametere.forOrdinærValidator());
    }

    public FoedselsnummerSjekkerModus(
            final String bane,
            final List<String> filtyper,
            final ValidatorParametere validatorParametere
    ) {
        this.bane = requireNonNull(bane, "bane var påkrevd, men var null");
        this.filtyper = requireNonNull(filtyper, "filtyper var påkrevd, men var null");
        this.validatorParametere = requireNonNull(validatorParametere, "validatorParametere var påkrevd, men var null");
        this.fødselsnummerRegex = validatorParametere.mønster();

        skalFiltrerePåFiletternavn = filtyper.size() > 0;
        resultater = new ArrayList<>();
    }

    public void sjekk() throws FileNotFoundException {
        sjekk(new File(bane));

        resultater
                .forEach(System.out::println);
    }

    private void sjekk(final File fil) throws FileNotFoundException {
        if (fil == null || !fil.exists()) {
            throw new FileNotFoundException("Filen/katalogen eksisterer ikke.");
        } else if (fil.isFile()) {
            sjekkEnkeltfil(fil);
        } else if (fil.isDirectory()) {
            for (final File underfil : requireNonNull(fil.listFiles())) {
                sjekk(underfil);
            }
        }
    }

    private void sjekkEnkeltfil(final File fil) throws FileNotFoundException {
        if (skalFiltrerePåFiletternavn && !filtyper.contains(finnFiletternavn(fil).toLowerCase(Locale.ROOT))) {
            return;
        }

        resultater.addAll(
                new Scanner(fil)
                        .findAll(fødselsnummerRegex)
                        .map(MatchResult::group)
                        .map(potensieltFødselsnummer -> resultat(foedslesnummer(potensieltFødselsnummer, validatorParametere), fil.getAbsolutePath()))
                        .collect(Collectors.toUnmodifiableList())
                );
    }

    private String finnFiletternavn(final File fil) {
        return fil.getAbsolutePath().substring(fil.getAbsolutePath().lastIndexOf(".") + 1);
    }
}
