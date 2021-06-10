package no.spk.gdpr.anonymisert.fnr;

import static java.util.Objects.requireNonNull;
import static org.apache.commons.codec.digest.DigestUtils.md5Hex;

import no.spk.gdpr.validator.fnr.Foedselsnummer;
import no.spk.gdpr.validator.fnr.ValidatorParametere;

public class AnonymisertFoedselsnummer {

    private final String fødselsnummer;

    private AnonymisertFoedselsnummer(final String fødselsnummer) {
        this.fødselsnummer = requireNonNull(fødselsnummer, "fødselsnummer er påkrevd, men var null");
    }

    public static AnonymisertFoedselsnummer fraFoedselsnummer(
            final Foedselsnummer fødselsnummer,
            final ValidatorParametere validatorParametere
    ) {
        requireNonNull(fødselsnummer, "fødselsnummer er påkrevd, men var null");
        requireNonNull(validatorParametere, "validator-parametere er påkrevd, men var null");

        final String fødselsnummerFørPersonnummer = fødselsnummer.fødselsnummer().substring(0, validatorParametere.personnummerStart());
        final String anonymisertPersonnummer = anonymiserPersonnummer(fødselsnummer.personnummer());

        return new AnonymisertFoedselsnummer(fødselsnummerFørPersonnummer + anonymisertPersonnummer);
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

    private static String anonymiserPersonnummer(final String personnummer) {
        return md5Hex(personnummer).substring(0, personnummer.length());
    }
}
