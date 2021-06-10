package no.spk.gdpr.validator.fnr;

import static java.util.Objects.requireNonNull;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Optional;

public class FoedselsnummerValidator {

    private final ValidatorParametere parametere;
    private final String fødselsnummer;
    private final Optional<String> datoDel;
    private final Optional<String> individnummerDel;
    private final Optional<String> sjekksumDel;

    private FoedselsnummerValidator(final String fødselsnummer, final ValidatorParametere parametere) {
        this.fødselsnummer = requireNonNull(fødselsnummer, "fødselsnummer er påkrevd, men var null");
        this.parametere = requireNonNull(parametere, "parametere er påkrevd, men var null");

        if (this.fødselsnummer.length() == parametere.fødselsnummerLengde()) {
            this.datoDel = Optional.of(fødselsnummer.substring(parametere.datoStart(), parametere.datoSlutt()));
            this.individnummerDel = Optional.of(fødselsnummer.substring(parametere.individnummerStart(), parametere.individnummerSlutt()));
            this.sjekksumDel = Optional.of(fødselsnummer.substring(parametere.sjekksumStart(), parametere.sjekksumSlutt()));
        } else {
            this.datoDel = Optional.empty();
            this.individnummerDel = Optional.empty();
            this.sjekksumDel = Optional.empty();
        }
    }

    public static FoedselsnummerValidator foedselsnummerValidator(final String fødselsnummer, final ValidatorParametere parametere) {
        return new FoedselsnummerValidator(fødselsnummer, parametere);
    }

    public boolean erGyldig() {
        return erRiktigLengde() && erGyldigFoedselsdato() && erGyldigIndividnummer(true) && erGyldigSjekksum();
    }

    public boolean erNestenGyldig() {
        return erRiktigLengde() && erGyldigFoedselsdato() && erGyldigIndividnummer(false) && erGyldigSjekksum();
    }

    private boolean erRiktigLengde() {
        return fødselsnummer.length() == parametere.fødselsnummerLengde();
    }

    private boolean erGyldigFoedselsdato() {
        try {
            datoFraStreng(datoDel.orElse(""), parametere.fødselsdatoMønster());
        } catch (final Exception ex) {
            return false;
        }

        return true;
    }

    private boolean erGyldigIndividnummer(final boolean erSjekkenStreng) {
        try {
            final int individnummer = Integer.parseInt(individnummerDel.orElseThrow(NumberFormatException::new));
            final Date fødselsdato = datoFraStreng(datoDel.orElseThrow(IllegalStateException::new), parametere.fødselsdatoMønster());

            // Egentlig skal fødselsnummere ha individnummere innenfor visse spenn, avhengig av fødselsår. Men ikke alle
            // fødselsnummergeneratorer følger denne standaren. Man kan slå av denne sjekken med flagget erSjekkenStreng.
            if (erSjekkenStreng) {
                if (fødselsdato.after(datoFraStreng("1854-01-01")) && fødselsdato.before(datoFraStreng("1899-01-01"))) {
                    if (!(500 <= individnummer && individnummer <= 749)) {
                        return false;
                    }
                } else if (fødselsdato.after(datoFraStreng("1900-01-01")) && fødselsdato.before(datoFraStreng("1999-01-01"))) {
                    if (!(0 <= individnummer && individnummer <= 499)) {
                        return false;
                    }
                } else if (fødselsdato.after(datoFraStreng("2000-01-01")) && fødselsdato.before(datoFraStreng("2039-01-01"))) {
                    if (!(500 <= individnummer && individnummer <= 999)) {
                        return false;
                    }
                }
            }
        } catch (final Exception ex) {
            return false;
        }

        return true;
    }

    private boolean erGyldigSjekksum() {
        try {
            final int d1Idx = parametere.fødselsdatoMønster().indexOf('d');
            final int d2Idx = parametere.fødselsdatoMønster().lastIndexOf('d');
            final int d1 = Integer.parseInt(datoDel.orElseThrow(Exception::new).substring(d1Idx, d1Idx + 1));
            final int d2 = Integer.parseInt(datoDel.orElseThrow(Exception::new).substring(d2Idx, d2Idx + 1));

            final int m1Idx = parametere.fødselsdatoMønster().indexOf('M');
            final int m2Idx = parametere.fødselsdatoMønster().lastIndexOf('M');
            final int m1 = Integer.parseInt(datoDel.orElseThrow(Exception::new).substring(m1Idx, m1Idx + 1));
            final int m2 = Integer.parseInt(datoDel.orElseThrow(Exception::new).substring(m2Idx, m2Idx + 1));

            final int y2Idx = parametere.fødselsdatoMønster().lastIndexOf('y');
            final int y1Idx = y2Idx - 1;
            final int y1 = Integer.parseInt(datoDel.orElseThrow(Exception::new).substring(y1Idx, y1Idx + 1));
            final int y2 = Integer.parseInt(datoDel.orElseThrow(Exception::new).substring(y2Idx, y2Idx + 1));

            final int i1 = Integer.parseInt(individnummerDel.orElseThrow(Exception::new).substring(0, 1));
            final int i2 = Integer.parseInt(individnummerDel.orElseThrow(Exception::new).substring(1, 2));
            final int i3 = Integer.parseInt(individnummerDel.orElseThrow(Exception::new).substring(2, 3));

            int cs1 = 11 - ((3 * d1 + 7 * d2 + 6 * m1 + m2 + 8 * y1 + 9 * y2 + 4 * i1 + 5 * i2 + 2 * i3) % 11);
            int cs2 = 11 - ((5 * d1 + 4 * d2 + 3 * m1 + 2 * m2 + 7 * y1 + 6 * y2 + 5 * i1 + 4 * i2 + 3 * i3 + 2 * cs1) % 11);

            String reellSjekksum;

            if (cs1 == 11 && cs2 == 11) {
                reellSjekksum = "00";
            } else if (cs1 == 11) {
                reellSjekksum = String.format("0%d", cs2);
            } else if (cs2 == 11) {
                reellSjekksum = String.format("%d0", cs1);
            } else {
                reellSjekksum = String.format("%d%d", cs1, cs2);
            }

            final String påståttSjekksum = sjekksumDel.orElseThrow(NumberFormatException::new);

            return påståttSjekksum.equals(reellSjekksum);
        } catch (final Exception ex) {
            return false;
        }
    }

    private static Date datoFraStreng(final String dato) throws ParseException {
        SimpleDateFormat date = new SimpleDateFormat("yyyy-MM-dd");
        date.setLenient(false);
        return date.parse(dato);
    }

    private static Date datoFraStreng(final String dato, final String pattern) throws ParseException {
        SimpleDateFormat date = new SimpleDateFormat(pattern);
        date.setLenient(false);
        return date.parse(dato);
    }
}