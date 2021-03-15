import static picocli.CommandLine.Command;
import static picocli.CommandLine.Option;

import java.util.concurrent.Callable;

import picocli.CommandLine;

@Command(name = "pandagdprvalidator", mixinStandardHelpOptions = true,
        description = "Validerer GDPR-relatert data.")
public class PandaGdprValidatorCli implements Callable<Integer> {

    @Option(names = {"-m", "--modus"}, description = "Spesifiser ønsket modus (tilgjengelig: fødselsnummer)")
    private String modus;

    @Option(names = {"-k", "--katalog"}, description = "Spesifiser ønsket katalog å sjekke i")
    private String katalog;

    @Override
    public Integer call() {
        System.out.format("modus: %s, katalog: %s\n", modus, katalog);

        return 0;
    }

    public static void main(String... args) {
        final CommandLine commandLine = new CommandLine(new PandaGdprValidatorCli());

        final int exitCode = commandLine.execute(args);

        System.exit(exitCode);
    }
}
