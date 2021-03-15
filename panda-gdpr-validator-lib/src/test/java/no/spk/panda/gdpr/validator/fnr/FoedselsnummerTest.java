package no.spk.panda.gdpr.validator.fnr;

import static no.spk.panda.gdpr.validator.fnr.Foedselsnummer.foedslesnummer;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.Arrays;
import java.util.List;

import org.junit.Test;

public class FoedselsnummerTest {

    @Test
    public void skal_returnere_true_ved_gyldig_fødselsnummere() {
        // Hent fødselsnummere å teste med her: http://www.fnrinfo.no/Verktoy/FinnLovlige_Tilfeldig.aspx
        // Slett fødselsnummerene før commit.

        List<String> gyldigeFødselsnummere = Arrays.asList();

        gyldigeFødselsnummere
                .forEach(fnr ->
                        assertThat(foedslesnummer(fnr).erNestenGyldig())
                                .isTrue()
                );
    }

    @Test
    public void skal_returnere_false_ved_ugyldige_fødselsnummere() {
        List<String> ugyldigeFødselsnummere = Arrays.asList(
                "asd",
                "11111111111",
                "62130012345",
                "12121211111",
                "a1111111111"
        );

        ugyldigeFødselsnummere
                .forEach(fnr ->
                        assertThat(foedslesnummer(fnr).erGyldig())
                                .isFalse()
                );

        ugyldigeFødselsnummere
                .forEach(fnr ->
                        assertThat(foedslesnummer(fnr).erNestenGyldig())
                                .isFalse()
                );
    }
}