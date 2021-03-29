package no.spk.panda.gdpr.validator.fnr;

import static java.util.Objects.requireNonNull;
import static no.spk.panda.gdpr.validator.fnr.FoedselsnummerValidator.foedselsnummerValidator;

public class Foedselsnummer {

    private final String fødselsnummer;
    private final FoedselsnummerValidator validator;

    private Foedselsnummer(final String fødselsnummer, final ValidatorParametere validatorParametere) {
        this.fødselsnummer = requireNonNull(fødselsnummer, "fødselsnummer er påkrevd, men var null");
        validator = foedselsnummerValidator(fødselsnummer, validatorParametere);
    }

    public static Foedselsnummer foedslesnummer(final String fødselsnummer, final ValidatorParametere validatorParametere) {
        return new Foedselsnummer(fødselsnummer, validatorParametere);
    }

    public boolean erGyldig() {
        return validator.erGyldig();
    }

    public boolean erNestenGyldig() {
        return validator.erNestenGyldig();
    }

    @Override
    public String toString() {
        return "fødselsnummer='" + fødselsnummer + '\'' +
                " er gyldig='" + erGyldig() + '\'' +
                " er nesten gyldig='" + erNestenGyldig() + '\'';
    }
}
