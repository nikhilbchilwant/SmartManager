package mas.machineproxy.behaviors;

import jade.core.behaviours.Behaviour;
import mas.jobproxy.Batch;
import mas.jobproxy.job;
import mas.machineproxy.MachineStatus;
import mas.machineproxy.Simulator;
import mas.util.AgentUtil;
import mas.util.ID;
import mas.util.ZoneDataUpdate;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class TakeJobFromBatchBehavior extends Behaviour {

	private static final long serialVersionUID = 1L;
	private Logger log;
	private Batch currBatch;
	private job jobFromBatch;
	private Simulator machineSimulator;
	private int step = 0;
	private long time;
	private long LIMIT = 1000;
	private boolean isMsgDisplayed = false;

	public TakeJobFromBatchBehavior(Simulator simulator) {
		log = LogManager.getLogger();

		machineSimulator = simulator;
		getDataStore().put(Simulator.simulatorStoreName, simulator);
		currBatch = machineSimulator.getCurrentBatch();
		time = System.currentTimeMillis();
	}

	@Override
	public void action() {
		if(machineSimulator.getStatus() != MachineStatus.FAILED && 
				machineSimulator.getStatus() != MachineStatus.PAUSED ) {

			switch(step) {
			case 0:
				if (currBatch != null) {
					//					log.info("current batch : " + currBatch);
					machineSimulator.setUnloadFlag(false);
					machineSimulator.getGui().unlockUnloadButton();

					this.jobFromBatch = currBatch.getCurrentJob();
					currBatch.incrementCurrentJob();
					machineSimulator.setCurrentJob(jobFromBatch);
					LoadJobBehavior addjob = new LoadJobBehavior(this.jobFromBatch,machineSimulator);
					addjob.setDataStore(getDataStore());
					myAgent.addBehaviour(addjob);
					
					/**
					 * update zone-data for current job on machine
					 */
					ZoneDataUpdate currentJobUpdate = new ZoneDataUpdate.
							Builder(ID.Machine.ZoneData.currentJobOnMachine).
							value(jobFromBatch).
							Build();
					
					AgentUtil.sendZoneDataUpdate(Simulator.blackboardAgent ,
							currentJobUpdate, myAgent);

					step = 1;
				}
				else {
					currBatch = machineSimulator.getCurrentBatch();
					if(!isMsgDisplayed && (System.currentTimeMillis()-time > 0.8*LIMIT ) ) {
						machineSimulator.getGui().showNoJobInBatch();
						isMsgDisplayed = true;
					}
					block(100);
				}
				break;
			}
		}
	}

	@Override
	public boolean done() {
		return step >= 1 || (System.currentTimeMillis() - time > LIMIT);
	}

}
