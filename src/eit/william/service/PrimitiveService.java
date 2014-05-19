package eit.william.service;

import eit.william.magnitudes.Time;
import eit.william.magnitudes.Time_Sec;

public class PrimitiveService extends Service {

	public PrimitiveService(String name) {
		super(name);
	}

	@Override
	public void body(){};
	
	public int getSessionSize() {
		return (int) (40*Math.random()) + 1;
	}

	public Time getInterRequestTime() {
		return new Time_Sec(0.1*Math.random());
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
