package application.domain.valueobjects;

import lombok.Getter;

@Getter
public final class Currency extends DomainCatalog {

    public static final Currency COP = new Currency("COP", "Colombian Peso", "COP", "$");
    public static final Currency USD = new Currency("USD", "United States Dollar", "USD", "$");
    public static final Currency EUR = new Currency("EUR", "Euro", "EUR", "€");

    private final String isoCode;
    private final String symbol;

    private Currency(String code, String name, String isoCode, String symbol) {
        super(code, name, name);
        this.isoCode = isoCode;
        this.symbol = symbol;
    }
}
