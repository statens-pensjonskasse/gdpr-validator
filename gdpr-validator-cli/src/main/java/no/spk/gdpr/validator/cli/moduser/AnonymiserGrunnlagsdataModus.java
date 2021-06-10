package no.spk.gdpr.validator.cli.moduser;

import static java.util.Objects.requireNonNull;
import static no.spk.gdpr.validator.fnr.ValidatorParametere.parametereForKasperMedSemikolonValidator;
import static no.spk.gdpr.validator.fnr.ValidatorParametere.parametereForKasperValidator;

import java.io.File;
import java.io.FileNotFoundException;
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

    final String fnrtype;
    final String bane;
    final List<String> filtyper;
    final UtgangsInnstillinger utgangsInnstillinger;

    private AnonymiserGrunnlagsdataModus(
            final String fnrtype,
            final String bane,
            final List<String> filtyper,
            final UtgangsInnstillinger utgangsInnstillinger
    ) {
        this.fnrtype = requireNonNull(fnrtype, "fnrtype er påkrevd, men var null");
        this.bane = requireNonNull(bane, "bane er påkrevd, men var null");
        this.filtyper = requireNonNull(filtyper, "filtyper er påkrevd, men var null");
        this.utgangsInnstillinger = requireNonNull(utgangsInnstillinger, "utgangsInnstillinger er påkrevd, men var null");
    }

    public static AnonymiserGrunnlagsdataModus anonymiserGrunnlagsdataModus(
            final String fnrtype,
            final String bane,
            final List<String> filtyper,
            final UtgangsInnstillinger utgangsInnstillinger
    ) {
        return new AnonymiserGrunnlagsdataModus(
                fnrtype,
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

    private void anonymiserMedlemsdata(final File fil) throws IOException {
        final Map<Foedselsnummer, AnonymisertFoedselsnummer> instanser = finnAlleFødselsnummereOgAnonymiser(fil);

        System.out.println(instanser);
    }

    private Map<Foedselsnummer, AnonymisertFoedselsnummer> finnAlleFødselsnummereOgAnonymiser(final File fil) throws IOException {
        final Map<Foedselsnummer, AnonymisertFoedselsnummer> instanser = new HashMap<>();

        try (final Stream<String> linesStream = Files.lines(fil.toPath())) {
            linesStream.forEach(line -> {
                System.out.println(line);

                finnOgLeggTilFødselsnummerForLinje(instanser, line, parametereForKasperValidator());
                finnOgLeggTilFødselsnummerForLinje(instanser, line, parametereForKasperMedSemikolonValidator());
            });
        }

        return instanser;
    }

    private void finnOgLeggTilFødselsnummerForLinje(
            final Map<Foedselsnummer, AnonymisertFoedselsnummer> instanser,
            final String line,
            final ValidatorParametere validatorParametere
    ) {
        final Matcher matcher = validatorParametere.mønster().matcher(line);
        while (matcher.find()) {
            System.out.println(matcher.group());

            final Foedselsnummer fødselsnummer = Foedselsnummer.foedslesnummer(
                    matcher.group(),
                    validatorParametere
            );

            final AnonymisertFoedselsnummer anonymisertFødselsnummer = AnonymisertFoedselsnummer.fraFoedselsnummer(
                    fødselsnummer,
                    validatorParametere
            );

            instanser.put(fødselsnummer, anonymisertFødselsnummer);
        }
    }
}
