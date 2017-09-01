
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

public class Main {
	static Queue<Road> queryRoads = new LinkedList<Road>();
	static ArrayList<Query> queries = new ArrayList<Query>();
	static StringBuffer result = new StringBuffer();

	public static void main(String[] args) {
		long startTime = System.currentTimeMillis(); // 获取开始时间
		String testpath = "AI/";
		String environment = testpath + "square_1000000.txt";
		String queryFile = testpath + "square_1000000_query.txt";
		String outputfile = "output.txt";
		Map<String, Junction> junctions = new HashMap<String, Junction>();
		init(junctions, environment, queryFile);
		System.out.println("程序运行时间： " + (System.currentTimeMillis() - startTime) + "ms");
		// initial junctions cost to -1.0, before starting a new loop of query
		for (Query query : queries) {
			for (Map.Entry<String, Junction> entry : junctions.entrySet()) {
				entry.getValue().setCost(-1.0f);
			}
			mainLoop(query);
		}
		// mainLoop();
		output(outputfile);

		long endTime = System.currentTimeMillis(); // 获取结束时间
		System.out.println("程序运行时间： " + (endTime - startTime) + "ms");
	}

	public static void mainLoop(Query query) {
		Road initRoad = query.getInit();
		Road goalRoad = query.getEnd();
		int initPlot = query.getInitPlot();
		int goalPlot = query.getEndPlot();
		Junction goalJunction = new Junction();
		Road fakeRoadStart = new Road();
		Road fakeRoadEnd = new Road();
		float shortestCost = 0;
		String path = "";
		Comparator<Junction> OrderIsdn = getComparator();
		PriorityQueue<Junction> junctionsQueue = new PriorityQueue<Junction>(OrderIsdn);
		Set<Junction> visited = new HashSet<Junction>();

		if (initPlot > initRoad.getnLots() || initPlot < 1 || goalPlot > goalRoad.getnLots() || goalPlot < 1) {
			printResult(shortestCost, path);
			return;
		}

		initPlot = isEven(initPlot);
		goalPlot = isEven(goalPlot);

		// if initial road is goal road
		if (initRoad.equals(goalRoad)) {
			shortestCost = Math.abs(goalPlot - initPlot) / 2 * initRoad.getLengthOfLot();
			printResult(shortestCost, initRoad.getName());
			return;
		}

		// set goal junctions node
		setInitValue(goalRoad.getStartJunction(), fakeRoadStart, goalJunction);
		fakeRoadStart.setLength(computePlotValue(goalRoad, goalPlot));
		setInitValue(goalRoad.getEndJunction(), fakeRoadEnd, goalJunction);
		fakeRoadEnd.setLength(goalRoad.getLength() - fakeRoadStart.getLength());

		initRoad.getStartJunction().setCost(computePlotValue(initRoad, initPlot));
		initRoad.getEndJunction().setCost(initRoad.getLength() - initRoad.getStartJunction().getCost());
		initRoad.getStartJunction().setParentJunction(null);
		initRoad.getEndJunction().setParentJunction(null);

		try {
			// push initial node's start and end junction to priority queue
			junctionsQueue.add(initRoad.getStartJunction());
			junctionsQueue.add(initRoad.getEndJunction());

			// main loop to get the result
			while (!junctionsQueue.isEmpty() && !junctionsQueue.peek().equals(goalJunction)) {
				Junction peakJunction = junctionsQueue.poll();
				visited.add(peakJunction);

				// if (peakJunction == goalJunction) {
				// shortestCost = peakJunction.getCost();
				// path = initRoad.getName() + " - " +
				// getParentPath(peakJunction.getParentJunction()) + " - "
				// + goalRoad.getName();
				// break;
				// }

				for (Junction junctionNode : peakJunction.getConnectionRoad().keySet()) {
					if (!visited.contains(junctionNode)) {
						// float tempCost = peakJunction.getCost()
						// +
						// peakJunction.getConnectionRoad().get(junctionNode).getLength();
						// if (junctionNode.getCost() == -1.0) {
						// junctionNode.setCost(tempCost);
						// junctionsQueue.add(junctionNode);
						// junctionNode.setParentJunction(peakJunction);
						// junctionNode.setParentRoad(peakJunction.getConnectionRoad().get(junctionNode));
						// } else if (tempCost < junctionNode.getCost()) {
						// junctionNode.setCost(tempCost);
						// junctionNode.setParentJunction(peakJunction);
						// junctionNode.setParentRoad(peakJunction.getConnectionRoad().get(junctionNode));
						// if (junctionsQueue.remove(junctionNode)) {
						// junctionsQueue.add(junctionNode);
						// }
						// }
						pushSuccessor(junctionsQueue, peakJunction, junctionNode,
								peakJunction.getConnectionRoad().get(junctionNode));
					}
				}
			}
		} catch (NullPointerException e) {
		}
		{
			goalRoad.getStartJunction().getConnectionRoad().remove(fakeRoadStart);
			goalRoad.getEndJunction().getConnectionRoad().remove(fakeRoadEnd);
		}
		printResult(shortestCost, path);
	}

	public static void setInitValue(Junction junction, Road newRoad, Junction goal) {
		junction.addConnection(goal, newRoad);
		newRoad.setStartJunction(junction);
		newRoad.setEndJunction(goal);
	}

	public static float computePlotValue(Road road, int plot) {
		return road.getLengthOfLot() * (0.5f + plot / 2);
	}

	public static void pushSuccessor(PriorityQueue<Junction> junctionsQueue, Junction peakJunction, Junction junction,
			Road roadNode) {
		// set temp cost
		float tempCost = peakJunction.getCost() + roadNode.getLength();
		if (junction.getCost() == -1.0 || tempCost < junction.getCost()) {
			junction.setCost(tempCost);
			if (junctionsQueue.remove(junction)) {
				junctionsQueue.add(junction);
			}
			junction.setParentJunction(peakJunction);
			junction.setParentRoad(roadNode);
		}
	}

	public static float setJunctionsValue(Junction junction, Road road, int plot) {
		return road.getLengthOfLot() * (0.5f + plot / 2);
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
	public static void init(Map<String, Junction> junctions, String environment, String query) {
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
				startJuncitonInit.addConnection(endJunctionInit, tempRoad);
				endJunctionInit.addConnection(startJuncitonInit, tempRoad);
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
				tempQuery.setInit(roads.get(lineEnv[0].substring(getNumIndex(lineEnv[0]) + 1, lineEnv[0].length())));
				tempQuery.setEndPlot(Integer.parseInt(lineEnv[1].substring(0, getNumIndex(lineEnv[1]) + 1)));
				tempQuery.setEnd(roads.get(lineEnv[1].substring(getNumIndex(lineEnv[1]) + 1, lineEnv[1].length())));

				try {
					queryRoads.add(tempQuery.getInit());
					queryRoads.add(tempQuery.getEnd());
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

	public static void output(String outputfile) {
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

	public static int isEven(int number) {
		if ((number % 2) == 0) {
			return number--;
		}
		return number;
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
