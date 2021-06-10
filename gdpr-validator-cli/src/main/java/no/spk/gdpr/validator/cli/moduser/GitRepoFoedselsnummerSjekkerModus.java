package no.spk.gdpr.validator.cli.moduser;

import static java.util.Objects.requireNonNull;
import static no.spk.gdpr.validator.cli.util.Util.erGitUrl;
import static no.spk.gdpr.validator.cli.util.Util.klonRepo;

import java.io.FileNotFoundException;

import no.spk.gdpr.validator.cli.FantIkkeGitRepositoryException;

public class GitRepoFoedselsnummerSjekkerModus {

    private final LokalFoedselsnummerSjekkerModus lokalFoedselsnummerSjekkerModus;

    private GitRepoFoedselsnummerSjekkerModus(
            final LokalFoedselsnummerSjekkerModus lokalFoedselsnummerSjekkerModus
    ) {
        this.lokalFoedselsnummerSjekkerModus = requireNonNull(
                lokalFoedselsnummerSjekkerModus,
                "lokalFoedselsnummerSjekkerModus var påkrevd, men var null"
        );
    }

    public static GitRepoFoedselsnummerSjekkerModus gitRepoFoedselsnummerSjekkerModus(
            final LokalFoedselsnummerSjekkerModus lokalFoedselsnummerSjekkerModus
    ) {
        return new GitRepoFoedselsnummerSjekkerModus(lokalFoedselsnummerSjekkerModus);
    }

    public void sjekkEttRepo(final String bane) throws FileNotFoundException {
        if (erGitUrl(bane)) {
            klonRepo(bane);
            lokalFoedselsnummerSjekkerModus.kjør();
        } else {
            throw new FantIkkeGitRepositoryException(String.format("Du anga ikke et git-repository. Bane: %s", bane));
        }
    }
}
