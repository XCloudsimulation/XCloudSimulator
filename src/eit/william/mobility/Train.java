/**
 * 
 */
package eit.william.mobility;

import java.util.ArrayList;

/**
 * @author eit-wit
 *
 */
public class Train implements MobileEntity {
	
	ArrayList<MobileEntity> vessles;
	
	public Train(ArrayList<MobileEntity> vessles){
		this.vessles = vessles;
	}
	
	public void updateLoction(Location location){
		for(MobileEntity entity: vessles){
			entity.updateLoction(location);
		}
	}

	public void incrementX(double x) {
		for(MobileEntity entity: vessles){
			entity.incrementX(x);
		}
	}

	public void incrementY(double y) {
		for(MobileEntity entity: vessles){
			entity.incrementX(y);
		}
	}
}
