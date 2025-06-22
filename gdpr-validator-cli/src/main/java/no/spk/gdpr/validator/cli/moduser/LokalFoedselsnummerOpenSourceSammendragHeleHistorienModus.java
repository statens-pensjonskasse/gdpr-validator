package no.spk.gdpr.validator.cli.moduser;

import static java.lang.String.format;
import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.summingInt;
import static java.util.stream.Collectors.teeing;
import static java.util.stream.StreamSupport.stream;
import static no.spk.gdpr.validator.cli.util.Tuple.tuple;
import static no.spk.gdpr.validator.fnr.Foedselsnummer.foedselsnummer;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.time.LocalDateTime;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import no.spk.gdpr.validator.cli.util.Tuple;
import no.spk.gdpr.validator.fnr.ValidatorParametere;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.diff.DiffEntry;
import org.eclipse.jgit.diff.DiffFormatter;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.ObjectReader;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevTree;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;
import org.eclipse.jgit.treewalk.AbstractTreeIterator;
import org.eclipse.jgit.treewalk.CanonicalTreeParser;

public class LokalFoedselsnummerOpenSourceSammendragHeleHistorienModus {

    private final Pattern fødselsnummerRegex;
    private final ValidatorParametere validatorParametere;

    private final String bane;

    private LokalFoedselsnummerOpenSourceSammendragHeleHistorienModus(
            final String bane,
            final ValidatorParametere validatorParametere
    ) {
        this.bane = requireNonNull(bane, "bane var påkrevd, men var null");
        this.validatorParametere = requireNonNull(validatorParametere, "validatorParametere var påkrevd, men var null");
        this.fødselsnummerRegex = validatorParametere.mønster();
    }

    public static LokalFoedselsnummerOpenSourceSammendragHeleHistorienModus lokalFoedselsnummerOpenSourceSammendragHeleHistorienModus(
            final String bane,
            final ValidatorParametere validatorParametere
    ) {
        return new LokalFoedselsnummerOpenSourceSammendragHeleHistorienModus(bane, validatorParametere);
    }

    public void kjør() {

        System.out.printf(
                """
                        Oppsummering:%n%nFødselsnummer datoformat %s og mønster %s
                        
                        Starter %s
                        """,
                validatorParametere.fødselsdatoMønster(),
                validatorParametere.mønster(),
                LocalDateTime.now()
                );

        final Tuple<Integer, Integer> antall = scanEnFilOgGitHistorikk();

        System.out.printf(
                """
                        Avsluttet %s
                        
                        Fant %d fødselsnummere:
                            - %d er gyldig(e)
                        
                        """,
                LocalDateTime.now(),
                antall.andre(),
                antall.første()
        );
    }

    private Tuple<Integer, Integer> scanEnFilOgGitHistorikk() {
        final Repository repo;
        try {
            repo = new FileRepositoryBuilder()
                    .setGitDir(new File(bane + "/.git"))
                    .build();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        try (final Git git = new Git(repo)) {
            return alleCommits(git)
                    .filter(commit -> commit.getParentCount() > 0)
                    .flatMap(commit -> finnAlleDifferICommitten(commit, repo, git))
                    .flatMap(commitOgDiff -> finnAlleFødselsnummereIDiffen(commitOgDiff, repo))
                    .collect(
                            teeing(
                                    summingInt(Tuple::første),
                                    summingInt(Tuple::andre),
                                    Tuple::new
                            )
                    );
        } catch (GitAPIException | IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static Stream<RevCommit> alleCommits(final Git git) throws GitAPIException, IOException {
        return stream(
                git.log().all().call().spliterator(),
                true
        );
    }

    private static Stream<CommitOgDiff> finnAlleDifferICommitten(final RevCommit commit, final Repository repo, final Git git) {
        try {
            return git.diff()
                    .setOldTree(prepareTreeParser(repo, commit.getParent(0).getName()))
                    .setNewTree(prepareTreeParser(repo, commit.getId().getName()))
                    .call()
                    .stream()
                    .parallel()
                    .map(diff -> new CommitOgDiff(commit, diff));
        } catch (final GitAPIException e) {
            throw new RuntimeException(e);
        } catch (final IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private Stream<Tuple<Integer, Integer>> finnAlleFødselsnummereIDiffen(final CommitOgDiff commitOgDiff, final Repository repo) {
        try {
            final ByteArrayOutputStream bs = new ByteArrayOutputStream();
            try (final DiffFormatter formatter = new DiffFormatter(bs)) {
                formatter.setRepository(repo);
                formatter.format(commitOgDiff.diff);

                return fødselsnummerRegex.matcher(bs.toString(UTF_8))
                        .results()
                        .map(matchResult -> matchResult.group("fnr"))
                        .map(match -> foedselsnummer(match, validatorParametere))
                        .map(fnr -> tuple(fnr.erGyldig() ? 1 : 0, 1));
            }
        } catch (final IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private static String lagFilnavn(final RevCommit commit, final DiffEntry entry) {
        if (entry.getOldPath().equals(entry.getNewPath())) {
            return entry.getNewPath() + " (" + commit.getId().getName() + ")";
        } else if (entry.getOldPath().equals("/dev/null")) {
            return entry.getNewPath() + " (" + commit.getId().getName() + ")";
        } else if (entry.getNewPath().equals("/dev/null")) {
            return entry.getOldPath() + " (" + commit.getId().getName() + ")";
        } else {
            return entry.getOldPath() + " --> " + entry.getNewPath() + " (" + commit.getId().getName() + ")";
        }
    }

    private static AbstractTreeIterator prepareTreeParser(final Repository repository, final String objectId) throws IOException {
        try (final RevWalk walk = new RevWalk(repository)) {
            final RevCommit commit = walk.parseCommit(ObjectId.fromString(objectId));
            final RevTree tree = walk.parseTree(commit.getTree().getId());

            final CanonicalTreeParser treeParser = new CanonicalTreeParser();
            try (final ObjectReader reader = repository.newObjectReader()) {
                treeParser.reset(reader, tree.getId());
            }

            walk.dispose();

            return treeParser;
        }
    }

    private record CommitOgDiff(RevCommit commit, DiffEntry diff) {
    }
}
