package application.domain.valueobjects;

import lombok.EqualsAndHashCode;
import lombok.Getter;

@Getter
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public abstract class DomainCatalog {

    @EqualsAndHashCode.Include
    private final String code;
    private final String name;
    private final String description;

    protected DomainCatalog(String code, String name, String description) {
        this.code = code;
        this.name = name;
        this.description = description;
    }
}
