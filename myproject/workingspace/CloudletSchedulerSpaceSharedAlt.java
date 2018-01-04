package workingspace;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.cloudbus.cloudsim.Cloudlet;
import org.cloudbus.cloudsim.CloudletSchedulerSpaceShared;
import org.cloudbus.cloudsim.DatacenterBroker;
import org.cloudbus.cloudsim.Log;
import org.cloudbus.cloudsim.Pe;
import org.cloudbus.cloudsim.ResCloudlet;
import org.cloudbus.cloudsim.Vm;
import org.cloudbus.cloudsim.core.CloudSim;
import org.cloudbus.cloudsim.provisioners.PeProvisionerSimple;

public class CloudletSchedulerSpaceSharedAlt extends
CloudletSchedulerSpaceShared {

	/** List containing all PEs. */
	private List<Pe> peList; 

	/** The cache of the previous time when the {@link #getCurrentRequestedMips()} was called. */
	private double cachePreviousTime;

	/** The cache of the last current requested MIPS. 
	 * @see  #getCurrentRequestedMips() 
	 */
	private List<Double> cacheCurrentRequestedMips;

	public CloudletSchedulerSpaceSharedAlt(double mips, int numberOfPes) {
		this(generateGenericPeList(mips, numberOfPes));
	}

	public CloudletSchedulerSpaceSharedAlt(List<Pe> peList) {
		super();
		setPeList(peList);
	}

	@Override
	public double cloudletSubmit(Cloudlet cloudlet, double fileTransferTime) {
		// it can go to the exec list
		ResCloudlet rcl = new ResCloudlet(cloudlet);
		if ((currentCpus - usedPes) >= cloudlet.getNumberOfPes()) {
			rcl.setCloudletStatus(Cloudlet.INEXEC);
			for (int i = 0; i < cloudlet.getNumberOfPes(); i++) {
				rcl.setMachineAndPeId(0, i);
			}
			getCloudletExecList().add(rcl);
			usedPes += cloudlet.getNumberOfPes();
		} else {// no enough free PEs: go to the waiting queue
			rcl.setCloudletStatus(Cloudlet.QUEUED);
			getCloudletWaitingList().add(rcl);
		}

		// Update the host of the VM receiving this cloudlet to fix resource allocation
		DatacenterBroker broker = (DatacenterBroker) CloudSim.getEntity(cloudlet.getUserId());
		for (Vm vm : broker.getVmList()) {
			if (vm.getId() == cloudlet.getVmId()) {
				vm.getHost().updateVmsProcessing(getPreviousTime());
				break;
			}
		}

		if (!getCloudletExecList().contains(rcl)) {
			return 0.0;
		} else {
			// calculate the expected time for cloudlet completion
			double capacity = 0.0;
			int cpus = 0;
			for (Double mips : getCurrentMipsShare()) {
				capacity += mips;
				if (mips > 0) {
					cpus++;
				}
			}

			currentCpus = cpus;
			capacity /= cpus;

			// use the current capacity to estimate the extra amount of
			// time to file transferring. It must be added to the cloudlet length
			double extraSize = capacity * fileTransferTime * cloudlet.getNumberOfPes();
			long length = cloudlet.getCloudletLength();
			length += extraSize;
			cloudlet.setCloudletLength(length);
			return cloudlet.getCloudletLength() / capacity;
		}

	}

	@Override
	public List<Double> getCurrentRequestedMips() {
		List<Double> mipsShare = new ArrayList<Double>();
		Iterator<Pe> peIterator = getPeList().iterator();
		Pe PE = null;

		if (getCloudletExecList().size() > getPeList().size()) {
			Log.printLine("There was an error in a space shared cloudled scheduler where there were more cloudlets executing simultaniously than PE's in the VM.");
			Log.printLine("THIS SHOULD NEVER HAPEN! The scheduler should not allow this at all!.");
			Log.printConcatLine(getCloudletExecList().toString()," > " , getPeList().toString());
			System.exit(0);
		}

		if (getCloudletExecList() != null) {

			for (ResCloudlet rc1 : getCloudletExecList()) {
				if (!peIterator.hasNext()) {
					Log.printLine("How did you even get this error? a previous check should have kicked you out already!?!?!");
					Log.printLine("This was generated in a space shared cloudlet scheduler");
					System.exit(0);
				}
				PE = peIterator.next();
				mipsShare.add(rc1.getCloudlet().getUtilizationOfCpu(getPreviousTime()) * PE.getMips());
			}
		}
		if (mipsShare.size() < getPeList().size()){
			Iterator<ResCloudlet> newCloudlets = getCloudletWaitingList().iterator();
			ResCloudlet newCloudlet = null;
			while (mipsShare.size() < getPeList().size() && newCloudlets.hasNext()) {
				if (!peIterator.hasNext()) {
					Log.printConcatLine("How did you even get this error? a previous check should have kicked you out already!?!?!");
					Log.printConcatLine("This was generated in a space shared cloudlet scheduler");
					System.exit(0);
				}
				PE = peIterator.next();
				newCloudlet = newCloudlets.next();
				mipsShare.add(newCloudlet.getCloudlet().getUtilizationOfCpu(getPreviousTime()) * PE.getMips());
			}
		}
		return mipsShare;
	}

	/**
	 * Gets the currently total amount of mips assigned to the vm
	 * 
	 * @return total current mips assigned to vm
	 */


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
