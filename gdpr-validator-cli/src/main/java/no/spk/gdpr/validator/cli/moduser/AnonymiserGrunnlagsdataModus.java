package no.spk.gdpr.validator.cli.moduser;

import static java.util.Objects.requireNonNull;
import static no.spk.gdpr.validator.fnr.ValidatorParametere.parametereForKasperMedSemikolonValidator;
import static no.spk.gdpr.validator.fnr.ValidatorParametere.parametereForKasperValidator;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.stream.Stream;

import no.spk.gdpr.anonymisert.fnr.AnonymisertFoedselsnummer;
import no.spk.gdpr.validator.cli.IkkeMedlemsdatafilException;
import no.spk.gdpr.validator.cli.UtgangsInnstillinger;
import no.spk.gdpr.validator.cli.util.Tuple;
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
            anonymiser(new File(fil.getAbsolutePath() + '/' + MEDLEMSDATA_FILNAVN));
        }
    }

    private static void anonymiserMedlemsdata(final File fil) throws IOException {
        final Tuple<Map<Foedselsnummer, AnonymisertFoedselsnummer>, Map<Integer, List<Foedselsnummer>>> fødselsnummere =
                finnAlleFødselsnummereOgAnonymiser(fil);
        byttUtFødselsnummereIfil(fil, fødselsnummere);
    }

    private static Tuple<Map<Foedselsnummer, AnonymisertFoedselsnummer>, Map<Integer, List<Foedselsnummer>>> finnAlleFødselsnummereOgAnonymiser(
            final File fil
    ) throws IOException {
        final Map<Foedselsnummer, AnonymisertFoedselsnummer> fødselsnummere = new HashMap<>();
        final Map<Integer, List<Foedselsnummer>> fødselsnummerPåLinje = new HashMap<>();

        try (final Stream<String> linjeStream = Files.lines(fil.toPath())) {
            var linjeNummer = new Object() {
                int val = 0;
            };
            linjeStream.forEach(linje -> {
                finnOgLeggTilFødselsnummerForLinje(fødselsnummere, fødselsnummerPåLinje, linjeNummer.val, linje, parametereForKasperValidator());
                finnOgLeggTilFødselsnummerForLinje(fødselsnummere, fødselsnummerPåLinje, linjeNummer.val, linje, parametereForKasperMedSemikolonValidator());
                linjeNummer.val++;
            });
        }

        return new Tuple<>(fødselsnummere, fødselsnummerPåLinje);
    }

    private static void finnOgLeggTilFødselsnummerForLinje(
            final Map<Foedselsnummer, AnonymisertFoedselsnummer> fødselsnummere,
            final Map<Integer, List<Foedselsnummer>> fødselsnummerPåLinje,
            final int linjeNummer,
            final String line,
            final ValidatorParametere validatorParametere
    ) {
        final Matcher matcher = validatorParametere.mønster().matcher(line);
        while (matcher.find()) {
            final Foedselsnummer fødselsnummer = Foedselsnummer.foedselsnummer(
                    matcher.group("fnr"),
                    validatorParametere
            );

            final AnonymisertFoedselsnummer anonymisertFødselsnummer = AnonymisertFoedselsnummer.fraFoedselsnummer(
                    fødselsnummer,
                    validatorParametere
            );

            if (fødselsnummer.erGyldig() || fødselsnummer.erNestenGyldig()) {
                fødselsnummere.putIfAbsent(fødselsnummer, anonymisertFødselsnummer);

                final List<Foedselsnummer> fødselsnummerePåLinjen;
                if (fødselsnummerPåLinje.containsKey(linjeNummer)) {
                    fødselsnummerePåLinjen = fødselsnummerPåLinje.get(linjeNummer);
                } else {
                    fødselsnummerePåLinjen = new ArrayList<>();
                }
                fødselsnummerePåLinjen.add(fødselsnummer);
                fødselsnummerPåLinje.put(linjeNummer, fødselsnummerePåLinjen);
            }
        }
    }

    private static void byttUtFødselsnummereIfil(
            final File fil,
            final Tuple<Map<Foedselsnummer, AnonymisertFoedselsnummer>, Map<Integer, List<Foedselsnummer>>> fødselsnummere
    ) throws IOException {
        final File newFile = new File(fil.getAbsolutePath() + ".txt");


        try (final FileWriter writer = new FileWriter(newFile)) {
            try (final Stream<String> linjeStream = Files.lines(fil.toPath())) {
                var linjeNummer = new Object() { int val; };
                linjeStream.forEach(linje -> {
                            var modfisertLinje = new Object() { String linje; };
                            modfisertLinje.linje = linje;
                            if (fødselsnummere.andre().get(linjeNummer.val) != null) {
                                fødselsnummere.andre().get(linjeNummer.val)
                                        .forEach(foedselsnummer ->
                                                modfisertLinje.linje = modfisertLinje.linje.replace(
                                                        foedselsnummer.fødselsnummer(),
                                                        fødselsnummere.første().get(foedselsnummer).fødselsnummer()
                                                )
                                        );

                                try {
                                    writer.write(modfisertLinje.linje + "\n");
                                } catch (final IOException e) {
                                    e.printStackTrace();
                                }
                            }

                            linjeNummer.val++;
                        }
                );
            }
        }
    }
}
