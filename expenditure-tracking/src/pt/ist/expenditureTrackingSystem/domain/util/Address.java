package pt.ist.expenditureTrackingSystem.domain.util;

import pt.ist.expenditureTrackingSystem.domain.DomainException;

public class Address {

    private String line1;
    private String line2;
    private String postalCode;
    private String location;
    private String country;

    public Address(final String line1, final String line2, final String postalCode, final String location, final String country) {
	setLine1(line1);
	setLine2(line2);
	setLocation(location);
	setCountry(country);
	setPostalCode(postalCode);
    }

    public String getLine1() {
	return line1;
    }

    public String getLine2() {
	return line2;
    }

    public String getPostalCode() {
	return postalCode;
    }

    public String getLocation() {
	return location;
    }

    public String getCountry() {
	return country;
    }

    protected void setLine1(String line1) {
	if (line1 == null || line1.isEmpty()) {
	    throw new DomainException("error.address.line1.cannot.be.empty");
	}
	this.line1 = line1;
    }

    protected void setLine2(String line2) {
	this.line2 = line2;
    }

    protected void setPostalCode(String postalCode) {
	if (postalCode == null || postalCode.isEmpty()) {
	    throw new DomainException("error.address.postalCode.cannot.be.empty");
	}
	this.postalCode = postalCode;
    }

    protected void setLocation(String location) {
	if (location == null || location.isEmpty()) {
	    throw new DomainException("error.address.location.cannot.be.empty");
	}
	this.location = location;
    }

    protected void setCountry(String country) {
	if (country == null || country.isEmpty()) {
	    throw new DomainException("error.address.country.cannot.be.empty");
	}
	this.country = country;
    }
}
