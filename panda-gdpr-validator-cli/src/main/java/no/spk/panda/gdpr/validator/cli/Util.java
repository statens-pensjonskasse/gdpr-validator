package no.spk.panda.gdpr.validator.cli;

import static no.spk.panda.gdpr.validator.cli.KommandoHjelper.kommandoHjelper;

import java.io.File;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

import org.assertj.core.util.Lists;

class Util {

    static String filetternavn(final File fil) {
        return filetternavn(fil.getAbsolutePath());
    }

    static String filetternavn(final String fil) {
        if (harFiletternavn(fil)) {
            return fil.substring(fil.lastIndexOf(".") + 1);
        } else {
            return "";
        }
    }

    static String repositorynavn(final String bane) {
        if (harRepositorynavn(bane)) {
            return bane.substring(bane.lastIndexOf("/") + 1);
        } else {
            throw new FantIkkeGitRepositoryException(String.format("Klarte ikke å lese repositorynavn fra banen %s", bane));
        }
    }

    static List<String> tilLowercase(final List<String> lst) {
        return lst.stream().map(s -> s.toLowerCase(Locale.ROOT)).collect(Collectors.toUnmodifiableList());
    }

    private static boolean harFiletternavn(final String fil) {
        return fil.lastIndexOf(".") != -1;
    }

    private static boolean harRepositorynavn(final String bane) {
        return bane.lastIndexOf("/") != -1;
    }

    static boolean erGitUrl(final String bane) {
        return filetternavn(bane).equals("git") &&
                (bane.contains("http://") || bane.contains("https://") || bane.contains("git://") || bane.contains("ssh://"));
    }

    static void klonRepo(final String url) {
        kommandoHjelper().kjoerKommando(Lists.newArrayList("git", "clone", url, repositorynavn(url)));
    }
}
