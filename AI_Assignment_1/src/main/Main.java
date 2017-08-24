
package main;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.Set;

import bean.Junction;
import bean.Query;
import bean.Road;
import util.FileUtil;

/**
 * 1. str.replaceAll(" ","")
 *
 */
public class Main {
	static String testpath = "AI/";

	// static String environment = testpath + "1_test-simple.txt";
	// static String query = testpath + "1_query-simple.txt";

	// static String environment = testpath + "1_test2.txt";
	// static String query = testpath + "1_query2.txt";

	// static String environment = testpath + "1000.txt";
	// static String query = testpath + "1000_query.txt";

	// static String environment = testpath + "10000.txt";
	// static String query = testpath + "10000_query.txt";

	// static String environment = testpath + "100000.txt";
	// static String query = testpath + "100000_query.txt";

	// static String environment = testpath + "1000000.txt";
	// static String query = testpath + "1000000_query.txt";

	// static String environment = testpath + "corner.txt";
	// static String query = testpath + "corner_query.txt";

	// static String environment = testpath + "no_repeats_1000.txt";
	// static String query = testpath + "no_repeats_1000_query.txt";

	// static String environment = testpath + "no_repeats_100000.txt";
	// static String query = testpath + "no_repeats_100000_query.txt";

	// static String environment = testpath + "no_repeats_1000000.txt";
	// static String query = testpath + "no_repeats_1000000_query.txt";

	// static String environment = testpath + "no_solution.txt";
	// static String query = testpath + "no_solution_query.txt";

	// static String environment = testpath + "square_1000.txt";
	// static String query = testpath + "square_1000_query.txt";

	// static String environment = testpath + "square_10000.txt";
	// static String query = testpath + "square_10000_query.txt";

	// static String environment = testpath + "square_100000.txt";
	// static String query = testpath + "square_100000_query.txt";

	static String environment = testpath + "square_1000000.txt";
	static String query = testpath + "square_1000000_query.txt";
	static String outputfile = "output.txt";

	static Queue<Road> queryRoads = new LinkedList<Road>();
	static Map<String, Junction> junctions = new HashMap<String, Junction>();
	static Set<Junction> visited = new HashSet<Junction>();
	static ArrayList<Query> queries = new ArrayList<Query>();
	static StringBuffer result = new StringBuffer();

	public static void main(String[] args) {
		long startTime = System.currentTimeMillis(); // 获取开始时间

		// environment = args[0];
		// query = args[1];
		// outputfile = args[2];

		init();
		long endTime1 = System.currentTimeMillis(); // 获取结束时间
		System.out.println("程序运行时间： " + (endTime1 - startTime) + "ms");
		// initial junctions cost to -1.0, before starting a new loop of query
		for (Query query : queries) {
			for (Map.Entry<String, Junction> entry : junctions.entrySet()) {
				entry.getValue().setCost(-1.0f);
				// entry.getValue().setParentJunction(null);
				// entry.getValue().setParentRoad(null);
				// entry.getValue().setPath(new StringBuffer());
			}
			visited.removeAll(visited);
			mainLoop(query);
		}
		// mainLoop();
		output();

		long endTime = System.currentTimeMillis(); // 获取结束时间
		System.out.println("程序运行时间： " + (endTime - startTime) + "ms");
	}

