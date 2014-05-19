package eit.william.sim;

import eduni.simjava.Sim_system;
import eit.william.magnitudes.Time;

/*
 * Produces clock pulses with an interval of "period"
 */
public class Clock_ServeFirst extends Clock{

	public Clock_ServeFirst(String name, Time time, int pulse_ID) {
		super(name, time, pulse_ID);
	}
	
	public void body(){
		while(Sim_system.running()){
			sim_schedule(out_port, 0.0, pulse_ID);
			sim_pause(period);
		}	
	}
}
