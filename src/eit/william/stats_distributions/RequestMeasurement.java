package eit.william.stats_distributions;

public class RequestMeasurement {

	public int transitions;
	public double waiting_time;
	public double start_time;
	public int from;
	public int session;
	public int session_size;
	public int request;
	
	public RequestMeasurement(int from, int session, int request, int session_size, double start_time){
		this.from = from;
		this.session = session;
		this.request = request;
		this.session_size = session_size;
		this.start_time = start_time;
		
		transitions = 0;
		waiting_time = 0;
	}
}
