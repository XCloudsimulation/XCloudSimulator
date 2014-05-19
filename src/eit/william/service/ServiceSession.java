package eit.william.service;

import java.util.ArrayList;

import eit.william.stats_distributions.Stats;

public class ServiceSession {

	private ArrayList<Double> latencies;
	private ArrayList<String> radioNodes;

	private double nbr_requests;
	
	@SuppressWarnings("unused")
	private double t_start;
	
	public ServiceSession(int nbr_requests, double t_start) {
		this.nbr_requests = nbr_requests;
		this.t_start = t_start;
		
		latencies = new ArrayList<Double>();
		radioNodes = new ArrayList<String>();
	}
	
	public synchronized void addRequest(double latency, String radioNode){
		latencies.add(latency);
		radioNodes.add(radioNode);
	}
	
	public String Dump(){
		System.out.println("---------- " + nbr_requests + "/" + latencies.size());
		for(double d:latencies)
			System.out.println(d);
		
		System.out.println("--- " + Stats.getMean(latencies) + ";" + Stats.getVariance(latencies) + ";" + Stats.getUniqueCount(radioNodes) + "\r");
		return Stats.getMean(latencies) + ";" + Stats.getVariance(latencies) + ";" + Stats.getUniqueCount(radioNodes) + "\r";
	}

	public synchronized double getNbr_requests() {
		return nbr_requests;
	}

}
