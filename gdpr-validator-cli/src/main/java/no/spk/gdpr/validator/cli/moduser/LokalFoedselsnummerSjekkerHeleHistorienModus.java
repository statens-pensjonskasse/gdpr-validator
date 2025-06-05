package no.spk.gdpr.validator.cli.moduser;

import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.Objects.requireNonNull;
import static java.util.stream.StreamSupport.stream;
import static no.spk.gdpr.validator.cli.Resultat.resultat;
import static no.spk.gdpr.validator.fnr.Foedselsnummer.foedslesnummer;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.MatchResult;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

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

public class LokalFoedselsnummerSjekkerHeleHistorienModus {

    private final Pattern fødselsnummerRegex;
    private final ValidatorParametere validatorParametere;
    private final UtgangsInnstillinger utgangsInnstillinger;

    private final String bane;
    private final List<Resultat> resultater;

    private LokalFoedselsnummerSjekkerHeleHistorienModus(
            final String bane,
            final ValidatorParametere validatorParametere,
            final UtgangsInnstillinger utgangsInnstillinger
    ) {
        this.bane = requireNonNull(bane, "bane var påkrevd, men var null");
        this.validatorParametere = requireNonNull(validatorParametere, "validatorParametere var påkrevd, men var null");
        this.fødselsnummerRegex = validatorParametere.mønster();
        this.utgangsInnstillinger = requireNonNull(utgangsInnstillinger, "utgangsInnstillinger var påkrevd, men var null");

        resultater = new ArrayList<>();
    }

    public static LokalFoedselsnummerSjekkerHeleHistorienModus lokalFoedselsnummerSjekkerHeleHistorienModus(
            final String bane,
            final ValidatorParametere validatorParametere,
            final UtgangsInnstillinger utgangsInnstillinger
    ) {
        return new LokalFoedselsnummerSjekkerHeleHistorienModus(bane, validatorParametere, utgangsInnstillinger);
    }

    public void kjør() throws IOException, GitAPIException {
        letIHeleGitLoggen();

        if (utgangsInnstillinger.visOppsummering()) {
            System.out.println(Resultat.lagOppsummering(resultater));
        }

        resultater
                .forEach(r -> System.out.println(r.filtrertOutput(utgangsInnstillinger)));
    }

    private void letIHeleGitLoggen() throws IOException, GitAPIException {
        final Repository repo = new FileRepositoryBuilder()
                .setGitDir(new File(bane + "/.git"))
                .build();

        try (final Git git = new Git(repo)) {
            resultater.addAll(
                    alleCommits(git)
                            .filter(commit -> commit.getParentCount() > 0)
                            .flatMap(commit -> finnAlleDifferICommitten(commit, repo, git))
                            .flatMap(commitOgDiff -> finnAlleFødselsnummereIDiffen(commitOgDiff, repo))
                            .toList()
            );
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

                final Matcher fødselsnummerMatcher = fødselsnummerRegex.matcher(bs.toString(UTF_8));
                if (fødselsnummerMatcher.find()) {
                    return fødselsnummerMatcher
                            .results()
                            .map(MatchResult::group)
                            .distinct()
                            .map(potensieltFødselsnummer ->
                                    resultat(
                                            foedslesnummer(potensieltFødselsnummer, validatorParametere),
                                            lagFilnavn(commitOgDiff.commit, commitOgDiff.diff)
                                    )
                            );
                } else {
                    return Stream.empty();
                }
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
