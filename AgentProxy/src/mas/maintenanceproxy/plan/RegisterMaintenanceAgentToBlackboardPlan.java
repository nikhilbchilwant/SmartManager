package mas.maintenanceproxy.plan;

import jade.core.AID;
import jade.core.behaviours.OneShotBehaviour;
import mas.blackboard.nameZoneData.NamedZoneData;
import mas.util.AgentUtil;
import mas.util.ID;
import mas.util.MessageIds;
import mas.util.SubscriptionForm;
import bdi4jade.plan.PlanBody;
import bdi4jade.plan.PlanInstance;
import bdi4jade.plan.PlanInstance.EndState;

/**
 * @author Anand Prajapati
 * <p>
 * Plan to register LMA on blackboard.
 * It creates zonedata's for this agent as well as subscribes to zonedata's of other agents
 * </p>
 */
public class RegisterMaintenanceAgentToBlackboardPlan extends OneShotBehaviour implements PlanBody {

	private static final long serialVersionUID = 1L;

	@Override
	public EndState getEndState() {
		return EndState.SUCCESSFUL;
	}

	@Override
	public void init(PlanInstance planInstance) {
		
	}

	@Override
	public void action() {

		AID bb_aid = AgentUtil.findBlackboardAgent(myAgent);

		NamedZoneData ZoneDataName1 = 
				new NamedZoneData.Builder(ID.Maintenance.ZoneData.correctiveMaintdata).
				MsgID(MessageIds.msgcorrectiveMaintdata).
				build();

		NamedZoneData ZoneDataName2 = 
				new NamedZoneData.Builder(ID.Maintenance.ZoneData.prevMaintData).
				MsgID(MessageIds.msgprevMaintData).
				build();
		
		NamedZoneData ZoneDataName3 = 
				new NamedZoneData.Builder(ID.Maintenance.ZoneData.preventiveMaintJob).
				MsgID(MessageIds.msgpreventiveMaintJob).
				build();

		NamedZoneData ZoneDataName4 = 
				new NamedZoneData.Builder(ID.Maintenance.ZoneData.inspectionJob).
				MsgID(MessageIds.msginspectionJob).
				build();
		
		NamedZoneData ZoneDataName5 = 
				new NamedZoneData.Builder(ID.Maintenance.ZoneData.inspectionJobData).
				MsgID(MessageIds.msginspectionJobData).
				build();
		
		NamedZoneData ZoneDataName6 = 
				new NamedZoneData.Builder(ID.Maintenance.ZoneData.machineStatus).
				MsgID(MessageIds.msgmachineStatus).
				build();
		
		NamedZoneData[] ZoneDataNames =  { ZoneDataName1,
				ZoneDataName2, ZoneDataName3, ZoneDataName4, ZoneDataName5, ZoneDataName6 };

		AgentUtil.makeZoneBB(myAgent,ZoneDataNames);

		SubscriptionForm subform = new SubscriptionForm();
		
		String suffix = myAgent.getLocalName().split("#")[1];
		AID machineTarget = new AID(ID.Machine.LocalName + "#" + suffix, AID.ISLOCALNAME);

		String[] machineParams = { ID.Machine.ZoneData.myHealth,
				ID.Machine.ZoneData.machineFailures, ID.Machine.ZoneData.maintenanceStart,
				ID.Machine.ZoneData.inspectionStart };

		subform.AddSubscriptionReq(machineTarget, machineParams);

		AID lSchedulingTarget = new AID(ID.LocalScheduler.LocalName + "#" + suffix, AID.ISLOCALNAME);
		String[] lParams = { ID.LocalScheduler.ZoneData.MaintConfirmationLSA };

		subform.AddSubscriptionReq(lSchedulingTarget, lParams);

		AgentUtil.subscribeToParam(myAgent, bb_aid, subform);
	}
}
