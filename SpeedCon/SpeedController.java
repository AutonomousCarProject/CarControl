package SpeedCon;

public class SpeedController {
	
	private final double MAX_SPEED = 20;
	private final double MIN_SPEED = 1;
	
	private double currentEstimatedSpeed;
	private double desiredSpeed;
	
	public void calculateDesiredSpeed(){
		double curveSteepness = 0; // Steering.getCurveSteepness();
		desiredSpeed = Math.max((1 - curveSteepness)*MAX_SPEED, MIN_SPEED);
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
