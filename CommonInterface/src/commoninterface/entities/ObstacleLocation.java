package commoninterface.entities;

import java.util.LinkedHashSet;

import commoninterface.RobotCI;
import commoninterface.utils.jcoord.LatLon;

public class ObstacleLocation extends GeoEntity {
	private static final long serialVersionUID = -1089260825776015322L;
	private double radius = 2;

	public ObstacleLocation(String name, LatLon latLon, double radius) {
		super(name, latLon);
		this.radius = radius;
	}

	public ObstacleLocation(String name, LatLon latLon) {
		super(name, latLon);
	}

	public double getRadius() {
		return radius;
	}

	public static LinkedHashSet<ObstacleLocation> getObstacleLocations(RobotCI robot) {
		LinkedHashSet<ObstacleLocation> obstacleLocations = new LinkedHashSet<ObstacleLocation>();

		for (Entity e : robot.getEntities()) {
			if (e instanceof ObstacleLocation)
				obstacleLocations.add((ObstacleLocation) e);
		}

		return obstacleLocations;
	}

	@Override
	public ObstacleLocation clone() {
		return new ObstacleLocation(name, new LatLon(latLon), radius);
	}
	
	public void setRadius(double radius) {
		this.radius = radius;
	}
}
