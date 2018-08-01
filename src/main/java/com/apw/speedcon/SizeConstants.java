package com.apw.speedcon;

import java.util.List;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class SizeConstants {
	
	/**
	 * This is a file of size constants, so that we can accurately tell our distance from an object by knowing its width 
	 * once we have identified it.
	 * 
	 * <p> All heights/widths stored as doubles with units in meters
	 */
	
	//Order of variable types within categories (boolean -> int -> double)
	//Add more if need be

	
	public static final double
	HEIGHT_HUMAN = 1.69,
	WIDTH_HUMAN = .45;
	
	/**
	 * SIGN_INFO is a map of sizes that match to different street signs.
	 * 
	 * <p>Unfortunately, streetsigns come in 5 different sizes. These sizes are:
	 * <p>1. Conventional Road Size
	 * <p>2. Expressway Size
	 * <p>3. Freeway Size
	 * <p>4. Minimum Size
	 * <p>5. Oversized Size
	 * 
	 * <p>For the purposes of this data, the sizes will always be in that order.
	 * 
	 * <p>This map only contains signs that are rectangular, triangular, circular, or octagonal in shape. If you need to detect a railroad
	 * crossing or something similar, this map will not help you.
	 * 
	 * <p>All shapes are regular, except for rectangles, which do not have to be. As a result, rectangles have 10 values of relevance,
	 * while other shapes only have 5.
	 * 
	 * <p>Depending upon where you are, the streetsign will be one of these sizes. Our code cannot currently figure this out, so
	 * we just assume that we are on a conventional road, and leave it at that. However, in future years, it might be a good
	 * idea to account for different places having different sized signs. Fortunately for whoever has to do that, they have this
	 * map.
	 * 
	 * <p>When given a String as a key, the map will return an Integer Arraylist. 
	 * The first value in this list dictates the shape of the sign, and the meaning of the following values
	 * <p>If the first value is a:
	 * <p>0.0 -- The sign is a rectangle, and the next 10 values are heights and widths for each road size, e.g. {0,ht0,wd0,ht1,wd1,ht2,wd2....}
	 * <p>1.0 -- The sign is an equilateral triangle, and the next 5 values are the side length for each road size
	 * <p>2.0 -- The sign is a circle, and the next 5 values are the diameter of the sign for each road size
	 * <p>3.0 -- The sign is octagonal, and the next 5 values are widths.
	 * 
	 * <p>Arrays are doubles, which represent meters.
	 * 
	 * <p>If a value is a zero, it does not exist. For example, there is no standard stopsign size for freeways. It will be up to 
	 * whoever needs this code to figure out what to do in these edge cases. If I were you, however, I would check for an expressway size
	 * while on the freeway, and vice-versa, etc...
	 * 
	 * <p>If a key value ends with 'Ground', it is ground mounted, 'Post' means post mounted,
	 * 'Overhead' means mounted on a streetlight or something similar.
	 */
	
	public final Map<String, List<Double>> SIGN_INFO = new HashMap<String, List<Double>>();
	
	public SizeConstants() {
		SIGN_INFO.put("Stop", (List<Double>) Arrays.asList(3.0, .750, .900, .0, .600, 1.200));
		SIGN_INFO.put("StopLightWidth", (List<Double>) Arrays.asList(3.0, .300, .00, .0, .200, 0.0));
		SIGN_INFO.put("Stop", (List<Double>) Arrays.asList(3.0, .750, .900, .0, .600, 1.200));
		SIGN_INFO.put("Yield", (List<Double>) Arrays.asList(1.0, .900, 1.200, 1.500, .750, 0.0));
		SIGN_INFO.put("4Way", (List<Double>) Arrays.asList(0.0, .300, .150, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0));
		SIGN_INFO.put("AllWay", (List<Double>) Arrays.asList(0.0, .450, .150, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0));
		SIGN_INFO.put("SpeedLimit", (List<Double>) Arrays.asList(0.0, .600, .750, 0.900, 1.200, 1.200, 1.500, 0.0, 0.0, 0.0, 0.0));
		SIGN_INFO.put("TruckSpeedLimit", (List<Double>) Arrays.asList(0.0, .600, .600, 0.900, 0.900, 1.200, 1.200, 0.0, 0.0, 0.0, 0.0));
		SIGN_INFO.put("NightSpeedLimit", (List<Double>) Arrays.asList(0.0, .600, .600, 0.900, 0.900, 1.200, 1.200, 0.0, 0.0, 0.0, 0.0));
		SIGN_INFO.put("MinimumSpeedLimit", (List<Double>) Arrays.asList(0.0, .600, .750, 0.900, 1.200, 1.200, 1.500, 0.0, 0.0, 0.0, 0.0));
		SIGN_INFO.put("CombinedSpeedLimit", (List<Double>) Arrays.asList(0.0, .600, .1200, 0.900, 1.800, 1.200, 2.400, 0.0, 0.0, 0.0, 0.0));
		SIGN_INFO.put("ReducedSpeedAhead", (List<Double>) Arrays.asList(0.0, .600, .750, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0));
		SIGN_INFO.put("TurnProhibition", (List<Double>) Arrays.asList(0.0, .600, .600, 0.900, 0.900, 0.0, 0.0, 0.0, 0.0, 1.200, 1.200));
		SIGN_INFO.put("MandatoryMovementLaneControl", (List<Double>) Arrays.asList(0.0, .750, .900, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0));
		SIGN_INFO.put("MandatoryMovementLaneControl2", (List<Double>) Arrays.asList(0.0, .750, .750, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0));
		SIGN_INFO.put("OptionalMovementLaneControl", (List<Double>) Arrays.asList(0.0, .750, .900, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0));
		//Advance Intersection Lane Control has a variable width, so good luck with sizing that
		//SIGN_INFO.put("AdvanceIntersectionLaneControl", (List<Double>) Arrays.asList(0.0, 1.800, 1.500, 2.400, 1.800, 2.700, 1.100, 0.0, 0.0, 0.0, 0.0));
		SIGN_INFO.put("TwoWayLeftTurnOnlyOverhead", (List<Double>) Arrays.asList(0.0, .750, .900, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0));
		SIGN_INFO.put("TwoWayLeftTurnOnlyGround", (List<Double>) Arrays.asList(0.0, .600, .900, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.900, 1.200));
		SIGN_INFO.put("ReversibleLaneControl", (List<Double>) Arrays.asList(0.0, 2.700, .900, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0));
		SIGN_INFO.put("EndReverseLane", (List<Double>) Arrays.asList(0.0, 2.700, 1.200, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0));
		SIGN_INFO.put("HOV2+LaneAheadGround", (List<Double>) Arrays.asList(0.0, .750, 1.050, 0.900, 1.500, 1.200, 1.100, 0.0, 0.0, 0.0, 0.0));
		SIGN_INFO.put("CenterLaneHOV2+OnlyPost", (List<Double>) Arrays.asList(0.0, .750, 1.050, 0.0, 0.0, 1.400, 1.100, 0.0, 0.0, 0.0, 0.0));
		SIGN_INFO.put("HOV2+LaneEndsPost", (List<Double>) Arrays.asList(0.0, .750, 1.050, 0.900, 1.500, 1.200, 1.100, 0.0, 0.0, 0.0, 0.0));
		SIGN_INFO.put("HOV2+LaneAheadOverhead", (List<Double>) Arrays.asList(0.0, 1.650, 0.900, 2.100, 1.200, 2.550, 1.500, 0.0, 0.0, 0.0, 0.0));
		SIGN_INFO.put("HOV2+OnlyOverhead", (List<Double>) Arrays.asList(0.0, 1.800, 1.500, 2.400, 1.800, 2.700, 1.100, 0.0, 0.0, 0.0, 0.0));
		SIGN_INFO.put("HOV2+LaneEndsOverhead", (List<Double>) Arrays.asList(0.0, 1.650, 0.900, 2.100, 1.200, 2.550, 1.500, 0.0, 0.0, 0.0, 0.0));
		SIGN_INFO.put("BicycleLaneAhead", (List<Double>) Arrays.asList(0.0, 0.600, 0.750, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0));
		SIGN_INFO.put("BicycleLaneEnds", (List<Double>) Arrays.asList(0.0, 0.600, 0.750, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0));
		SIGN_INFO.put("RightLaneBicycleOnly", (List<Double>) Arrays.asList(0.0, 0.600, 0.750, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0));
		SIGN_INFO.put("BicycleLaneWithVehicleParking", (List<Double>) Arrays.asList(0.0, 0.600, 0.750, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0));
		SIGN_INFO.put("DoNotPass", (List<Double>) Arrays.asList(0.0, 0.600, 0.750, 0.900, 1.200, 1.200, 1.500, 0.450, 0.600, 0.0, 0.0));
		SIGN_INFO.put("PassWithCare", (List<Double>) Arrays.asList(0.0, 0.600, 0.750, 0.900, 1.200, 1.200, 1.500, 0.450, 0.600, 0.0, 0.0));
		SIGN_INFO.put("SlowerTrafficKeepRight", (List<Double>) Arrays.asList(0.0, 0.600, 0.750, 0.900, 1.200, 1.200, 1.500, 0.0, 0.0, 0.0, 0.0));
		SIGN_INFO.put("TrucksUseRightLane", (List<Double>) Arrays.asList(0.0, 0.600, 0.750, 0.900, 1.200, 1.200, 1.500, 0.0, 0.0, 0.0, 0.0));
		SIGN_INFO.put("TruckLane500Feet", (List<Double>) Arrays.asList(0.0, 0.600, 0.750, 0.900, 1.200, 1.200, 1.500, 0.0, 0.0, 0.0, 0.0));
		SIGN_INFO.put("KeepRight", (List<Double>) Arrays.asList(0.0, 0.600, 0.750, 0.900, 1.200, 1.200, 1.500, 0.450, 0.600, 0.0, 0.0));
		SIGN_INFO.put("KeepLeft", (List<Double>) Arrays.asList(0.0, 0.600, 0.750, 0.900, 1.200, 1.200, 1.500, 0.450, 0.600, 0.0, 0.0));
		SIGN_INFO.put("DoNotEnter", (List<Double>) Arrays.asList(0.0, 0.750, 0.750, 0.900, 0.900, 1.200, 1.200, 0.0, 0.0, 0.0, 0.0));
		SIGN_INFO.put("WrongWay", (List<Double>) Arrays.asList(0.0, 0.900, 0.600, 0.900, 0.600, 1.050, 0.750, 0.0, 0.0, 0.0, 0.0));
		SIGN_INFO.put("NoTrucks", (List<Double>) Arrays.asList(0.0, 0.600, 0.600, 0.750, 0.750, 0.900, 0.900, 0.0, 0.0, 1.200, 1.200));
		SIGN_INFO.put("MotorVehicleProhibition", (List<Double>) Arrays.asList(0.0, 0.600, 0.600, 0.000, 0.000, 0.000, 0.000, 0.0, 0.0, 0.000, 0.000));
		SIGN_INFO.put("CommercialVehiclesExcluded", (List<Double>) Arrays.asList(0.0, 0.600, 0.750, 0.900, 1.200, 1.200, 1.500, 0.0, 0.0, 0.000, 0.000));
		SIGN_INFO.put("VehiclesWithLugsProhibited", (List<Double>) Arrays.asList(0.0, 0.600, 0.750, 0.900, 1.200, 1.200, 1.500, 0.0, 0.0, 0.000, 0.000));
		SIGN_INFO.put("NoBicycles", (List<Double>) Arrays.asList(0.0, 0.600, 0.600, 0.750, 0.750, 0.900, 0.900, 0.0, 0.0, 1.200, 1.200));
		SIGN_INFO.put("NonmotorizedTrafficProhibited", (List<Double>) Arrays.asList(0.0, 0.750, 0.600, 1.050, 0.600, 1.200, 0.750, 0.0, 0.0, 0.0, 0.0));
		SIGN_INFO.put("MotorDrivenCyclesProhibited", (List<Double>) Arrays.asList(0.0, 0.750, 0.600, 1.050, 0.600, 1.200, 0.750, 0.0, 0.0, 0.0, 0.0));
		SIGN_INFO.put("PedestriansBicyclesMotorDrivenCyclesProhibited", (List<Double>) Arrays.asList(0.0, 0.750, 0.900, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0));
		SIGN_INFO.put("PedestriansBicyclesProhibited", (List<Double>) Arrays.asList(0.0, 0.750, 0.450, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0));
		SIGN_INFO.put("PedestriansProhibited", (List<Double>) Arrays.asList(0.0, 0.600, 0.300, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0));
		SIGN_INFO.put("OneWay", (List<Double>) Arrays.asList(0.0, 0.900, 0.300, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0));
		SIGN_INFO.put("OneWay2", (List<Double>) Arrays.asList(0.0, 0.450, 0.600, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.600, 0.750));
		SIGN_INFO.put("DividedHighwayCrossing", (List<Double>) Arrays.asList(0.0, 0.600, 0.450, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0));
		SIGN_INFO.put("NoParking", (List<Double>) Arrays.asList(0.0, 0.300, 0.450, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0));
		SIGN_INFO.put("NoParkingTransitLogo", (List<Double>) Arrays.asList(0.0, 0.300, 0.750, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0));
		SIGN_INFO.put("NoParkingRestrictedParking", (List<Double>) Arrays.asList(0.0, 0.500, 0.450, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0));
		SIGN_INFO.put("TowAwayZone", (List<Double>) Arrays.asList(0.0, 0.300, 0.150, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0));
		SIGN_INFO.put("ThisSideOfSign", (List<Double>) Arrays.asList(0.0, 0.300, 0.165, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0));
		SIGN_INFO.put("NoParkingPavement", (List<Double>) Arrays.asList(0.0, 0.600, 0.750, 0.900, 1.200, 1.200, 1.500, 0.0, 0.0, 0.0, 0.0));
		SIGN_INFO.put("NoParkingOnShoulder", (List<Double>) Arrays.asList(0.0, 0.600, 0.750, 0.900, 1.200, 1.200, 1.500, 0.0, 0.0, 0.0, 0.0));
		SIGN_INFO.put("NoParking", (List<Double>) Arrays.asList(0.0, 0.600, 0.750, 0.900, 1.200, 1.200, 1.500, 0.450, 0.600, 0.0, 0.0));
		SIGN_INFO.put("NoParkingSymbol", (List<Double>) Arrays.asList(0.0, 0.600, 0.750, 0.900, 0.900, 1.200, 1.200, 0.300, 0.300, 0.0, 0.0));
		SIGN_INFO.put("EmergencyParking", (List<Double>) Arrays.asList(0.0, 0.750, 0.600, 0.750, 0.600, 1.200, 0.900, 0.000, 0.000, 0.0, 0.0));
		SIGN_INFO.put("DoNotStopOnTracks", (List<Double>) Arrays.asList(0.0, 0.600, 0.750, 0.900, 1.200, 1.200, 1.500, 0.0, 0.0, 0.0, 0.0));
		SIGN_INFO.put("WalkOnLeft", (List<Double>) Arrays.asList(0.0, 0.450, 0.600, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0));
		SIGN_INFO.put("CrossOnlyAtCrosswalk", (List<Double>) Arrays.asList(0.0, 0.300, 0.450, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0));
		SIGN_INFO.put("PedestriansProhibited2", (List<Double>) Arrays.asList(0.0, 0.300, 0.450, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0));
		SIGN_INFO.put("PedestriansProhibitedSymbol", (List<Double>) Arrays.asList(0.0, 0.450, 0.450, 0.600, 0.600, 0.750, 0.750, 0.0, 0.0, 0.0, 0.0));
		SIGN_INFO.put("UseCrosswalk", (List<Double>) Arrays.asList(0.0, 0.450, 0.300, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0));
		SIGN_INFO.put("NoHitchHiking", (List<Double>) Arrays.asList(0.0, 0.450, 0.600, 0.0, 0.0, 0.0, 0.0, 0.450, 0.450, 0.0, 0.0));
		SIGN_INFO.put("NoHitchHikingSymbol", (List<Double>) Arrays.asList(0.0, 0.450, 0.450, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0));
		SIGN_INFO.put("BicyclistsUsePedSignal", (List<Double>) Arrays.asList(0.0, 0.300, 0.450, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0));
		SIGN_INFO.put("BicyclistsYieldToPeds", (List<Double>) Arrays.asList(0.0, 0.400, 0.450, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0));
		SIGN_INFO.put("KeepLeftRightToPedestriansBicyclists", (List<Double>) Arrays.asList(0.0, 0.300, 0.450, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0));
		SIGN_INFO.put("PedestrianCrosswalk", (List<Double>) Arrays.asList(0.0, 0.600, 0.300, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0));
		SIGN_INFO.put("SidewalkClosed", (List<Double>) Arrays.asList(0.0, 0.600, 0.300, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0));
		SIGN_INFO.put("SidewalkClosedUseOtherSide", (List<Double>) Arrays.asList(0.0, 0.600, 0.300, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0));
		SIGN_INFO.put("SidewalkClosedAheadCrossHere", (List<Double>) Arrays.asList(0.0, 0.600, 0.300, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0));
		SIGN_INFO.put("PedestrianTrafficSignalSign", (List<Double>) Arrays.asList(0.0, 0.300, 0.450, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0));
		SIGN_INFO.put("PedestrianTrafficSignalSign2", (List<Double>) Arrays.asList(0.0, 0.225, 0.300, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0));
		SIGN_INFO.put("LeftOnGreenArrowOnly", (List<Double>) Arrays.asList(0.0, 0.600, 0.750, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 1.200, 1.500));
		SIGN_INFO.put("StopHereOnRed", (List<Double>) Arrays.asList(0.0, 0.600, 0.900, 0.0, 0.0, 0.0, 0.0, 0.600, 0.750, 0.0, 0.0));
		SIGN_INFO.put("DoNotBlockIntersection", (List<Double>) Arrays.asList(0.0, 0.600, 0.750, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0));
		SIGN_INFO.put("UseLaneWithGreenArrow", (List<Double>) Arrays.asList(0.0, 0.600, 0.750, 0.900, 1.050, 0.0, 0.0, 0.0, 0.0, 1.500, 1.800));
		SIGN_INFO.put("LeftRightTurnSignal", (List<Double>) Arrays.asList(0.0, 0.600, 0.750, 0.900, 1.050, 0.0, 0.0, 0.0, 0.0, 1.500, 1.800));
		SIGN_INFO.put("NoTurnOnRed", (List<Double>) Arrays.asList(0.0, 0.600, 0.750, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 1.200, 1.200));
		SIGN_INFO.put("NoTurnOnRed2", (List<Double>) Arrays.asList(0.0, 0.600, 0.600, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.750, 0.750));
		SIGN_INFO.put("LeftTurnYieldOnGreen", (List<Double>) Arrays.asList(0.0, 0.600, 0.750, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.000, 0.000));
		SIGN_INFO.put("EmergencySignal", (List<Double>) Arrays.asList(0.0, 0.900, 0.600, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.000, 0.000));
		SIGN_INFO.put("KeepOffMedian", (List<Double>) Arrays.asList(0.0, 0.600, 0.750, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.000, 0.000));
		SIGN_INFO.put("RoadClosed", (List<Double>) Arrays.asList(0.0, 1.200, 0.750, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.000, 0.000));
		SIGN_INFO.put("RoadClosedLocalTraffic", (List<Double>) Arrays.asList(0.0, 1.500, 0.750, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.000, 0.000));
		SIGN_INFO.put("WeightLimit", (List<Double>) Arrays.asList(0.0, 0.600, 0.750, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.900, 1.200));
		SIGN_INFO.put("WeightLimit2", (List<Double>) Arrays.asList(0.0, 0.600, 0.900, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0));
		SIGN_INFO.put("WeightLimit3", (List<Double>) Arrays.asList(0.0, 0.750, 0.600, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0));
		SIGN_INFO.put("WeightLimit4", (List<Double>) Arrays.asList(0.0, 0.600, 0.900, 0.900, 1.200, 1.200, 1.500, 0.0, 0.0, 0.0, 0.0));
		SIGN_INFO.put("WeighStation", (List<Double>) Arrays.asList(0.0, 1.800, 1.200, 2.400, 1.650, 3.000, 1.100, 0.0, 0.0, 0.0, 0.000));
		SIGN_INFO.put("TruckRoute", (List<Double>) Arrays.asList(0.0, 0.600, 0.450, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.000, 0.000));
		SIGN_INFO.put("HazardousCargo", (List<Double>) Arrays.asList(0.0, 0.600, 0.600, 0.750, 0.750, 0.900, 0.900, 0.0, 0.0, 1.050, 1.050));
		SIGN_INFO.put("NationalNetwork", (List<Double>) Arrays.asList(0.0, 0.600, 0.600, 0.750, 0.750, 0.900, 0.900, 0.0, 0.0, 1.050, 1.050));
		SIGN_INFO.put("SeatBeltSymbol", (List<Double>) Arrays.asList(0.0, 0.375, 0.500, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.000, 0.000));
	}
}
