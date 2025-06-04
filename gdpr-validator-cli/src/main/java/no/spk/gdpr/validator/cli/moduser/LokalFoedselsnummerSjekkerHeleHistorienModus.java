package no.spk.gdpr.validator.cli.moduser;

import static java.util.Objects.requireNonNull;

import java.io.FileNotFoundException;

public class LokalFoedselsnummerSjekkerHeleHistorienModus {

    private final LokalFoedselsnummerSjekkerModus lokalFoedselsnummerSjekkerModus;

    private LokalFoedselsnummerSjekkerHeleHistorienModus(
            final LokalFoedselsnummerSjekkerModus lokalFoedselsnummerSjekkerModus
    ) {
        this.lokalFoedselsnummerSjekkerModus = requireNonNull(lokalFoedselsnummerSjekkerModus, "lokalFoedselsnummerSjekkerModus er påkrevd, men var null");
    }

    public static LokalFoedselsnummerSjekkerHeleHistorienModus lokalFoedselsnummerSjekkerHeleHistorienModus(
            final LokalFoedselsnummerSjekkerModus lokalFoedselsnummerSjekkerModus
    ) {
        return new LokalFoedselsnummerSjekkerHeleHistorienModus(lokalFoedselsnummerSjekkerModus);
    }

    public void kjør() throws FileNotFoundException {
        // Per diff, kjør modus...
        lokalFoedselsnummerSjekkerModus.kjør();
    }
}
