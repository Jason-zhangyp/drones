package commoninterface.mathutils;

import net.jafama.FastMath;

public class Vector2d extends Point2d {
	private static final long serialVersionUID = 870190008808982607L;
	private double ay;
	private double ax;
	private double angle;

	public Vector2d(double x, double y) {
		super(x, y);
	}

	public Vector2d() {
		super();
	}

	public Vector2d(Vector2d v) {
		super();
		double xVal = v.getX();
		double yVal = v.getY();

		super.setX(xVal);
		super.setY(yVal);
	}

	/**
	 * Returns the length of this vector.
	 * 
	 * @return the length of this vector
	 */
	public final double length() {
		return FastMath.sqrt(this.x * this.x + this.y * this.y);
	}

	/**
	 * Returns the squared length of this vector.
	 * 
	 * @return the squared length of this vector
	 */
	public final double lengthSquared() {
		return (this.x * this.x + this.y * this.y);
	}

	public void rotate(double angle) {
		double xTemp = x * FastMath.cos(angle) - y * FastMath.sin(angle);
		y = x * FastMath.sin(angle) + y * FastMath.cos(angle);
		x = xTemp;
		// if(Double.isNaN(x)||Double.isNaN(y))
		// System.out.println("ERROR");
	}

	// Make vec perpendicular to itself
	public void dVec2Perpendicular() {
		double tempx = x;
		x = -y;
		y = tempx;
		// if(Double.isNaN(x)||Double.isNaN(y))
		// System.out.println("ERROR");
	}

	public void sub(Vector2d B) {
		x -= B.x;
		y -= B.y;
		// if(Double.isNaN(x)||Double.isNaN(y))
		// System.out.println("ERROR");
	}

	// Returns the vector going _from_ A to B (thus B - A)
	public void subFrom(Vector2d B) {
		x = B.x - x;
		y = B.y - y;
		// if(Double.isNaN(x)||Double.isNaN(y))
		// System.out.println("ERROR");
	}

	public void add(Vector2d v1) {
		x += v1.x;
		y += v1.y;
		// if(Double.isNaN(x)||Double.isNaN(y))
		// System.out.println("ERROR");
	}

	/**
	 * Computes the dot product of the this vector and vector v1.
	 * 
	 * @param v1
	 *            the other vector
	 */
	public final double dot(Vector2d v1) {
		return (this.x * v1.x + this.y * v1.y);
	}

	public double getAngle() {
		// double a= Math.atan2(y, x) ;
		if (ax != x || ay != y) {
			angle = FastMath.atan2(y, x);
			ax = x;
			ay = y;
		}
		return angle;
		// return Math.atan2(y, x) ;
	}

	/**
	 * Returns the angle in radians between this vector and the vector
	 * parameter; the return value is constrained to the range [0,PI].
	 * 
	 * @param v1
	 *            the other vector
	 * @return the angle in radians in the range [0,PI]
	 */
	public final double angle(Vector2d v1) {
		double vDot = this.dot(v1) / (this.length() * v1.length());
		if (vDot < -1.0)
			vDot = -1.0;
		if (vDot > 1.0)
			vDot = 1.0;
		return ((FastMath.acos(vDot)));

	}

	public void negate() {
		x = -x;
		y = -y;
		// if(Double.isNaN(x)||Double.isNaN(y))
		// System.out.println("ERROR");
	}

	public final void setLength(double newLength) {
		// if(x==0 && y==0)
		// System.out.println("ERROR");
		double factor = newLength / length();
		x *= factor;
		y *= factor;
	}

	public double distanceTo(Vector2d position) {
		double x = position.x - this.x;
		double y = position.y - this.y;
		return FastMath.sqrt(x * x + y * y);
	}

	public void setPolarCoordinates(double length, double tetha) {
		this.x = length * FastMath.cos(tetha);
		this.y = length * FastMath.sin(tetha);
	}

	@Override
	public String toString() {
		return "[angle=" + angle + ", x=" + x + ", y=" + y + "]";
	}

	public static Vector2d parseVector2d(String string) {
		String str = string.replace("[", "").replace("]", "");
		String[] split = str.split(",");

		double x = Double.parseDouble(split[1].trim().substring(2));
		double y = Double.parseDouble(split[2].trim().substring(2));

		return new Vector2d(x, y);
	}

	@Override
	public boolean equals(Object obj) {
		return obj instanceof Vector2d && ((Vector2d) obj).getX() == getX() && ((Vector2d) obj).getY() == getY()
				&& ((Vector2d) obj).getAngle() == getAngle();
	}
}