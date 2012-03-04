/*
 * @(#)ImportEmployeesAndResponsiblesAux.java
 *
 * Copyright 2011 Instituto Superior Tecnico
 * Founding Authors: Luis Cruz, Nuno Ochoa, Paulo Abrantes
 * 
 *      https://fenix-ashes.ist.utl.pt/
 * 
 *   This file is part of the Expenditure Tracking Module.
 *
 *   The Expenditure Tracking Module is free software: you can
 *   redistribute it and/or modify it under the terms of the GNU Lesser General
 *   Public License as published by the Free Software Foundation, either version 
 *   3 of the License, or (at your option) any later version.
 *
 *   The Expenditure Tracking Module is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *   GNU Lesser General Public License for more details.
 *
 *   You should have received a copy of the GNU Lesser General Public License
 *   along with the Expenditure Tracking Module. If not, see <http://www.gnu.org/licenses/>.
 * 
 */
package pt.ist.expenditureTrackingSystem.domain.task;

import module.organization.domain.Accountability;
import module.organization.domain.AccountabilityType;
import module.organization.domain.Person;
import module.organizationIst.domain.IstAccountabilityType;
import myorg.domain.User;
import net.sourceforge.fenixedu.domain.RemotePerson;

import org.joda.time.LocalDate;

import pt.ist.expenditureTrackingSystem.domain.organization.CostCenter;
import pt.ist.expenditureTrackingSystem.domain.organization.Unit;

/**
 * 
 * @author João Antunes
 * @author Luis Cruz
 * 
 */
public class ImportEmployeesAndResponsiblesAux {

    public static void executeTask() {
	final User user = User.findByUsername("ist24439");
	final Person somePerson = user.getPerson();
	final RemotePerson someRemotePerson = somePerson.getRemotePerson();

	final String allTeacherInformation = someRemotePerson.readAllTeacherInformation();
	updateInformation(IstAccountabilityType.TEACHING_PERSONNEL, allTeacherInformation);
	final String allResearcherInformation = someRemotePerson.readAllResearcherInformation();
	updateInformation(IstAccountabilityType.RESEARCH_PERSONNEL, allResearcherInformation);
	final String allEmployeeInformation = someRemotePerson.readAllEmployeeInformation();
	updateInformation(IstAccountabilityType.PERSONNEL, allEmployeeInformation);
	final String allGrantOwnerInformation = someRemotePerson.readAllGrantOwnerInformation();
	updateInformation(IstAccountabilityType.GRANT_OWNER_PERSONNEL, allGrantOwnerInformation);
	final String allExternalResearcherInformation = someRemotePerson.readAllExternalResearcherInformation();
	updateInformation(IstAccountabilityType.EXTERNAL_RESEARCH_PERSONNEL, allExternalResearcherInformation);
    }

    private static void updateInformation(final IstAccountabilityType istAccountabilityType, final String allInformation) {
	final AccountabilityType accountabilityType = istAccountabilityType.readAccountabilityType();
	final LocalDate now = new LocalDate();
	for (int i = 0; i < allInformation.length(); ) {
	    final int sep1 = allInformation.indexOf(':', i);
	    final int sep2 = allInformation.indexOf(':', sep1 + 1);
	    final int sep3 = allInformation.indexOf('|', sep2 + 1);

	    if (sep1 > i && sep2 > sep1 && sep3 > sep2) {
		final String username = allInformation.substring(i, sep1);
		final String costCenterCode = allInformation.substring(sep2 + 1, sep3);

		updateInformation(now, accountabilityType, username, costCenterCode);
		i = sep3 + 1;
	    } else {
		i++;
	    }
	}
    }

    private static void updateInformation(final LocalDate now, final AccountabilityType accountabilityType, final String username, final String costCenterCode) {
	final User user = User.findByUsername(username);
	if (user != null) {
	    final Person person = user.getPerson();
	    if (person != null) {
		final Unit unit = CostCenter.findUnitByCostCenter(costCenterCode);
		if (unit != null) {
		    updateInformation(now, accountabilityType, person, unit.getUnit());
		} else {
		    System.out.println("Did not find cost center with code: " + costCenterCode);
		}
	    } else {
		System.out.println("User with username: " + username + " has no person");
	    }
	} else {
	    System.out.println("Did not find user with username: " + username);
	}
    }

    private static void updateInformation(final LocalDate now, final AccountabilityType accountabilityType, final Person person, final module.organization.domain.Unit unit) {
	for (final Accountability accountability : person.getParentAccountabilitiesSet()) {
	    if (accountability.getAccountabilityType() == accountabilityType && accountability.isActive(now)) {
		if (accountability.getParent() == unit) {
		    return;
		} else {
		    accountability.editDates(accountability.getBeginDate(), now.minusDays(1));
		}
	    }
	}
	unit.addChild(person, accountabilityType, now, null);
    }

}
