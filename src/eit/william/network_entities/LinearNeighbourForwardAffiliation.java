package eit.william.network_entities;

import java.util.ArrayList;

import eit.william.magnitudes.Distance;
import eit.william.world_entities.User;

public class LinearNeighbourForwardAffiliation extends AffiliationStrategy {
	
	private Distance node_range;
	
	public LinearNeighbourForwardAffiliation(ArrayList<RadioNode> rbs_enteties, Distance node_range) {
		super(rbs_enteties);
		this.node_range = node_range;
	}

	@Override
	public synchronized int AssertAffiliation(User user) {
		int current_affiliation = user.getRadioNodeAffiliation();
		int init_node;
		
		if (current_affiliation == Network.NO_AFFILIATION){
			init_node = 0;
		}
		else{ 
			init_node = current_affiliation;
		}
		
		int result = assessAffiliatino(init_node, user);
		
		user.assignRadioNodeAffiliation(result);
		
		return result;
	}
	
	private synchronized boolean inRange(RadioNode rn, User u){
		return rn.getLocation().getDistance(u.getLocation()) <= node_range.tom();
	}
	
	private synchronized int assessAffiliatino(int start_node, User user){		
		for(int node=start_node; node < rbs_enteties.size(); node++){
			if (inRange(rbs_enteties.get(node),user))
				return node;
		}
		
		return Network.NO_AFFILIATION;
	}
}
