package no.spk.gdpr.validator.cli.moduser;

import static java.util.Objects.requireNonNull;
import static no.spk.gdpr.validator.cli.util.Util.erGitUrl;
import static no.spk.gdpr.validator.cli.util.Util.klonRepo;

import java.io.FileNotFoundException;

import no.spk.gdpr.validator.cli.FantIkkeGitRepositoryException;

public class GitRepoHeleHistorienFoedselsnummerSjekkerModus {

    private final LokalFoedselsnummerSjekkerHeleHistorienModus lokalFoedselsnummerSjekkerHeleHistorienModus;

    private GitRepoHeleHistorienFoedselsnummerSjekkerModus(
            final LokalFoedselsnummerSjekkerHeleHistorienModus lokalFoedselsnummerSjekkerModus
    ) {
        this.lokalFoedselsnummerSjekkerHeleHistorienModus = requireNonNull(
                lokalFoedselsnummerSjekkerModus,
                "lokalFoedselsnummerSjekkerHeleHistorienModus var påkrevd, men var null"
        );
    }

    public static GitRepoHeleHistorienFoedselsnummerSjekkerModus gitRepoHeleHistorienFoedselsnummerSjekkerModus(
            final LokalFoedselsnummerSjekkerHeleHistorienModus lokalFoedselsnummerSjekkerModus
    ) {
        return new GitRepoHeleHistorienFoedselsnummerSjekkerModus(lokalFoedselsnummerSjekkerModus);
    }

    public void sjekkEttRepo(final String bane) throws FileNotFoundException {
        if (erGitUrl(bane)) {
            klonRepo(bane);
            lokalFoedselsnummerSjekkerHeleHistorienModus.kjør();
        } else {
            throw new FantIkkeGitRepositoryException(String.format("Du anga ikke et git-repository. Bane: %s", bane));
        }
    }
}
