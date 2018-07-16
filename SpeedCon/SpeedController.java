package SpeedCon;

public class SpeedController {
	
	private double currentEstimatedSpeed;
	private double desiredSpeed;
	
	public void calculateDesiredSpeed(){
		double curveSteepness = 0; // Steering.getCurveSteepness();
		desiredSpeed = Math.max((1 - curveSteepness)*Constants.MAX_SPEED, Constants.MIN_SPEED);
	}
	
	public double getEstimatedSpeed(){
		return currentEstimatedSpeed; 
	}
	
	public void setEstimatedSpeed(double estimatedSpeed){
		currentEstimatedSpeed = estimatedSpeed;
	}
	
	public int getDesiredSpeed(){
		return (int)desiredSpeed;
	}
	
}
