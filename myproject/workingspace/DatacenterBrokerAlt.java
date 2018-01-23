package workingspace;

import java.util.ArrayList;
import java.util.List;

import org.cloudbus.cloudsim.Cloudlet;
import org.cloudbus.cloudsim.DatacenterBroker;
import org.cloudbus.cloudsim.Log;
import org.cloudbus.cloudsim.Vm;
import org.cloudbus.cloudsim.core.CloudSim;
import org.cloudbus.cloudsim.core.CloudSimTags;
import org.cloudbus.cloudsim.core.SimEvent;
import org.cloudbus.cloudsim.lists.VmList;

public class DatacenterBrokerAlt extends DatacenterBroker {

	/** The delay list of cloudlets submitted to the broker. 
     * @see #submitCloudletList(java.util.List) 
     */
protected List<Double> delayList;
	
	public DatacenterBrokerAlt(String name) throws Exception {
		super(name);
		setDelayList(new ArrayList<Double>());
		// TODO Auto-generated constructor stub
	}

	public void submitCloudletList(List<? extends Cloudlet> cloudletList, List<Double> delayList) {
		submitCloudletList(cloudletList);
		submitDelayList(delayList);
	}
	
	/**
	 * Submit cloudlets with a delay to the created VMs.
	 * 
	 * @pre $none
	 * @post $none
         * @see #submitCloudletList(java.util.List) 
	 */
	protected void submitDelayedCloudlets() {
		if (getCloudletList().size() != getDelayList().size()){
			Log.print("WARNING! Delay list size differs from cloudlet list size! sending cloudlets withouth delay!");
			submitCloudlets();
			getDelayList().clear();
		}
		
		int vmIndex = 0;
		List<Cloudlet> successfullySubmitted = new ArrayList<Cloudlet>();
		List<Double> successfullySubmittedDelays = new ArrayList<Double>();
		for (int i = 0; i<getCloudletList().size(); i++) {
			Cloudlet cloudlet = getCloudletList().get(i);
			double delay = getDelayList().get(i);
			Vm vm;
			// if user didn't bind this cloudlet and it has not been executed yet
			if (cloudlet.getVmId() == -1) {
				vm = getVmsCreatedList().get(vmIndex);
			} else { // submit to the specific vm
				vm = VmList.getById(getVmsCreatedList(), cloudlet.getVmId());
				if (vm == null) { // vm was not created
					if(!Log.isDisabled()) {				    
					    Log.printConcatLine(CloudSim.clock(), ": ", getName(), ": Postponing execution of cloudlet ",
							cloudlet.getCloudletId(), ": bount VM not available");
					}
					continue;
				}
			}

			if (!Log.isDisabled()) {
			    Log.printConcatLine(CloudSim.clock(), ": ", getName(), ": Sending cloudlet ",
					cloudlet.getCloudletId(), " to VM #", vm.getId(), " with delay ", delay);
			}
			
			cloudlet.setVmId(vm.getId());
			schedule(getVmsToDatacentersMap().get(vm.getId()), delay, CloudSimTags.CLOUDLET_SUBMIT, cloudlet);
			//sendNow(getVmsToDatacentersMap().get(vm.getId()), CloudSimTags.CLOUDLET_SUBMIT, cloudlet);
			cloudletsSubmitted++;
			vmIndex = (vmIndex + 1) % getVmsCreatedList().size();
			getCloudletSubmittedList().add(cloudlet);
			successfullySubmitted.add(cloudlet);
			successfullySubmittedDelays.add(delay);
		}

		// remove submitted cloudlets from waiting list
		getCloudletList().removeAll(successfullySubmitted);
		getDelayList().removeAll(successfullySubmittedDelays);
	}
	
	protected void processVmCreate(SimEvent ev) {
		int[] data = (int[]) ev.getData();
		int datacenterId = data[0];
		int vmId = data[1];
		int result = data[2];

		if (result == CloudSimTags.TRUE) {
			getVmsToDatacentersMap().put(vmId, datacenterId);
			getVmsCreatedList().add(VmList.getById(getVmList(), vmId));
			Log.printConcatLine(CloudSim.clock(), ": ", getName(), ": VM #", vmId,
					" has been created in Datacenter #", datacenterId, ", Host #",
					VmList.getById(getVmsCreatedList(), vmId).getHost().getId());
		} else {
			Log.printConcatLine(CloudSim.clock(), ": ", getName(), ": Creation of VM #", vmId,
					" failed in Datacenter #", datacenterId);
		}

		incrementVmsAcks();

		// all the requested VMs have been created
		if (getVmsCreatedList().size() == getVmList().size() - getVmsDestroyed()) {
			submitDelayedCloudlets();
		} else {
			// all the acks received, but some VMs were not created
			if (getVmsRequested() == getVmsAcks()) {
				// find id of the next datacenter that has not been tried
				for (int nextDatacenterId : getDatacenterIdsList()) {
					if (!getDatacenterRequestedIdsList().contains(nextDatacenterId)) {
						createVmsInDatacenter(nextDatacenterId);
						return;
					}
				}

				// all datacenters already queried
				if (getVmsCreatedList().size() > 0) { // if some vm were created
					submitCloudlets();
				} else { // no vms created. abort
					Log.printLine(CloudSim.clock() + ": " + getName()
							+ ": none of the required VMs could be created. Aborting");
					finishExecution();
				}
			}
		}
	}

	
	public void submitDelayList(List<Double> list) {
		getDelayList().addAll(list);
	}
	
	private void setDelayList(List<Double> delayList) {
		this.delayList = delayList;
	}
	
	protected List<Double> getDelayList() {
		return delayList;
	}
	
}
