package bean;

import java.util.HashMap;
import java.util.Map;

public class Junction {

	private String name;
	private float cost;
	private Map<Junction, Road> connectionRoad;
	// private StringBuffer path;
	private Junction parentJunction;
	private Road parentRoad;

	public Junction() {
		// TODO Auto-generated constructor stub
		// set cost default value is -1.0
		// setPath(new StringBuffer());
		// parentJunction = null;
		parentRoad = null;
		cost = -1.0f;
		connectionRoad = new HashMap<Junction, Road>();
	}

	public void addConnection(Junction junction, Road road) {
		connectionRoad.put(junction, road);
	}

	public Map<Junction, Road> getConnectionRoad() {
		return connectionRoad;
	}

	public void setConnectionRoad(Map<Junction, Road> connectionRoad) {
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

	// public String getPath() {
	// // StringBuffer stringBuffer = new StringBuffer();
	// // getParentPath(this, stringBuffer);
	// return getParentPath(this);
	// }

	public String getParentPath(Junction junction) {
		String path = "";
		while (junction.getParentJunction() != null) {
			// stringBuffer.insert(0, junction.getParentRoad().getName() +
			// junction.getName());
			path += junction.getParentRoad().getName() + " - " + junction.getName();
			junction = junction.getParentJunction();
		}
		return junction.getName() + " - " + path;
		// stringBuffer.insert(0, junction.getName());
		// if (this.getParentJunction() == null) {
		// // return new StringBuffer(this.getName());
		// stringBuffer.insert(0, this.getName());
		// return;
		// }
		// stringBuffer.insert(0, this.getParentRoad().getName() +
		// this.getName());
		// this.getParentJunction().getParentPath(stringBuffer);
	}

	//
	// public void setPath(StringBuffer path) {
	// this.path = null;
	// this.path = path;
	// }

	public Road getParentRoad() {
		return parentRoad;
	}

	public void setParentRoad(Road parentRoad) {
		this.parentRoad = parentRoad;
	}

	public Junction getParentJunction() {
		return parentJunction;
	}

	public void setParentJunction(Junction parentJunction) {
		this.parentJunction = parentJunction;
	}

}
