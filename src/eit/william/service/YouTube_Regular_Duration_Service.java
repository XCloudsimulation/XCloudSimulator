package eit.william.service;

import org.apache.commons.math3.distribution.WeibullDistribution;

import eduni.simjava.distributions.Sim_gamma_obj;
import eit.william.magnitudes.Time;
import eit.william.magnitudes.Time_Sec;

public class YouTube_Regular_Duration_Service extends Service {
	
	private Sim_gamma_obj gen_delay;
	private WeibullDistribution gen_duration;
	
	public YouTube_Regular_Duration_Service(String name) {
		super(name);
		gen_delay = new Sim_gamma_obj("Gamma_Duration", 0.182855, 0.339434);
		gen_duration = new WeibullDistribution(1.13,246.07);
		System.out.println(get_name() + " Weibull Distribution mean = " + gen_duration.getNumericalMean()/60.0);
		add_generator(gen_delay);
	}

	@Override
	public void body(){}
	
	public int getSessionSize() {
		double duration = gen_duration.sample();
		sim_trace(1, "New session of duration: " + duration/60.0 + "m");
		return (int) ((new Time_Sec(duration).toMin())/(new Time_Sec(gen_delay.sample()).toMin()));
	}

	public Time getInterRequestTime() {
		double delay;
		do{
			delay = gen_delay.sample();
		} while(delay <0);
		
		Time result = new Time_Sec(delay);
		//System.out.println(get_name() + " - Intra request delay " + delay + "m");
		sim_trace(1,"Intra request delay " + result.toMin() + " min");
		return result;
	}

	public Time getInterSessionTime() {
		// TODO Auto-generated method stub
		return new Time_Sec(0);
	}

	public Time getMeanInterRequestTime() {
		// TODO Auto-generated method stub
		return null;
	}

	public Time getMeanInterSessionTime() {
		// TODO Auto-generated method stub
		return null;
	}

	public Time getMeanArrivalRate() {
		// TODO Auto-generated method stub
		return null;
	}

	public Time getMeanSessionTime() {
		// TODO Auto-generated method stub
		return null;
	}

}
