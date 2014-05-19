package eit.william.network_entities;

import java.util.ArrayList;
import java.util.HashMap;

import org.apache.commons.math3.distribution.ExponentialDistribution;

import eduni.simjava.Sim_entity;
import eduni.simjava.Sim_event;
import eduni.simjava.Sim_from_p;
import eduni.simjava.Sim_port;
import eduni.simjava.Sim_stat;
import eduni.simjava.Sim_system;
import eduni.simjava.Sim_type_p;
import eit.william.magnitudes.Time;
import eit.william.magnitudes.Time_mSec;
import eit.william.stats_distributions.Measurement;
import eit.william.stats_distributions.RequestMeasurement;
import eit.william.stats_distributions.Session_Measurement;

public class ServiceHostNode extends Sim_entity {

	public static final String IN_PORT_NAME = "IN";
	public static final String NEIGHBOUR_PORT_NAME = "NEIGHBOUR";

	/*
	 * Simulation measurements
	 */
	public static final String QUEUE_LENGTH_MEAS = "Queue length";
	public static final String WATITING_TIME_MEAS = "Waiting time";
	public static final String TRANSFERED_MEAS = "Transfered requests";
	public static final String PROCESSED_MEAS = "Processed requests";
	public static final String TRANSFERED_PROPORTIONAL_MASS_MEAS = "Transfered proportional mass";
	public static final String ARRIVAL_RATE_MEAS = "Arrival rate";
	public static final String SESSION_RESIDENCE_COUNT = "Session residence count";
	public static final String SESSION_MIGRATION_COUNT = "Sessino migration count";
	
	/*
	 * Simulation setup
	 */
	public static final Time MEAN_SERVICE_TIME = new Time_mSec(10.0);
	public static final Time MEAN_MIGRATION_TIME = new Time_mSec(40.0);
	
	private static final boolean migrate = true;
	
	// Service time
	private ExponentialDistribution service_time;
	private Sim_port in_port, neighbour_port;
	private Sim_stat stat;

	private HashMap<String,ArrayList<Measurement>> meas;

	private HashMap<Integer, HashMap<Integer, ArrayList<RequestMeasurement>>> processed_requests;
	private HashMap<Integer, HashMap<Integer, Session_Measurement>> sessions;
	
	public ServiceHostNode(String name, Time st) {
		super(name);
		service_time = new ExponentialDistribution(MEAN_SERVICE_TIME.toMin());
		//System.out.println(get_name() + " mean service time = " + service_time.getMean() + " min, sample : " + service_time.sample() + " min");

		stat = new Sim_stat();
		stat.add_measure(Sim_stat.THROUGHPUT);
		stat.add_measure(Sim_stat.ARRIVAL_RATE);
		stat.add_measure(Sim_stat.QUEUE_LENGTH);
		stat.add_measure(Sim_stat.RESIDENCE_TIME);
		stat.add_measure(Sim_stat.UTILISATION);
		stat.add_measure(Sim_stat.WAITING_TIME);
		set_stat(stat);

		sim_trace(1, get_name() + " - Created - With service time " + st.toSec() + " s \t Type: " + this.getClass());

		in_port = new Sim_port(IN_PORT_NAME);
		add_port(in_port);
		
		neighbour_port = new Sim_port(NEIGHBOUR_PORT_NAME);
		add_port(neighbour_port);

		meas = new HashMap<String,ArrayList<Measurement>>();
		meas.put(QUEUE_LENGTH_MEAS, new ArrayList<Measurement>());
		meas.put(WATITING_TIME_MEAS, new ArrayList<Measurement>());
		meas.put(TRANSFERED_MEAS, new ArrayList<Measurement>());
		meas.put(TRANSFERED_PROPORTIONAL_MASS_MEAS, new ArrayList<Measurement>());
		meas.put(SESSION_RESIDENCE_COUNT, new ArrayList<Measurement>());
		meas.put(SESSION_MIGRATION_COUNT, new ArrayList<Measurement>());
		
		processed_requests = new HashMap<Integer, HashMap<Integer, ArrayList<RequestMeasurement>>>();
		sessions = new HashMap<Integer, HashMap<Integer, Session_Measurement>>();
	}

