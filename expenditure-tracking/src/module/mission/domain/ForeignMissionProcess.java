package module.mission.domain;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import module.geography.domain.Country;
import module.mission.domain.activity.AddFinancerActivity;
import module.mission.domain.activity.AddItemActivity;
import module.mission.domain.activity.AddParticipantActivity;
import module.mission.domain.activity.AllocateFundsActivity;
import module.mission.domain.activity.AllocateProjectFundsActivity;
import module.mission.domain.activity.ApproveActivity;
import module.mission.domain.activity.ArchiveItemActivity;
import module.mission.domain.activity.AuthoriseParticipantActivity;
import module.mission.domain.activity.AuthorizeActivity;
import module.mission.domain.activity.CancelProcessActivity;
import module.mission.domain.activity.DefineParticipantAuthorizationChainActivity;
import module.mission.domain.activity.DistributeItemCostsActivity;
import module.mission.domain.activity.EditItemActivity;
import module.mission.domain.activity.ProcessCanceledPersonnelActivity;
import module.mission.domain.activity.ProcessPersonnelActivity;
import module.mission.domain.activity.RemoveFinancerActivity;
import module.mission.domain.activity.RemoveItemActivity;
import module.mission.domain.activity.RemoveParticipantActivity;
import module.mission.domain.activity.RevertMissionForEditingActivity;
import module.mission.domain.activity.RevertTerminationActivity;
import module.mission.domain.activity.SendForProcessTerminationActivity;
import module.mission.domain.activity.SendForProcessTerminationWithChangesActivity;
import module.mission.domain.activity.SubmitForApprovalActivity;
import module.mission.domain.activity.TogleParticipantSalaryActivity;
import module.mission.domain.activity.UnAllocateFundsActivity;
import module.mission.domain.activity.UnAllocateProjectFundsActivity;
import module.mission.domain.activity.UnApproveActivity;
import module.mission.domain.activity.UnAuthoriseParticipantActivity;
import module.mission.domain.activity.UnAuthorizeActivity;
import module.mission.domain.activity.UnProcessPersonnelActivity;
import module.mission.domain.activity.UnSubmitForApprovalActivity;
import module.mission.domain.activity.UpdateForeignMissionDetailsActivity;
import module.workflow.activities.ActivityInformation;
import module.workflow.activities.GiveProcess;
import module.workflow.activities.ReleaseProcess;
import module.workflow.activities.StealProcess;
import module.workflow.activities.TakeProcess;
import module.workflow.activities.WorkflowActivity;
import module.workflow.domain.WorkflowProcess;
import myorg.util.ClassNameBundle;

import org.joda.time.DateTime;

import pt.utl.ist.fenix.tools.util.i18n.MultiLanguageString;

@ClassNameBundle(key="label.module.mission.domain.ForeignMissions", bundle="resources/MissionResources")
public class ForeignMissionProcess extends ForeignMissionProcess_Base {

    private static final List<WorkflowActivity<? extends WorkflowProcess, ? extends ActivityInformation>> activities;
    static {
	final List<WorkflowActivity<? extends WorkflowProcess, ? extends ActivityInformation>> activitiesAux = new ArrayList<WorkflowActivity<? extends WorkflowProcess, ? extends ActivityInformation>>();
	activitiesAux.add(new UpdateForeignMissionDetailsActivity());
	activitiesAux.add(new AddParticipantActivity());
	activitiesAux.add(new RemoveParticipantActivity());
	activitiesAux.add(new TogleParticipantSalaryActivity());
	activitiesAux.add(new DefineParticipantAuthorizationChainActivity());
	activitiesAux.add(new AddFinancerActivity());
	activitiesAux.add(new RemoveFinancerActivity());
	activitiesAux.add(new AddItemActivity());
	activitiesAux.add(new EditItemActivity());
	activitiesAux.add(new DistributeItemCostsActivity());
	activitiesAux.add(new RemoveItemActivity());
	activitiesAux.add(new SubmitForApprovalActivity());
	activitiesAux.add(new UnSubmitForApprovalActivity());
	activitiesAux.add(new ApproveActivity());
	activitiesAux.add(new UnApproveActivity());
	activitiesAux.add(new AllocateFundsActivity());
	activitiesAux.add(new AllocateProjectFundsActivity());
	activitiesAux.add(new UnAllocateFundsActivity());
	activitiesAux.add(new UnAllocateProjectFundsActivity());
	activitiesAux.add(new AuthorizeActivity());
	activitiesAux.add(new UnAuthorizeActivity());
	activitiesAux.add(new AuthoriseParticipantActivity());
	activitiesAux.add(new UnAuthoriseParticipantActivity());
	activitiesAux.add(new ProcessPersonnelActivity());
	activitiesAux.add(new UnProcessPersonnelActivity());
	activitiesAux.add(new ProcessCanceledPersonnelActivity());
	activitiesAux.add(new SendForProcessTerminationWithChangesActivity());
	activitiesAux.add(new SendForProcessTerminationActivity());
	activitiesAux.add(new RevertMissionForEditingActivity());
	activitiesAux.add(new ArchiveItemActivity());
	activitiesAux.add(new RevertTerminationActivity());

	activitiesAux.add(new GiveProcess<MissionProcess>(new MissionGiveProcessUserNotifier()));
	activitiesAux.add(new TakeProcess<MissionProcess>());
	activitiesAux.add(new ReleaseProcess<MissionProcess>());
	activitiesAux.add(new StealProcess<MissionProcess>());

	activitiesAux.add(new CancelProcessActivity());

	activities = Collections.unmodifiableList(activitiesAux);
    }

    public ForeignMissionProcess(final Country country, final String location,
	    final DateTime daparture, final DateTime arrival, final String objective, final Boolean isCurrentUserAParticipant,
	    final Boolean grantOwnerEquivalence) {
	new ForeignMission(this, country, location, daparture, arrival, objective, isCurrentUserAParticipant,
		grantOwnerEquivalence);
    }

    @Override
    public <T extends WorkflowActivity<? extends WorkflowProcess, ? extends ActivityInformation>> List<T> getActivities() {
	return (List) activities;
    }

    @Override
    public boolean isActive() {
	return true;
    }

    @Override
    public String getPresentationName() {
	final Mission mission = getMission();
	final Country country = mission.getCountry();
	final MultiLanguageString name = country == null ? null : country.getName();
	final String countryName = name == null ? "" : name.getContent();
        return super.getPresentationName() + countryName + ", " + mission.getLocation();
    }

}
