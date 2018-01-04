
package workingspace;

/*
 * Title:        T.B.D.
 * Description:  Model used to test different allocation algorithms for VM's
 */

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.LinkedList;
import java.util.List;

import org.cloudbus.cloudsim.Cloudlet;
import org.cloudbus.cloudsim.CloudletScheduler;
import org.cloudbus.cloudsim.CloudletSchedulerSpaceShared;
import org.cloudbus.cloudsim.CloudletSchedulerTimeShared;
import org.cloudbus.cloudsim.CloudletSchedulerDynamicWorkload;
import org.cloudbus.cloudsim.Datacenter;
import org.cloudbus.cloudsim.DatacenterBroker;
import org.cloudbus.cloudsim.DatacenterCharacteristics;
import org.cloudbus.cloudsim.Host;
import org.cloudbus.cloudsim.HostDynamicWorkload;
import org.cloudbus.cloudsim.Log;
import org.cloudbus.cloudsim.Pe;
import org.cloudbus.cloudsim.Storage;
import org.cloudbus.cloudsim.UtilizationModel;
import org.cloudbus.cloudsim.UtilizationModelFull;
import org.cloudbus.cloudsim.UtilizationModelStochastic;
import org.cloudbus.cloudsim.Vm;
import org.cloudbus.cloudsim.VmAllocationPolicySimple;
import org.cloudbus.cloudsim.VmSchedulerSpaceShared;
import org.cloudbus.cloudsim.VmSchedulerTimeShared;
import org.cloudbus.cloudsim.VmSchedulerTimeSharedOverSubscription;
import org.cloudbus.cloudsim.core.CloudSim;
import org.cloudbus.cloudsim.provisioners.BwProvisionerSimple;
import org.cloudbus.cloudsim.provisioners.PeProvisionerSimple;
import org.cloudbus.cloudsim.provisioners.RamProvisionerSimple;

import workingspace.VmAlt;
import workingspace.VmSchedulerTimeSharedOverSubscriptionAlt;

/**
 * A simple example showing how to create a data center with one host and run one cloudlet on it.
 */
public class CloudSimModel1 {
	/** The cloudlet list. */
	private static List<Cloudlet> cloudletList;
	/** The vmlist. */
	private static List<Vm> vmlist;

