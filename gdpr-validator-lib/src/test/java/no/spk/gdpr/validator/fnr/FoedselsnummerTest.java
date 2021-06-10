package no.spk.gdpr.validator.fnr;

import static no.spk.gdpr.validator.fnr.Foedselsnummer.foedslesnummer;
import static no.spk.gdpr.validator.fnr.ValidatorParametere.parametereForKasperValidator;
import static no.spk.gdpr.validator.fnr.ValidatorParametere.parametereForOrdinærValidator;
import static no.spk.gdpr.validator.fnr.ValidatorParametere.parametereForKasperMedSemikolonValidator;
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

    @Test
    public void skal_være_et_verdiobjekt() {
        final ValidatorParametere validatorParametere = parametereForOrdinærValidator();

        assertThat(
                foedslesnummer("11111111111", validatorParametere)
        )
                .isEqualTo(
                        foedslesnummer("11111111111", validatorParametere)
                );

        assertThat(
                foedslesnummer("11111111111", validatorParametere)
        )
                .isNotEqualTo(
                        foedslesnummer("11111111112", validatorParametere)
                );
    }

    @Test
    public void skal_returnere_fødselsdato() {
        final ValidatorParametere validatorParametereForOrdinær = parametereForOrdinærValidator();

        assertThat(
                foedslesnummer("11111122222", validatorParametereForOrdinær)
                        .fødselsdato()
        )
                .isEqualTo(
                        "111111"
                );

        final ValidatorParametere validatorParametereForKasper = parametereForKasperValidator();

        assertThat(
                foedslesnummer("3311111122222", validatorParametereForKasper)
                        .fødselsdato()
        )
                .isEqualTo(
                        "33111111"
                );

        final ValidatorParametere validatorParametereForKasperMedSemikolon = parametereForKasperMedSemikolonValidator();

        assertThat(
                foedslesnummer("33111111;22222", validatorParametereForKasperMedSemikolon)
                        .fødselsdato()
        )
                .isEqualTo(
                        "33111111"
                );
    }

    @Test
    public void skal_returnere_personnummer() {
        final ValidatorParametere validatorParametereForOrdinær = parametereForOrdinærValidator();

        assertThat(
                foedslesnummer("11111122222", validatorParametereForOrdinær)
                        .personnummer()
        )
                .isEqualTo(
                        "22222"
                );

        final ValidatorParametere validatorParametereForKasper = parametereForKasperValidator();

        assertThat(
                foedslesnummer("3311111122222", validatorParametereForKasper)
                        .personnummer()
        )
                .isEqualTo(
                        "22222"
                );

        final ValidatorParametere validatorParametereForKasperMedSemikolon = parametereForKasperMedSemikolonValidator();

        assertThat(
                foedslesnummer("33111111;22222", validatorParametereForKasperMedSemikolon)
                        .personnummer()
        )
                .isEqualTo(
                        "22222"
                );
    }

    @Test
    public void skal_ha_menneskevennlig_toString() {
        final ValidatorParametere validatorParametere = parametereForKasperMedSemikolonValidator();
        final Foedselsnummer fødselsnummer = foedslesnummer("33111111;22222", validatorParametere);
        assertThat(
                fødselsnummer
                        .toString()
        )
                .isEqualTo("fødselsnummer='33111111;22222' er gyldig='false' er nesten gyldig='false'");
    }
}
