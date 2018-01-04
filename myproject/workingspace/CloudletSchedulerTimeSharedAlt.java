package workingspace;

import java.util.ArrayList;
import java.util.List;

import org.cloudbus.cloudsim.Cloudlet;
import org.cloudbus.cloudsim.CloudletSchedulerTimeShared;
import org.cloudbus.cloudsim.DatacenterBroker;
import org.cloudbus.cloudsim.Pe;
import org.cloudbus.cloudsim.ResCloudlet;
import org.cloudbus.cloudsim.Vm;
import org.cloudbus.cloudsim.core.CloudSim;
import org.cloudbus.cloudsim.provisioners.PeProvisionerSimple;

public class CloudletSchedulerTimeSharedAlt extends CloudletSchedulerTimeShared {

	/** List containing all PEs. */
	private List<Pe> peList; 

	/** The cache of the previous time when the {@link #getCurrentRequestedMips()} was called. */
	private double cachePreviousTime;

	/** The cache of the last current requested MIPS. 
	 * @see  #getCurrentRequestedMips() 
	 */
	private List<Double> cacheCurrentRequestedMips;
	
	public CloudletSchedulerTimeSharedAlt(double mips, int numberOfPes) {
		this(generateGenericPeList(mips, numberOfPes));
	}
	
	public CloudletSchedulerTimeSharedAlt(List<Pe> peList) {
		super();
		setPeList(peList);
	}

	@Override
	public double cloudletSubmit(Cloudlet cloudlet, double fileTransferTime) {
		ResCloudlet rcl = new ResCloudlet(cloudlet);
		rcl.setCloudletStatus(Cloudlet.INEXEC);
		for (int i = 0; i < cloudlet.getNumberOfPes(); i++) {
			rcl.setMachineAndPeId(0, i);
		}

		getCloudletExecList().add(rcl);

		// Update the host of the VM receiving this cloudlet to fix resource allocation
		DatacenterBroker broker = (DatacenterBroker) CloudSim.getEntity(cloudlet.getUserId());
		for (Vm vm : broker.getVmList()) {
			if (vm.getId() == cloudlet.getVmId()) {
				vm.getHost().updateVmsProcessing(getPreviousTime());
				break;
			}
		}
		
		// use the current capacity to estimate the extra amount of
		// time to file transferring. It must be added to the cloudlet length
		double extraSize = getCapacity(getCurrentMipsShare()) * fileTransferTime * cloudlet.getNumberOfPes();
		long length = (long) (cloudlet.getCloudletLength() + extraSize);
		cloudlet.setCloudletLength(length);

		return cloudlet.getCloudletLength() / getCapacity(getCurrentMipsShare());
	}
	
	@Override
	public List<Double> getCurrentRequestedMips() {
		int pesRequested = 0;
		for (ResCloudlet rcl : getCloudletExecList()) {
			pesRequested += rcl.getNumberOfPes();
		}
		
		if (pesRequested >= getNumberOfPes()) {
			return getMips();
		} else {
			List<Double> mipsShare = new ArrayList<Double>();
			if (pesRequested > 0) {
				mipsShare = new ArrayList<Double>(getMips().subList(0, pesRequested));
			}
			
			return mipsShare;
		}
		
		
	}

	@Override
	public double getTotalCurrentAvailableMipsForCloudlet(ResCloudlet rcl, List<Double> mipsShare) {
            /*@todo It isn't being used any the the given parameters.*/
            return getCapacity(getCurrentMipsShare()) * rcl.getNumberOfPes();
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
	
	protected void setPeList(List<Pe> peList) {
		this.peList = peList;
	}
	
	
	/**
	 * Gets the list of PEs of the VM this scheduler is assigned to.
	 * 
	 * @return list of PEs of the VM this scheduler is assigned to.
	 */
	
	public List<Pe> getPeList() {
		return peList;
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
	
	protected static List<Pe> generateGenericPeList(double mips, int numberOfPes) {
		List<Pe> peList = new ArrayList<Pe>();
		for (int i = 100; i < (numberOfPes + 100); i++) {
			peList.add(new Pe(i,new PeProvisionerSimple(mips)));
		}
		return peList;
	}
}