	public static void mainLoop(Query query) {
		String initRoadName = query.getInitName();
		String goalRoadName = query.getEndName();
		int initPlot = query.getInitPlot();
		int goalPlot = query.getEndPlot();
		// public static void mainLoop() {
		// String initRoadName = "road_6652_0_6682";
		// String goalRoadName = "road_66656_1_66705";
		// int initPlot = 200;
		// int goalPlot = 200;

		Comparator<Junction> OrderIsdn = getComparator(); // set comparator by
															// decrease
		PriorityQueue<Junction> junctionsQueue = new PriorityQueue<Junction>(OrderIsdn);
		float shortestCost = 0;
		String path = "";

		// find initial and end Road
		// iRoads[0] for initial road
		// iRoads[1] for goal road
		Road[] iRoads = setInitRoad(initRoadName, goalRoadName);

		if (initPlot > iRoads[0].getnLots() || initPlot < 1 || goalPlot > iRoads[1].getnLots() || goalPlot < 1) {
			printResult(shortestCost, path);
			return;
		}
		if (isEven(initPlot)) {
			initPlot--;
		}

		if (isEven(goalPlot)) {
			goalPlot--;
		}

		// if initial road is goal road
		if (initRoadName.equals(goalRoadName)) {
			shortestCost = Math.abs(goalPlot - initPlot) / 2 * iRoads[0].getLengthOfLot();
			printResult(shortestCost, initRoadName);
			return;
		}

		// set goal junctions node
		Junction goalStartJunction = iRoads[1].getStartJunction();
		Junction goalEndJunction = iRoads[1].getEndJunction();
		setJunctionsValue(goalStartJunction, goalEndJunction, iRoads[1], goalPlot);

		try {
			float goalStartJunctionCost = goalStartJunction.getCost();
			float goalEndJunctionCost = goalEndJunction.getCost();

			// set goal junctions to default value -1.0f
			goalStartJunction.setCost(-1f);
			goalEndJunction.setCost(-1f);

			// set Initial junctions node
			Junction initStartJunction = iRoads[0].getStartJunction();
			Junction initEndJunction = iRoads[0].getEndJunction();
			setJunctionsValue(initStartJunction, initEndJunction, iRoads[0], initPlot);
			initStartJunction.setParentJunction(null);
			initEndJunction.setParentJunction(null);
			/*
			 * 8/17 4:22pm
			 */
			if (initStartJunction == goalStartJunction) {
				initStartJunction.setCost(initStartJunction.getCost() + goalStartJunctionCost);
			} else if (initStartJunction == goalEndJunction) {
				initStartJunction.setCost(initStartJunction.getCost() + goalEndJunctionCost);
			}
			if (initEndJunction == goalStartJunction) {
				initEndJunction.setCost(initEndJunction.getCost() + goalStartJunctionCost);
			} else if (initEndJunction == goalEndJunction) {
				initEndJunction.setCost(initEndJunction.getCost() + goalEndJunctionCost);
			}

			// push initial node's start and end junction to priority queue
			junctionsQueue.add(initStartJunction);
			junctionsQueue.add(initEndJunction);

			// main loop to get the result
			while (true) {
				Junction peakJunction = junctionsQueue.poll();
				visited.add(peakJunction);
				if (peakJunction == null) {
					break;
				}

				if (peakJunction == goalStartJunction && peakJunction.getCost() != -1) {
					shortestCost = peakJunction.getCost();
					path = initRoadName + " - " + getParentPath(peakJunction) + " - " + goalRoadName;
					break;
				}
				if (peakJunction == goalEndJunction && peakJunction.getCost() != -1) {
					shortestCost = peakJunction.getCost();
					path = initRoadName + " - " + getParentPath(peakJunction) + " - " + goalRoadName;
					break;
				}

				ArrayList<Road> peakConnetedRoads = peakJunction.getConnectionRoad();
				for (Road roadNode : peakConnetedRoads) {
					if (roadNode.getStartJunction() == peakJunction) {
						if (!visited.contains(roadNode.getEndJunction())) {
							pushSuccessor(junctionsQueue, peakJunction, roadNode.getEndJunction(), roadNode,
									goalStartJunction, goalEndJunction, goalStartJunctionCost, goalEndJunctionCost);

						}
					} else {
						if (!visited.contains(roadNode.getStartJunction())) {
							pushSuccessor(junctionsQueue, peakJunction, roadNode.getStartJunction(), roadNode,
									goalStartJunction, goalEndJunction, goalStartJunctionCost, goalEndJunctionCost);

						}
					}
				}
			}
		} catch (NullPointerException e) {
		}
		printResult(shortestCost, path);
	}

	public static void pushSuccessor(PriorityQueue<Junction> junctionsQueue, Junction peakJunction, Junction junction,
			Road roadNode, Junction goalStartJunction, Junction goalEndJunction, float goalStartJunctionCost,
			float goalEndJunctionCost) {
		float tempCost;
		// set temp cost

		if (junction == goalStartJunction) {
			tempCost = peakJunction.getCost() + roadNode.getLength() + goalStartJunctionCost;
		} else if (junction == goalEndJunction) {
			tempCost = peakJunction.getCost() + roadNode.getLength() + goalEndJunctionCost;
		} else {
			tempCost = peakJunction.getCost() + roadNode.getLength();
		}

		// if next junction's cost is default value OR
		// bigger than temp cost
		// then push it into queue
		if (junction.getCost() == -1.0 || tempCost < junction.getCost()) {
			junction.setCost(tempCost);
			if (!junctionsQueue.contains(junction)) {
				junctionsQueue.add(junction);
			}
			junction.setParentJunction(peakJunction);
			junction.setParentRoad(roadNode);
		}
	}

	public static Road[] setInitRoad(String initRoadName, String goalRoadName) {
		try {
			Road[] tempRoads = { new Road(), new Road() };

			/*
			 * Added by Ray 14:12pm 23 AUG 2017
			 */
			// tempRoads[0] = roads.get(initRoadName);
			// tempRoads[1] = roads.get(goalRoadName);
			tempRoads[0] = queryRoads.poll();
			tempRoads[1] = queryRoads.poll();
			/*
			 * Added end
			 */

			return tempRoads;
		} catch (NullPointerException e) {
			// TODO: handle exception
			System.err.println("setInitRoad");
		}
		return null;
	}

	public static void setJunctionsValue(Junction startJunction, Junction endJunction, Road road, int plot) {
		try {

			startJunction.setCost(road.getLengthOfLot() * (0.5f + plot / 2));
			endJunction.setCost(road.getLength() - startJunction.getCost());
		} catch (

		NullPointerException e) {
			System.err.println("setJunctionsValue");
		}
	}

