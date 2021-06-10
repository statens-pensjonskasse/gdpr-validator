package no.spk.panda.gdpr.validator.cli;

import java.time.LocalDateTime;
import java.util.List;

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

    public static String lagOppsummering(final List<Resultat> resultater) {
        String oppsummering = "Oppsummering:\n\n";

        oppsummering += String.format("Avsluttet %s\n\n", LocalDateTime.now());
        oppsummering += String.format("Fant %d fødselsnummere:\n", resultater.size());
        oppsummering += String.format("\t- %d er gyldig(e)\n", resultater.stream().filter(f -> f.fødselsnummer.erGyldig()).count());
        oppsummering += String.format("\t- %d er nesten gyldig(e)\n", resultater.stream().filter(f -> f.fødselsnummer.erNestenGyldig()).count());
        oppsummering += String.format("\t- %d er ugyldig(e)\n", resultater.stream().filter(f -> !f.fødselsnummer.erNestenGyldig()).count());

        oppsummering += "\n\n";

        return oppsummering;
    }

    @Override
    public String toString() {
        return "Resultat{" +
                fødselsnummer +
                ", filbane='" + filbane + '\'' +
                '}';
    }
}
