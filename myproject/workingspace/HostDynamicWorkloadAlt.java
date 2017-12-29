package workingspace;

import java.util.ArrayList;
import java.util.List;

import org.cloudbus.cloudsim.HostDynamicWorkload;
import org.cloudbus.cloudsim.Log;
import org.cloudbus.cloudsim.Pe;
import org.cloudbus.cloudsim.Vm;
import org.cloudbus.cloudsim.VmScheduler;
import org.cloudbus.cloudsim.core.CloudSim;
import org.cloudbus.cloudsim.provisioners.BwProvisioner;
import org.cloudbus.cloudsim.provisioners.RamProvisioner;

import workingspace.VmAlt;

/**
 * Extends HostDynamicWorkload to fix a logging issue involving getMips()
 * Fixes should be properly merged with HostDynamicWorkload
 * 
 * @author Marijn Peppelman
 * @since CloudSim Toolkit 4.0+ additions and fixes by Marijn Peppelman
 */

public class HostDynamicWorkloadAlt extends HostDynamicWorkload {

	
	/** The list of VMs assigned to the host. */
	private final List<? extends VmAlt> vmList = new ArrayList<VmAlt>();
	
	public HostDynamicWorkloadAlt(int id, RamProvisioner ramProvisioner,
			BwProvisioner bwProvisioner, long storage,
			List<? extends Pe> peList, VmScheduler vmScheduler) {
		super(id, ramProvisioner, bwProvisioner, storage, peList, vmScheduler);
		// TODO Auto-generated constructor stub
		// TODO Check if correct
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public List<VmAlt> getVmList() {
		return (List<VmAlt>) vmList;
	}
	
	@Override
	public double updateVmsProcessing(double currentTime) {
		
		//execute super's updateVmsProcessing without log
		boolean previousLogState = !Log.isDisabled();
		Log.disable();
		double smallerTime = super.updateVmsProcessing(currentTime);
		
		//restore log status
		if (previousLogState) {Log.enable();}
		
		//log corrected version if needed
		for (VmAlt vm : getVmList()) {
			double totalRequestedMips = vm.getCurrentRequestedTotalMips();
			double totalAllocatedMips = getVmScheduler().getTotalAllocatedMipsForVm(vm);

			if (!Log.isDisabled()) {
				Log.formatLine(
						"%.2f: [Host #" + getId() + "] Total allocated MIPS for VM #" + vm.getId()
								+ " (Host #" + vm.getHost().getId()
								+ ") is %.2f, was requested %.2f out of total %.2f (%.2f%%)",
						CloudSim.clock(),
						totalAllocatedMips,
						totalRequestedMips,
						vm.getTotalMips(),
						totalRequestedMips / vm.getTotalMips() * 100);

				List<Pe> pes = getVmScheduler().getPesAllocatedForVM(vm);
				StringBuilder pesString = new StringBuilder();
				for (Pe pe : pes) {
					pesString.append(String.format(" PE #" + pe.getId() + ": %.2f.", pe.getPeProvisioner()
							.getTotalAllocatedMipsForVm(vm)));
				}
				Log.formatLine(
						"%.2f: [Host #" + getId() + "] MIPS for VM #" + vm.getId() + " by PEs ("
								+ getNumberOfPes() + " * " + getVmScheduler().getPeCapacity() + ")."
								+ pesString,
						CloudSim.clock());
			}

		}

		return smallerTime;
	}

}
