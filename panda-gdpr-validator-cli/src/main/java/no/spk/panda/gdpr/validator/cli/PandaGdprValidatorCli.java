package no.spk.panda.gdpr.validator.cli;

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

    @Parameters(description = "Spesifiser ønsket bane å sjekke i")
    private String bane;

    @Option(names = {"-m", "--modus"}, required = true, description = "Spesifiser ønsket modus (tilgjengelig: fødselsnummer)")
    private String modus;

    @Option(names = {"-t", "--typer"}, description = "Spesifiser ønsket filtype å sjekke i")
    private List<String> filtyper;

    @Override
    public Integer call() {
        try {
            //noinspection SwitchStatementWithTooFewBranches
            switch (modus) {
                case "fødselsnummer":
                    if (filtyper == null) {
                        filtyper = new ArrayList<>();
                        System.out.format("Leter etter fødselsnummere i %s med alle filtyper og validerer dem...\n\n", bane);
                    } else {
                        System.out.format("Leter etter fødselsnummere i %s med filtyper %s og validerer dem...\n\n", bane, filtyper);
                    }

                    final FoedselsnummerSjekkerModus foedselsnummerSjekker = new FoedselsnummerSjekkerModus(bane, filtyper);
                    foedselsnummerSjekker.sjekk();

                    return OK;
                default:
                    throw new RuntimeException("Ukjent modus");
            }
        } catch (FileNotFoundException ex) {
            System.out.format("Forsøkte å åpne en fil som ikke eksisterer: %s\n", ex.getMessage());
            return FIL_FEIL;
        } catch (Exception ex) {
            System.out.format("Exception ble kastet: %s\n", ex.getMessage());
            return TOTALT_FEIL;
        }
    }

    public static void main(String... args) {
        final CommandLine commandLine = new CommandLine(new PandaGdprValidatorCli());

        final int exitCode = commandLine.execute(args);

        System.exit(exitCode);
    }
}
