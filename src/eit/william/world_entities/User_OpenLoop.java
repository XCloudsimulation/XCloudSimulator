package eit.william.world_entities;

import java.util.ArrayList;

import eduni.simjava.Sim_system;
import eit.william.network_entities.Network;
import eit.william.network_entities.RadioNode;
import eit.william.service.Service;
import eit.william.service.ServiceSelector;
import eit.william.stats_distributions.RequestMeasurement;

public class User_OpenLoop extends User {

	public User_OpenLoop(String name, double inter_lambda,
			ArrayList<RadioNode> radioNodes, ServiceSelector service_selector,
			String type) {
		super(name, inter_lambda, radioNodes, service_selector, type);
	}

	@Override
	public void body(){
		assignRadioNodeAffiliation(0);
		requests = 0;
		sessions = 0;

		while(Sim_system.running()){
			sessions ++;
			
			Service current_service = service_selector.getSelectService();
			
			sim_pause(current_service.getInterSessionTime().toMin());
			
			requests = current_service.getSessionSize();
			//System.out.println(get_name() + " New session spawned with " +  requests + " requests, at " + Sim_system.sim_clock() + " min.");
			
			int nbr_trans = 0;
			int prev_node;
			int curr_node = getRadioNodeAffiliation();

			double t_2;
			double t_1 = Sim_system.sim_clock();
			
			if (curr_node != Network.NO_AFFILIATION){
				sim_schedule(rbs_ports.get(curr_node), 0.0, Network.NEW_SESSION);
			
				for(int i=0; i<requests; i++){
					sim_pause(current_service.getInterRequestTime().toMin());
	
					// Evaluate node affiliation transition - local variable
					prev_node = curr_node;
					curr_node = getRadioNodeAffiliation();
	
					if (prev_node!=curr_node && curr_node >= 1){ 
						//System.out.println(get_name() + " - " + get_id() + ", being handed over from " + prev_node + " to " + curr_node);
						nbr_trans ++;
						sim_schedule(rbs_ports.get(prev_node), 0.0, Network.HANDOVER_SIGNAL, new Integer(get_id()));
					}
	
					sim_schedule(rbs_ports.get(curr_node), 0.0, Network.DATA_PACKET, new RequestMeasurement(get_id(), sessions-1, i, requests, Sim_system.sim_clock()));
					sim_trace(1,"Sending request " + (i+1) + "/" + requests + ", in session " + sessions + ", to RBS" + curr_node);
				}
			}
			t_2 = Sim_system.sim_clock();
			
			stat.update(MID_SESSION_HANDOVER_MEASUREMENT, nbr_trans, t_1, t_2);
			transitions.add(nbr_trans);
		}	
	}
}
