package eit.william.service;

import org.apache.commons.math3.distribution.WeibullDistribution;

import eit.william.magnitudes.Time;
import eit.william.magnitudes.Time_Min;
import eit.william.magnitudes.Time_Sec;
import jsc.distributions.Pareto;

public class FileTransfer1998 extends Service {
	
	private Pareto requestSize,fileSize, offTime_pareto; // Shape = alpha, Location = k
	private WeibullDistribution offTime;

	public FileTransfer1998(String name) {
		super(name);
		requestSize = new Pareto(1000.0, 1.0);
		fileSize = new Pareto(133000.0, 1.1); // Tail
		//fileSize = new LogNormalDistribution(9.357, 1.318);
		offTime = new WeibullDistribution(1.46, 0.382);
		offTime_pareto = new Pareto(1, 1.5); 
		
		System.out.println("- " + get_name() + " ----");
		System.out.println(" Request size: " + getMeanRequetSize() + " KB");
		System.out.println(" File size: " + getMeanFileSize() + " KB");
		System.out.println(" Inter request time: " + getMeanInterRequestTime().toSec() + " sec");
		System.out.println(" Inter session time: " + getMeanInterSessionTime().toSec() + " sec");
		System.out.println(" Mean arrival rate: " + getMeanArrivalRate().toMin() + " per min");
		System.out.println("-------------------------");
	}
	
	@Override
	public void body(){}

	public synchronized int getSessionSize() {
		return (int) (fileSize.random()/requestSize.random());
	}

	public synchronized Time getInterRequestTime() {
		return new Time_Sec(offTime.sample());
	}

	public synchronized Time getInterSessionTime() {
		return new Time_Sec(offTime_pareto.random());
	}
	
	@Override
	public synchronized Time getMeanArrivalRate(){
		int p = (int)getMeanSessionSize();
		double irr = p*getMeanInterRequestTime().toMin();
		double isr = getMeanInterSessionTime().toMin();
		
		return new Time_Min(p/(irr+isr));
	}

	public synchronized Time getMeanInterRequestTime() {
		return new Time_Sec(offTime.getNumericalMean());
	}

	public synchronized Time getMeanInterSessionTime() {
		return new Time_Sec(offTime_pareto.mean());
	}
	
	private synchronized double getMeanFileSize(){
		return fileSize.mean();
	}
	
	private synchronized double getMeanRequetSize(){
		double result = 0;
		for(int i=0; i < 50000; i++)
		{
			result += requestSize.random()/50000.0;
		}
		return result;
	}
	
	private synchronized double getMeanSessionSize(){
		return getMeanFileSize()/getMeanRequetSize();
	}

	public synchronized Time getMeanSessionTime() {
		return new Time_Min(0.7);
	}
}
