package SpeedCon;

public class SpeedController {
	
	private double currentEstimatedSpeed;
	private double desiredSpeed;
	
	public SpeedController(){
		SpeedFinder speedFinder = new SpeedFinder();
	}
	
	public void calculateDesiredSpeed(){
		double curveSteepness = 0; // Steering.getCurveSteepness();
		desiredSpeed = Math.max((1 - curveSteepness)*Constants.MAX_SPEED, Constants.MIN_SPEED);
	}
	
	public double getEstimatedSpeed(){
		
		return currentEstimatedSpeed; 
	}
	
	public void calculateEstimatedSpeed(int gasAmount){
		currentEstimatedSpeed = speedFinder.calculateSpeed(gasAmount);
	}
	
	public int getDesiredSpeed(){
		return (int)desiredSpeed;
	}
	
}
