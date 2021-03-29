package no.spk.panda.gdpr.validator.cli;

import static java.util.Objects.requireNonNull;
import static no.spk.panda.gdpr.validator.cli.Util.erGitUrl;
import static no.spk.panda.gdpr.validator.cli.Util.klonRepo;

import java.io.FileNotFoundException;

class GitRepoFoedselsnummerSjekkerModus {

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
