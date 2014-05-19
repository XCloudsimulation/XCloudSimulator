package eit.william.mobility;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

import eduni.simjava.Sim_system;
import eit.william.magnitudes.AccelerationVsTimeEvent;
import eit.william.magnitudes.Speed;

/**
 * Linear mobility model:
 * MobileEntetiy object are moved in displaced along a linear path. Velocity is in m/m, time in minutes, and displacement is in m. 
 * 
 * The model requires:
 * 	- Initial speed (double) in m/m
 * 	- Acceleration (double) in m/m^2
 * 	- Time (double) in min
 * 	- Acceleration vs. time array with at least 2 points.
 * 
 * @author      William Tarneberg
 */

public class Linear_MobilityModel extends MobilityModel{
	
	private ArrayList<AccelerationVsTimeEvent> acc_vs_time;
	private Speed init_speed;
	
	private double speed;
	
	private int event_index;
	
	public Linear_MobilityModel(String name, ArrayList<MobileEntity> mobileEntities, Speed init_speed, ArrayList<AccelerationVsTimeEvent> acc_vs_time) {
		super(name, mobileEntities);
		this.acc_vs_time = acc_vs_time;
		this.init_speed = init_speed;
		acc_vs_time.get(acc_vs_time.size()-1).getTime().toMin();
	}

	protected void ResetModel() {
		speed = init_speed.tomPerMin();
		t_prev = 0;
		t_now = 0;
		event_index = 0;
	}
	
	private double calculateDistanceTraveled(){
		double x_dispacement = 0.0;
		AccelerationVsTimeEvent current = acc_vs_time.get(event_index);
		
		if(event_index+1 < acc_vs_time.size() && acc_vs_time.get(event_index + 1).getTime().toMin() < t_now ){
			
			AccelerationVsTimeEvent next = acc_vs_time.get(event_index+1);
			
			x_dispacement = (speed + 
					current.getAcceleration().tomPerMin2() * (next.getTime().toMin()-t_prev)/2 + 
					next.getAcceleration().tomPerMin2() * (t_now - next.getTime().toMin())/2)*(t_now-t_prev); // speed.
			
			speed += (current.getAcceleration().tomPerMin2() * (next.getTime().toMin()-t_prev) + 
					next.getAcceleration().tomPerMin2() * (t_now - next.getTime().toMin()));
			
			event_index ++;
			
		} else{
			x_dispacement = (speed + (current.getAcceleration().tomPerMin2()*(t_now-t_prev))/2) * (t_now-t_prev) ; 
			
			speed += current.getAcceleration().tomPerMin2()*(t_now-t_prev);
		}
		
		return x_dispacement;
	}

	public synchronized void UpdateLocation() {
		double distance_travelled = calculateDistanceTraveled();
		for(MobileEntity me : mobileEnteties){
			me.incrementX(distance_travelled);
		}
	}
	
	
	
	/*@Override
	protected synchronized void UpdateProgress() {
		double sys_time = Sim_system.sim_clock();
		long glob_time = System.currentTimeMillis();
		String timeString = "?";
		if(prev_world_progress_time != -1){
			double remaining_sim_time = total_time-sys_time;
			double sample_time = sys_time-prev_sim_progress_time;
			
			Duration d = new Duration( (long)(((glob_time-prev_world_progress_time)/sample_time)*remaining_sim_time) );
			timeString = d.getStandardHours() + "h " + (d.getStandardMinutes()-d.getStandardHours()*60) + "m " + (d.getStandardSeconds()-d.getStandardMinutes()*60) + "s";
			//timeString = (((glob_time-prev_world_progress_time)/sample_time)*remaining_sim_time)/1000 + "ms" + remaining_sim_time + ", sample time = " + sample_time;
			
			// Temp
			d = new Duration((long)(((glob_time-prev_world_progress_time)/sample_time)*total_time));
			System.out.println(get_name() + " - " +  "Estimated total simulation time: " + d.getStandardHours() + "h " + (d.getStandardMinutes()-d.getStandardHours()*60) + "m " + (d.getStandardSeconds()-d.getStandardMinutes()*60) + "s @ " + glob_time);
		} else {
			prev_world_progress_time = glob_time;
			prev_sim_progress_time = sys_time;
			System.out.println(get_name() + " - " + "Sim start time: " + glob_time);
		}

		String message = 	"Total Displacement: " + (int)total_displacement + " m, v: " + (int)(speed/1000)*60.0 + " km/h, a: "  + (int)acc_vs_time.get(event_index).getAcceleration().tomPerMin2() + " km/m^2" + 
							//" |" + blanks((int) (sys_time/total_time)*10) + (speed>0? '>' : speed<0 ? '<' : '-') + blanks((int) ((1-(sys_time/total_time))*10)) + "|" +
							", ERST : " + timeString + "\r";
		
		System.out.println(get_name() + " - " + message);
		sim_trace(1,message);
	}*/
	
	@SuppressWarnings("unused")
	private String blanks(int blanks){
		// Slow implementation how two String builders instead.
		StringBuilder result = new StringBuilder();
		for(int i=0; i<blanks; i++){
			result.append(" ");
		}
		
		return result.toString();
	}

	protected void UpdateProgress() {
		System.out.println("# - " + Sim_system.sim_clock());
		writeToFile(System.currentTimeMillis() + "\r");
	}
	
	public void writeToFile(String string){
		FileWriter fw;
		try {
			fw = new FileWriter("remainig time estimator.csv", true);
			fw.append(string);
			fw.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
