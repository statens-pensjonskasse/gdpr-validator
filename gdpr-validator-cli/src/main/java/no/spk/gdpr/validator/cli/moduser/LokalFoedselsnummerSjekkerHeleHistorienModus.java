package no.spk.gdpr.validator.cli.moduser;

import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.Objects.requireNonNull;
import static java.util.stream.StreamSupport.stream;
import static no.spk.gdpr.validator.cli.Oppsummering.lagOppsummering;
import static no.spk.gdpr.validator.cli.Resultat.resultat;
import static no.spk.gdpr.validator.fnr.Foedselsnummer.foedslesnummer;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import no.spk.gdpr.validator.cli.Oppsummering;
import no.spk.gdpr.validator.cli.Resultat;
import no.spk.gdpr.validator.cli.UtgangsInnstillinger;
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
import org.jetbrains.annotations.NotNull;

public class LokalFoedselsnummerSjekkerHeleHistorienModus {

    private final Pattern fødselsnummerRegex;
    private final ValidatorParametere validatorParametere;
    private final UtgangsInnstillinger utgangsInnstillinger;

    private final String bane;
    private Optional<Oppsummering> oppsummering;

    private LokalFoedselsnummerSjekkerHeleHistorienModus(
            final String bane,
            final ValidatorParametere validatorParametere,
            final UtgangsInnstillinger utgangsInnstillinger
    ) {
        this.bane = requireNonNull(bane, "bane var påkrevd, men var null");
        this.validatorParametere = requireNonNull(validatorParametere, "validatorParametere var påkrevd, men var null");
        this.fødselsnummerRegex = validatorParametere.mønster();
        this.utgangsInnstillinger = requireNonNull(utgangsInnstillinger, "utgangsInnstillinger var påkrevd, men var null");

        oppsummering = Optional.of(Oppsummering.initier()).filter(x -> utgangsInnstillinger.visOppsummering());
    }

    public static LokalFoedselsnummerSjekkerHeleHistorienModus lokalFoedselsnummerSjekkerHeleHistorienModus(
            final String bane,
            final ValidatorParametere validatorParametere,
            final UtgangsInnstillinger utgangsInnstillinger
    ) {
        return new LokalFoedselsnummerSjekkerHeleHistorienModus(bane, validatorParametere, utgangsInnstillinger);
    }

    public void kjør() {
        letIHeleGitLoggen();

        oppsummering
                .map(Oppsummering::toString)
                .ifPresent(System.out::println);
    }

    private void letIHeleGitLoggen() {

        if (oppsummering.isPresent()) {
            oppsummering = oppsummering
                    .map(
                            eksisterende ->
                                    eksisterende
                                            .pluss(
                                                    lagOppsummering(
                                                            scanEnFilOgGitHistorikk()
                                                                    .peek(this::skrivUt)
                                                                    .toList()
                                                    )
                                            )
                    );
        } else {
            scanEnFilOgGitHistorikk()
                    .forEach(this::skrivUt);
        }
    }

    private Stream<Resultat> scanEnFilOgGitHistorikk() {
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
                    .flatMap(commitOgDiff -> finnAlleFødselsnummereIDiffen(commitOgDiff, repo));
        } catch (GitAPIException | IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void skrivUt(final Resultat r) {
        System.out.println(r.filtrertOutput(utgangsInnstillinger));
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

    private Stream<Resultat> finnAlleFødselsnummereIDiffen(final CommitOgDiff commitOgDiff, final Repository repo) {
        try {
            final ByteArrayOutputStream bs = new ByteArrayOutputStream();
            try (final DiffFormatter formatter = new DiffFormatter(bs)) {
                formatter.setRepository(repo);
                formatter.format(commitOgDiff.diff);

                final List<Resultat> resultater = new ArrayList<>();
                final Matcher fødselsnummerMatcher = fødselsnummerRegex.matcher(bs.toString(UTF_8));

                while (fødselsnummerMatcher.find()) {
                    resultater.add(
                            resultat(
                                    foedslesnummer(fødselsnummerMatcher.group("fnr"), validatorParametere),
                                    lagFilnavn(commitOgDiff.commit, commitOgDiff.diff)
                            )
                    );
                }

                return resultater.stream();
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
