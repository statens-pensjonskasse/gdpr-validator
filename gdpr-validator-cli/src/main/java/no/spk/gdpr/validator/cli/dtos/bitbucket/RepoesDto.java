package no.spk.gdpr.validator.cli.dtos.bitbucket;

import java.util.List;

public class RepoesDto {
    public int size;
    public int limit;
    public boolean isLastPage;
    public List<ValueDto> values;
    public int start;
}
