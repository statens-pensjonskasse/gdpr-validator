package no.spk.panda.gdpr.validator.cli;

public class UtgangsInnstillinger {

    private final boolean visOppsummering;
    private final boolean visGyldig;
    private final boolean visNestenGyldig;
    private final boolean visFilbane;

    private UtgangsInnstillinger(
            final boolean visOppsummering,
            final boolean visGyldig,
            final boolean visNestenGyldig,
            final boolean visFilbane
    ) {
        this.visOppsummering = visOppsummering;
        this.visGyldig = visGyldig;
        this.visNestenGyldig = visNestenGyldig;
        this.visFilbane = visFilbane;
    }

    public static UtgangsInnstillinger utgangsInnstillinger(
            final boolean visOppsummering,
            final boolean visGyldig,
            final boolean visNestenGyldig,
            final boolean visFilbane
    ) {
        return new UtgangsInnstillinger(visOppsummering, visGyldig, visNestenGyldig, visFilbane);
    }

    public static UtgangsInnstillinger visAlleUtgangsvariabler() {
        return new UtgangsInnstillinger(true, true, true, true);
    }

    public boolean visOppsummering() {
        return visOppsummering;
    }

    public boolean visGyldig() {
        return visGyldig;
    }

    public boolean visNestenGyldig() {
        return visNestenGyldig;
    }

    public boolean visFilbane() {
        return visFilbane;
    }
}
