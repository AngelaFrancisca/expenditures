package pt.ist.expenditureTrackingSystem.domain.util;

import java.io.Serializable;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Currency;

import pt.ist.expenditureTrackingSystem.domain.DomainException;
import pt.utl.ist.fenix.tools.util.i18n.Language;

public class Money implements Serializable, Comparable<Money> {

    private static final String SEPERATOR = ":";
    private static final Currency defaultCurrency = Language.getDefaultLocale() != null ? Currency.getInstance(Language
	    .getDefaultLocale()) : Currency.getInstance("EUR");
    public static final Money ZERO = new Money("0");

    private final Currency currency;
    private final BigDecimal value;

    protected Money(final Currency currency, final BigDecimal value) {
	if (currency == null || value == null) {
	    throw new DomainException("error.wrong.init.money.args");
	}
	this.currency = currency;
	this.value = value;
    }

    public Money(final BigDecimal value) {
	this(defaultCurrency, value);
    }

    public Money(final String value) {
	this(defaultCurrency, new BigDecimal(value));
    }

    private Money newMoney(BigDecimal value) {
	return new Money(getCurrency(), value);
    }

    public String serialize() {
	final StringBuilder stringBuilder = new StringBuilder();
	stringBuilder.append(getCurrency().getCurrencyCode());
	stringBuilder.append(SEPERATOR);
	stringBuilder.append(getValue().toString());
	return stringBuilder.toString();
    }

    public Money percentage(final BigDecimal percentage) {
	return newMoney(valuePercentage(percentage));
    }

    private BigDecimal valuePercentage(final BigDecimal percentage) {
	return getValue().multiply(percentage.divide(new BigDecimal(100)));
    }

    public Money addPercentage(final BigDecimal percentage) {
	return newMoney(percentage.add(valuePercentage(percentage)));
    }

    public Money subtractPercentage(final BigDecimal percentage) {
	return newMoney(percentage.subtract(valuePercentage(percentage)));
    }

    public Money add(final Money money) {
	checkCurreny(money);
	return newMoney(getValue().add(money.getValue()));
    }

    public Money subtract(final Money money) {
	checkCurreny(money);
	return newMoney(getValue().subtract(money.getValue()));
    }

    public Money multiply(final BigDecimal mult) {
	return newMoney(getValue().multiply(mult));
    }

    public Money multiply(final long mult) {
	return multiply(BigDecimal.valueOf(mult));
    }

    public boolean isZero() {
	return getValue().equals(BigDecimal.ZERO);
    }

    public boolean isPositive() {
	return getValue().compareTo(BigDecimal.ZERO) > 0;
    }

    public boolean isNegative() {
	return getValue().compareTo(BigDecimal.ZERO) < 0;
    }

    public boolean isLessThan(final Money money) {
	return this.compareTo(money) < 0;
    }

    public boolean isGreaterThan(final Money money) {
	return this.compareTo(money) > 0;
    }

    public boolean isLessThanOrEqual(final Money money) {
	return this.compareTo(money) <= 0;
    }

    public boolean isGreaterThanOrEqual(final Money money) {
	return this.compareTo(money) >= 0;
    }

    protected void checkCurreny(Money money) {
	if (!this.getCurrency().equals(money.getCurrency())) {
	    throw new DomainException("error.diferent.currencies");
	}
    }

    private int getScale() {
	return getCurrency().getDefaultFractionDigits();
    }

    public Money[] allocate(int n) {
	Money lowResult = newMoney(getValue().divide(BigDecimal.valueOf(n), getScale(), RoundingMode.FLOOR));
	BigDecimal remainder = getValue().subtract(lowResult.getValue().multiply(BigDecimal.valueOf(n)));

	BigDecimal addingUnit = BigDecimal.valueOf(1).movePointLeft(getScale());
	Money[] results = new Money[n];
	for (int i = 0; i < n; i++) {
	    if (remainder.compareTo(BigDecimal.ZERO) > 0) {
		results[i] = lowResult.add(newMoney(addingUnit));
		remainder = remainder.subtract(addingUnit);
	    } else {
		results[i] = lowResult;
	    }
	}
	return results;
    }

    public static Money deserialize(final String serializedMoney) {
	final int seperatorIndex = serializedMoney.indexOf(SEPERATOR);
	final String currencyCode = serializedMoney.substring(0, seperatorIndex);
	final String valueString = serializedMoney.substring(seperatorIndex + 1);
	final BigDecimal value = new BigDecimal(valueString);
	return new Money(Currency.getInstance(currencyCode), value);
    }

    public Currency getCurrency() {
	return currency;
    }

    public BigDecimal getValue() {
	return value;
    }

    @Override
    public int compareTo(Money money) {
	checkCurreny(money);
	return getValue().compareTo(money.getValue());
    }

    @Override
    public int hashCode() {
	return getValue().hashCode();
    }

    @Override
    public boolean equals(Object obj) {
	return (obj instanceof Money) && equals((Money) obj);
    }

    public boolean equals(Money money) {
	return getValue().equals(money.getValue()) && getCurrency().equals(money.getCurrency());
    }

}
