package no.spk.panda.gdpr.validator.fnr;

import static no.spk.panda.gdpr.validator.fnr.Foedselsnummer.foedslesnummer;
import static no.spk.panda.gdpr.validator.fnr.ValidatorParametere.parametereForOrdinærValidator;
import static no.spk.panda.gdpr.validator.fnr.ValidatorParametere.parametereForKasperMedSemikolonValidator;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.Arrays;
import java.util.List;

import org.junit.Test;

public class FoedselsnummerTest {

    @SuppressWarnings({"ArraysAsListWithZeroOrOneArgument", "RedundantOperationOnEmptyContainer"})
    @Test
    public void skal_returnere_true_ved_gyldig_fødselsnummer_for_ordinær_fødselsnummervalidering() {
        // Hent fødselsnummere å teste med her: http://www.fnrinfo.no/Verktoy/FinnLovlige_Tilfeldig.aspx
        // Slett fødselsnummerene før commit.

        final List<String> gyldigeFødselsnummere = Arrays.asList();

        gyldigeFødselsnummere
                .forEach(fnr ->
                        assertThat(foedslesnummer(fnr, parametereForOrdinærValidator()).erNestenGyldig())
                                .isTrue()
                );
    }

    @SuppressWarnings({"ArraysAsListWithZeroOrOneArgument", "RedundantOperationOnEmptyContainer"})
    @Test
    public void skal_returnere_true_ved_gyldig_fødselsnummer_for_semikolon_fødselsnummervalidering() {
        // Hent fødselsnummere å teste med her: http://www.fnrinfo.no/Verktoy/FinnLovlige_Tilfeldig.aspx
        // Slett fødselsnummerene før commit.

        final List<String> gyldigeFødselsnummere = Arrays.asList();

        gyldigeFødselsnummere
                .forEach(fnr ->
                        assertThat(foedslesnummer(fnr, parametereForKasperMedSemikolonValidator()).erNestenGyldig())
                                .isTrue()
                );
    }

    @Test
    public void skal_returnere_false_ved_ugyldige_fødselsnummere() {
        final ValidatorParametere validatorParametere = parametereForOrdinærValidator();

        final List<String> ugyldigeFødselsnummere = Arrays.asList(
                "asd",
                "11111111111",
                "62130012345",
                "92121211111",
                "a1111111111",
                "00000000000"
        );

        ugyldigeFødselsnummere
                .forEach(fnr ->
                        assertThat(foedslesnummer(fnr, validatorParametere).erGyldig())
                                .isFalse()
                );

        ugyldigeFødselsnummere
                .forEach(fnr ->
                        assertThat(foedslesnummer(fnr, validatorParametere).erNestenGyldig())
                                .isFalse()
                );
    }
}