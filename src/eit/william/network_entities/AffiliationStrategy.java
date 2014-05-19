package eit.william.network_entities;

import java.util.ArrayList;

import eit.william.world_entities.User;

public abstract class AffiliationStrategy {

	protected ArrayList<RadioNode> rbs_enteties; 
	
	public AffiliationStrategy(ArrayList<RadioNode> rbs_enteties){
		this.rbs_enteties = rbs_enteties;
	}
	
	public abstract int AssertAffiliation(User user);

}
