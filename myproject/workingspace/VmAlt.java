package workingspace;

import org.cloudbus.cloudsim.CloudletScheduler;
import org.cloudbus.cloudsim.Vm;

/**
 * Extends Vm.java to add a new method getTotalMips().
 * This is to reduce confusion getting the total mips in case of a VM with multiple PEs.
 * Should be properly merged with VM.java
 * 
 * @author Marijn Peppelman
 * @since CloudSim Toolkit 4.0+ additions and fixes by Marijn Peppelman
 */


public class VmAlt extends Vm {

	public VmAlt(int id, int userId, double mips, int numberOfPes, int ram,
			long bw, long size, String vmm, CloudletScheduler cloudletScheduler) {
		super(id, userId, mips, numberOfPes, ram, bw, size, vmm,
				cloudletScheduler);
		// TODO Auto-generated constructor stub
		// TODO check good or not?
	}

	/**
	 * Gets the total mips that the vm can request at maximum.
	 * Assumes each PE in the vm has equal mips
	 * 
	 * @return the total mips at maximum
	 */
	 
	public double getTotalMips() {
		double totalMips = 0;
		for (int PE = 0; PE < super.getNumberOfPes(); PE++){
			totalMips += super.getMips();
		}
		return totalMips;
	}
	
}
