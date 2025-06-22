package no.spk.gdpr.validator.fnr;

import static java.util.Objects.requireNonNull;

public class Foedselsnummer {

    private final String fødselsnummer;
    private final FoedselsnummerValidator validator;

    private Foedselsnummer(final String fødselsnummer, final ValidatorParametere validatorParametere) {
        this.fødselsnummer = requireNonNull(fødselsnummer, "fødselsnummer er påkrevd, men var null");
        validator = FoedselsnummerValidator.foedselsnummerValidator(fødselsnummer, validatorParametere);
    }

    public static Foedselsnummer foedselsnummer(final String fødselsnummer, final ValidatorParametere validatorParametere) {
        return new Foedselsnummer(fødselsnummer, validatorParametere);
    }

    public boolean erGyldig() {
        return validator.erGyldig();
    }

    public boolean erNestenGyldig() {
        return validator.erNestenGyldig();
    }

    public String fødselsnummer() {
        return fødselsnummer;
    }

    public String fødselsdato() {
        return validator.fødselsdato();
    }

    public String personnummer() {
        return validator.personnummer();
    }

    @Override
    public String toString() {
        return "fødselsnummer='" + fødselsnummer + '\'' +
                " er gyldig='" + erGyldig() + '\'' +
                " er nesten gyldig='" + erNestenGyldig() + '\'';
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        final Foedselsnummer that = (Foedselsnummer) o;

        return fødselsnummer.equals(that.fødselsnummer);
    }

    @Override
    public int hashCode() {
        return fødselsnummer.hashCode();
    }
}
