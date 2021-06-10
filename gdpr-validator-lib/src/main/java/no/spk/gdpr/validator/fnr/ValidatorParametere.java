package no.spk.gdpr.validator.fnr;

import java.util.regex.Pattern;

public class ValidatorParametere {

    private final Pattern mønster;
    private final int datoStart;
    private final int datoSlutt;
    private final int individnummerStart;
    private final int individnummerSlutt;
    private final int sjekksumStart;
    private final int sjekksumSlutt;
    private final int fødselsnummerLengde;
    private final String fødselsdatoMønster;

    private ValidatorParametere(final int datoStart, final int datoSlutt,
                                final int individnummerStart, final int individnummerSlutt,
                                final int sjekksumStart, final int sjekksumSlutt,
                                final int fødselsnummerLengde, final String fødselsdatoMønster,
                                final Pattern mønster
    ) {
        this.datoStart = datoStart;
        this.datoSlutt = datoSlutt;
        this.individnummerStart = individnummerStart;
        this.individnummerSlutt = individnummerSlutt;
        this.sjekksumStart = sjekksumStart;
        this.sjekksumSlutt = sjekksumSlutt;
        this.fødselsnummerLengde = fødselsnummerLengde;
        this.fødselsdatoMønster = fødselsdatoMønster;
        this.mønster = mønster;
    }

    public static ValidatorParametere parametereForOrdinærValidator() {
        // DDMMYYIIISS
        final int DATO_START = 0;
        final int DATO_SLUTT = 6;
        final int INDIVIDNUMMER_START = 6;
        final int INDIVIDNUMMER_SLUTT = 9;
        final int SJEKKSUM_START = 9;
        final int SJEKKSUM_SLUTT = 11;
        final int FØDSELSNUMMER_LENGDE = 11;
        final String FØDSELSDATO_MØNSTER = "ddMMyy";
        final Pattern MØNSTER = Pattern.compile("(?<fnr>\\d{11})");

        return new ValidatorParametere(
                DATO_START, DATO_SLUTT,
                INDIVIDNUMMER_START, INDIVIDNUMMER_SLUTT,
                SJEKKSUM_START, SJEKKSUM_SLUTT,
                FØDSELSNUMMER_LENGDE, FØDSELSDATO_MØNSTER,
                MØNSTER
        );
    }

    public static ValidatorParametere parametereForKasperValidator() {
        // YYYYMMDDIIISS
        final int DATO_START = 0;
        final int DATO_SLUTT = 8;
        final int INDIVIDNUMMER_START = 8;
        final int INDIVIDNUMMER_SLUTT = 11;
        final int SJEKKSUM_START = 11;
        final int SJEKKSUM_SLUTT = 13;
        final int FØDSELSNUMMER_LENGDE = 13;
        final String FØDSELSDATO_MØNSTER = "yyyyMMdd";
        final Pattern MØNSTER = Pattern.compile("(?<fnr>\\d{13})");

        return new ValidatorParametere(
                DATO_START, DATO_SLUTT,
                INDIVIDNUMMER_START, INDIVIDNUMMER_SLUTT,
                SJEKKSUM_START, SJEKKSUM_SLUTT,
                FØDSELSNUMMER_LENGDE, FØDSELSDATO_MØNSTER,
                MØNSTER
        );
    }

    public static ValidatorParametere parametereForKasperMedSemikolonValidator() {
        // YYYYMMDD;IIISS
        final int DATO_START = 0;
        final int DATO_SLUTT = 8;
        final int INDIVIDNUMMER_START = 9;
        final int INDIVIDNUMMER_SLUTT = 12;
        final int SJEKKSUM_START = 12;
        final int SJEKKSUM_SLUTT = 14;
        final int FØDSELSNUMMER_LENGDE = 14;
        final String FØDSELSDATO_MØNSTER = "yyyyMMdd";
        final Pattern MØNSTER = Pattern.compile("(^|;)(?<fnr>\\d{8};\\d{5})(;|$)");

        return new ValidatorParametere(
                DATO_START, DATO_SLUTT,
                INDIVIDNUMMER_START, INDIVIDNUMMER_SLUTT,
                SJEKKSUM_START, SJEKKSUM_SLUTT,
                FØDSELSNUMMER_LENGDE, FØDSELSDATO_MØNSTER,
                MØNSTER
        );
    }

    public Pattern mønster() {
        return mønster;
    }

    public int personnummerStart() {
        return individnummerStart();
    }

    public int personnummerSlutt() {
        return sjekksumSlutt;
    }

    int datoStart() {
        return datoStart;
    }

    int datoSlutt() {
        return datoSlutt;
    }

    int individnummerStart() {
        return individnummerStart;
    }

    int individnummerSlutt() {
        return individnummerSlutt;
    }

    int sjekksumStart() {
        return sjekksumStart;
    }

    int sjekksumSlutt() {
        return sjekksumSlutt;
    }

    int fødselsnummerLengde() {
        return fødselsnummerLengde;
    }

    String fødselsdatoMønster() {
        return fødselsdatoMønster;
    }
}
