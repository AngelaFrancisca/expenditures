package pt.ist.expenditureTrackingSystem.presentationTier.renderers.validator;

import myorg.domain.util.Money;
import pt.ist.expenditureTrackingSystem.presentationTier.renderers.MoneyInputRenderer;
import pt.ist.fenixWebFramework.renderers.validators.HtmlChainValidator;
import pt.ist.fenixWebFramework.renderers.validators.HtmlValidator;

public class MoneyValidator extends HtmlValidator {

    public MoneyValidator(HtmlChainValidator htmlChainValidator) {
	super(htmlChainValidator);

	setBundle("EXPENDITURE_RESOURCES");
	setMessage("messages.exception.validation.invalidMoney");
    }

    public MoneyValidator() {
	super();
	setBundle("EXPENDITURE_RESOURCES");
	setMessage("messages.exception.validation.invalidMoney");
    }

    @Override
    public void performValidation() {
	setValid(true);
	try {
	    new MoneyInputRenderer.MoneyInputConverter().convert(Money.class, getComponent().getValue());
	} catch (Exception e) {
	    setValid(false);
	}
    }

    @Override
    public boolean hasJavascriptSupport() {
	return true;
    }

    @Override
    protected String getSpecificValidatorScript() {
	return "function(element) { var text = $(element).attr('value');"
		+ "return text.length == 0 || text.search(/^[0-9]+((\\.||,)[0-9]+)?$/) == 0; }";
    }
}
