package workingspace;

import java.util.LinkedList;
import java.util.List;

import org.cloudbus.cloudsim.CloudletScheduler;
import org.cloudbus.cloudsim.HostStateHistoryEntry;

public class VmDetailedHist extends VmAlt {

	/** The host utilization state history. */
	private final List<VmStateHistoryEntryDetailed> stateHistoryDetailed = new LinkedList<VmStateHistoryEntryDetailed>();
	
	/** The running average of the mips.  */
	private double runningAverageMips;
	
	public VmDetailedHist(int id, int userId, double mips, int numberOfPes,
			int ram, long bw, long size, String vmm,
			CloudletScheduler cloudletScheduler) {
		super(id, userId, mips, numberOfPes, ram, bw, size, vmm,
				cloudletScheduler);
		// TODO Auto-generated constructor stub
	}
	
	/**
	 * Adds a host state history entry.
	 * 
	 * @param time the time
	 * @param allocatedMips the allocated mips
	 * @param requestedMips the requested mips
	 * @param isActive the is active
	 */
	public void addStateHistoryEntryDetailed(
			double time, 
			double allocatedMips, 
			double requestedMips, 
			boolean isInMigration) {
		int hostId = -1;
		if (!(getHost() == null)){
			hostId = getHost().getId();
		}
		
		VmStateHistoryEntryDetailed newState = new VmStateHistoryEntryDetailed(
				time,
				allocatedMips,
				requestedMips,
				isInMigration,
				hostId);
		
		if (!getStateHistoryDetailed().isEmpty()) {
			VmStateHistoryEntryDetailed previousState = getStateHistoryDetailed().get(getStateHistoryDetailed().size() - 1);
			
			if (previousState.getHostId() != -1 && time != 0) {
				updateRunningAverageMips(time, allocatedMips);
			}
			
			if (previousState.getTime() == time) {
				getStateHistoryDetailed().set(getStateHistoryDetailed().size() - 1, newState);
				return;
			}
		}
		getStateHistoryDetailed().add(newState);
		
	}

	/**
	 * Gets the host state history.
	 * 
	 * @return the state history
	 */
	public List<VmStateHistoryEntryDetailed> getStateHistoryDetailed() {
		return stateHistoryDetailed;
	}
	
	/** Get time of last entry.
	 * 
	 * @return Time of last entry.
	 */
	protected double getPreviousTime(){
		return getStateHistoryDetailed().get(getStateHistoryDetailed().size() - 1).getTime();
	}
	
	/** Update the running average mips.  */
	protected void updateRunningAverageMips(double time, double allocatedMips){
		double reducedAverage = getRunningAverageMips() * (getPreviousTime()/time);
		double newAverage = reducedAverage + ( getRunningAverageMips() * ( (time - getPreviousTime()) /time) );
		this.runningAverageMips = newAverage;
	}

	/** Get the running average mips.
	 * 
	 * @return The running average mips
	 */
	protected double getRunningAverageMips(){
		return runningAverageMips;
	}
	
	
}
