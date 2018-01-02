package workingspace;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.cloudbus.cloudsim.CloudletSchedulerSpaceShared;
import org.cloudbus.cloudsim.Log;
import org.cloudbus.cloudsim.Pe;
import org.cloudbus.cloudsim.ResCloudlet;

public class CloudletSchedulerSpaceSharedAlt extends
CloudletSchedulerSpaceShared {

	/** List containing all PEs. */
	private List<Pe> Pes; 

	/** The cache of the previous time when the {@link #getCurrentRequestedMips()} was called. */
	private double cachePreviousTime;

	/** The cache of the last current requested MIPS. 
	 * @see  #getCurrentRequestedMips() 
	 */
	private List<Double> cacheCurrentRequestedMips;


	public CloudletSchedulerSpaceSharedAlt() {
		super();
		
		// TODO Auto-generated constructor stub
		// TODO suficient?
	}

	@Override
	public List<Double> getCurrentRequestedMips() {
		List<Double> mipsShare = new ArrayList<Double>();
		
		if (getCloudletExecList().size() > getPeList().size()) {
			Log.printConcatLine("There was an error in a speca shared cloudled scheduler where there were more cloudlets executing simultaniously than PE's in the VM.");
			Log.printConcatLine("THIS SHOULD NEVER HAPEN! The scheduler should not allow this at all!.");
			System.exit(0);
		}
		
		if (getCloudletExecList() != null) {
			Iterator<Pe> peIterator = getPeList().iterator();
			Pe PE = null;
			for (ResCloudlet rc1 : getCloudletExecList()) {
				if (!peIterator.hasNext()) {
					Log.printConcatLine("How did you even get this error? a previous check should have kicked you out already!?!?!");
					Log.printConcatLine("This was generated in a space shared cloudlet scheduler");
					System.exit(0);
				}
				PE = peIterator.next();
				mipsShare.add(rc1.getCloudlet().getUtilizationOfCpu(getPreviousTime()) * PE.getMips());
			}
		}
		return mipsShare;
	}

	
	public int getTotalCurrentMips() {
		int totalCurrentMips = 0;
		for (double mips : getCurrentMipsShare()) {
			totalCurrentMips += mips;
		}
		return totalCurrentMips;
	}
	
	/**
	 * Gets the list of PEs of the VM this scheduler is assigned to.
	 * 
	 * @return list of PEs of the VM this scheduler is assigned to.
	 */
	
	public List<Pe> getPeList() {
		return Pes;
	}
	
	/**
	 * Gets the total mips.
	 * 
	 * @return the total mips
	 */
	public double getTotalMips() {
		double totalMips = 0;
		for (Pe PE : getPeList()) {
			totalMips += PE.getMips();
		}
		return totalMips;
	}

	/**
	 * Gets the pes number.
	 * 
	 * @return the pes number
	 */
	public int getNumberOfPes() {
		return getPeList().size();
	}

	/**
	 * Gets the mips of all PEs.
	 * 
	 * @return the mips of all PEs
	 */
	public List<Double> getMips() {
		List<Double> mips = new ArrayList<Double>();
		for (Pe PE : getPeList()) {
			mips.add((double)PE.getMips());
		}
		return mips;
	}
	
	/**
	 * Gets the cache of previous time.
	 * 
	 * @return the cache previous time
	 */
	protected double getCachePreviousTime() {
		return cachePreviousTime;
	}

	/**
	 * Sets the cache of previous time.
	 * 
	 * @param cachePreviousTime the new cache previous time
	 */
	protected void setCachePreviousTime(double cachePreviousTime) {
		this.cachePreviousTime = cachePreviousTime;
	}

	/**
	 * Gets the cache of current requested mips.
	 * 
	 * @return the cache current requested mips
	 */
	protected List<Double> getCacheCurrentRequestedMips() {
		return cacheCurrentRequestedMips;
	}

	/**
	 * Sets the cache of current requested mips.
	 * 
	 * @param cacheCurrentRequestedMips the new cache current requested mips
	 */
	protected void setCacheCurrentRequestedMips(List<Double> cacheCurrentRequestedMips) {
		this.cacheCurrentRequestedMips = cacheCurrentRequestedMips;
	}
}
