package no.spk.panda.gdpr.validator.cli;

import static picocli.CommandLine.Command;
import static picocli.CommandLine.Option;

import java.util.concurrent.Callable;

import picocli.CommandLine;

@Command(name = "pandagdprvalidator",
        mixinStandardHelpOptions = true,
        description = "Validerer GDPR-relatert data.",
        version = "pandagdprvalidator 0.0.1"
)
public class PandaGdprValidatorCli implements Callable<Integer> {

    private static final int TOTALT_FEIL = 1;


    @Option(names = {"-m", "--modus"}, required = true, description = "Spesifiser ønsket modus (tilgjengelig: fødselsnummer)")
    private String modus;

    @Option(names = {"-b", "--bane"}, description = "Spesifiser ønsket bane å sjekke i")
    private String bane;

    @Override
    public Integer call() {
        System.out.format("modus: %s, bane: %s\n", modus, bane);

        try {
            switch (modus) {
                case "fødselsnummer":
                    System.out.format("Leter etter fødselsnummere i %s og validerer dem...\n", bane);
                    FoedselsnummerSjekker foedselsnummerSjekker = new FoedselsnummerSjekker();
                    return foedselsnummerSjekker.sjekk(bane);
                default:
                    throw new RuntimeException("Ukjent modus");
            }
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
