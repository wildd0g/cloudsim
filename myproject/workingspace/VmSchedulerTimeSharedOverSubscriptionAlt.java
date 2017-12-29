package workingspace;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.cloudbus.cloudsim.Log;
import org.cloudbus.cloudsim.Pe;
import org.cloudbus.cloudsim.VmSchedulerTimeSharedOverSubscription;
import org.cloudbus.cloudsim.provisioners.PeProvisioner;

public class VmSchedulerTimeSharedOverSubscriptionAlt extends
		VmSchedulerTimeSharedOverSubscription {

	public VmSchedulerTimeSharedOverSubscriptionAlt(List<? extends Pe> pelist) {
		super(pelist);
		// TODO Auto-generated constructor stub
	}

	
	/**
	 * Update allocation of VMs on PEs.
         * @too The method is too long and may be refactored to make clearer its
         * responsibility.
	 */
	@Override
	protected void updatePeProvisioning() {
		getPeMap().clear();
		for (Pe pe : getPeList()) {
			pe.getPeProvisioner().deallocateMipsForAllVms();
		}

		Iterator<Pe> peIterator = getPeList().iterator();
		Pe pe = peIterator.next();
		PeProvisioner peProvisioner = pe.getPeProvisioner();
		double availableMips = peProvisioner.getAvailableMips();

		for (Map.Entry<String, List<Double>> entry : getMipsMap().entrySet()) {
			String vmUid = entry.getKey();
			getPeMap().put(vmUid, new LinkedList<Pe>());
			for (double mips : entry.getValue()) {
				while (mips >= 0.1) {
					//	Moved this check up from (availableMips >= mips) else section, in the case that a PE has been exactly filled, it won't also be assigned to a VM while contributing 0 mips.   
					if (availableMips <= 0) {
						if (!peIterator.hasNext()) {
							Log.printConcatLine("There is no enough MIPS (", mips, ") to accommodate VM ", vmUid);
							// System.exit(0);
						}
						pe = peIterator.next();
						peProvisioner = pe.getPeProvisioner();
						availableMips = peProvisioner.getAvailableMips();	
					}
					if (availableMips >= mips) {
						peProvisioner.allocateMipsForVm(vmUid, mips);
						if (!getPeMap().get(vmUid).contains(pe)) {getPeMap().get(vmUid).add(pe);}	//added check to see if the VM's PE list already contains the PE in question
						availableMips -= mips;
						break;
					} else {
						peProvisioner.allocateMipsForVm(vmUid, availableMips);
						if (!getPeMap().get(vmUid).contains(pe)) {getPeMap().get(vmUid).add(pe);}	//added check to see if the VM's PE list already contains the PE in question
						mips -= availableMips;
						if (mips <= 0.1) {
							break;
						}
					}
				}
			}
		}
	}
	
}
