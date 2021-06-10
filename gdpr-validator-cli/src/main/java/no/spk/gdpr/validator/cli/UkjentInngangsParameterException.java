package no.spk.gdpr.validator.cli;

import static java.util.Objects.requireNonNull;

class UkjentInngangsParameterException extends RuntimeException {

    private static final long serialVersionUID = 0L;
    private final String feilmelding;

    public UkjentInngangsParameterException(final String feilmelding) {
        this.feilmelding = requireNonNull(feilmelding, "feilmelding er påkrevd, men var null");
    }

    public UkjentInngangsParameterException(final String feilmelding, final Throwable cause) {
        super(cause);
        this.feilmelding = requireNonNull(feilmelding, "feilmelding er påkrevd, men var null");
    }

    @Override
    public String getMessage() {
        return feilmelding;
    }
}
