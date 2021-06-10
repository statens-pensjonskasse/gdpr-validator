package no.spk.gdpr.anonymisert.fnr;

import static no.spk.gdpr.validator.fnr.Foedselsnummer.foedslesnummer;
import static no.spk.gdpr.validator.fnr.ValidatorParametere.parametereForOrdinærValidator;
import static org.assertj.core.api.Assertions.assertThat;

import no.spk.gdpr.validator.fnr.ValidatorParametere;

import org.junit.Test;

public class AnonymisertFoedselsnummerTest {

    @Test
    public void skal_være_et_verdiobjekt() {
        final ValidatorParametere validatorParametere = parametereForOrdinærValidator();

        assertThat(
                AnonymisertFoedselsnummer.fraFoedselsnummer(
                        foedslesnummer("11111111111", validatorParametere)
                )
        )
                .isEqualTo(
                        AnonymisertFoedselsnummer.fraFoedselsnummer(
                                foedslesnummer("11111111111", validatorParametere)
                        )
                );

        assertThat(
                AnonymisertFoedselsnummer.fraFoedselsnummer(
                        foedslesnummer("11111111111", validatorParametere)
                )
        )
                .isNotEqualTo(
                        AnonymisertFoedselsnummer.fraFoedselsnummer(
                                foedslesnummer("11111111112", validatorParametere)
                        )
                );
    }

}