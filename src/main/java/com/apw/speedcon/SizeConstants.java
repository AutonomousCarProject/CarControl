package com.apw.speedcon;

import java.awt.List;
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
	
	public static final Map<String, ArrayList<Double>> SIGN_INFO = new HashMap<String, ArrayList<Double>>(){{
		put("Stop", (ArrayList<Double>) Arrays.asList(3.0, .750, .900, .0, .600, 1.200));
		put("StopLightWidth", (ArrayList<Double>) Arrays.asList(.300, .0, .00, .0, .200, 0.0));
		put("Stop", (ArrayList<Double>) Arrays.asList(3.0, .750, .900, .0, .600, 1.200));
		put("Yield", (ArrayList<Double>) Arrays.asList(1.0, .900, 1.200, 1.500, .750, 0.0));
		put("4Way", (ArrayList<Double>) Arrays.asList(0.0, .300, .150, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0));
		put("AllWay", (ArrayList<Double>) Arrays.asList(0.0, .450, .150, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0));
		put("SpeedLimit", (ArrayList<Double>) Arrays.asList(0.0, .600, .750, 0.900, 1.200, 1.200, 1.500, 0.0, 0.0, 0.0, 0.0));
		put("TruckSpeedLimit", (ArrayList<Double>) Arrays.asList(0.0, .600, .600, 0.900, 0.900, 1.200, 1.200, 0.0, 0.0, 0.0, 0.0));
		put("NightSpeedLimit", (ArrayList<Double>) Arrays.asList(0.0, .600, .600, 0.900, 0.900, 1.200, 1.200, 0.0, 0.0, 0.0, 0.0));
		put("MinimumSpeedLimit", (ArrayList<Double>) Arrays.asList(0.0, .600, .750, 0.900, 1.200, 1.200, 1.500, 0.0, 0.0, 0.0, 0.0));
		put("CombinedSpeedLimit", (ArrayList<Double>) Arrays.asList(0.0, .600, .1200, 0.900, 1.800, 1.200, 2.400, 0.0, 0.0, 0.0, 0.0));
		put("ReducedSpeedAhead", (ArrayList<Double>) Arrays.asList(0.0, .600, .750, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0));
		put("TurnProhibition", (ArrayList<Double>) Arrays.asList(0.0, .600, .600, 0.900, 0.900, 0.0, 0.0, 0.0, 0.0, 1.200, 1.200));
		put("MandatoryMovementLaneControl", (ArrayList<Double>) Arrays.asList(0.0, .750, .900, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0));
		put("MandatoryMovementLaneControl2", (ArrayList<Double>) Arrays.asList(0.0, .750, .750, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0));
		put("OptionalMovementLaneControl", (ArrayList<Double>) Arrays.asList(0.0, .750, .900, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0));
		//Advance Intersection Lane Control has a variable width, so good luck with sizing that
		//put("AdvanceIntersectionLaneControl", (ArrayList<Double>) Arrays.asList(0.0, 1.800, 1.500, 2.400, 1.800, 2.700, 1.100, 0.0, 0.0, 0.0, 0.0));
		put("TwoWayLeftTurnOnlyOverhead", (ArrayList<Double>) Arrays.asList(0.0, .750, .900, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0));
		put("TwoWayLeftTurnOnlyGround", (ArrayList<Double>) Arrays.asList(0.0, .600, .900, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.900, 1.200));
		put("ReversibleLaneControl", (ArrayList<Double>) Arrays.asList(0.0, 2.700, .900, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0));
		put("EndReverseLane", (ArrayList<Double>) Arrays.asList(0.0, 2.700, 1.200, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0));
		put("HOV2+LaneAheadGround", (ArrayList<Double>) Arrays.asList(0.0, .750, 1.050, 0.900, 1.500, 1.200, 1.100, 0.0, 0.0, 0.0, 0.0));
		put("CenterLaneHOV2+OnlyPost", (ArrayList<Double>) Arrays.asList(0.0, .750, 1.050, 0.0, 0.0, 1.400, 1.100, 0.0, 0.0, 0.0, 0.0));
		put("HOV2+LaneEndsPost", (ArrayList<Double>) Arrays.asList(0.0, .750, 1.050, 0.900, 1.500, 1.200, 1.100, 0.0, 0.0, 0.0, 0.0));
		put("HOV2+LaneAheadOverhead", (ArrayList<Double>) Arrays.asList(0.0, 1.650, 0.900, 2.100, 1.200, 2.550, 1.500, 0.0, 0.0, 0.0, 0.0));
		put("HOV2+OnlyOverhead", (ArrayList<Double>) Arrays.asList(0.0, 1.800, 1.500, 2.400, 1.800, 2.700, 1.100, 0.0, 0.0, 0.0, 0.0));
		put("HOV2+LaneEndsOverhead", (ArrayList<Double>) Arrays.asList(0.0, 1.650, 0.900, 2.100, 1.200, 2.550, 1.500, 0.0, 0.0, 0.0, 0.0));
		put("BicycleLaneAhead", (ArrayList<Double>) Arrays.asList(0.0, 0.600, 0.750, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0));
		put("BicycleLaneEnds", (ArrayList<Double>) Arrays.asList(0.0, 0.600, 0.750, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0));
		put("RightLaneBicycleOnly", (ArrayList<Double>) Arrays.asList(0.0, 0.600, 0.750, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0));
		put("BicycleLaneWithVehicleParking", (ArrayList<Double>) Arrays.asList(0.0, 0.600, 0.750, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0));
		put("DoNotPass", (ArrayList<Double>) Arrays.asList(0.0, 0.600, 0.750, 0.900, 1.200, 1.200, 1.500, 0.450, 0.600, 0.0, 0.0));
		put("PassWithCare", (ArrayList<Double>) Arrays.asList(0.0, 0.600, 0.750, 0.900, 1.200, 1.200, 1.500, 0.450, 0.600, 0.0, 0.0));
		put("SlowerTrafficKeepRight", (ArrayList<Double>) Arrays.asList(0.0, 0.600, 0.750, 0.900, 1.200, 1.200, 1.500, 0.0, 0.0, 0.0, 0.0));
		put("TrucksUseRightLane", (ArrayList<Double>) Arrays.asList(0.0, 0.600, 0.750, 0.900, 1.200, 1.200, 1.500, 0.0, 0.0, 0.0, 0.0));
		put("TruckLane500Feet", (ArrayList<Double>) Arrays.asList(0.0, 0.600, 0.750, 0.900, 1.200, 1.200, 1.500, 0.0, 0.0, 0.0, 0.0));
		put("KeepRight", (ArrayList<Double>) Arrays.asList(0.0, 0.600, 0.750, 0.900, 1.200, 1.200, 1.500, 0.450, 0.600, 0.0, 0.0));
		put("KeepLeft", (ArrayList<Double>) Arrays.asList(0.0, 0.600, 0.750, 0.900, 1.200, 1.200, 1.500, 0.450, 0.600, 0.0, 0.0));
		put("DoNotEnter", (ArrayList<Double>) Arrays.asList(0.0, 0.750, 0.750, 0.900, 0.900, 1.200, 1.200, 0.0, 0.0, 0.0, 0.0));
		put("WrongWay", (ArrayList<Double>) Arrays.asList(0.0, 0.900, 0.600, 0.900, 0.600, 1.050, 0.750, 0.0, 0.0, 0.0, 0.0));
		put("NoTrucks", (ArrayList<Double>) Arrays.asList(0.0, 0.600, 0.600, 0.750, 0.750, 0.900, 0.900, 0.0, 0.0, 1.200, 1.200));
		put("MotorVehicleProhibition", (ArrayList<Double>) Arrays.asList(0.0, 0.600, 0.600, 0.000, 0.000, 0.000, 0.000, 0.0, 0.0, 0.000, 0.000));
		put("CommercialVehiclesExcluded", (ArrayList<Double>) Arrays.asList(0.0, 0.600, 0.750, 0.900, 1.200, 1.200, 1.500, 0.0, 0.0, 0.000, 0.000));
		put("VehiclesWithLugsProhibited", (ArrayList<Double>) Arrays.asList(0.0, 0.600, 0.750, 0.900, 1.200, 1.200, 1.500, 0.0, 0.0, 0.000, 0.000));
		put("NoBicycles", (ArrayList<Double>) Arrays.asList(0.0, 0.600, 0.600, 0.750, 0.750, 0.900, 0.900, 0.0, 0.0, 1.200, 1.200));
		put("NonmotorizedTrafficProhibited", (ArrayList<Double>) Arrays.asList(0.0, 0.750, 0.600, 1.050, 0.600, 1.200, 0.750, 0.0, 0.0, 0.0, 0.0));
		put("MotorDrivenCyclesProhibited", (ArrayList<Double>) Arrays.asList(0.0, 0.750, 0.600, 1.050, 0.600, 1.200, 0.750, 0.0, 0.0, 0.0, 0.0));
		put("PedestriansBicyclesMotorDrivenCyclesProhibited", (ArrayList<Double>) Arrays.asList(0.0, 0.750, 0.900, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0));
		put("PedestriansBicyclesProhibited", (ArrayList<Double>) Arrays.asList(0.0, 0.750, 0.450, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0));
		put("PedestriansProhibited", (ArrayList<Double>) Arrays.asList(0.0, 0.600, 0.300, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0));
		put("OneWay", (ArrayList<Double>) Arrays.asList(0.0, 0.900, 0.300, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0));
		put("OneWay2", (ArrayList<Double>) Arrays.asList(0.0, 0.450, 0.600, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.600, 0.750));
		put("DividedHighwayCrossing", (ArrayList<Double>) Arrays.asList(0.0, 0.600, 0.450, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0));
		put("NoParking", (ArrayList<Double>) Arrays.asList(0.0, 0.300, 0.450, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0));
		put("NoParkingTransitLogo", (ArrayList<Double>) Arrays.asList(0.0, 0.300, 0.750, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0));
		put("NoParkingRestrictedParking", (ArrayList<Double>) Arrays.asList(0.0, 0.500, 0.450, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0));
		put("TowAwayZone", (ArrayList<Double>) Arrays.asList(0.0, 0.300, 0.150, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0));
		put("ThisSideOfSign", (ArrayList<Double>) Arrays.asList(0.0, 0.300, 0.165, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0));
		put("NoParkingPavement", (ArrayList<Double>) Arrays.asList(0.0, 0.600, 0.750, 0.900, 1.200, 1.200, 1.500, 0.0, 0.0, 0.0, 0.0));
		put("NoParkingOnShoulder", (ArrayList<Double>) Arrays.asList(0.0, 0.600, 0.750, 0.900, 1.200, 1.200, 1.500, 0.0, 0.0, 0.0, 0.0));
		put("NoParking", (ArrayList<Double>) Arrays.asList(0.0, 0.600, 0.750, 0.900, 1.200, 1.200, 1.500, 0.450, 0.600, 0.0, 0.0));
		put("NoParkingSymbol", (ArrayList<Double>) Arrays.asList(0.0, 0.600, 0.750, 0.900, 0.900, 1.200, 1.200, 0.300, 0.300, 0.0, 0.0));
		put("EmergencyParking", (ArrayList<Double>) Arrays.asList(0.0, 0.750, 0.600, 0.750, 0.600, 1.200, 0.900, 0.000, 0.000, 0.0, 0.0));
		put("DoNotStopOnTracks", (ArrayList<Double>) Arrays.asList(0.0, 0.600, 0.750, 0.900, 1.200, 1.200, 1.500, 0.0, 0.0, 0.0, 0.0));
		put("WalkOnLeft", (ArrayList<Double>) Arrays.asList(0.0, 0.450, 0.600, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0));
		put("CrossOnlyAtCrosswalk", (ArrayList<Double>) Arrays.asList(0.0, 0.300, 0.450, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0));
		put("PedestriansProhibited2", (ArrayList<Double>) Arrays.asList(0.0, 0.300, 0.450, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0));
		put("PedestriansProhibitedSymbol", (ArrayList<Double>) Arrays.asList(0.0, 0.450, 0.450, 0.600, 0.600, 0.750, 0.750, 0.0, 0.0, 0.0, 0.0));
		put("UseCrosswalk", (ArrayList<Double>) Arrays.asList(0.0, 0.450, 0.300, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0));
		put("NoHitchHiking", (ArrayList<Double>) Arrays.asList(0.0, 0.450, 0.600, 0.0, 0.0, 0.0, 0.0, 0.450, 0.450, 0.0, 0.0));
		put("NoHitchHikingSymbol", (ArrayList<Double>) Arrays.asList(0.0, 0.450, 0.450, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0));
		put("BicyclistsUsePedSignal", (ArrayList<Double>) Arrays.asList(0.0, 0.300, 0.450, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0));
		put("BicyclistsYieldToPeds", (ArrayList<Double>) Arrays.asList(0.0, 0.400, 0.450, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0));
		put("KeepLeftRightToPedestriansBicyclists", (ArrayList<Double>) Arrays.asList(0.0, 0.300, 0.450, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0));
		put("PedestrianCrosswalk", (ArrayList<Double>) Arrays.asList(0.0, 0.600, 0.300, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0));
		put("SidewalkClosed", (ArrayList<Double>) Arrays.asList(0.0, 0.600, 0.300, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0));
		put("SidewalkClosedUseOtherSide", (ArrayList<Double>) Arrays.asList(0.0, 0.600, 0.300, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0));
		put("SidewalkClosedAheadCrossHere", (ArrayList<Double>) Arrays.asList(0.0, 0.600, 0.300, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0));
		put("PedestrianTrafficSignalSign", (ArrayList<Double>) Arrays.asList(0.0, 0.300, 0.450, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0));
		put("PedestrianTrafficSignalSign2", (ArrayList<Double>) Arrays.asList(0.0, 0.225, 0.300, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0));
		put("LeftOnGreenArrowOnly", (ArrayList<Double>) Arrays.asList(0.0, 0.600, 0.750, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 1.200, 1.500));
		put("StopHereOnRed", (ArrayList<Double>) Arrays.asList(0.0, 0.600, 0.900, 0.0, 0.0, 0.0, 0.0, 0.600, 0.750, 0.0, 0.0));
		put("DoNotBlockIntersection", (ArrayList<Double>) Arrays.asList(0.0, 0.600, 0.750, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0));
		put("UseLaneWithGreenArrow", (ArrayList<Double>) Arrays.asList(0.0, 0.600, 0.750, 0.900, 1.050, 0.0, 0.0, 0.0, 0.0, 1.500, 1.800));
		put("LeftRightTurnSignal", (ArrayList<Double>) Arrays.asList(0.0, 0.600, 0.750, 0.900, 1.050, 0.0, 0.0, 0.0, 0.0, 1.500, 1.800));
		put("NoTurnOnRed", (ArrayList<Double>) Arrays.asList(0.0, 0.600, 0.750, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 1.200, 1.200));
		put("NoTurnOnRed2", (ArrayList<Double>) Arrays.asList(0.0, 0.600, 0.600, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.750, 0.750));
		put("LeftTurnYieldOnGreen", (ArrayList<Double>) Arrays.asList(0.0, 0.600, 0.750, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.000, 0.000));
		put("EmergencySignal", (ArrayList<Double>) Arrays.asList(0.0, 0.900, 0.600, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.000, 0.000));
		put("KeepOffMedian", (ArrayList<Double>) Arrays.asList(0.0, 0.600, 0.750, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.000, 0.000));
		put("RoadClosed", (ArrayList<Double>) Arrays.asList(0.0, 1.200, 0.750, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.000, 0.000));
		put("RoadClosedLocalTraffic", (ArrayList<Double>) Arrays.asList(0.0, 1.500, 0.750, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.000, 0.000));
		put("WeightLimit", (ArrayList<Double>) Arrays.asList(0.0, 0.600, 0.750, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.900, 1.200));
		put("WeightLimit2", (ArrayList<Double>) Arrays.asList(0.0, 0.600, 0.900, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0));
		put("WeightLimit3", (ArrayList<Double>) Arrays.asList(0.0, 0.750, 0.600, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0));
		put("WeightLimit4", (ArrayList<Double>) Arrays.asList(0.0, 0.600, 0.900, 0.900, 1.200, 1.200, 1.500, 0.0, 0.0, 0.0, 0.0));
		put("WeighStation", (ArrayList<Double>) Arrays.asList(0.0, 1.800, 1.200, 2.400, 1.650, 3.000, 1.100, 0.0, 0.0, 0.0, 0.000));
		put("TruckRoute", (ArrayList<Double>) Arrays.asList(0.0, 0.600, 0.450, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.000, 0.000));
		put("HazardousCargo", (ArrayList<Double>) Arrays.asList(0.0, 0.600, 0.600, 0.750, 0.750, 0.900, 0.900, 0.0, 0.0, 1.050, 1.050));
		put("NationalNetwork", (ArrayList<Double>) Arrays.asList(0.0, 0.600, 0.600, 0.750, 0.750, 0.900, 0.900, 0.0, 0.0, 1.050, 1.050));
		put("SeatBeltSymbol", (ArrayList<Double>) Arrays.asList(0.0, 0.375, 0.500, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.000, 0.000));
	}};
		
	

}
