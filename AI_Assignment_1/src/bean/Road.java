package bean;

public class Road {

	private String name;
	private Junction startJunction;
	private Junction endJunction;
	private float length;
	private float nLots;
	private float lengthOfLot;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Junction getStartJunction() {
		return startJunction;
	}

	public void setStartJunction(Junction startJunction) {
		this.startJunction = startJunction;
	}

	public Junction getEndJunction() {
		return endJunction;
	}

	public void setEndJunction(Junction endJunction) {
		this.endJunction = endJunction;
	}

	public float getLength() {
		return length;
	}

	public void setLength(float length) {
		this.length = length;
	}

	public float getnLots() {
		return nLots;
	}

	public void setnLots(float nLots) {
		this.nLots = nLots;
	}

	@Override
	public String toString() {
		// TODO Auto-generated method stub
		return this.getName() + this.getStartJunction().getName() + this.getEndJunction().getName() + this.getLength()
				+ this.getnLots();
	}

	public float getLengthOfLot() {
		return lengthOfLot;
	}

	public void setLengthOfLot(float lengthOfLot) {
		this.lengthOfLot = lengthOfLot;
	}

}
