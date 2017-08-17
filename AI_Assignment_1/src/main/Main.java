
package main;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
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
	static String environment = "testcase/testcase-2/test2.txt";
	static String query = "testcase/testcase-2/query2.txt";
	// static String environment = "testcase/testcase-1/test-simple.txt";
	// static String query = "testcase/testcase-1/query-simple.txt";
	static ArrayList<Road> roads = new ArrayList<Road>();
	static ArrayList<Junction> junctions = new ArrayList<Junction>();
	static ArrayList<Query> queries = new ArrayList<Query>();
	// static boolean goalStart = false;
	// static boolean goalEnd = false;

	public static void main(String[] args) {
		init();
		// initial junctions cost to -1.0, before starting a new loop of query
		for (Query query : queries) {
			for (Junction junction : junctions) {
				// goalStart = false;
				// goalEnd = false;
				junction.setCost(-1.0f);
				junction.setPath("");
			}
			mainLoop(query);
		}

		// mainLoop();
	}

	public static void mainLoop(Query query) {
		String initRoadName = query.getInitName();
		String goalRoadName = query.getEndName();
		int initPlot = query.getInitPlot();
		int goalPlot = query.getEndPlot();
		// public static void mainLoop() {
		// String initRoadName = "Road-28";
		// String goalRoadName = "Road-6";
		// int initPlot = 1;
		// int goalPlot = 20;

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
			System.out.println(shortestCost);
			System.out.println(initRoadName);
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
			// System.out.println(goalStartJunctionCost);
			// System.out.println(goalEndJunctionCost);

			// set Initial junctions node
			Junction initStartJunction = iRoads[0].getStartJunction();
			Junction initEndJunction = iRoads[0].getEndJunction();
			setJunctionsValue(initStartJunction, initEndJunction, iRoads[0], initPlot);

			// // push initial node's start and end junction to priority queue
			// Junction root = new Junction();
			// root.addConnection(iRoads[0]);
			// root.setCost(0);
			// junctionsQueue.add(root);

			junctionsQueue.add(initStartJunction);
			initStartJunction.setPath(initStartJunction.getName());
			junctionsQueue.add(initEndJunction);
			initEndJunction.setPath(initEndJunction.getName());
			// System.out.println(initStartJunction.getCost());
			// System.out.println(initEndJunction.getCost());

			// boolean result = false;
			// main loop to get the result
			while (true) {
				Junction peakJunction = junctionsQueue.poll();
				// System.out.println(peakJunction.getName() + " " +
				// peakJunction.getCost());
				if (peakJunction == null) {
					break;
				}

				// System.out.println(peakJunction.getName());
				// if ((peakJunction == goalStartJunction)) {
				// if (goalStart) {
				// shortestCost = peakJunction.getCost();
				// path = initRoadName + " - " + peakJunction.getPath() + " - "
				// + goalRoadName;
				// break;
				// } else {
				//
				// }
				//
				// }
				// if (peakJunction == goalEndJunction && goalEnd) {
				// if (goalEnd) {
				// shortestCost = peakJunction.getCost();
				// path = initRoadName + " - " + peakJunction.getPath() + " - "
				// + goalRoadName;
				// break;
				// } else {
				//
				// }
				//
				// }

				if (peakJunction == goalStartJunction && peakJunction.getCost() != -1) {
					// goalStartJunction.setCost(peakJunction.getCost() +
					// goalStartJunctionCost);
					shortestCost = peakJunction.getCost() + goalStartJunctionCost;
					// peakJunction.setPath(peakJunction.getPath() + " - " +
					// peakJunction.getName());
					// System.out.println("test " + peakJunction.getPath());
					path = initRoadName + " - " + peakJunction.getPath() + " - " + goalRoadName;
					break;
				}
				if (peakJunction == goalEndJunction && peakJunction.getCost() != -1) {
					// goalEndJunction.setCost(peakJunction.getCost() +
					// goalEndJunctionCost);
					shortestCost = peakJunction.getCost() + goalEndJunctionCost;
					// peakJunction.setPath(peakJunction.getPath() + " - " +
					// peakJunction.getName());
					// System.out.println("test " + peakJunction.getPath());
					path = initRoadName + " - " + peakJunction.getPath() + " - " + goalRoadName;
					// System.out.println("test " + path);
					break;
				}

				ArrayList<Road> peakConnetedRoads = peakJunction.getConnectionRoad();

				for (Road roadNode : peakConnetedRoads) {
					// float tempCost;
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
		System.out.println(shortestCost);
		System.out.println(path);
	}

	public static void pushSuccessor(PriorityQueue<Junction> junctionsQueue, Junction peakJunction, Junction junction,
			Road roadNode, Junction goalStartJunction, Junction goalEndJunction, float goalStartJunctionCost,
			float goalEndJunctionCost) {
		float tempCost;
		// Junction endJunction = roadNode.getEndJunction();
		// set temp cost

		// if (junction == goalStartJunction) {
		// tempCost = peakJunction.getCost() + roadNode.getLength() +
		// goalStartJunctionCost;
		// } else if (junction == goalEndJunction) {
		// tempCost = peakJunction.getCost() + roadNode.getLength() +
		// goalEndJunctionCost;
		// } else {
		// tempCost = peakJunction.getCost() + roadNode.getLength();
		// }

		tempCost = peakJunction.getCost() + roadNode.getLength();

		// if next junction's cost is default value OR
		// bigger than temp cost
		// then push it into queue
		if (junction.getCost() == -1.0 || tempCost < junction.getCost()) {
			junction.setCost(tempCost);
			if (!junctionsQueue.contains(junction)) {
				junctionsQueue.add(junction);
				junction.setPath(peakJunction.getPath() + " - " + roadNode.getName() + " - " + junction.getName());
			}

			// if (junction == goalStartJunction) {
			// goalStart = true;
			// } else if (junction == goalEndJunction) {
			// goalEnd = true;
			// }
			// junction.setPath(peakJunction.getPath() + " - " +
			// roadNode.getName() + " - " + junction.getName());
		}
	}

	public static Road[] setInitRoad(String initRoadName, String goalRoadName) {
		try {
			int initAndGoalFlag = 0;
			Road[] tempRoads = { new Road(), new Road() };
			for (Road tempRoad : roads) {
				String tempRoadName = tempRoad.getName();
				if (initAndGoalFlag == 2) {
					break;
				}
				if (tempRoadName.equals(initRoadName)) {
					tempRoads[0] = tempRoad;
					initAndGoalFlag++;
				}
				if (tempRoadName.equals(goalRoadName)) {
					tempRoads[1] = tempRoad;
					initAndGoalFlag++;
				}
			}
			return tempRoads;
		} catch (NullPointerException e) {
			// TODO: handle exception
			System.err.println("setInitRoad");
		}
		return null;
	}

	public static void setJunctionsValue(Junction startJunction, Junction endJunction, Road road, int plot) {
		try {
			// if (plot < (road.getnLots() / 2)) {
			// startJunction.setCost(road.getLengthOfLot() * (0.5f + plot / 2));
			// endJunction.setCost(road.getLength() - startJunction.getCost());
			// } else {
			// endJunction.setCost(road.getLengthOfLot() * (0.5f + plot / 2));
			// startJunction.setCost(road.getLength() - endJunction.getCost());
			// }

			startJunction.setCost(road.getLengthOfLot() * (0.5f + plot / 2));
			endJunction.setCost(road.getLength() - startJunction.getCost());
			// System.out.println(plot / 2);
			// System.out.println(0.5f + plot / 2);
			// System.out.println("startJunction " + startJunction.getCost());
			// System.out.println("endJunction " + endJunction.getCost());
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
		// ArrayList<Road> roads = new ArrayList<Road>();
		// ArrayList<Junction> junctions = new ArrayList<Junction>();

		File fileEnvironment = FileUtil.openFile(environment);
		File fileQuery = FileUtil.openFile(query);

		BufferedReader readerEnvironment = FileUtil.getReader(fileEnvironment);
		BufferedReader readerQuery = FileUtil.getReader(fileQuery);

		try {
			String tempLine = null;
			while ((tempLine = readerEnvironment.readLine()) != null) {
				Road tempRoad = new Road();
				boolean startJuncitonFlag = true;
				boolean endJuncitonFlag = true;
				String[] lineEnv = tempLine.split(" ; ");
				tempRoad.setName(lineEnv[0]);
				tempRoad.setLength(Float.parseFloat(lineEnv[3]));
				tempRoad.setnLots(Float.parseFloat(lineEnv[4]));
				tempRoad.setLengthOfLot(2f * Float.parseFloat(lineEnv[3]) / Float.parseFloat(lineEnv[4]));
				if (junctions != null) {
					for (int i = 0; i < junctions.size(); i++) {
						if (lineEnv[1].equals(junctions.get(i).getName())) {
							tempRoad.setStartJunction(junctions.get(i));
							startJuncitonFlag = false;
							continue;
						} else if (lineEnv[2].equals(junctions.get(i).getName())) {
							tempRoad.setEndJunction(junctions.get(i));
							endJuncitonFlag = false;
							continue;
						}
						if (!startJuncitonFlag && !endJuncitonFlag) {
							break;
						}
					}
				}

				if (startJuncitonFlag) {
					Junction tempJuction = new Junction();
					tempJuction.setName(lineEnv[1]);
					junctions.add(tempJuction);
					tempRoad.setStartJunction(tempJuction);
				}
				if (endJuncitonFlag) {
					Junction tempJuction = new Junction();
					tempJuction.setName(lineEnv[2]);
					junctions.add(tempJuction);
					tempRoad.setEndJunction(tempJuction);
				}
				tempRoad.getStartJunction().addConnection(tempRoad);
				tempRoad.getEndJunction().addConnection(tempRoad);
				roads.add(tempRoad);
			}
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

		try {
			String tempLine = null;
			while ((tempLine = readerQuery.readLine()) != null) {
				Query tempQuery = new Query();
				String[] lineEnv = tempLine.split(" ; ");
				// String a = lineEnv[0].substring(0, getNumIndex(lineEnv[0]) +
				// 1);
				// System.out.println(a);
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

	public static boolean isEven(float number) {
		if ((number % 2) == 0) {
			return true;
		}
		return false;
	}
}
