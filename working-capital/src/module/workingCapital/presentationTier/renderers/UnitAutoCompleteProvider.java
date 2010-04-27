package module.workingCapital.presentationTier.renderers;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import myorg.presentationTier.renderers.autoCompleteProvider.AutoCompleteProvider;

import org.apache.commons.lang.StringUtils;

import pt.ist.expenditureTrackingSystem.domain.ExpenditureTrackingSystem;
import pt.ist.expenditureTrackingSystem.domain.organization.CostCenter;
import pt.ist.expenditureTrackingSystem.domain.organization.Project;
import pt.ist.expenditureTrackingSystem.domain.organization.SubProject;
import pt.ist.expenditureTrackingSystem.domain.organization.Unit;
import pt.utl.ist.fenix.tools.util.StringNormalizer;

public class UnitAutoCompleteProvider implements AutoCompleteProvider {

    public Collection getSearchResults(Map<String, String> argsMap, String value, int maxCount) {
	final List<Unit> units = new ArrayList<Unit>();

	final String trimmedValue = value.trim();

	if (isNumeric(trimmedValue)) {
	    final int code = Integer.parseInt(trimmedValue);
	    for (final Unit unit : ExpenditureTrackingSystem.getInstance().getUnits()) {
		if (unit instanceof CostCenter) {
		    final CostCenter costCenter = (CostCenter) unit;
		    final String unitCode = costCenter.getCostCenter();
		    if (!StringUtils.isEmpty(unitCode) && code == Integer.parseInt(unitCode)) {
			units.add(unit);
		    }
		} else if (unit instanceof Project) {
		    final Project project = (Project) unit;
		    final String unitCode = project.getProjectCode();
		    if (!StringUtils.isEmpty(unitCode) && code == Integer.parseInt(unitCode)) {
			units.add(unit);
		    }
		}
	    }
	} else {
	    final String[] input = trimmedValue.split(" ");
	    StringNormalizer.normalize(input);

	    for (final Unit unit : ExpenditureTrackingSystem.getInstance().getUnits()) {
		if (unit instanceof CostCenter || unit instanceof Project) {
		    final String unitName = StringNormalizer.normalize(unit.getName());
		    if (hasMatch(input, unitName)) {
			units.add(unit);
		    }
		}
	    }
	}

	Collections.sort(units, Unit.COMPARATOR_BY_PRESENTATION_NAME);

	return units;
    }

    private boolean hasMatch(final String[] input, final String unitNameParts) {
	for (final String namePart : input) {
	    if (unitNameParts.indexOf(namePart) == -1) {
		return false;
	    }
	}
	return true;
    }

    private boolean isNumeric(String someString) {
	boolean isNumeric = StringUtils.isNumeric(someString);
	if (isNumeric) {
	    try {
		int i = Integer.parseInt(someString);
	    } catch (NumberFormatException e) {
		return false;
	    }
	    return true;
	}
	return false;
    }
}
