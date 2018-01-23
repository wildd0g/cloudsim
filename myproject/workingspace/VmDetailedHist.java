package workingspace;

import java.util.LinkedList;
import java.util.List;

import org.cloudbus.cloudsim.CloudletScheduler;
import org.cloudbus.cloudsim.HostStateHistoryEntry;
import org.cloudbus.cloudsim.Log;

public class VmDetailedHist extends VmAlt {

	/** The host utilization state history. */
	private final List<VmStateHistoryEntryDetailed> stateHistoryDetailed = new LinkedList<VmStateHistoryEntryDetailed>();
	
	/** The running average of the mips.  */
	private double runningAverageMips;
	
	/** The running standard deviation/.  */
	private double runningStandardDeviation;
	
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
	
	/** Update the running average mips.  
	 * 
	 * WARNING! running average might fail in case of VM not active since time = 0, use recalculate first when accuracy is key
	 * */
	protected void updateRunningAverageMips(double time, double allocatedMips){
		double reducedAverage = getRunningAverageMips() * (getPreviousTime()/time);
		double newAverage = reducedAverage + ( allocatedMips * ( (time - getPreviousTime()) /time) );
		this.runningAverageMips = newAverage;
	}

	/** Get the running average mips.
	 * 
	 * @return The running average mips
	 */
	protected double getRunningAverageMips(){
		return runningAverageMips;
	}
	
	protected void recalculateRunningAverageMips(){
		List<VmStateHistoryEntryDetailed> histList = getStateHistoryDetailed(); 
		double runningTotalMi = 0;
		double totalRunningTime = 0;
		for (int i = 1; i < histList.size(); i++){
			if (histList.get(i-1).getHostId() > 0){
				runningTotalMi += histList.get(i-1).getAllocatedMips();
				totalRunningTime += histList.get(i).getTime()-histList.get(i-1).getTime();
			}
		}
		if (totalRunningTime != 0) {
		this.runningAverageMips = runningTotalMi/totalRunningTime;
		} else {
			Log.print("VM#" + getId() + " update running average mips failed!!! div by 0");
		}
			
	}
	
	/** Update the running average mips.  
	 *
	 * WARNING! running average might fail in case of VM not active since time = 0, use recalculate first when accuracy is key
	 */
	protected void updateRunningStandardDeviation(double time, double allocatedMips){
		double reducedAverage = (Math.pow(getRunningStandardDeviation(), 2)) * (getPreviousTime()/time); //de sqrt and reduce share
		double newAverage = Math.sqrt(reducedAverage +
				( Math.pow(allocatedMips - getRunningAverageMips(), 2) 
						* ( (time - getPreviousTime()) /time) //share of new deviation 
						));
		this.runningStandardDeviation = newAverage;
	}

	/** Get the running average mips.
	 * 
	 * @return The running average mips
	 */
	protected double getRunningStandardDeviation(){
		return runningStandardDeviation;
	}
	
	protected void recalculateRunningStandardDeviation(){
		List<VmStateHistoryEntryDetailed> histList = getStateHistoryDetailed(); 
		double runningTotalMi = 0;
		double totalRunningTime = 0;
		for (int i = 1; i < histList.size(); i++){
			if (histList.get(i-1).getHostId() > 0){
				runningTotalMi += histList.get(i-1).getAllocatedMips();
				totalRunningTime += histList.get(i).getTime()-histList.get(i-1).getTime();
			}
		}
		if (totalRunningTime != 0) {
		this.runningStandardDeviation = runningTotalMi/totalRunningTime;
		} else {
			Log.print("VM#" + getId() + " update running standard deviation failed!!! div by 0");
		}
			
	}
	
	
}
