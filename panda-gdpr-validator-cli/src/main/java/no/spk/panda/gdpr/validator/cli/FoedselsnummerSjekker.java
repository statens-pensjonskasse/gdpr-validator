package no.spk.panda.gdpr.validator.cli;

import static no.spk.panda.gdpr.validator.cli.Resultat.resultat;
import static no.spk.panda.gdpr.validator.fnr.Foedselsnummer.foedslesnummer;

import java.util.List;
import java.util.stream.Collectors;

public class FoedselsnummerSjekker {

    public static List<Resultat> sjekk(final List<String> potensielleFødselsnummere, final String filbane) {
        return potensielleFødselsnummere
                .stream()
                .map(potensieltFødselsnummer -> resultat(foedslesnummer(potensieltFødselsnummer), filbane))
                .collect(Collectors.toUnmodifiableList());
    }
}
