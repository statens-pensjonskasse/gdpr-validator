package no.spk.panda.gdpr.validator.cli;

import static java.util.Objects.requireNonNull;
import static no.spk.panda.gdpr.validator.cli.KommandoHjelper.kommandoHjelper;
import static no.spk.panda.gdpr.validator.cli.Util.filetternavn;
import static no.spk.panda.gdpr.validator.cli.Util.repositorynavn;

import java.io.FileNotFoundException;

import org.assertj.core.util.Lists;

class GitRepoFoedselsnummerSjekkerModus {

    private final LokaleKatalogerFoedselsnummerSjekkerModus lokaleKatalogerFoedselsnummerSjekkerModus;

    private GitRepoFoedselsnummerSjekkerModus(
            final LokaleKatalogerFoedselsnummerSjekkerModus lokaleKatalogerFoedselsnummerSjekkerModus
    ) {
        this.lokaleKatalogerFoedselsnummerSjekkerModus = requireNonNull(
                lokaleKatalogerFoedselsnummerSjekkerModus,
                "lokaleKatalogerFoedselsnummerSjekkerModus var påkrevd, men var null"
        );
    }

    public static GitRepoFoedselsnummerSjekkerModus gitRepoFoedselsnummerSjekkerModus(
            final LokaleKatalogerFoedselsnummerSjekkerModus lokaleKatalogerFoedselsnummerSjekkerModus
    ) {
        return new GitRepoFoedselsnummerSjekkerModus(lokaleKatalogerFoedselsnummerSjekkerModus);
    }

    public void sjekkEttRepo(final String bane) throws FileNotFoundException {
        if (erGitUrl(bane)) {
            klonRepo(bane);
            lokaleKatalogerFoedselsnummerSjekkerModus.kjør();
        } else {
            throw new FantIkkeGitRepositoryException(String.format("Du anga ikke et git-repository. Bane: %s", bane));
        }
    }

    private boolean erGitUrl(final String bane) {
        return filetternavn(bane).equals("git") &&
                (bane.contains("http://") || bane.contains("https://") || bane.contains("git://") || bane.contains("ssh://"));
    }

    private void klonRepo(final String url) {
        kommandoHjelper().kjoerKommando(Lists.newArrayList("git", "clone", url, repositorynavn(url)));
    }
}
