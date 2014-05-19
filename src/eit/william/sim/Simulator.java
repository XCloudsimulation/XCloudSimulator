package eit.william.sim;

import eit.william.email_notifier.EmailNotifier;

public class Simulator {
	
	private static final String[] NOTIFICATION_RECIPIENTS = {"william.tarneberg@gmail.com", "ettmarcus@gmail.com", "amoxus@gmail.com"};
	
	static EmailNotifier en;
	
	public static void main(String[] args) {
		
/*		int sample_runs = 10;
		double[] loads = {0.9, 1.0, 1.05, 1.1, 1.15, 1.2, 1.25};

		int nbr_simulations = loads.length*sample_runs;
		int index = 1;

		en = new EmailNotifier("eit.wit.lth@gmail.com", "smtp.gmail.com", "465", "eit.wit.lth@gmail.com", "ehuset3135");
		
		try {
			System.out.print("Sending notification email to : " + Arrays.toString(NOTIFICATION_RECIPIENTS));
			//en.sendNotification(NOTIFICATION_RECIPIENTS, "[Simulation] Omnipresent cloud simulation on " + InetAddress.getLocalHost().getHostName() + " started", "");
			System.out.println(" [Done]");
		} catch (Exception e) {
			System.err.println(" [Failed]");
			//e.printStackTrace();
		}
		
		long t_2;
		long t_1 = System.currentTimeMillis();
		*/
		OmniSimulator os;
		/*
		for(double load: loads){
			for(int i=1; i<=sample_runs; i++){
				System.out.println(" ## Simulation nbr " + index +"/"+nbr_simulations + ", Load: " + load + ", Sample run: " + i);
		*/		os = new OmniSimulator();
				os.simulate(10,.5);
/*				index++;
				System.out.println(" ########################### \r");
			}
		}
		

		t_2 = System.currentTimeMillis();

		// Calulate execution time
		Duration d = new Duration(t_2-t_1);
	
		try {
			System.out.print("\r Sending notification email to : " + Arrays.toString(NOTIFICATION_RECIPIENTS) + "\t");
			en.sendNotification(NOTIFICATION_RECIPIENTS, "[Simulation] Omnipresent cloud simulation on " + InetAddress.getLocalHost().getHostName() + " completed", "Simulation completed in " + d.getStandardHours() + "h " + (d.getStandardMinutes()-d.getStandardHours()*60) + "m " + (d.getStandardSeconds()-d.getStandardMinutes()*60) + "s, after " + nbr_simulations + " simulations.");
			System.out.println(" [Done]");
		} catch (Exception e) {
			System.err.println(" [Failed]");
			//e.printStackTrace();
		}	
*/	}
}