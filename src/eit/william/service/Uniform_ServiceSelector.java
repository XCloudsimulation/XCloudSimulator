package eit.william.service;

import java.util.ArrayList;
import java.util.Random;

import eit.william.magnitudes.Time;
import eit.william.magnitudes.Time_Min;

public class Uniform_ServiceSelector extends ServiceSelector {

	private Random rnd;
	
	public Uniform_ServiceSelector(String name, ArrayList<Service> services) {
		super(name, services);
		rnd = new Random();
	}

	@Override
	public void body(){}
	
	public Service getSelectService() {
		return services.get(rnd.nextInt(nbr_services));
	}

	public Time interSessionTime() {
		return new Time_Min(5*rnd.nextInt());
	}

	public ServiceSession getSession() {
		return null;
	}
}
