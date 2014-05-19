package eit.william.service;

import eduni.simjava.Sim_entity;
import eit.william.magnitudes.Time;

public abstract class Service extends Sim_entity{

	public Service(String name) {
		super(name);
	}

	public abstract int getSessionSize();
	
	public abstract Time getInterRequestTime();
	
	public abstract Time getInterSessionTime();
	
	public abstract Time getMeanInterRequestTime();
	
	public abstract Time getMeanInterSessionTime();

	public abstract Time getMeanArrivalRate();
	
	public abstract Time getMeanSessionTime();
}
