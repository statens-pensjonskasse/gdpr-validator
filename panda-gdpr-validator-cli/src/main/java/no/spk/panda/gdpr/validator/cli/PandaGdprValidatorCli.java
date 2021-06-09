package no.spk.panda.gdpr.validator.cli;

import static no.spk.panda.gdpr.validator.cli.GitRepoFoedselsnummerSjekkerModus.gitRepoFoedselsnummerSjekkerModus;
import static no.spk.panda.gdpr.validator.cli.GitRepoerFoedselsnummerSjekkerModus.gitRepoerFoedselsnummerSjekkerModus;
import static no.spk.panda.gdpr.validator.cli.LokalFoedselsnummerSjekkerModus.lokalFoedselsnummerSjekkerModus;
import static no.spk.panda.gdpr.validator.cli.UtgangsInnstillinger.utgangsInnstillinger;
import static no.spk.panda.gdpr.validator.cli.Util.repositorynavn;
import static no.spk.panda.gdpr.validator.cli.Util.tilLowercase;
import static no.spk.panda.gdpr.validator.fnr.ValidatorParametere.parametereForKasperMedSemikolonValidator;
import static no.spk.panda.gdpr.validator.fnr.ValidatorParametere.parametereForKasperValidator;
import static no.spk.panda.gdpr.validator.fnr.ValidatorParametere.parametereForOrdinærValidator;
import static picocli.CommandLine.Command;
import static picocli.CommandLine.Option;
import static picocli.CommandLine.Parameters;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;

import no.spk.panda.gdpr.validator.fnr.ValidatorParametere;

import picocli.CommandLine;

@SuppressWarnings("unused")
@Command(name = "gdprvalidator",
        mixinStandardHelpOptions = true,
        description = "Validerer GDPR-relatert data.",
        version = "gdprvalidator 0.0.2"
)
public class PandaGdprValidatorCli implements Callable<Integer> {

    private static final int OK = 0;
    private static final int TOTALT_FEIL = 1;
    private static final int FIL_FEIL = 2;
    private static final int GIT_FEIL = 3;
    private static final int UKJENT_MODUS_FEIL = 4;
    private static final int UKJENT_INNGANGSVERDI_FEIL = 5;

    @Parameters(description = "Spesifiser ønsket bane å sjekke i.")
    private String bane;

    @Option(names = {"-m", "--modus"},
            required = true,
            description = "Spesifiser ønsket modus (tilgjengelig: fødselsnummer, fødselsnummer_ett_repo, fødselsnummer_alle_repoer)."
    )
    private String modus;

    @Option(names = {"-f", "--fnrtype"},
            description = "Spesifiser fødselsnummertype (tilgjengelig: ordinær, kasper, kasper_med_semikolon).",
            defaultValue = "ordinær"
    )
    private String fnrtype;

    @Option(names = {"-t", "--filtype"},
            description = "Spesifiser ønsket filtype å sjekke i."
    )
    private List<String> filtyper;

    @Option(names = {"-o", "--visOppsummering"},
            description = "Vis oppsummering av resultatene."
    )
    private boolean visOppsummering;

    @Option(names = {"-g", "--visGyldighet"},
            description = "Vis gyldighet av fødselsnummer."
    )
    private boolean visGyldighet;

    @Option(names = {"-n", "--visNestenGyldighet"},
            description = "Vis nesten gyldighet av fødselsnummer."
    )
    private boolean visNestenGyldighet;

    @Option(names = {"-b", "--visFilbane"},
            description = "Vis filbanen fødselsnummeret eksisterer i."
    )
    private boolean visFilbane;

    @Override
    public Integer call() {
        if (filtyper == null) {
            filtyper = new ArrayList<>();
        }

        try {
            switch (modus) {
                case "fødselsnummer":
                case "fødselsnummer_ett_repo":
                case "fødselsnummer_alle_repoer":
                    final UtgangsInnstillinger utgangsInnstillinger = utgangsInnstillinger(visOppsummering, visGyldighet, visNestenGyldighet, visFilbane);
                    foedselsnummerSjekk(modus, fnrtype, bane, tilLowercase(filtyper), utgangsInnstillinger);
                    return OK;
                default:
                    System.out.format("Modusen \"%s\" er ukjent.\n", modus);
                    return UKJENT_MODUS_FEIL;
            }
        } catch (final UkjentInngangsParameterException ex) {
            System.out.format(ex.getMessage());
            return UKJENT_INNGANGSVERDI_FEIL;
        } catch (final FantIkkeGitRepositoryException ex) {
            System.out.println(ex.getMessage());
            return GIT_FEIL;
        } catch (final FileNotFoundException ex) {
            System.out.format("Forsøkte å åpne en fil som ikke eksisterer: %s\n", ex.getMessage());
            return FIL_FEIL;
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

    private static void foedselsnummerSjekk(
            final String modus,
            final String fnrtype,
            final String bane,
            final List<String> filtyper,
            final UtgangsInnstillinger utgangsInnstillinger
    ) throws IOException {

        final ValidatorParametere parametere;

        switch (fnrtype) {
            case "ordinær":
                parametere = parametereForOrdinærValidator();
                break;
            case "kasper":
                parametere = parametereForKasperValidator();
                break;
            case "kasper_med_semikolon":
                parametere = parametereForKasperMedSemikolonValidator();
                break;
            default:
                throw new UkjentInngangsParameterException(String.format("Fødselsnummertypen \"%s\" er ukjent.\n", fnrtype));
        }

        switch (modus) {
            case "fødselsnummer":
                if (filtyper.isEmpty()) {
                    System.out.format("Leter etter fødselsnummere i %s med alle filtyper og validerer dem...\n\n", bane);
                } else {
                    System.out.format("Leter etter fødselsnummere i %s med filtyper %s og validerer dem...\n\n", bane, filtyper);
                }

                lokalFoedselsnummerSjekkerModus(bane, filtyper, parametere, utgangsInnstillinger)
                        .kjør();
                break;
            case "fødselsnummer_ett_repo":
                if (filtyper.isEmpty()) {
                    System.out.format("Leter etter fødselsnummere i Git-repoet %s med alle filtyper og validerer dem...\n\n", bane);
                } else {
                    System.out.format("Leter etter fødselsnummere i Git-repoet %s med filtyper %s og validerer dem...\n\n", bane, filtyper);
                }

                gitRepoFoedselsnummerSjekkerModus(lokalFoedselsnummerSjekkerModus(repositorynavn(bane), filtyper, parametere, utgangsInnstillinger))
                        .sjekkEttRepo(bane);
                break;
            case "fødselsnummer_alle_repoer":
                if (filtyper.isEmpty()) {
                    System.out.format("Leter etter fødselsnummere i Git-prosjektet %s med alle filtyper og validerer dem...\n\n", bane);
                } else {
                    System.out.format("Leter etter fødselsnummere i Git-prosjektet %s med filtyper %s og validerer dem...\n\n", bane, filtyper);
                }

                gitRepoerFoedselsnummerSjekkerModus(filtyper, parametere, utgangsInnstillinger)
                        .sjekkMangeRepoer(bane);
                break;
            default:
                throw new UkjentInngangsParameterException(String.format("Modusen \"%s\" er ukjent.\n", modus));
        }
    }
}
