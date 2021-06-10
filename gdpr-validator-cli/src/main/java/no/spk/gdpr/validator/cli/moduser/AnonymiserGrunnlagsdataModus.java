package no.spk.gdpr.validator.cli.moduser;

import static java.util.Objects.requireNonNull;
import static no.spk.gdpr.validator.fnr.ValidatorParametere.parametereForKasperMedSemikolonValidator;
import static no.spk.gdpr.validator.fnr.ValidatorParametere.parametereForKasperValidator;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.stream.Stream;

import no.spk.gdpr.anonymisert.fnr.AnonymisertFoedselsnummer;
import no.spk.gdpr.validator.cli.IkkeMedlemsdatafilException;
import no.spk.gdpr.validator.cli.UtgangsInnstillinger;
import no.spk.gdpr.validator.fnr.Foedselsnummer;
import no.spk.gdpr.validator.fnr.ValidatorParametere;

public class AnonymiserGrunnlagsdataModus {

    final static String MEDLEMSDATA_FILNAVN = "medlemsdata.csv";

    final String bane;
    final List<String> filtyper;
    final UtgangsInnstillinger utgangsInnstillinger;

    private AnonymiserGrunnlagsdataModus(
            final String bane,
            final List<String> filtyper,
            final UtgangsInnstillinger utgangsInnstillinger
    ) {
        this.bane = requireNonNull(bane, "bane er påkrevd, men var null");
        this.filtyper = requireNonNull(filtyper, "filtyper er påkrevd, men var null");
        this.utgangsInnstillinger = requireNonNull(utgangsInnstillinger, "utgangsInnstillinger er påkrevd, men var null");
    }

    public static AnonymiserGrunnlagsdataModus anonymiserGrunnlagsdataModus(
            final String bane,
            final List<String> filtyper,
            final UtgangsInnstillinger utgangsInnstillinger
    ) {
        return new AnonymiserGrunnlagsdataModus(
                bane,
                filtyper,
                utgangsInnstillinger
        );
    }

    public void anonymiser() throws IOException {
        anonymiser(new File(bane));
    }

    private void anonymiser(final File fil) throws IOException {
        if (fil == null || !fil.exists()) {
            throw new FileNotFoundException(String.format("Filen/katalogen eksisterer ikke: %s.", fil != null ? fil.getAbsolutePath() : "null"));
        } else if (fil.isFile()) {
            if (fil.getName().equals(MEDLEMSDATA_FILNAVN)) {
                anonymiserMedlemsdata(fil);
            } else {
                throw new IkkeMedlemsdatafilException(fil.getName());
            }
        } else if (fil.isDirectory()) {
            anonymiser(new File(MEDLEMSDATA_FILNAVN));
        }
    }

    private static void anonymiserMedlemsdata(final File fil) throws IOException {
        final Map<Foedselsnummer, AnonymisertFoedselsnummer> instanser = finnAlleFødselsnummereOgAnonymiser(fil);
        byttUtFødselsnummereIfil(fil, instanser);
    }

    private static Map<Foedselsnummer, AnonymisertFoedselsnummer> finnAlleFødselsnummereOgAnonymiser(final File fil) throws IOException {
        final Map<Foedselsnummer, AnonymisertFoedselsnummer> instanser = new HashMap<>();

        try (final Stream<String> linjeStream = Files.lines(fil.toPath())) {
            linjeStream.forEach(linje -> {
                finnOgLeggTilFødselsnummerForLinje(instanser, linje, parametereForKasperValidator());
                finnOgLeggTilFødselsnummerForLinje(instanser, linje, parametereForKasperMedSemikolonValidator());
            });
        }

        return instanser;
    }

    private static void finnOgLeggTilFødselsnummerForLinje(
            final Map<Foedselsnummer, AnonymisertFoedselsnummer> instanser,
            final String line,
            final ValidatorParametere validatorParametere
    ) {
        final Matcher matcher = validatorParametere.mønster().matcher(line);
        while (matcher.find()) {
            final Foedselsnummer fødselsnummer = Foedselsnummer.foedslesnummer(
                    matcher.group(),
                    validatorParametere
            );

            final AnonymisertFoedselsnummer anonymisertFødselsnummer = AnonymisertFoedselsnummer.fraFoedselsnummer(
                    fødselsnummer,
                    validatorParametere
            );

            instanser.putIfAbsent(fødselsnummer, anonymisertFødselsnummer);
        }
    }

    private static void byttUtFødselsnummereIfil(
            final File fil,
            final Map<Foedselsnummer, AnonymisertFoedselsnummer> instanser
    ) throws IOException {
        final File newFile = new File(fil.getAbsolutePath() + ".txt");

        try (final FileWriter writer = new FileWriter(newFile)) {
            try (final Stream<String> linjeStream = Files.lines(fil.toPath())) {
                linjeStream.forEach(linje -> {
                            var modfisertLinje = new Object() { String linje; };
                            modfisertLinje.linje = linje;
                            instanser
                                    .forEach((foedselsnummer, anonymisertFoedselsnummer) ->
                                            modfisertLinje.linje = modfisertLinje.linje.replace(
                                                    foedselsnummer.fødselsnummer(),
                                                    anonymisertFoedselsnummer.fødselsnummer()
                                            )
                                    );

                            try {
                                writer.write(modfisertLinje.linje + "\n");
                            } catch (final IOException e) {
                                e.printStackTrace();
                            }
                        }
                );
            }
        }
    }
}
