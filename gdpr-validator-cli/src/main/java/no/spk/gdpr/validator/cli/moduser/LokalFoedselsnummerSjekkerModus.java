package no.spk.gdpr.validator.cli.moduser;

import static java.lang.String.format;
import static java.util.Objects.requireNonNull;
import static no.spk.gdpr.validator.cli.Oppsummering.lagOppsummering;
import static no.spk.gdpr.validator.cli.Resultat.resultat;
import static no.spk.gdpr.validator.cli.UtgangsInnstillinger.visAlleUtgangsvariabler;
import static no.spk.gdpr.validator.fnr.Foedselsnummer.foedslesnummer;
import static no.spk.gdpr.validator.fnr.ValidatorParametere.parametereForOrdinærValidator;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.Scanner;
import java.util.regex.MatchResult;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import no.spk.gdpr.validator.cli.Oppsummering;
import no.spk.gdpr.validator.cli.util.Util;
import no.spk.gdpr.validator.cli.Resultat;
import no.spk.gdpr.validator.cli.UtgangsInnstillinger;
import no.spk.gdpr.validator.fnr.ValidatorParametere;

import org.jetbrains.annotations.NotNull;

public class LokalFoedselsnummerSjekkerModus {

    private final Pattern fødselsnummerRegex;
    private final ValidatorParametere validatorParametere;
    private final UtgangsInnstillinger utgangsInnstillinger;

    private final String bane;
    private final List<String> filtyper;
    private final boolean skalFiltrerePåFiletternavn;
    private Optional<Oppsummering> oppsummering;

    private LokalFoedselsnummerSjekkerModus(
            final String bane,
            final List<String> filtyper
    ) {
        this(bane, filtyper, parametereForOrdinærValidator(), visAlleUtgangsvariabler());
    }

    private LokalFoedselsnummerSjekkerModus(
            final String bane,
            final List<String> filtyper,
            final ValidatorParametere validatorParametere,
            final UtgangsInnstillinger utgangsInnstillinger
    ) {
        this.bane = requireNonNull(bane, "bane var påkrevd, men var null");
        this.filtyper = requireNonNull(filtyper, "filtyper var påkrevd, men var null");
        this.validatorParametere = requireNonNull(validatorParametere, "validatorParametere var påkrevd, men var null");
        this.fødselsnummerRegex = validatorParametere.mønster();
        this.utgangsInnstillinger = requireNonNull(utgangsInnstillinger, "utgangsInnstillinger var påkrevd, men var null");

        skalFiltrerePåFiletternavn = !filtyper.isEmpty();

        oppsummering = Optional.of(Oppsummering.initier()).filter(x -> utgangsInnstillinger.visOppsummering());
    }

    public static LokalFoedselsnummerSjekkerModus lokalFoedselsnummerSjekkerModus(
            final String bane,
            final List<String> filtyper
    ) {
        return new LokalFoedselsnummerSjekkerModus(bane, filtyper);
    }

    public static LokalFoedselsnummerSjekkerModus lokalFoedselsnummerSjekkerModus(
            final String bane,
            final List<String> filtyper,
            final ValidatorParametere validatorParametere,
            final UtgangsInnstillinger utgangsInnstillinger
    ) {
        return new LokalFoedselsnummerSjekkerModus(bane, filtyper, validatorParametere, utgangsInnstillinger);
    }

    public void kjør() throws FileNotFoundException {
        sjekk(new File(bane));

        oppsummering
                .map(Oppsummering::toString)
                .ifPresent(System.out::println);
    }

    private void skrivUt(final Resultat r) {
        System.out.println(r.filtrertOutput(utgangsInnstillinger));
    }

    private void sjekk(final File fil) throws FileNotFoundException {
        if (fil == null || !fil.exists()) {
            throw new FileNotFoundException(format("Filen/katalogen eksisterer ikke: %s.", fil != null ? fil.getAbsolutePath() : "null"));
        } else if (fil.isFile()) {
            sjekkEnkeltfil(fil);
        } else if (fil.isDirectory()) {
            for (final File underfil : requireNonNull(fil.listFiles())) {
                sjekk(underfil);
            }
        }
    }

    private void sjekkEnkeltfil(final File fil) {
        if (skalFiltrerePåFiletternavn && !filetternavnetErRelevant(fil)) {
            return;
        }

        if (oppsummering.isPresent()) {
            oppsummering = oppsummering
                    .map(
                            eksisterende ->
                                    eksisterende
                                            .pluss(
                                                    lagOppsummering(
                                                            scanEnFil(fil)
                                                                    .peek(this::skrivUt)
                                                                    .toList()
                                                    )
                                            )
                    );
        } else {
            scanEnFil(fil)
                    .forEach(this::skrivUt);
        }
    }

    private Stream<Resultat> scanEnFil(final File fil) {
        try {
            return new Scanner(fil)
                    .findAll(fødselsnummerRegex)
                    .map(MatchResult::group)
                    .map(potensieltFødselsnummer ->
                            resultat(foedslesnummer(potensieltFødselsnummer, validatorParametere), fil.getAbsolutePath())
                    );
        } catch (FileNotFoundException e) {
            System.out.printf("Fant ikke fil: %s%n", fil.getAbsolutePath());
        }
        return Stream.empty();
    }

    private boolean filetternavnetErRelevant(final File fil) {
        return filtyper.contains(Util.filetternavn(fil).toLowerCase(Locale.ROOT));
    }
}
