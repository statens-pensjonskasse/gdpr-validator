package no.spk.gdpr.validator.cli;

import static java.util.function.Predicate.not;

import java.time.LocalDateTime;
import java.util.List;

public class Oppsummering {
    final long fødselsnummer;
    final long gyldigFødselsnummer;
    final long nestenGyldigFødselsnummer;
    final long ugyldigFødselsnummer;

    private Oppsummering(final long fødselsnummer, final long gyldigFødselsnummer, final long nestenGyldigFødselsnummer, final long ugyldigFødselsnummer) {
        this.fødselsnummer = fødselsnummer;
        this.gyldigFødselsnummer = gyldigFødselsnummer;
        this.nestenGyldigFødselsnummer = nestenGyldigFødselsnummer;
        this.ugyldigFødselsnummer = ugyldigFødselsnummer;
    }

    public static Oppsummering initier() {
        return new Oppsummering(0, 0, 0, 0);
    }

    @Override
    public String toString() {
        String oppsummering = "Oppsummering:\n\n";

        oppsummering += String.format("Avsluttet %s\n\n", LocalDateTime.now());
        oppsummering += String.format("Fant %d fødselsnummere:\n", fødselsnummer);
        oppsummering += String.format("\t- %d er gyldig(e)\n", gyldigFødselsnummer);
        oppsummering += String.format("\t- %d er nesten gyldig(e)\n", nestenGyldigFødselsnummer);
        oppsummering += String.format("\t- %d er ugyldig(e)\n", ugyldigFødselsnummer);

        oppsummering += "\n\n";

        return oppsummering;
    }

    public static Oppsummering lagOppsummering(final List<Resultat> resultater) {
        return new Oppsummering(
                resultater.size(),
                resultater.stream().filter(f -> f.fødselsnummer().erGyldig()).count(),
                resultater.stream().filter(f -> f.fødselsnummer().erNestenGyldig()).count(),
                resultater.stream().filter(not(f -> f.fødselsnummer().erNestenGyldig())).count()
        );
    }

    public Oppsummering pluss(final Oppsummering other) {
        return new Oppsummering(
                this.fødselsnummer + other.fødselsnummer,
                this.gyldigFødselsnummer + other.gyldigFødselsnummer,
                this.nestenGyldigFødselsnummer + other.nestenGyldigFødselsnummer,
                this.ugyldigFødselsnummer + other.ugyldigFødselsnummer
        );
    }
}
