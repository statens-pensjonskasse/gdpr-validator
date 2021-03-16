package no.spk.panda.gdpr.validator.cli;

import static no.spk.panda.gdpr.validator.cli.GitRepoFoedselsnummerSjekkerModus.gitRepoFoedselsnummerSjekkerModus;
import static no.spk.panda.gdpr.validator.cli.LokaleKatalogerFoedselsnummerSjekkerModus.lokaleKatalogerFoedselsnummerSjekkerModus;
import static no.spk.panda.gdpr.validator.cli.Util.repositorynavn;
import static no.spk.panda.gdpr.validator.cli.Util.tilLowercase;
import static no.spk.panda.gdpr.validator.fnr.ValidatorParametere.parametereForSemikolonValidator;
import static picocli.CommandLine.Command;
import static picocli.CommandLine.Option;
import static picocli.CommandLine.Parameters;

import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;

import picocli.CommandLine;

@SuppressWarnings("unused")
@Command(name = "pandagdprvalidator",
        mixinStandardHelpOptions = true,
        description = "Validerer GDPR-relatert data.",
        version = "pandagdprvalidator 0.0.1"
)
public class PandaGdprValidatorCli implements Callable<Integer> {

    private static final int OK = 0;
    private static final int TOTALT_FEIL = 1;
    public static final int FIL_FEIL = 2;
    public static final int GIT_FEIL = 3;
    public static final int UKJENT_MODUS_FEIL = 4;

    @Parameters(description = "Spesifiser ønsket bane å sjekke i")
    private String bane;

    @Option(names = {"-m", "--modus"},
            required = true,
            description = "Spesifiser ønsket modus (tilgjengelig: fødselsnummer, kaspernummer, " +
                    "fødselsnummer_med_semikolon, fødselsnummer_ett_repo, fødselsnummer_alle_repoer)"
    )
    private String modus;

    @Option(names = {"-t", "--typer"}, description = "Spesifiser ønsket filtype å sjekke i")
    private List<String> filtyper;

    @Override
    public Integer call() {
        try {
            switch (modus) {
                case "fødselsnummer":
                    if (filtyper == null) {
                        filtyper = new ArrayList<>();
                        System.out.format("Leter etter fødselsnummere i %s med alle filtyper og validerer dem...\n\n", bane);
                    } else {
                        System.out.format("Leter etter fødselsnummere i %s med filtyper %s og validerer dem...\n\n", bane, filtyper);
                    }

                    lokaleKatalogerFoedselsnummerSjekkerModus(bane, tilLowercase(filtyper)).kjør();

                    return OK;
                case "kaspernummer":
                    return OK;
                case "fødselsnummer_med_semikolon":
                    if (filtyper == null) {
                        filtyper = new ArrayList<>();
                        System.out.format("Leter etter fødselsnummere med semikolon i %s med alle filtyper og validerer dem...\n\n", bane);
                    } else {
                        System.out.format("Leter etter fødselsnummere med semikolon i %s med filtyper %s og validerer dem...\n\n", bane, filtyper);
                    }

                    lokaleKatalogerFoedselsnummerSjekkerModus(bane, tilLowercase(filtyper), parametereForSemikolonValidator()).kjør();

                    return OK;
                case "fødselsnummer_ett_repo":
                    if (filtyper == null) {
                        filtyper = new ArrayList<>();
                        System.out.format("Leter etter fødselsnummere i Git-repoet %s med alle filtyper og validerer dem...\n\n", bane);
                    } else {
                        System.out.format("Leter etter fødselsnummere i Git-repoet %s med filtyper %s og validerer dem...\n\n", bane, filtyper);
                    }

                    gitRepoFoedselsnummerSjekkerModus(
                            lokaleKatalogerFoedselsnummerSjekkerModus(repositorynavn(bane), tilLowercase(filtyper))
                    )
                            .sjekkEttRepo(bane);

                    return OK;
                case "fødselsnummer_alle_repoer":
                    if (filtyper == null) {
                        filtyper = new ArrayList<>();
                        System.out.format("Leter etter fødselsnummere i Git-prosjektet %s med alle filtyper og validerer dem...\n\n", bane);
                    } else {
                        System.out.format("Leter etter fødselsnummere i Git-prosjektet %s med filtyper %s og validerer dem...\n\n", bane, filtyper);
                    }

                    return OK;
                default:
                    System.out.format("Modusen \"%s\" er ukjent.\n", modus);
                    return UKJENT_MODUS_FEIL;
            }
        } catch (final FileNotFoundException ex) {
            System.out.format("Forsøkte å åpne en fil som ikke eksisterer: %s\n", ex.getMessage());
            return FIL_FEIL;
        } catch (final FantIkkeGitRepositoryException ex) {
            System.out.println(ex.getMessage());
            return GIT_FEIL;
        } catch (final Exception ex) {
            System.out.format("Exception ble kastet: %s\n", ex.getMessage());
            return TOTALT_FEIL;
        }
    }

    public static void main(final String... args) {
        final CommandLine commandLine = new CommandLine(new PandaGdprValidatorCli());

        final int exitCode = commandLine.execute(args);

        System.exit(exitCode);
    }
}