	/**
	 * Creates main() to run this example.
	 *
	 * @param args the args
	 */
	@SuppressWarnings("unused")
	public static void main(String[] args) {
		Log.printLine("Starting CloudSimModel1...");
		
		try {
			// First step: Initialize the CloudSim package. It should be called before creating any entities.
			int num_user = 1; // number of cloud users
			Calendar calendar = Calendar.getInstance(); // Calendar whose fields have been initialized with the current date and time.
 			boolean trace_flag = false; // trace events

			/* Comment Start - Dinesh Bhagwat 
			 * Initialize the CloudSim library. 
			 * init() invokes initCommonVariable() which in turn calls initialize() (all these 3 methods are defined in CloudSim.java).
			 * initialize() creates two collections - an ArrayList of SimEntity Objects (named entities which denote the simulation entities) and 
			 * a LinkedHashMap (named entitiesByName which denote the LinkedHashMap of the same simulation entities), with name of every SimEntity as the key.
			 * initialize() creates two queues - a Queue of SimEvents (future) and another Queue of SimEvents (deferred). 
			 * initialize() creates a HashMap of of Predicates (with integers as keys) - these predicates are used to select a particular event from the deferred queue. 
			 * initialize() sets the simulation clock to 0 and running (a boolean flag) to false.
			 * Once initialize() returns (note that we are in method initCommonVariable() now), a CloudSimShutDown (which is derived from SimEntity) instance is created 
			 * (with numuser as 1, its name as CloudSimShutDown, id as -1, and state as RUNNABLE). Then this new entity is added to the simulation 
			 * While being added to the simulation, its id changes to 0 (from the earlier -1). The two collections - entities and entitiesByName are updated with this SimEntity.
			 * the shutdownId (whose default value was -1) is 0    
			 * Once initCommonVariable() returns (note that we are in method init() now), a CloudInformationService (which is also derived from SimEntity) instance is created 
			 * (with its name as CloudInformatinService, id as -1, and state as RUNNABLE). Then this new entity is also added to the simulation. 
			 * While being added to the simulation, the id of the SimEntitiy is changed to 1 (which is the next id) from its earlier value of -1. 
			 * The two collections - entities and entitiesByName are updated with this SimEntity.
			 * the cisId(whose default value is -1) is 1
			 * Comment End - Dinesh Bhagwat 
			 */
			CloudSim.init(num_user, calendar, trace_flag);

			// Second step: Create Datacenters
			// Datacenters are the resource providers in CloudSim. We need at
			// list one of them to run a CloudSim simulation
			Datacenter datacenter0 = createDatacenter("Datacenter_0");

			// Third step: Create Broker
			DatacenterBroker broker = createBroker();
			int brokerId = broker.getId();

			// Fourth step: Create one virtual machine
			vmlist = new ArrayList<Vm>();

			// VM description
			int vmid = 0;
			int mips = 1000;
			long size = 10000; // image size (MB)
			int ram = 512; // vm memory (MB)
			long bw = 1000;
			int pesNumber = 8; // number of cpus
			String vmm = "Xen"; // VMM name
			//CloudletScheduler sched = new CloudletSchedulerDynamicWorkload(mips, pesNumber);
			//CloudletScheduler sched = new CloudletSchedulerSpaceShared();
			CloudletScheduler sched = new CloudletSchedulerSpaceSharedAlt(mips, pesNumber);
			//CloudletScheduler sched = new CloudletSchedulerTimeShared();
			//CloudletScheduler sched = new CloudletSchedulerTimeSharedAlt(mips, pesNumber);

			// create VM
			Vm vm = new VmAlt(vmid, brokerId, mips, pesNumber, ram, bw, size, vmm, sched);

			// add the VM to the vmList
			vmlist.add(vm);

			// submit vm list to the broker
			broker.submitVmList(vmlist);

			// Fifth step: Create one Cloudlet
			cloudletList = new ArrayList<Cloudlet>();

			// Cloudlet properties
			int id = 0;
			long length = 400000;
			long fileSize = 300;
			long outputSize = 300;
			//UtilizationModel utilizationModel = new UtilizationModelFull();
			//UtilizationModel utilizationModel = new UtilizationModelStochastic();
			UtilizationModel utilizationModel = new UtilizationModelFull();
			
			for (int i=0; i<10; i++) {
				id = i;
				pesNumber = 1;
				Cloudlet cloudlet = 
						new Cloudlet(id, length, pesNumber, fileSize, 
								outputSize, utilizationModel, utilizationModel, 
								utilizationModel);
				cloudlet.setUserId(brokerId);
				cloudlet.setVmId(vmid);

				// add the cloudlet to the list
				cloudletList.add(cloudlet);
			}
			// submit cloudlet list to the broker
			broker.submitCloudletList(cloudletList);

			printCloudletList(cloudletList);
			
			// Sixth step: Starts the simulation
			CloudSim.startSimulation();

			CloudSim.stopSimulation();

			//Final step: Print results when simulation is over
			List<Cloudlet> newList = broker.getCloudletList();
			printCloudletList(cloudletList);

			Log.printLine("CloudSimExample1 finished!");
		} catch (Exception e) {
			e.printStackTrace();
			Log.printLine("Unwanted errors happen");
		}
	}

	/**
	 * Creates a new Host in the specified Host list
	 * 
	 * @param hostList	Hostlist that the host needs to be added to
	 * @param ram		The RAM avaiable in the host (MB)
	 * @param storage	The storage avaiable inb the host
	 * @param bw		The bandwith avaiable to the host
	 * @param mipsPerPE	The mips each CPU/core/PE has
	 * @param PEs		The number of CPU's/cores/PEs
	 */
	