	// decreased order according to cost
	public static Comparator<Junction> getComparator() {
		return new Comparator<Junction>() {
			@Override
			public int compare(Junction o1, Junction o2) {
				// TODO Auto-generated method stub
				float numbera = o1.getCost();
				float numberb = o2.getCost();
				if (numbera < numberb) {
					return -1;
				} else if (numbera > numberb) {
					return 1;
				} else {
					return 0;
				}
			}
		};
	}

	// read input file set Road and Junction
	public static void init() {
		Map<String, Road> roads = new HashMap<String, Road>();

		File fileEnvironment = FileUtil.openFile(environment);
		File fileQuery = FileUtil.openFile(query);

		BufferedReader readerEnvironment = FileUtil.getReader(fileEnvironment);
		BufferedReader readerQuery = FileUtil.getReader(fileQuery);

		try {
			String tempLine = null;
			while ((tempLine = readerEnvironment.readLine()) != null && !tempLine.equals("")) {
				Road tempRoad = new Road();
				// boolean startJuncitonFlag = true;
				// boolean endJuncitonFlag = true;
				String[] lineEnv = tempLine.replaceAll(" ", "").split(";");
				tempRoad.setName(lineEnv[0]);
				Junction startJuncitonInit = junctions.get(lineEnv[1]);
				Junction endJunctionInit = junctions.get(lineEnv[2]);
				tempRoad.setLength(Float.parseFloat(lineEnv[3]));
				tempRoad.setnLots(Float.parseFloat(lineEnv[4]));
				tempRoad.setLengthOfLot(2f * Float.parseFloat(lineEnv[3]) / Float.parseFloat(lineEnv[4]));
				/*
				 * Added by Ray 14:08pm 23 AUG 2017
				 */
				if (startJuncitonInit == null) {
					startJuncitonInit = new Junction();
					startJuncitonInit.setName(lineEnv[1]);
					junctions.put(lineEnv[1], startJuncitonInit);
				}
				if (endJunctionInit == null) {
					endJunctionInit = new Junction();
					endJunctionInit.setName(lineEnv[2]);
					junctions.put(lineEnv[2], endJunctionInit);
				}
				tempRoad.setStartJunction(startJuncitonInit);
				tempRoad.setEndJunction(endJunctionInit);
				startJuncitonInit.addConnection(tempRoad);
				endJunctionInit.addConnection(tempRoad);
				roads.put(lineEnv[0], tempRoad);
				/*
				 * Added end
				 */
			}
		} catch (IOException e1) {
			e1.printStackTrace();
		} catch (NullPointerException e) {

		}

		try {
			String tempLine = null;
			while ((tempLine = readerQuery.readLine()) != null && !tempLine.equals("")) {
				Query tempQuery = new Query();
				String[] lineEnv = tempLine.replaceAll(" ", "").split(";");
				tempQuery.setInitPlot(Integer.parseInt(lineEnv[0].substring(0, getNumIndex(lineEnv[0]) + 1)));
				tempQuery.setInitName(lineEnv[0].substring(getNumIndex(lineEnv[0]) + 1, lineEnv[0].length()));
				tempQuery.setEndPlot(Integer.parseInt(lineEnv[1].substring(0, getNumIndex(lineEnv[1]) + 1)));
				tempQuery.setEndName(lineEnv[1].substring(getNumIndex(lineEnv[1]) + 1, lineEnv[1].length()));

				try {
					queryRoads.add(roads.get(tempQuery.getInitName()));
					queryRoads.add(roads.get(tempQuery.getEndName()));
				} catch (NullPointerException e) {
					System.err.println("queryRoads.add");
				}
				queries.add(tempQuery);
			}
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

		try {
			readerEnvironment.close();
			readerQuery.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public static int getNumIndex(String string) {
		int index = 0;
		for (int i = 0; i < string.length(); i++) {
			if (Character.isDigit(string.charAt(i))) {
				index = i;
			} else {
				break;
			}
		}
		return index;
	}

	public static void printResult(float cost, String path) {
		// System.out.println(cost + "\n" + path);
		if (cost == 0) {
			result.append("no-path" + "\r\n");
		} else {
			result.append(cost + " ; " + path + "\r\n");
		}
	}

	public static void output() {
		try {
			int i = result.lastIndexOf("\r\n");
			result = new StringBuffer(result.substring(0, i));
			FileWriter writer = new FileWriter(outputfile);
			writer.write(result.toString());
			writer.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static boolean isEven(float number) {
		if ((number % 2) == 0) {
			return true;
		}
		return false;
	}

	public static String getParentPath(Junction junction) {
		Junction junction2 = junction;
		String path = "";
		while (junction2.getParentJunction() != null) {
			path = " - " + junction2.getParentRoad().getName() + " - " + junction2.getName() + path;
			junction2 = junction2.getParentJunction();
		}
		return junction2.getName() + path;
	}
}
