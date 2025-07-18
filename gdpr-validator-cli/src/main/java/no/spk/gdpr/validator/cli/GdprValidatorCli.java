package no.spk.gdpr.validator.cli;

import static no.spk.gdpr.validator.cli.UtgangsInnstillinger.utgangsInnstillinger;
import static no.spk.gdpr.validator.cli.moduser.GitRepoFoedselsnummerSjekkerModus.gitRepoFoedselsnummerSjekkerModus;
import static no.spk.gdpr.validator.cli.moduser.GitRepoerFoedselsnummerSjekkerModus.gitRepoerFoedselsnummerSjekkerModus;
import static no.spk.gdpr.validator.cli.moduser.LokalFoedselsnummerOpenSourceSammendragHeleHistorienModus.lokalFoedselsnummerOpenSourceSammendragHeleHistorienModus;
import static no.spk.gdpr.validator.cli.moduser.LokalFoedselsnummerSjekkerHeleHistorienModus.lokalFoedselsnummerSjekkerHeleHistorienModus;
import static no.spk.gdpr.validator.cli.moduser.LokalFoedselsnummerSjekkerModus.lokalFoedselsnummerSjekkerModus;
import static no.spk.gdpr.validator.cli.util.Util.repositorynavn;
import static no.spk.gdpr.validator.cli.util.Util.tilLowercase;
import static no.spk.gdpr.validator.fnr.ValidatorParametere.parametereForKasperMedSemikolonValidator;
import static no.spk.gdpr.validator.fnr.ValidatorParametere.parametereForKasperValidator;
import static no.spk.gdpr.validator.fnr.ValidatorParametere.parametereForOrdinærValidator;
import static picocli.CommandLine.Command;
import static picocli.CommandLine.Option;
import static picocli.CommandLine.Parameters;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.stream.Stream;

import no.spk.gdpr.validator.cli.moduser.AnonymiserGrunnlagsdataModus;
import no.spk.gdpr.validator.cli.moduser.LokalFoedselsnummerOpenSourceSammendragHeleHistorienModus;
import no.spk.gdpr.validator.fnr.ValidatorParametere;

import picocli.CommandLine;

@SuppressWarnings("unused")
@Command(name = "gdprvalidator",
        mixinStandardHelpOptions = true,
        description = "Validerer GDPR-relatert data.",
        version = "gdprvalidator 0.0.3"
)
public class GdprValidatorCli implements Callable<Integer> {

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
            description = "Spesifiser ønsket modus (tilgjengelig: fødselsnummer, fødselsnummer_ett_repo, fødselsnummer_alle_repoer, fødselsnummer_ett_repo_hele_historien, anonymiser_grunnlagsdata)."
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
                case "fødselsnummer_ett_repo_hele_historien":
                case "open_source_sammendrag_fra_lokalt_repo_med_historien":
                    final UtgangsInnstillinger utgangsInnstillinger = utgangsInnstillinger(visOppsummering, visGyldighet, visNestenGyldighet, visFilbane);
                    foedselsnummerSjekk(modus, fnrtype, bane, tilLowercase(filtyper), utgangsInnstillinger);
                    return OK;
                case "anonymiser_grunnlagsdata":
                    final UtgangsInnstillinger utgangsInnstillinger2 = utgangsInnstillinger(visOppsummering, visGyldighet, visNestenGyldighet, visFilbane);
                    anonymiserGrunnlagsdata(bane, tilLowercase(filtyper), utgangsInnstillinger2);
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
        final CommandLine commandLine = new CommandLine(new GdprValidatorCli());

        final int exitCode = commandLine.execute(args);

        System.exit(exitCode);
    }

    private static ValidatorParametere velgParametereFraCmdLineArgs(final String fnrtype) {
        return switch (fnrtype) {
            case "ordinær" -> parametereForOrdinærValidator();
            case "kasper" -> parametereForKasperValidator();
            case "kasper_med_semikolon" -> parametereForKasperMedSemikolonValidator();
            default ->
                    throw new UkjentInngangsParameterException(String.format("Fødselsnummertypen \"%s\" er ukjent.\n", fnrtype));
        };
    }

    private static void foedselsnummerSjekk(
            final String modus,
            final String fnrtype,
            final String bane,
            final List<String> filtyper,
            final UtgangsInnstillinger utgangsInnstillinger
    ) throws IOException {

        final ValidatorParametere parametere = velgParametereFraCmdLineArgs(fnrtype);

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
            case "fødselsnummer_ett_repo_hele_historien":
                if (filtyper.isEmpty()) {
                    System.out.format("Leter etter fødselsnummere i Git-prosjektet %s i hele Git-loggen med alle filtyper og validerer dem...\n\n", bane);
                } else {
                    System.out.format("Leter etter fødselsnummere i Git-prosjektet %s i hele Git-loggen med filtyper %s og validerer dem...\n\n", bane, filtyper);
                }

                lokalFoedselsnummerSjekkerHeleHistorienModus(bane, parametere, utgangsInnstillinger)
                        .kjør();
                break;
            case "open_source_sammendrag_fra_lokalt_repo_med_historien":
                if (filtyper.isEmpty()) {
                    System.out.format("Leter etter fødselsnummere i Git-prosjektet %s i hele Git-loggen med alle filtyper og validerer dem...\n\n", bane);
                } else {
                    System.out.format("Leter etter fødselsnummere i Git-prosjektet %s i hele Git-loggen med filtyper %s og validerer dem...\n\n", bane, filtyper);
                }

                Stream.of("ordinær", "kasper", "kasper_med_semikolon")
                        .map(GdprValidatorCli::velgParametereFraCmdLineArgs)
                        .map(param -> lokalFoedselsnummerOpenSourceSammendragHeleHistorienModus(bane, param))
                        .forEach(LokalFoedselsnummerOpenSourceSammendragHeleHistorienModus::kjør);
                break;
            default:
                throw new UkjentInngangsParameterException(String.format("Modusen \"%s\" er ukjent.\n", modus));
        }
    }

    private static void anonymiserGrunnlagsdata(
            final String bane,
            final List<String> filtyper,
            final UtgangsInnstillinger utgangsInnstillinger
    ) throws IOException {

        if (filtyper.isEmpty()) {
            System.out.format("Anonymiserer grunnlagsdata i %s med alle filtyper...\n\n", bane);
        } else {
            System.out.format("Anonymiserer grunnlagsdata i %s med filtyper %s...\n\n", bane, filtyper);
        }

        AnonymiserGrunnlagsdataModus.anonymiserGrunnlagsdataModus(bane, filtyper, utgangsInnstillinger)
                .anonymiser();
    }
}
