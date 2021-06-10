package no.spk.panda.gdpr.validator.cli;

import static java.util.Objects.requireNonNull;

public class IkkeMedlemsdatafilException extends RuntimeException {

    private static final long serialVersionUID = 0L;
    private final String filnavn;

    public IkkeMedlemsdatafilException(final String filnavn) {
        this.filnavn = requireNonNull(filnavn, "filnavn er påkrevd, men var null");
    }

    public IkkeMedlemsdatafilException(final String filnavn, final Throwable cause) {
        super(cause);
        this.filnavn = requireNonNull(filnavn, "filnavn er påkrevd, men var null");
    }

    @Override
    public String getMessage() {
        return String.format("Du anga %s, som ikke er medlemsdata.csv.", filnavn);
    }
}
