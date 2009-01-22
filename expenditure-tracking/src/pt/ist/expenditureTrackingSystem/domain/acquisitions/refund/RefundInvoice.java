package pt.ist.expenditureTrackingSystem.domain.acquisitions.refund;

import java.math.BigDecimal;

import org.joda.time.LocalDate;

import pt.ist.expenditureTrackingSystem.domain.DomainException;
import pt.ist.expenditureTrackingSystem.domain.ExpenditureTrackingSystem;
import pt.ist.expenditureTrackingSystem.domain.acquisitions.RequestItem;
import pt.ist.expenditureTrackingSystem.domain.organization.Supplier;
import pt.ist.expenditureTrackingSystem.domain.util.Money;
import pt.ist.fenixframework.pstm.Transaction;

public class RefundInvoice extends RefundInvoice_Base {

    public RefundInvoice(String invoiceNumber, LocalDate invoiceDate, Money value, BigDecimal vatValue, Money refundableValue,
	    RefundItem item, Supplier supplier) {
	super();
	check(item, supplier, value, refundableValue);
	this.setExpenditureTrackingSystem(ExpenditureTrackingSystem.getInstance());
	this.setInvoiceNumber(invoiceNumber);
	this.setInvoiceDate(invoiceDate);
	this.setValue(value);
	this.setVatValue(vatValue);
	this.setRefundableValue(refundableValue);
	this.setRefundItem(item);
	this.setSupplier(supplier);
    }

    private void check(RequestItem item, Supplier supplier, Money value, Money refundableValue) {
	if (!supplier.isFundAllocationAllowed(value)) {
	    throw new DomainException("acquisitionRequestItem.message.exception.fundAllocationNotAllowed");
	}
	Money realValue = item.getRealValue();
	Money estimatedValue = item.getValue();
	if ((realValue != null && realValue.add(refundableValue).isGreaterThan(estimatedValue)) || realValue == null
		&& refundableValue.isGreaterThan(estimatedValue)) {
	    throw new DomainException("refundItem.message.info.realValueLessThanRefundableValue");
	}
    }

    public RefundInvoice(String invoiceNumber, LocalDate invoiceDate, Money value, BigDecimal vatValue, Money refundableValue,
	    byte[] invoiceFile, String filename, RefundItem item, Supplier supplier) {
	this(invoiceNumber, invoiceDate, value, vatValue, refundableValue, item, supplier);
	new RefundableInvoiceFile(this, invoiceFile, filename);
    }

    public void delete() {
	getRefundItem().clearRealShareValues();
	removeRefundItem();
	removeSupplier();
	removeExpenditureTrackingSystem();
	getFile().delete();
	Transaction.deleteObject(this);
    }

    public void editValues(Money value, BigDecimal vatValue, Money refundableValue) {
	check(getRefundItem(), getSupplier(), value, refundableValue);
	this.setValue(value);
	this.setVatValue(vatValue);
	this.setRefundableValue(refundableValue);
    }

    public void resetValues() {
	this.setValue(Money.ZERO);
	this.setVatValue(BigDecimal.ZERO);
	this.setRefundableValue(Money.ZERO);
    }

    public Money getValueWithVat() {
	return getValue().addPercentage(getVatValue());
    }
}