	@Override
	public void body(){
		Sim_event e;
		
		int session_migration_count = 0;
		int session_completion_count = 0;
		int session_residence_count = 0;
		
		while(Sim_system.running()){
			
			if(migrate){
				e = new Sim_event();
				
				// Look for new sessions
				sim_select(new Sim_type_p(Network.NEW_SESSION), e);
				while(e.get_tag() == Network.NEW_SESSION){
					session_residence_count ++;
					//System.out.println("New session from " + e.get_src());
					e = new Sim_event();
					sim_select(new Sim_type_p(Network.NEW_SESSION), e);
				}
				
				// Look for handover signals
				sim_select(new Sim_type_p(Network.HANDOVER_SIGNAL), e);
				while(e.get_tag() == Network.HANDOVER_SIGNAL){
					//System.out.println(get_name() + " - Initiating migration. " + sim_waiting());
					
					//meas.get(WATITING_TIME_MEAS).add(new Measurement(e.end_waiting_time()-e.event_time(), e.end_waiting_time()));
					meas.get(QUEUE_LENGTH_MEAS).add(new Measurement(sim_waiting(), e.end_waiting_time()));
					
					int from = e.get_src();
					
					// Collect event to be migrated with corresponding ID
					e = new Sim_event();
					sim_select(new Sim_from_p(from),e);
					
					//sim_pause(MEAN_MIGRATION_TIME.toMin());
					
					while(e.scheduled_by() == from){	
						
						//meas.get(WATITING_TIME_MEAS).add(new Measurement(e.end_waiting_time()-e.event_time(), e.end_waiting_time()));
						meas.get(QUEUE_LENGTH_MEAS).add(new Measurement(sim_waiting(), Sim_system.sim_clock()));
						
						RequestMeasurement rm = (RequestMeasurement) e.get_data();
						rm.transitions ++;
						
						addMigrated(rm);
						
						e.set_data(rm);
						send_on_intact(e,neighbour_port);

						e = new Sim_event();
						sim_select(new Sim_from_p(from),e);
					}
					
					//System.out.println("\t " + get_name() + " " + count + " packets moved. " + sim_waiting());
					meas.get(SESSION_MIGRATION_COUNT).add(new Measurement(session_migration_count, Sim_system.sim_clock()));
					e = new Sim_event();
					
					session_migration_count ++;
				}
				
				//System.out.println(e==null?"no migration":"After migration, queue length = " + sim_waiting());
			}
			
			e = new Sim_event();
			sim_get_next(e);	// Get the next event
			
			if(e.get_tag() == Network.DATA_PACKET){
				RequestMeasurement rm = (RequestMeasurement) e.get_data();
				
				meas.get(WATITING_TIME_MEAS).add(new Measurement(e.end_waiting_time()-rm.start_time, Sim_system.sim_clock()));
				meas.get(QUEUE_LENGTH_MEAS).add(new Measurement(sim_waiting(), Sim_system.sim_clock()));
				
				double delay = service_time.sample();
				
				sim_process(delay);
				
				sim_trace(1, "Processed package from " + e.get_src() + ", waiting time " + Sim_system.sim_clock());
				
				rm.waiting_time = Sim_system.sim_clock()-rm.start_time;
				addRequest(rm);
				addProcessed(rm);
				
				sim_completed(e);
				
				if(rm.request==rm.session_size-1){
					session_completion_count ++;
					//System.out.println("Session completed : " + rm.request + "/" + rm.session_size);
				}
				
			}
			
			meas.get(SESSION_RESIDENCE_COUNT).add(new Measurement(session_residence_count - session_completion_count, Sim_system.sim_clock()));
		}
	}

	public String writeMeasurements(){
		StringBuffer sb = new StringBuffer();

		for(String key: meas.keySet()){
			for(Measurement target: meas.get(key))
				sb.append(target.value + ";" + target.time + "\r");
		}
		
		return sb.toString();
	}
	
	public String getMeasureData(String key){
		StringBuffer sb = new StringBuffer();

		for(Measurement target: meas.get(key))
			sb.append(target.value + ";" + target.time + "\r");
		
		return sb.toString();
	}
	
	public String getCapturedPacketData(){
		StringBuffer sb = new StringBuffer();
		
		for(Integer user: processed_requests.keySet()){
			for(Integer session: processed_requests.get(user).keySet()){
				for(RequestMeasurement rm : processed_requests.get(user).get(session)){
					sb.append(rm.from + ";" + rm.request + ";" + rm.session + ";" + rm.session_size + ";" +  rm.transitions + ";" + rm.waiting_time + "\r");
				}
			}
		}
		
		return sb.toString();
	}
	
	public String getSessionInteractionData(){
		StringBuffer sb = new StringBuffer();
		
		for(Integer user: sessions.keySet()){
			for(Integer session: sessions.get(user).keySet()){
				Session_Measurement target = sessions.get(user).get(session);
				sb.append(target.processed + ";" + target.migrated + ";" + target.size + "\r");
			}
		}
		
		return sb.toString();
	}
	
	public HashMap<Integer, HashMap<Integer, ArrayList<RequestMeasurement>>> getCapturedPacketDataFull(){
		return processed_requests;
	};
	
	public void setMeanServiceTime(Time t_s){
		service_time = new ExponentialDistribution(t_s.toMin());
	}
	
	private void addRequest(RequestMeasurement rm){
		if(!processed_requests.containsKey(rm.from)){
			processed_requests.put(rm.from, new HashMap<Integer, ArrayList<RequestMeasurement>>());
		}
		
		if(!processed_requests.get(rm.from).containsKey(rm.session)){
			processed_requests.get(rm.from).put(rm.session, new ArrayList<RequestMeasurement>());
		}
		processed_requests.get(rm.from).get(rm.session).add(rm);
	}
	
	private void addProcessed(RequestMeasurement rm){
		getSession(rm).processed++;
	}
	
	private void addMigrated(RequestMeasurement rm){
		getSession(rm).migrated++;
	}
	
	private Session_Measurement getSession(RequestMeasurement rm){
		if(!sessions.containsKey(rm.from)){
			sessions.put(rm.from, new HashMap<Integer, Session_Measurement>());
		}
		
		if(!sessions.get(rm.from).containsKey(rm.session)){
			sessions.get(rm.from).put(rm.session, new Session_Measurement());
			sessions.get(rm.from).get(rm.session).size = rm.session_size;
		}
		
		return sessions.get(rm.from).get(rm.session);
		
	}
	
	private int getSessionCount(){
		int result = 0;
		for(Integer user: sessions.keySet()){
			result += sessions.get(user).size();
		}
		return result;
	}
}