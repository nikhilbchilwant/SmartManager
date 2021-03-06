package mas.blackboard.plan;

import jade.core.AID;
import jade.core.behaviours.OneShotBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.UnreadableException;

import java.util.HashMap;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import mas.blackboard.nameZoneData.NamedZoneData;
import mas.blackboard.util.MessageParams;
import mas.blackboard.zonespace.ZoneSpace;
import mas.util.AgentUtil;
import mas.util.ID;
import mas.util.ZoneDataUpdate;
import bdi4jade.belief.Belief;
import bdi4jade.core.BeliefBase;
import bdi4jade.message.MessageGoal;
import bdi4jade.plan.PlanBody;
import bdi4jade.plan.PlanInstance;
import bdi4jade.plan.PlanInstance.EndState;

/**
 * updates value of zone data after receiving update from concerned agent
 */

public class UpdateParam extends OneShotBehaviour implements PlanBody {

	private static final long serialVersionUID = 1L;

	private ACLMessage msg;
	
	//agent desiring to subscribe
	private AID Agent;
	private ZoneDataUpdate info;
	private BeliefBase BBBeliefBase;
	private Logger log;
	private MessageParams msgStruct;

	private HashMap<AID, String> serviceBase;

	@Override
	public EndState getEndState() {
		return EndState.SUCCESSFUL;
	}

	@Override
	public void init(PlanInstance pInstance) {
		log = LogManager.getLogger();
		MessageGoal goal = (MessageGoal) pInstance.getGoal();
		msg = goal.getMessage();
		Agent = msg.getSender();

		msgStruct = new MessageParams.Builder().
				replyWithParam(msg.getReplyWith()).
				Build();

		try {
			info = (ZoneDataUpdate) msg.getContentObject();
		} catch (UnreadableException e) {
			e.printStackTrace();
		}		
		BBBeliefBase = pInstance.getBeliefBase();
		serviceBase = (HashMap<AID, String>) BBBeliefBase.
				getBelief(ID.Blackboard.BeliefBaseConst.serviceDiary).
				getValue();
	}

	/**
	 * 
	 * @param agentToRegister AID of agent for which you want to get service
	 * @return Agent service
	 */
	private String getService(AID agentToRegister) {
		if(serviceBase != null && serviceBase.containsKey(agentToRegister)) {
			return serviceBase.get(agentToRegister);
		}
		String agentType = AgentUtil.GetAgentService(agentToRegister,myAgent);
		serviceBase.put(agentToRegister, agentType);
		BBBeliefBase.updateBelief(ID.Blackboard.BeliefBaseConst.serviceDiary, serviceBase);
		log.info("Adding service type : " + agentToRegister.getLocalName() + " : " + agentType);
		return agentType;
	}

	@Override
	public void action() {
		String AgentType = getService(Agent);
		Belief<HashMap<String,ZoneSpace>> ws=(Belief<HashMap<String,ZoneSpace>>)BBBeliefBase.getBelief(AgentType);

		if(ws == null) {
		}
		else {					

			HashMap<String,ZoneSpace> ZoneSpaceHashMap=ws.getValue();
			ZoneSpace zs=ZoneSpaceHashMap.get(Agent.getLocalName());

			if(zs != null) {
				NamedZoneData nzd = new NamedZoneData.Builder(info.getName()).build();

				if(zs.findZoneData(nzd) != null){
					zs.findZoneData(nzd).addItem(info.getValue(), msgStruct);
				}
				else{
					log.info("couldn't find zone for "+nzd.getName());
				}
				ZoneSpaceHashMap.put(Agent.getLocalName(), zs);

				((Belief<HashMap<String,ZoneSpace>>)BBBeliefBase.getBelief(AgentType)).setValue(ZoneSpaceHashMap);
			}
		}
	}
}

