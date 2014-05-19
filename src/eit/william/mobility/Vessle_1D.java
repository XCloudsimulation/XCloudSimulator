package eit.william.mobility;

import java.util.ArrayList;

import eit.william.world_entities.User;

public class Vessle_1D implements MobileEntity {
	ArrayList<User> users;
	
	public Vessle_1D(ArrayList<User> users, double dim_x, double offset, int row_width, int car_nbr){
		this.users = users;
		
		//System.out.println("- Configuring Vessle ---");
		
		int rows = (users.size()/row_width);
		double row_pitch = dim_x/rows;
		
		//System.out.println("\t" + rows + " rows, " + row_pitch + " row pitch");
		
		int index = 0;
		for(int row = 0; row < rows; row ++){
			for(int seat = 0; seat < row_width; seat ++){
				users.get(index).incrementX(row*row_pitch+car_nbr*dim_x+car_nbr*offset);
				index++;
			}
		}
		
		//System.out.println("------------------------");
	}
	
	public void updateLoction(Location location) {
		for(User user: users){
			user.updateLoction(location);
		}
	}

	public void incrementX(double x) {
		for(User user: users){
			user.incrementX(x);
		}
	}

	public void incrementY(double y) {
		for(User user: users){
			user.incrementY(y);
		}
	}

}
