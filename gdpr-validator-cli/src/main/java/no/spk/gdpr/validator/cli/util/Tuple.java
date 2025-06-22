package no.spk.gdpr.validator.cli.util;

public class Tuple<T, U> {

    private final T første;
    private final U andre;

    public Tuple(T første, U andre) {
        this.første = første;
        this.andre = andre;
    }

    public static <T, U> Tuple<T, U> tuple(T første, U andre) {
        return new Tuple<>(første, andre);
    }

    public T første() {
        return første;
    }

    public U andre() {
        return andre;
    }
}
