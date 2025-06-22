package no.spk.gdpr.validator.cli;

import no.spk.gdpr.validator.fnr.Foedselsnummer;

public class Resultat {

    private final Foedselsnummer fødselsnummer;
    private final String filbane;

    private Resultat(final Foedselsnummer fødselsnummer, final String filbane) {
        this.fødselsnummer = fødselsnummer;
        this.filbane = filbane;
    }

    public Foedselsnummer fødselsnummer() {
        return fødselsnummer;
    }

    public String filbane() {
        return filbane;
    }

    public boolean erGyldig() {
        return fødselsnummer.erGyldig();
    }

    public static Resultat resultat(final Foedselsnummer fødselsnummer, final String filbane) {
        return new Resultat(fødselsnummer, filbane);
    }

    public String filtrertOutput(final UtgangsInnstillinger utgangsInnstillinger) {
        String gyldighetString = "";
        if (utgangsInnstillinger.visGyldig()) {
            gyldighetString = ", er gyldig='" + fødselsnummer.erGyldig() + '\'';
        }

        String nestenGyldighetString =  "";
        if (utgangsInnstillinger.visNestenGyldig()) {
            nestenGyldighetString = ", er nesten gyldig='" + fødselsnummer.erNestenGyldig() + '\'';
        }

        String filbaneString = "";
        if (utgangsInnstillinger.visFilbane()) {
            filbaneString = ", filbane='" + filbane + '\'';
        }

        return "Resultat{" +
                "fødselsnummer='" + fødselsnummer.fødselsnummer() + '\'' +
                gyldighetString +
                nestenGyldighetString +
                filbaneString +
                '}';
    }

    @Override
    public String toString() {
        return "Resultat{" +
                fødselsnummer +
                ", filbane='" + filbane + '\'' +
                '}';
    }
}
