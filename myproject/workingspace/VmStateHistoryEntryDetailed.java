package workingspace;

import org.cloudbus.cloudsim.VmStateHistoryEntry;

public class VmStateHistoryEntryDetailed extends VmStateHistoryEntry {

	/** The vmID. */
	private int hostId;
	
	public VmStateHistoryEntryDetailed(double time, double allocatedMips,
			double requestedMips, boolean isInMigration, int hostId) {
		super(time, allocatedMips, requestedMips, isInMigration);
		setHostId(hostId);
		// TODO Auto-generated constructor stub
	}

	/**
	 * Sets the time.
	 * 
	 * @param time the new time
	 */
	protected void setHostId(int hostId) {
		this.hostId = hostId;
	}

	/**
	 * Gets the time.
	 * 
	 * @return the time
	 */
	public int getHostId() {
		return hostId;
	}
	
}
