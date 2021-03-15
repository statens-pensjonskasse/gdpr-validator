package no.spk.panda.gdpr.validator.cli;

import no.spk.panda.gdpr.validator.fnr.Foedselsnummer;

public class Resultat {

    private final Foedselsnummer fødselsnummer;
    private final String filbane;

    private Resultat(final Foedselsnummer fødselsnummer, final String filbane) {
        this.fødselsnummer = fødselsnummer;
        this.filbane = filbane;
    }

    public static Resultat resultat(final Foedselsnummer fødselsnummer, final String filbane) {
        return new Resultat(fødselsnummer, filbane);
    }

    public Foedselsnummer fødselsnummer() {
        return fødselsnummer;
    }

    public String filbane() {
        return filbane;
    }
}
