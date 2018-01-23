package workingspace;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.cloudbus.cloudsim.Cloudlet;
import org.cloudbus.cloudsim.Log;
import org.cloudbus.cloudsim.UtilizationModel;

public class CloudletLoadGenerator {

	public CloudletLoadGenerator() {
		// TODO Auto-generated constructor stub

	}
	/**
	 * @see workingspace.CloudletLoadGenerator#GenerateCloudletList(int, double, double, Cloudlet, long)
	 * Auto generates a seed from the sampleCloudlet id.
	 * @param numberOfCloudlets
	 * @param cloudletsPerSecond
	 * @param secondsVarianceRange
	 * @param sampleCloudlet
	 * @return List[] = {cloudletList, delayList}
	 */

	@SuppressWarnings("rawtypes")
	List[] GenerateCloudletList(int numberOfCloudlets, double cloudletsPerSecond, double secondsVarianceRange, Cloudlet sampleCloudlet){
		long seed = sampleCloudlet.getCloudletId();
		return GenerateCloudletList( numberOfCloudlets, cloudletsPerSecond, secondsVarianceRange, sampleCloudlet, seed);
	}


	/**
	 * Creates a list of cloudlets and a list of delays to be used by the DatacenterBroker with Delay capabilities.
	 * The SampleCloudlet will be the first entry in the list, and all cloudlets in the list will have incrementing id's, starting with SampleCloudlet's id.
	 * 
	 * @param cloudlets
	 * @param averageCloudletDelay
	 * @param secondsVarianceRange the maximum time that the delay can deviate from the perfect rate.
	 * @param sampleCloudlet
	 * @param seed A seed for the internal random function.
	 * @return List[] = {cloudletList, delayList}
	 */

	@SuppressWarnings("rawtypes")
	List[] GenerateCloudletList(int numberOfCloudlets, double averageCloudletDelay, double secondsVarianceRange, Cloudlet sampleCloudlet, long seed){

		List<Cloudlet> cloudletList = new ArrayList<Cloudlet>();
		List<Double> delayList = new ArrayList<Double>();

		int id;
		double delay = 0;
		long length = sampleCloudlet.getCloudletLength();
		long fileSize = sampleCloudlet.getCloudletFileSize();
		long outputSize = sampleCloudlet.getCloudletOutputSize();
		int pesNumber = sampleCloudlet.getNumberOfPes();
		UtilizationModel utilizationModelCpu = sampleCloudlet.getUtilizationModelCpu();
		UtilizationModel utilizationModelRam = sampleCloudlet.getUtilizationModelRam();
		UtilizationModel utilizationModelBw = sampleCloudlet.getUtilizationModelBw();
		int brokerId = sampleCloudlet.getUserId();

		Random generator = new Random(seed);

		cloudletList.add(sampleCloudlet);
		delayList.add(delay);

		for (int i=1; i<numberOfCloudlets; i++) {
			id = sampleCloudlet.getCloudletId() + i;
			Cloudlet cloudlet = 
					new Cloudlet(id, length, pesNumber, fileSize, 
							outputSize, utilizationModelCpu, utilizationModelRam, 
							utilizationModelBw);
			cloudlet.setUserId(brokerId);
			//cloudlet.setVmId(vmid);

			// add the cloudlet to the list
			cloudletList.add(cloudlet);

			//Calculate a delay
			delay += averageCloudletDelay;
			delay += ((generator.nextDouble() * 2) - 1) * secondsVarianceRange;
			if (delay < 0.0) {
				delay = 0.0;
			}

			delayList.add(delay);

		}

		List[] output = {cloudletList, delayList};
		return output;
	}

}
