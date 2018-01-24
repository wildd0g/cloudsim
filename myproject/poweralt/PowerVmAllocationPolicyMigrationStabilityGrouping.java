/*
 * Title:        CloudSim Toolkit
 * Description:  CloudSim (Cloud Simulation) Toolkit for Modeling and Simulation of Clouds
 * Licence:      GPL - http://www.gnu.org/copyleft/gpl.html
 *
 * Copyright (c) 2009-2012, The University of Melbourne, Australia
 */

package poweralt;

import java.util.List;
import java.util.Set;

import org.cloudbus.cloudsim.Host;
import org.cloudbus.cloudsim.Vm;
import org.cloudbus.cloudsim.power.PowerHost;
import org.cloudbus.cloudsim.power.PowerVm;
import org.cloudbus.cloudsim.power.PowerVmAllocationPolicyMigrationAbstract;
import org.cloudbus.cloudsim.power.PowerVmSelectionPolicy;

/**
 * A VM allocation policy that uses a Static CPU utilization Threshold (THR) to detect host over
 * utilization.
 * 
 * <br/>If you are using any algorithms, policies or workload included in the power package please cite
 * the following paper:<br/>
 * 
 * <ul>
 * <li><a href="http://dx.doi.org/10.1002/cpe.1867">Anton Beloglazov, and Rajkumar Buyya, "Optimal Online Deterministic Algorithms and Adaptive
 * Heuristics for Energy and Performance Efficient Dynamic Consolidation of Virtual Machines in
 * Cloud Data Centers", Concurrency and Computation: Practice and Experience (CCPE), Volume 24,
 * Issue 13, Pages: 1397-1420, John Wiley & Sons, Ltd, New York, USA, 2012</a>
 * </ul>
 * 
 * @author Anton Beloglazov
 * @since CloudSim Toolkit 3.0
 */
public class PowerVmAllocationPolicyMigrationStabilityGrouping extends PowerVmAllocationPolicyMigrationAbstract {

	/** The static host CPU utilization threshold to detect over utilization.
	 * It is a percentage value from 0 to 1
	 * that can be changed when creating an instance of the class. */
	private double utilizationThreshold = 0.9;

	/**
	 * Instantiates a new PowerVmAllocationPolicyMigrationStaticThreshold.
	 * 
	 * @param hostList the host list
	 * @param vmSelectionPolicy the vm selection policy
	 * @param utilizationThreshold the utilization threshold
	 */
	public PowerVmAllocationPolicyMigrationStabilityGrouping(
			List<? extends Host> hostList,
			PowerVmSelectionPolicy vmSelectionPolicy,
			double utilizationThreshold) {
		super(hostList, vmSelectionPolicy);
		setUtilizationThreshold(utilizationThreshold);
	}

	/**
	 * Checks if a host is over utilized, based on CPU usage.
	 * 
	 * @param host the host
	 * @return true, if the host is over utilized; false otherwise
	 */
	@Override
	protected boolean isHostOverUtilized(PowerHost host) {
		addHistoryEntry(host, getUtilizationThreshold());
		double totalRequestedMips = 0;
		for (Vm vm : host.getVmList()) {
			totalRequestedMips += vm.getCurrentRequestedTotalMips();
		}
		double utilization = totalRequestedMips / host.getTotalMips();
		return utilization > getUtilizationThreshold();
	}

	/**
	 * Sets the utilization threshold.
	 * 
	 * @param utilizationThreshold the new utilization threshold
	 */
	protected void setUtilizationThreshold(double utilizationThreshold) {
		this.utilizationThreshold = utilizationThreshold;
	}

	/**
	 * Gets the utilization threshold.
	 * 
	 * @return the utilization threshold
	 */
	protected double getUtilizationThreshold() {
		return utilizationThreshold;
	}

	/**
	 * Finds a PM that has enough resources to host a given VM
	 * and that will not be overloaded after placing the VM on it.
	 * The selected host will be that one with most efficient
	 * power usage for the given VM.
	 * 
	 * @param vm the VM
	 * @param excludedHosts the excluded hosts
	 * @return the host found to host the VM
	 */
	@Override
	public PowerHost findHostForVm(Vm vm, Set<? extends Host> excludedHosts) {
		double minRelativeVarianceDiff = Double.MAX_VALUE;
		PowerHost allocatedHost = null;

		for (PowerHost host : this.<PowerHost> getHostList()) {
			//Basic exclusion check
			if (excludedHosts.contains(host)) {
				continue;
			}
			// Check if host is able to contain VM at all
			if (host.isSuitableForVm(vm)) {
				//Set a base host currently switched off should no room be available
				if (allocatedHost == null) {allocatedHost = host; continue;}
				//Check if host is on, and wont over utilize after allocation
				if (getUtilizationOfCpuMips(host) == 0 || isHostOverUtilizedAfterAllocation(host, vm)) {
					continue;
				}

				try {
					double hostRelativeVariance = getHostRelativeStdDevAfterAllocation(host, vm);
					//Check the new relative variance and if the new mean + new stddev doesn't exceed the threshold metric
					if (hostRelativeVariance != -1
							&& ((getHostMeanAfterAllocation(host, vm) + getHostStdDevAfterAllocation(host, vm))
									< (host.getTotalMips() * getUtilizationThreshold()) ) ) {

						double relativeVarianceDiff = hostRelativeVariance - host.getPower();
						if (relativeVarianceDiff < minRelativeVarianceDiff) {
							minRelativeVarianceDiff = relativeVarianceDiff;
							allocatedHost = host;

						}
					}
				} catch (Exception e) {
				}
			}
		}
		return allocatedHost;
	}

	private double getHostRelativeStdDevAfterAllocation(PowerHost host, Vm vm) {
		double totalRelativeStdDev = -1;
		if (host.getVmList().size() != 0) {
			double vmMips = vm.getMips();
			double totalHostMips = 0;
			for (Vm hostVm : host.getVmList()) {
				totalHostMips += hostVm.getMips();
			}
			double toalMipsStdDev = getHostStdDevAfterAllocation(host, vm);
			totalRelativeStdDev = (toalMipsStdDev) / (Math.max( (vmMips + totalHostMips), 0.000000000001)) ; //added div-0 protection! 
		}
		return totalRelativeStdDev;
	}

	private double getHostStdDevAfterAllocation(PowerHost host, Vm vm) {
		double totalStdDev = 0;
		double vmVariance = ((PowerVm)vm).getUtilizationVariance();
		double totalHostVariance = 0;
		for (Vm hostVm : host.getVmList()) {
			totalHostVariance += ((PowerVm)hostVm).getUtilizationVariance();
		}
		totalStdDev = Math.sqrt(vmVariance + totalHostVariance);
		return totalStdDev;
	}

	private double getHostMeanAfterAllocation (PowerHost host, Vm vm) {
		double totalMean = 0;
		totalMean += ((PowerVm)vm).getUtilizationMean();

		for (Vm hostVm : host.getVmList()) {
			totalMean += ((PowerVm)hostVm).getUtilizationMean();
		}

		return totalMean;
	}
}
