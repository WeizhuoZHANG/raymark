
package main;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.PriorityQueue;

import bean.Junction;
import bean.Query;
import bean.Road;
import util.FileUtil;

/**
 * 1. did not solve the no way problem
 *
 */
public class Main {
	// static String testpath = "testcase/testcase-2/";
	// static String environment = testpath + "test2.txt";
	// static String query = testpath + "query2.txt";
	// static String outputfile = testpath + "output.txt";

	static String testpath = "AI/";
	static String environment = testpath + "1000000.txt";
	static String query = testpath + "1000000_query.txt";
	static String outputfile = "output.txt";

	// static String environment = "testcase/testcase-1/test-simple.txt";
	// static String query = "testcase/testcase-1/query-simple.txt";
	static Map<String, Road> roads = new HashMap<String, Road>();
	static Map<String, Junction> junctions = new HashMap<String, Junction>();
	// static ArrayList<Junction> junctions = new ArrayList<Junction>();
	static ArrayList<Query> queries = new ArrayList<Query>();
	static String result = "";

	public static void main(String[] args) {
		long startTime = System.currentTimeMillis(); // 获取开始时间

		// environment = args[0];
		// query = args[1];
		// outputfile = args[2];

		init();
		// initial junctions cost to -1.0, before starting a new loop of query
		for (Query query : queries) {
			for (Map.Entry<String, Junction> entry : junctions.entrySet()) {
				entry.getValue().setCost(-1.0f);
			}
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
			initStartJunction.setPath(initStartJunction.getName());
			junctionsQueue.add(initEndJunction);
			initEndJunction.setPath(initEndJunction.getName());

			// main loop to get the result
			while (true) {
				Junction peakJunction = junctionsQueue.poll();
				if (peakJunction == null) {
					printResult(shortestCost, path);
					break;
				}

				if (peakJunction == goalStartJunction && peakJunction.getCost() != -1) {
					shortestCost = peakJunction.getCost();
					// shortestCost = peakJunction.getCost() +
					// goalStartJunctionCost;
					path = initRoadName + " - " + peakJunction.getPath() + " - " + goalRoadName;
					break;
				}
				if (peakJunction == goalEndJunction && peakJunction.getCost() != -1) {
					shortestCost = peakJunction.getCost();
					// shortestCost = peakJunction.getCost() +
					// goalEndJunctionCost;
					path = initRoadName + " - " + peakJunction.getPath() + " - " + goalRoadName;
					break;
				}

				ArrayList<Road> peakConnetedRoads = peakJunction.getConnectionRoad();

				for (Road roadNode : peakConnetedRoads) {
					if (roadNode.getStartJunction() == peakJunction) {
						pushSuccessor(junctionsQueue, peakJunction, roadNode.getEndJunction(), roadNode,
								goalStartJunction, goalEndJunction, goalStartJunctionCost, goalEndJunctionCost);
					} else {
						pushSuccessor(junctionsQueue, peakJunction, roadNode.getStartJunction(), roadNode,
								goalStartJunction, goalEndJunction, goalStartJunctionCost, goalEndJunctionCost);
					}
				}
			}
		} catch (NullPointerException e) {
			// TODO: handle exception
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

		// tempCost = peakJunction.getCost() + roadNode.getLength();

		// if next junction's cost is default value OR
		// bigger than temp cost
		// then push it into queue
		if (junction.getCost() == -1.0 || tempCost < junction.getCost()) {
			junction.setCost(tempCost);
			if (!junctionsQueue.contains(junction)) {
				junctionsQueue.add(junction);
				junction.setPath(peakJunction.getPath() + " - " + roadNode.getName() + " - " + junction.getName());
			}

		}
	}

	public static Road[] setInitRoad(String initRoadName, String goalRoadName) {
		try {
			// int initAndGoalFlag = 0;
			Road[] tempRoads = { new Road(), new Road() };

			/*
			 * Added by Ray 14:12pm 23 AUG 2017
			 */
			tempRoads[0] = roads.get(initRoadName);
			tempRoads[1] = roads.get(goalRoadName);
			/*
			 * Added end
			 */

			/*
			 * Delete by Ray 14:12pm 23 AUG 2017
			 */
			// for (Road tempRoad : roads) {
			// String tempRoadName = tempRoad.getName();
			// if (initAndGoalFlag == 2) {
			// break;
			// }
			// if (tempRoadName.equals(initRoadName)) {
			// tempRoads[0] = tempRoad;
			// initAndGoalFlag++;
			// }
			// if (tempRoadName.equals(goalRoadName)) {
			// tempRoads[1] = tempRoad;
			// initAndGoalFlag++;
			// }
			// }
			/*
			 * Delete end
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
			// TODO: handle exception
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
		// HashMap<String, Junction> junctions = new HashMap<>();

		File fileEnvironment = FileUtil.openFile(environment);
		File fileQuery = FileUtil.openFile(query);

		BufferedReader readerEnvironment = FileUtil.getReader(fileEnvironment);
		BufferedReader readerQuery = FileUtil.getReader(fileQuery);

		try {
			String tempLine = null;
			while ((tempLine = readerEnvironment.readLine()) != null) {
				Road tempRoad = new Road();
				// boolean startJuncitonFlag = true;
				// boolean endJuncitonFlag = true;
				String[] lineEnv = tempLine.split(" ; ");
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

				/*
				 * Deleted by Ray 14:08pm 23 AUG 2017
				 */
				// if (junctions != null) {
				// for (int i = 0; i < junctions.size(); i++) {
				// if (lineEnv[1].equals(junctions.get(i).getName())) {
				// tempRoad.setStartJunction(junctions.get(i));
				// startJuncitonFlag = false;
				// continue;
				// } else if (lineEnv[2].equals(junctions.get(i).getName())) {
				// tempRoad.setEndJunction(junctions.get(i));
				// endJuncitonFlag = false;
				// continue;
				// }
				// if (!startJuncitonFlag && !endJuncitonFlag) {
				// break;
				// }
				// }
				// }

				// if (startJuncitonFlag) {
				// Junction tempJuction = new Junction();
				// tempJuction.setName(lineEnv[1]);
				// junctions.put(tempJuction.getName(), tempJuction);
				// // junctions.add(tempJuction);
				// tempRoad.setStartJunction(tempJuction);
				// }
				// if (endJuncitonFlag) {
				// Junction tempJuction = new Junction();
				// tempJuction.setName(lineEnv[2]);
				// junctions.put(tempJuction.getName(), tempJuction);
				// // junctions.add(tempJuction);
				// tempRoad.setEndJunction(tempJuction);
				// }
				// tempRoad.getStartJunction().addConnection(tempRoad);
				// tempRoad.getEndJunction().addConnection(tempRoad);
				// roads.add(tempRoad);
				/*
				 * Delete end
				 */
			}
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (NullPointerException e) {
			// TODO: handle exception

		}

		try {
			String tempLine = null;
			while ((tempLine = readerQuery.readLine()) != null) {
				Query tempQuery = new Query();
				String[] lineEnv = tempLine.split(" ; ");
				tempQuery.setInitPlot(Integer.parseInt(lineEnv[0].substring(0, getNumIndex(lineEnv[0]) + 1)));
				tempQuery.setInitName(lineEnv[0].substring(getNumIndex(lineEnv[0]) + 1, lineEnv[0].length()));
				tempQuery.setEndPlot(Integer.parseInt(lineEnv[1].substring(0, getNumIndex(lineEnv[1]) + 1)));
				tempQuery.setEndName(lineEnv[1].substring(getNumIndex(lineEnv[1]) + 1, lineEnv[1].length()));
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
			result += "no-path" + "\r\n";
		} else {
			result += cost + " ; " + path + "\r\n";
		}
	}

	public static void output() {
		try {
			int i = result.lastIndexOf("\r\n");
			result = result.substring(0, i);
			FileWriter writer = new FileWriter(outputfile);
			writer.write(result);
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
}
