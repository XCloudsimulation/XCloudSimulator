package eit.william.sim;

import eduni.simjava.Sim_system;
import eit.william.magnitudes.Time;

public class Clock_Regular extends Clock {

	public Clock_Regular(String name, Time time, int pulse_ID) {
		super(name, time, pulse_ID);
	}

	public void body(){
		while(Sim_system.running()){
			sim_pause(period);
			sim_schedule(out_port, 0.0, pulse_ID);
		}	
	}
}
