package no.spk.panda.gdpr.validator.cli;

import static java.util.Objects.requireNonNull;
import static no.spk.panda.gdpr.validator.cli.GitRepoFoedselsnummerSjekkerModus.gitRepoFoedselsnummerSjekkerModus;
import static no.spk.panda.gdpr.validator.cli.LokalFoedselsnummerSjekkerModus.lokalFoedselsnummerSjekkerModus;
import static no.spk.panda.gdpr.validator.cli.Util.repositorynavn;

import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.stream.Collectors;

import no.spk.panda.gdpr.validator.cli.dtos.bitbucket.RepoesDto;
import no.spk.panda.gdpr.validator.fnr.ValidatorParametere;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

class GitRepoerFoedselsnummerSjekkerModus {

    private static final String bitbucketApiUrl = "http://git.spk.no/rest/api/1.0/projects/${prosjekt}/repos?limit=100";

    private final List<String> filtyper;
    private final ValidatorParametere validatorParametere;

    private GitRepoerFoedselsnummerSjekkerModus(final List<String> filtyper, final ValidatorParametere validatorParametere) {
        this.filtyper = filtyper;
        this.validatorParametere = validatorParametere;
    }

    public static GitRepoerFoedselsnummerSjekkerModus gitRepoerFoedselsnummerSjekkerModus(
            final List<String> filtyper,
            final ValidatorParametere validatorParametere
    ) {
        return new GitRepoerFoedselsnummerSjekkerModus(filtyper, validatorParametere);
    }


    public void sjekkMangeRepoer(final String prosjekt) throws IOException {
        final List<GitRepo> repoer = hentRepoerDataFraApi(prosjekt);

        for (GitRepo repo : repoer) {
            gitRepoFoedselsnummerSjekkerModus(
                    lokalFoedselsnummerSjekkerModus(repositorynavn(repo.url()), filtyper, validatorParametere)
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
                    .collect(Collectors.toUnmodifiableList());
        }
    }

    private static class GitRepo {
        private final String navn;
        private final String url;

        private GitRepo(final String navn, final String url) {
            this.navn = navn;
            this.url = url;
        }

        public static GitRepo gitRepo(final String navn, final String url) {
            return new GitRepo(navn, url);
        }

        public String navn() {
            return navn;
        }

        public String url() {
            return url;
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
