package no.spk.gdpr.anonymisert.fnr;

import static java.util.Objects.requireNonNull;

import java.util.Random;

import no.spk.gdpr.validator.fnr.Foedselsnummer;

public class AnonymisertFoedselsnummer {

    private final String fødselsnummer;

    private AnonymisertFoedselsnummer(final String fødselsnummer) {
        this.fødselsnummer = requireNonNull(fødselsnummer, "fødselsnummer er påkrevd, men var null");
    }

    public static AnonymisertFoedselsnummer fraFoedselsnummer(final Foedselsnummer fødselsnummer) {
        requireNonNull(fødselsnummer, "fødselsnummer er påkrevd, men var null");
        return fraFoedselsnummer(fødselsnummer, 984654981);
    }

    public static AnonymisertFoedselsnummer fraFoedselsnummer(final Foedselsnummer fødselsnummer, final long seed) {
        requireNonNull(fødselsnummer, "fødselsnummer er påkrevd, men var null");

        final Random random = new Random(seed);
        return new AnonymisertFoedselsnummer(fødselsnummer.fødselsnummer());
    }

    public String fødselsnummer() {
        return fødselsnummer;
    }

    @Override
    public String toString() {
        return "anonymisert fødselsnummer='" + fødselsnummer + '\'';
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        final AnonymisertFoedselsnummer that = (AnonymisertFoedselsnummer) o;

        return fødselsnummer.equals(that.fødselsnummer);
    }

    @Override
    public int hashCode() {
        return fødselsnummer.hashCode();
    }
}
