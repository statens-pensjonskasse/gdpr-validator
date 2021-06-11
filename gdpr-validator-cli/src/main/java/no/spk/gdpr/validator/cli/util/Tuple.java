package no.spk.gdpr.validator.cli.util;

public class Tuple<T, U> {

    private final T første;
    private final U andre;

    public Tuple(T første, U andre) {
        this.første = første;
        this.andre = andre;
    }

    public T første() {
        return første;
    }

    public U andre() {
        return andre;
    }
}
