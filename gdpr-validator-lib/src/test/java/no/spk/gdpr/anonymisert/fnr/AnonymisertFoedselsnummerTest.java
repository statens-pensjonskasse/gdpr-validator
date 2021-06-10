package no.spk.gdpr.anonymisert.fnr;

import static no.spk.gdpr.validator.fnr.Foedselsnummer.foedslesnummer;
import static no.spk.gdpr.validator.fnr.ValidatorParametere.parametereForKasperMedSemikolonValidator;
import static no.spk.gdpr.validator.fnr.ValidatorParametere.parametereForKasperValidator;
import static no.spk.gdpr.validator.fnr.ValidatorParametere.parametereForOrdinærValidator;
import static org.assertj.core.api.Assertions.assertThat;

import no.spk.gdpr.validator.fnr.Foedselsnummer;
import no.spk.gdpr.validator.fnr.ValidatorParametere;

import org.junit.Test;

public class AnonymisertFoedselsnummerTest {

    @Test
    public void skal_være_et_verdiobjekt() {
        final ValidatorParametere validatorParametere = parametereForOrdinærValidator();

        assertThat(
                AnonymisertFoedselsnummer.fraFoedselsnummer(
                        foedslesnummer("11111111111", validatorParametere),
                        validatorParametere
                )
        )
                .isEqualTo(
                        AnonymisertFoedselsnummer.fraFoedselsnummer(
                                foedslesnummer("11111111111", validatorParametere),
                                validatorParametere
                        )
                );

        assertThat(
                AnonymisertFoedselsnummer.fraFoedselsnummer(
                        foedslesnummer("11111111111", validatorParametere),
                        validatorParametere
                )
        )
                .isNotEqualTo(
                        AnonymisertFoedselsnummer.fraFoedselsnummer(
                                foedslesnummer("11111111112", validatorParametere),
                                validatorParametere
                        )
                );
    }

    @Test
    public void skal_være_like_langt_unanonymt_ordinært_fødselsnummer() {
        final ValidatorParametere validatorParametere = parametereForOrdinærValidator();
        final Foedselsnummer unanonymtFødselsnummer = foedslesnummer("11111111111", validatorParametere);
        assertThat(
                AnonymisertFoedselsnummer.fraFoedselsnummer(
                        unanonymtFødselsnummer,
                        validatorParametere
                )
                        .fødselsnummer()
                        .length()
        )
                .isEqualTo(
                        unanonymtFødselsnummer
                                .fødselsnummer()
                                .length()
                );
    }

    @Test
    public void skal_være_like_langt_unanonymt_kasper_fødselsnummer() {
        final ValidatorParametere validatorParametere = parametereForKasperValidator();
        final Foedselsnummer unanonymtFødselsnummer = foedslesnummer("3311111122222", validatorParametere);
        assertThat(
                AnonymisertFoedselsnummer.fraFoedselsnummer(
                        unanonymtFødselsnummer,
                        validatorParametere
                )
                        .fødselsnummer()
                        .length()
        )
                .isEqualTo(
                        unanonymtFødselsnummer
                                .fødselsnummer()
                                .length()
                );
    }

    @Test
    public void skal_være_like_langt_unanonymt_kasper_med_semikolon_fødselsnummer() {
        final ValidatorParametere validatorParametere = parametereForKasperMedSemikolonValidator();
        final Foedselsnummer unanonymtFødselsnummer = foedslesnummer("33111111;22222", validatorParametere);
        assertThat(
                AnonymisertFoedselsnummer.fraFoedselsnummer(
                        unanonymtFødselsnummer,
                        validatorParametere
                )
                        .fødselsnummer()
                        .length()
        )
                .isEqualTo(
                        unanonymtFødselsnummer
                                .fødselsnummer()
                                .length()
                );
    }
}
