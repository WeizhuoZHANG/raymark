package bean;

import java.util.ArrayList;

public class Junction {

	private String name;
	private float cost;
	private ArrayList<Road> connectionRoad;
	private StringBuffer path;
	private Junction parent;

	public Junction() {
		// TODO Auto-generated constructor stub
		// set cost default value is -1.0
		setPath(new StringBuffer());
		cost = -1.0f;
		connectionRoad = new ArrayList<Road>();
	}

	public void addConnection(Road road) {
		boolean flag = true;
		if (connectionRoad != null) {
			for (Road tempRoad : connectionRoad) {
				if (tempRoad.getName().equals(road.getName())) {
					flag = false;
					break;
				}
			}
			if (flag) {
				connectionRoad.add(road);
			}
		} else {
			connectionRoad.add(road);
		}
	}

	public ArrayList<Road> getConnectionRoad() {
		return connectionRoad;
	}

	public void setConnectionRoad(ArrayList<Road> connectionRoad) {
		this.connectionRoad = connectionRoad;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public float getCost() {
		return cost;
	}

	public void setCost(float cost) {
		this.cost = cost;
	}

	@Override
	public String toString() {
		// TODO Auto-generated method stub
		return this.getName();
	}

	public StringBuffer getPath() {
		return path;
	}

	public void setPath(StringBuffer path) {
		this.path = null;
		this.path = path;
	}

	public Junction getParent() {
		return parent;
	}

	public void setParent(Junction parent) {
		this.parent = parent;
	}

}
