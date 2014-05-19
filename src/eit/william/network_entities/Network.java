package eit.william.network_entities;

import java.util.ArrayList;
import java.util.HashMap;

import eduni.simjava.Sim_entity;
import eduni.simjava.Sim_event;
import eduni.simjava.Sim_port;
import eduni.simjava.Sim_system;
import eit.william.stats_distributions.Measurement;
import eit.william.world_entities.User;

public class Network extends Sim_entity{
	
	public static final int NO_AFFILIATION = -1;
	
	public static final int HANDOVER_SIGNAL = 450;
	public static final int DATA_PACKET = 560;
	public static final int NEW_SESSION = 660;
	
	public static final String IN_PORT_NAME = "IN";

	private ArrayList<User> hosted_entities;
	private AffiliationStrategy affiliation_strategy;
	
	private Sim_port in_port;
	
	private static HashMap<Integer, ArrayList<Measurement>> node_allocations;
	
	public Network(String name, ArrayList<User> hosted_entities, ArrayList<RadioNode> rbs_enteties, AffiliationStrategy affiliation_strategy) {
		super(name);
		this.hosted_entities = hosted_entities;
		this.affiliation_strategy = affiliation_strategy;
		sim_trace(1, "Initilizing Network");
		in_port = new Sim_port(IN_PORT_NAME);
		add_port(in_port);
		
		sim_trace(1, " - Created - With " + hosted_entities.size() + " hosted entities of type " + hosted_entities.getClass() + 
				", and " + rbs_enteties.size() + " radio nodes" +
				"\t Type: " + this.getClass());
		
		node_allocations = new HashMap<Integer, ArrayList<Measurement>>();
		
		UpdateNodeAssociations();
	}

	@Override
	public void body(){
		while(Sim_system.running()){
			Sim_event e = new Sim_event();
			sim_get_next(e);	// Get the next event
			
			UpdateNodeAssociations(); // Updates node affiliation based on every clock pulse.
			
			sim_completed(e);	// The event has completed service
		}
	}
	
	public boolean AddEntity(User user){
		hosted_entities.add(user);
		return true;
	}
	
	public void RemoveUser(int id){
		hosted_entities.remove(id);
	}
	
	public synchronized void UpdateNodeAssociations(){
		HashMap<Integer, Integer> updated_node_allocations = new HashMap<Integer, Integer>();
		
		for(User user: hosted_entities){
			int node = affiliation_strategy.AssertAffiliation(user);
			if(!updated_node_allocations.containsKey(node)){
				updated_node_allocations.put(node, 0);
			}
			updated_node_allocations.put(node,updated_node_allocations.get(node) + 1);
		}
		
		for(Integer node: updated_node_allocations.keySet()){
			if(!node_allocations.containsKey(node)){
				node_allocations.put(node, new ArrayList<Measurement>());
			}
			node_allocations.get(node).add(new Measurement(updated_node_allocations.get(node),Sim_system.sim_clock()));
		}
	}

	public String getMeasureData(int rn){
		StringBuffer sb = new StringBuffer();

		for(Measurement target: node_allocations.get(rn))
			sb.append(target.value + ";" + target.time + "\r");
		
		return sb.toString();
	} 
	
	public static String getType(int tag){
		switch(tag){
		case DATA_PACKET:
			return "DATA PACKET";
		case HANDOVER_SIGNAL:
			return "HANDOVER SIGNAL";
		default:
			return "UNKNOWN TYPE";
		}
	};

}

