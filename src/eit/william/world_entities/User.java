package eit.william.world_entities;

import java.io.StringWriter;
import java.util.ArrayList;

import eduni.simjava.Sim_entity;
import eduni.simjava.Sim_port;
import eduni.simjava.Sim_stat;
import eduni.simjava.Sim_system;
import eit.william.mobility.Location;
import eit.william.mobility.MobileEntity;
import eit.william.network_entities.Network;
import eit.william.network_entities.RadioNode;
import eit.william.service.Service;
import eit.william.service.ServiceSelector;
import eit.william.stats_distributions.My_exponential_obj;
import eit.william.stats_distributions.RequestMeasurement;

public abstract class User extends Sim_entity implements MobileEntity{

	public static final String TYPE_MOBILE = "TYPE_MOBILE";
	public static final String TYPE_STATIC = "TYPE_STATIC";
	
	private static final String MID_SESSION_HANDOVER_MEASUREMENT = "Mid-session handovers";

	private Sim_stat stat;

	public ArrayList<Integer> transitions;

	private Location location;

	private ArrayList<Sim_port> rbs_ports;

	private int radionNode_affiliation; 

	private ServiceSelector service_selector;

	private String type;

	private My_exponential_obj inter_generator;

	private int sessions;
	private int requests;

	public User(String name, double inter_lambda, ArrayList<RadioNode> radioNodes, ServiceSelector service_selector, String type) {
		super(name);

		inter_generator = new My_exponential_obj("Poissonian inter session", inter_lambda);
		add_generator(inter_generator);

		this.service_selector = service_selector;

		this.location = new Location(0, 0);

		rbs_ports = new ArrayList<Sim_port>();

		for(RadioNode rn: radioNodes){
			Sim_port temp = new Sim_port(rn.get_name());
			rbs_ports.add(temp);
			add_port(temp);
			Sim_system.link_ports(get_name(), rn.get_name(), rn.get_name(), RadioNode.IN_PORT_NAME);
		}

		transitions = new ArrayList<Integer>();

		this.type = type;

		// Setup statistics
		stat = new Sim_stat();
		stat.add_measure(MID_SESSION_HANDOVER_MEASUREMENT, Sim_stat.STATE_BASED);
		stat.calc_proportions(MID_SESSION_HANDOVER_MEASUREMENT, new double[]{0,1,2,3,4});
		set_stat(stat);

		sim_trace(1, get_name() + " - Created -");
	}

	public synchronized int getRadioNodeAffiliation() {
		return radionNode_affiliation;
	}

	public synchronized void assignRadioNodeAffiliation(int radioNode_affiliation){
		this.radionNode_affiliation = radioNode_affiliation;
		
		sim_trace(1,"Changed affiliation to " + radioNode_affiliation);
	}

	public synchronized Location getLocation() {
		return location;
	}

	@Override
	public synchronized void incrementX(double x){
		location.x += x;
	}

	@Override
	public synchronized void incrementY(double y){
		location.y += y;
	}

	@Override
	public synchronized void updateLoction(Location location) {
		this.location = location;
	}

	public String dumpData(){
		StringWriter sw = new StringWriter(transitions.size()*2);

		for(Integer i: transitions)
			sw.append(i+";");
		
		return sw.toString();
	}
}