	private static void createHost(List<Host> hostList, int ram, long storage, int bw, int mipsPerPE, int PEs) {
		// ram = host memory (MB)
		//storage = host storage
		//bw = bandwith (default 10000?)
		
		// 2. A Machine contains one or more PEs or CPUs/Cores.
		List<Pe> peList = new ArrayList<Pe>();
		
		// 3. Create PEs and add these into a list.
		for (int i=0; i<PEs; i++){
			peList.add(new Pe(i, new PeProvisionerSimple(mipsPerPE))); // need to store Pe id and MIPS Rating
		}
		
		// 4. Create Host with its id and list of PEs and add them to the list
		// of machines
		int hostId = hostList.size();
		
		hostList.add(
				new HostDynamicWorkloadAlt(
						hostId,
						new RamProvisionerSimple(ram),
						new BwProvisionerSimple(bw),
						storage,
						peList,
						new VmSchedulerTimeSharedOverSubscriptionAlt(peList)
						)
				); // This is our machine
	}
	
	
	/**
	 * Creates the datacenter.
	 *
	 * @param name the name
	 *
	 * @return the datacenter
	 */
	private static Datacenter createDatacenter(String name) {

		// Here are the steps needed to create a PowerDatacenter:
		// 1. We need to create a list to store
		// our machine
		List<Host> hostList = new ArrayList<Host>();
		
		int mipsPerPE = 1000;
		int PEs = 32;
		int ram = 256 * 1024; // host memory (MB)
		long storage = 1000000; // host storage
		int bw = 10000;
		
		for (int i=0; i<8; i++) {
			createHost(hostList, ram, storage, bw, mipsPerPE, PEs);	
		}
		
		// 5. Create a DatacenterCharacteristics object that stores the
		// properties of a data center: architecture, OS, list of
		// Machines, allocation policy: time- or space-shared, time zone
		// and its price (G$/Pe time unit).
		String arch = "x86"; // system architecture
		String os = "Linux"; // operating system
		String vmm = "Xen";
		double time_zone = 10.0; // time zone this resource located
		double cost = 3.0; // the cost of using processing in this resource
		double costPerMem = 0.05; // the cost of using memory in this resource
		double costPerStorage = 0.001; // the cost of using storage in this
										// resource
		double costPerBw = 0.0; // the cost of using bw in this resource
		LinkedList<Storage> storageList = new LinkedList<Storage>(); // we are not adding SAN
													// devices by now

		DatacenterCharacteristics characteristics = new DatacenterCharacteristics(
				arch, os, vmm, hostList, time_zone, cost, costPerMem,
				costPerStorage, costPerBw);

		// 6. Finally, we need to create a PowerDatacenter object.
		Datacenter datacenter = null;
		try {
			datacenter = new DatacenterAlt(name, characteristics, new VmAllocationPolicySimple(hostList), storageList, 0);
		} catch (Exception e) {
			e.printStackTrace();
		}

		return datacenter;
	}
	
	

	// We strongly encourage users to develop their own broker policies, to
	// submit vms and cloudlets according
	// to the specific rules of the simulated scenario
	/**
	 * Creates the broker.
	 *
	 * @return the datacenter broker
	 */
	private static DatacenterBroker createBroker() {
		DatacenterBroker broker = null;
		try {
			broker = new DatacenterBroker("Broker");
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
		return broker;
	}

	/**
	 * Prints the Cloudlet objects.
	 *
	 * @param list list of Cloudlets
	 */
	private static void printCloudletList(List<Cloudlet> list) {
		int size = list.size();
		Cloudlet cloudlet;

		String indent = "    ";
		Log.printLine();
		Log.printLine("========== OUTPUT ==========");
		Log.printLine("Cloudlet ID" + indent + "STATUS" + indent
				+ "Data center ID" + indent + "VM ID" + indent + "Time" + indent
				+ "Start Time" + indent + "Finish Time");

		DecimalFormat dft = new DecimalFormat("###.##");
		for (int i = 0; i < size; i++) {
			cloudlet = list.get(i);
			Log.print(indent + cloudlet.getCloudletId() + indent + indent);

			if (cloudlet.getCloudletStatus() == Cloudlet.SUCCESS) {
				Log.print("SUCCESS");
			} else{
				Log.print("OTHER");
			}
				
			Log.printLine(indent + indent + cloudlet.getResourceId()
					+ indent + indent + indent + cloudlet.getVmId()
					+ indent + indent
					+ dft.format(cloudlet.getActualCPUTime()) + indent
					+ indent + dft.format(cloudlet.getExecStartTime())
					+ indent + indent
					+ dft.format(cloudlet.getFinishTime()));

		}
	}
}