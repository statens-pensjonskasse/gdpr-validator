package no.spk.gdpr.validator.cli.moduser;

import static java.util.Objects.requireNonNull;

import java.io.IOException;
import java.net.URL;
import java.util.List;

import no.spk.gdpr.validator.cli.FantIkkeGitRepositoryException;
import no.spk.gdpr.validator.cli.UtgangsInnstillinger;
import no.spk.gdpr.validator.cli.dtos.bitbucket.RepoesDto;
import no.spk.gdpr.validator.cli.util.Util;
import no.spk.gdpr.validator.fnr.ValidatorParametere;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class GitRepoerFoedselsnummerSjekkerModus {

    private static final String bitbucketApiUrl = "https://git.spk.no/rest/api/1.0/projects/${prosjekt}/repos?limit=100";

    private final List<String> filtyper;
    private final ValidatorParametere validatorParametere;
    private final UtgangsInnstillinger utgangsInnstillinger;

    private GitRepoerFoedselsnummerSjekkerModus(
            final List<String> filtyper,
            final ValidatorParametere validatorParametere,
            final UtgangsInnstillinger utgangsInnstillinger
    ) {
        this.filtyper = filtyper;
        this.validatorParametere = validatorParametere;
        this.utgangsInnstillinger = utgangsInnstillinger;
    }

    public static GitRepoerFoedselsnummerSjekkerModus gitRepoerFoedselsnummerSjekkerModus(
            final List<String> filtyper,
            final ValidatorParametere validatorParametere,
            final UtgangsInnstillinger utgangsInnstillinger
    ) {
        return new GitRepoerFoedselsnummerSjekkerModus(filtyper, validatorParametere, utgangsInnstillinger);
    }

    public void sjekkMangeRepoer(final String prosjekt) throws IOException {
        final List<GitRepo> repoer = hentRepoerDataFraApi(prosjekt);

        for (final GitRepo repo : repoer) {
            GitRepoFoedselsnummerSjekkerModus.gitRepoFoedselsnummerSjekkerModus(
                    LokalFoedselsnummerSjekkerModus.lokalFoedselsnummerSjekkerModus(Util.repositorynavn(repo.url()), filtyper, validatorParametere, utgangsInnstillinger)
            ).sjekkEttRepo(repo.url());
        }
    }

    private List<GitRepo> hentRepoerDataFraApi(final String prosjekt) throws IOException {
        final URL prosjekterUrl = new URL(bitbucketApiUrl.replace("${prosjekt}", prosjekt));
        final OkHttpClient client = new OkHttpClient();
        final Request request = new Request.Builder().url(prosjekterUrl).build();

        try (final Response response = client.newCall(request).execute()) {
            final String json = requireNonNull(response.body()).string();
            final RepoesDto repoesDto = new ObjectMapper()
                    .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
                    .readValue(json, RepoesDto.class);

            return repoesDto
                    .values
                    .stream()
                    .map(v -> GitRepo.gitRepo(
                                    v.name,
                                    v.links.clone
                                            .stream()
                                            .filter(l -> l.name.equals("http"))
                                            .findFirst()
                                            .orElseThrow(() -> new FantIkkeGitRepositoryException("Fant ikke http-repo"))
                                            .href
                            )
                    )
                    .toList();
        }
    }

    private record GitRepo(String navn, String url) {

        public static GitRepo gitRepo(final String navn, final String url) {
            return new GitRepo(navn, url);
        }

        @Override
        public String toString() {
            return "GitRepo{" +
                    "navn='" + navn + '\'' +
                    ", url='" + url + '\'' +
                    '}';
        }
    }
}
