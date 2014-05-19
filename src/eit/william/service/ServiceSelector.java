package eit.william.service;

import java.util.ArrayList;

import eduni.simjava.Sim_entity;
import eit.william.magnitudes.Time;

public abstract class ServiceSelector extends Sim_entity{

	protected ArrayList<Service> services; 
	protected int nbr_services;
	
	public ServiceSelector(String name, ArrayList<Service> services) {
		super(name);
		this.services = services;
		nbr_services = services.size();
	}

	public abstract Time interSessionTime();
	
	public abstract Service getSelectService();
	
	public abstract ServiceSession getSession();
}
