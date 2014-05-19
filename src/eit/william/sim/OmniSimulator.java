package eit.william.sim;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

import eduni.simjava.Sim_system;
import eit.william.magnitudes.AccelerationVsTimeEvent;
import eit.william.magnitudes.Distance;
import eit.william.magnitudes.Distance_km;
import eit.william.magnitudes.Distance_m;
import eit.william.magnitudes.Speed;
import eit.william.magnitudes.Time;
import eit.william.magnitudes.Time_Hour;
import eit.william.magnitudes.Time_Min;
import eit.william.magnitudes.Time_Sec;
import eit.william.mobility.Linear_MobilityModel;
import eit.william.mobility.Location;
import eit.william.mobility.MobileEntity;
import eit.william.mobility.MobilityModel;
import eit.william.mobility.Train;
import eit.william.mobility.Vessle_1D;
import eit.william.network_entities.LinearNeighbourForwardAffiliation;
import eit.william.network_entities.Network;
import eit.william.network_entities.RadioNode;
import eit.william.network_entities.ServiceHostNode;
import eit.william.service.FileTransfer1998;
import eit.william.service.Service;
import eit.william.service.Uniform_ServiceSelector;
import eit.william.stats_distributions.RequestMeasurement;
import eit.william.world_entities.User;

	
	public class OmniSimulator{
		
		// Constants
		static final int NBR_USERS_PER_CAR = 40;
		static final int NBR_CARS = 3; 
		final Time_Min SIMULATION_DURATION = new Time_Min(10); // min

		Distance RBS_RAD = new Distance_m(350); // 850m
		final Speed INIT_SPEED = new Speed(new Distance_km(110), new Time_Hour(1.0));

		private static final String TRANSIONS_DATA_FILE_NAME = "TRANSITIONS.csv";
		private static final String QUEUE_LENGTH_DATA_FILE_NAME = "QUEUE_LENGTH.csv";
		private static final String WAITING_TIME_DATA_FILE_NAME = "WAITING_TIME.csv";
		private static final String TRANSFERED_DATA_FILE_NAME = "TRANSFERED.csv";
		private static final String PROCESSED_DATA_FILE_NAME = "PROCESSED.csv";
		private static final String REQUESTS_DATA_FILE_NAME = "REQUESTS.csv";
		private static final String TRANSFERED_MASS_DATA_FILE_NAME = "TRANSFERED_MASS.csv";
		private static final String RADIO_NODE_ASS_DATA_FILE_NAME = "ASSOCIATIONS.csv";
		private static final String ARRIVAL_RATE_DATA_FILE_NAME = "ARRIVAL_RATE.csv";
		private static final String SESSION_MIGRATION_COUNT_DATA_FILE_NAME = "SESSION_MIGRATION_COUNT.csv";
		private static final String SESSION_RESIDENCE_COUNT_DATA_FILE_NAME = "SESSION_RESIDENCE_COUNT.csv";

		private static final String FOLDER_NAME = "results";
		
		// Entities
		ArrayList<RadioNode> rbsEntities;
		ArrayList<ServiceHostNode> serviceHosts;
		ArrayList<User> users;
		ArrayList<Service> services;
		ArrayList<MobileEntity> cars;
		ArrayList<MobileEntity> mobileEntities;
		MobilityModel mobilityModel;
		Network network;
		
		Clock clock_mobility;
		Clock clock_network;
		Clock clock_progress;

		ArrayList<AccelerationVsTimeEvent> acc_vs_time;
		
		double mean_arrival_rate;
		
		private HashMap<Integer, HashMap<Integer, ArrayList<RequestMeasurement>>> processed_requests;
		
		public OmniSimulator(){
			Sim_system.initialise(); // Initialise Sim_system
			
			processed_requests = new HashMap<Integer, HashMap<Integer, ArrayList<RequestMeasurement>>>();
			/*
			 * Clocks
			 */
			clock_mobility = new Clock_Regular("Clock_Mobility", new Time_Sec(0.1), Clock.CLOCK_EVENT);
			clock_network = new Clock_Regular("Clock_Network", new Time_Sec(0.1), Clock.CLOCK_EVENT);
			clock_progress = new Clock_ServeFirst("Clock_Progress", new Time_Min(1), Clock.CLOCK_EVENT_2);
	
			
			/*
			 * Acceleration vs. time
			 */
			acc_vs_time = new ArrayList<AccelerationVsTimeEvent>();
			acc_vs_time.add(new AccelerationVsTimeEvent(0, 0));
			acc_vs_time.add(new AccelerationVsTimeEvent(0, 2));
	
	
			/*
			 * Services
			 */
			services = new ArrayList<Service>();
			//services.add(new YouTube_Regular_Duration_Service("YouTube"));
			//services.add(new PrimitiveService("PrimitievService"));
			Service service = new FileTransfer1998("FileTransfer_1998");
			
			System.out.println("Mean session time = " + service.getMeanSessionTime().toMin() + " min");
			
			RBS_RAD = new Distance_m(service.getMeanSessionTime().toMin()*INIT_SPEED.tomPerMin()/2);
			services.add(service);
	
			
			/*
			 * Radio nodes
			 */
			rbsEntities = new ArrayList<RadioNode>();
			serviceHosts = new ArrayList<ServiceHostNode>();
			provisionFixedTimeRBS(new Time_Min(4.0), SIMULATION_DURATION, RBS_RAD, rbsEntities, serviceHosts);
	
			
			/*
			 * Mobile entities
			 */
			// Mobile Users
			users = new ArrayList<User>();
			cars = new ArrayList<MobileEntity>();
	
			for(int car = 0; car < NBR_CARS ; car++){
				ArrayList<User> carUsers = new ArrayList<User>();
				for(int user = 0; user < NBR_USERS_PER_CAR ; user++){
					carUsers.add(new User("User" + car + "." + user, 2, rbsEntities, new Uniform_ServiceSelector("UnformServiceSelector", services), User.TYPE_MOBILE));
				}
				users.addAll(carUsers);
				cars.add(new Vessle_1D(carUsers, 30.0, 4.0, 2, car));
			}
	
			mobileEntities = new ArrayList<MobileEntity>();
			mobileEntities.add(new Train(cars));
	
			mobilityModel = new Linear_MobilityModel("MobilityModel", mobileEntities, INIT_SPEED , acc_vs_time);
	
			// Network
			network = new Network("Network", users, rbsEntities, new LinearNeighbourForwardAffiliation(rbsEntities, RBS_RAD));
			
			/*
			 * Other...
			 */
			
			mean_arrival_rate = service.getMeanArrivalRate().toMin();
			
		}
		
		public void simulate(int sample_run, double load){
			System.out.println("System time: " + Sim_system.sim_clock());
			
			String file_prefix = load + "_" + sample_run + "_";
			Time t_s = new Time_Min(( 1.0/( mean_arrival_rate*users.size() ) )*load); // <----- Set service time
			System.out.println("\t Mean service time = " + t_s.toSec() + " sec.");
			for(ServiceHostNode node: serviceHosts){
				node.setMeanServiceTime(t_s);
			}
				
			/*
			 * Simulation associations
			 */
			// Clock to mobility model
			Sim_system.link_ports(clock_mobility.get_name(), Clock.OUT_PORT_NAME, mobilityModel.get_name(), MobilityModel.IN_PORT_NAME);
			Sim_system.link_ports(clock_progress.get_name(), Clock.OUT_PORT_NAME, mobilityModel.get_name(), MobilityModel.IN_PORT_NAME);
			Sim_system.link_ports(clock_network.get_name(),  Clock.OUT_PORT_NAME, network.get_name(), Network.IN_PORT_NAME);
	
			for (int i=0; i < serviceHosts.size()-1; i++){
				Sim_system.link_ports(serviceHosts.get(i).get_name(), ServiceHostNode.NEIGHBOUR_PORT_NAME, serviceHosts.get(i+1).get_name(), ServiceHostNode.IN_PORT_NAME);
				//System.out.println("Pairing " + serviceHosts.get(i).get_name() + " with " + serviceHosts.get(i+1).get_name());
			}	
	
			Sim_system.set_termination_condition(Sim_system.TIME_ELAPSED, SIMULATION_DURATION.toMin(), false);
			
			System.out.println("Simulation starting");
			Sim_system.run(); // Run the simulation
			
			/*
			 * Intra-sesson RBS association transitions
			 */ 
			FileWriter fw;
/*			System.out.print("- Dumping intra-sesson RBS association transitions from users. \t \t");

			try {
				fw = new FileWriter(file_prefix + TRANSIONS_DATA_FILE_NAME, false);
				for(User user : users)
					fw.append(user.dumpData());
				fw.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
			System.out.println("[Done]");
			*/
			System.out.print("- Dumping queue length measurements from service nodes. \t \t");
			try {
				for(ServiceHostNode shn :serviceHosts){
					fw = new FileWriter(file_prefix + shn.get_name() + "_" + QUEUE_LENGTH_DATA_FILE_NAME , false);
					fw.append(shn.getMeasureData(ServiceHostNode.QUEUE_LENGTH_MEAS));
					fw.close();
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
			System.out.println("[Done]");
			
			System.out.print("- Dumping " + ServiceHostNode.SESSION_MIGRATION_COUNT + " from service nodes. \t \t");
			try {
				for(ServiceHostNode shn :serviceHosts){
					fw = new FileWriter(file_prefix + shn.get_name() + "_" + SESSION_MIGRATION_COUNT_DATA_FILE_NAME , false);
					fw.append(shn.getMeasureData(ServiceHostNode.SESSION_MIGRATION_COUNT));
					fw.close();
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
			System.out.println("[Done]");
			
			System.out.print("- Dumping " + ServiceHostNode.SESSION_RESIDENCE_COUNT + " from service nodes. \t \t");
			try {
				for(ServiceHostNode shn :serviceHosts){
					fw = new FileWriter(file_prefix + shn.get_name() + "_" + SESSION_RESIDENCE_COUNT_DATA_FILE_NAME , false);
					fw.append(shn.getMeasureData(ServiceHostNode.SESSION_RESIDENCE_COUNT));
					fw.close();
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
			System.out.println("[Done]");
			
			System.out.print("- Dumping waiting time measurements from service nodes. \t \t");
			try {
				for(ServiceHostNode shn :serviceHosts){
					fw = new FileWriter(file_prefix + shn.get_name() + "_" + WAITING_TIME_DATA_FILE_NAME , false);
					fw.append(shn.getMeasureData(ServiceHostNode.WATITING_TIME_MEAS));
					fw.close();
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
			System.out.println("[Done]");
			/*
			System.out.print("- Dumping transfered tasks measurements from service nodes. \t \t");
			try {
				for(ServiceHostNode shn :serviceHosts){
					fw = new FileWriter(file_prefix + shn.get_name() + "_" + TRANSFERED_DATA_FILE_NAME , false);
					fw.append(shn.getMeasureData(ServiceHostNode.TRANSFERED_MEAS));
					fw.close();
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
			System.out.println("[Done]");
			
			System.out.print("- Dumping processed tasks measurements from service nodes. \t \t");
			try {
				for(ServiceHostNode shn :serviceHosts){
					fw = new FileWriter(file_prefix + shn.get_name() + "_" + PROCESSED_DATA_FILE_NAME , false);
					fw.append(shn.getMeasureData(ServiceHostNode.PROCESSED_MEAS));
					fw.close();
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
			System.out.println("[Done]");
			
			System.out.print("- Dumping request proportional mass measurements from service nodes. \t");
			try {
				for(ServiceHostNode shn :serviceHosts){
					fw = new FileWriter(file_prefix + shn.get_name() + "_" + TRANSFERED_MASS_DATA_FILE_NAME, false);
					fw.append(shn.getMeasureData(ServiceHostNode.TRANSFERED_PROPORTIONAL_MASS_MEAS));
					fw.close();
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
			System.out.println("[Done]");
			
			System.out.print("- Dumping " + ServiceHostNode.ARRIVAL_RATE_MEAS + " from service nodes. \t \t \t");
			try {
				for(ServiceHostNode shn :serviceHosts){
					fw = new FileWriter(file_prefix + shn.get_name() + "_" + ARRIVAL_RATE_DATA_FILE_NAME , false);
					fw.append(shn.getMeasureData(ServiceHostNode.ARRIVAL_RATE_MEAS));
					fw.close();
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
			System.out.println("[Done]");
			
			
			System.out.print("- Dumping request measurements from service nodes. \t \t \t");
			for(ServiceHostNode shn :serviceHosts){
				processed_requests.putAll(shn.getCapturedPacketDataFull());
			}
			
			String result = analyseRequests();
			
			try {
				fw = new FileWriter(file_prefix + REQUESTS_DATA_FILE_NAME, false);
				fw.append(result);
				fw.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
			System.out.println("[Done]");*/
			
			
			System.out.print("- Dumping session measurements from service nodes. \t \t \t");
			try {
				int index = 1;
				for(ServiceHostNode shn :serviceHosts){
					fw = new FileWriter(load + "_" + index + "_sessions.csv", true);
					fw.append(shn.getSessionInteractionData());
					fw.close();
					index ++;
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
			System.out.println("[Done]");
			
			/*System.out.print("- Dumping radio node associations from network. \t \t \t");
			try {
				for(RadioNode rn :rbsEntities){
					fw = new FileWriter(file_prefix + rn.get_name() + "_" + RADIO_NODE_ASS_DATA_FILE_NAME , false);
					fw.append(network.getMeasureData(rn.getNbr()));
					fw.close();
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
			System.out.println("[Done]");*/
		}
		
		private String analyseRequests() {
			StringBuffer sb = new StringBuffer();
			double mean_waiting_time;
			double total_waiting_time;
			int transitions;
			int session_size;
			int user_nbr;
			
			HashMap<Integer, User> users_map = new HashMap<Integer, User>();
			
			for(User user:users)
				users_map.put(user.get_id(), user);
			
			for(Integer user: processed_requests.keySet()){
				for(Integer session: processed_requests.get(user).keySet()){
					mean_waiting_time = 0;
					total_waiting_time = 0;
	
					transitions = users_map.get(user).transitions.get(session);
					session_size = processed_requests.get(user).get(session).get(0).session_size;
					for(RequestMeasurement rm : processed_requests.get(user).get(session)){
						mean_waiting_time = rm.waiting_time/session_size;
						total_waiting_time += rm.waiting_time;
					}
					sb.append(transitions + ";" + total_waiting_time + ";" + mean_waiting_time + "\r");
				}
			}
			
			return sb.toString();
		}
	
		public  Time provisionFixedRBS(Time start, Time sim_end, int nbr_nodes, Distance rbsRad, ArrayList<RadioNode> radioNodes, ArrayList<ServiceHostNode> serviceHosts){
	
			System.out.println("- Provisioning network -");
			
			if(start.toMin() > sim_end.toMin()){
				System.err.println("Start time is beyond the simulation time");
			}
			
			Distance start_dist = Distance.calculateTraveledDistance(start, INIT_SPEED, acc_vs_time);
			
			int index = 1;
	
			for(int i=(int) start_dist.tom() ; i < start_dist.tom()+nbr_nodes*RBS_RAD.tom()*2 ; i+=rbsRad.tom()*2){
				serviceHosts.add(new ServiceHostNode("RBS_"+index+"_service_node", new Time_Sec(0.01)));
				radioNodes.add(new RadioNode("RBS_"+index, new Location(i, 0),serviceHosts.get(index-1),index-1));
				Sim_system.link_ports("RBS_"+index, "RBS_"+index+"_service_node", "RBS_"+index+"_service_node", ServiceHostNode.IN_PORT_NAME);
				System.out.println("\t Radio node " + index + " at " + i + " m");
				index ++;
			}
			
			System.out.println("Provisioned " + radioNodes.size() + " radio nodes");
			System.out.println("------------------------");
			
			return Time.calculateTimeTo(new Distance_m(start_dist.tom()+nbr_nodes*RBS_RAD.tom()*2 + rbsRad.tom()*2), INIT_SPEED, acc_vs_time);
		}
		
		public  void provisionFixedTimeRBS(Time start, Time sim_end, Distance rbsRad, ArrayList<RadioNode> radioNodes, ArrayList<ServiceHostNode> serviceHosts){
	
			
			System.out.println("- Provisioning network - ");
			System.out.println(" Cell coverage radius = " + rbsRad.tom() + "m");
			
			if(start.toMin() > sim_end.toMin()){
				System.err.println("Start time is beyond the simulation time");
			}
			
			Distance start_dist = Distance.calculateTraveledDistance(start, INIT_SPEED, acc_vs_time);
			Distance end_dist = Distance.calculateTraveledDistance(sim_end, INIT_SPEED, acc_vs_time);
			
			int index = 1;
	
			for(int i=(int) start_dist.tom() ; i < end_dist.tom() ; i+=rbsRad.tom()*2){
				serviceHosts.add(new ServiceHostNode("RBS_"+index+"_service_node", new Time_Sec(0.0000001)));
				radioNodes.add(new RadioNode("RBS_"+index, new Location(i, 0),serviceHosts.get(index-1), index-1 ));
				Sim_system.link_ports("RBS_"+index, "RBS_"+index+"_service_node", "RBS_"+index+"_service_node", ServiceHostNode.IN_PORT_NAME);
				//System.out.println("\t Radio node " + index + " at " + i + " m");
				index ++;
			}
			
			System.out.println("Provisioned " + radioNodes.size() + " radio nodes");
			System.out.println("------------------------");
		}
		
		public ArrayList<RadioNode> provisionFlexibleRBS(ArrayList<AccelerationVsTimeEvent> acc_vs_time2, Speed initSpeed, Distance rbsRad){
	
			ArrayList<RadioNode> result = new ArrayList<RadioNode>();
	
			System.out.println("- Provisioning network -");
			double x_dispacement = 0.0;
	
			double speed = initSpeed.tomPerMin();
	
			double t_1 = 0;
			double t_2;
	
			for(int i = 0; i < acc_vs_time2.size()-1; i++){
				t_2 = acc_vs_time2.get(i + 1).getTime().toMin();
	
				x_dispacement += (t_2 - t_1)*(speed + acc_vs_time2.get(i).getAcceleration().tomPerMin2()*(t_2 - t_1)/2); // Acceleration from prev event
				speed += acc_vs_time2.get(i).getAcceleration().tomPerMin2()*(t_2 - t_1);
	
				t_1 = t_2;
			}
	
			int index = 1;
			int rr = (int) rbsRad.tom();
	
			for(int i=rr ; i<x_dispacement ; i+=rr*2){
				System.out.println("\t Radio node " + index + " at " + i + " m");
				serviceHosts.add(new ServiceHostNode("RBS_"+index+"_service_node", new Time_Sec(0.001)));
				result.add(new RadioNode("RBS_"+index, new Location(i, 0),serviceHosts.get(index-1), index-1 ));
				Sim_system.link_ports("RBS_"+index, "RBS_"+index+"_service_node", "RBS_"+index+"_service_node", ServiceHostNode.IN_PORT_NAME);
				index ++;
			}
			
			System.out.println("Provisioned " + result.size() + " radio nodes");
			System.out.println("------------------------");
			return result;
		}
		
	}
