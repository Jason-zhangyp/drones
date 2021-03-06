package gui.renderer;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RadialGradientPaint;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;

import commoninterface.CISensor;
import commoninterface.RobotCI;
import commoninterface.entities.Entity;
import commoninterface.entities.GeoEntity;
import commoninterface.entities.GeoFence;
import commoninterface.entities.RobotLocation;
import commoninterface.entities.SharedDroneLocation;
import commoninterface.entities.VirtualEntity;
import commoninterface.entities.Waypoint;
import commoninterface.entities.formation.Formation;
import commoninterface.entities.formation.Target;
import commoninterface.entities.formation.motion.MotionData;
import commoninterface.mathutils.Vector2d;
import commoninterface.sensors.ConeTypeCISensor;
import commoninterface.sensors.TargetComboCISensor;
import commoninterface.sensors.ThymioConeTypeCISensor;
import commoninterface.utils.CoordinateUtilities;
import environment.GridBoundaryEnvironment;
import environment.VoronoiEnvironment;
import environment.utils.EnvironmentGrid;
import kn.uni.voronoitreemap.j2d.PolygonSimple;
import kn.uni.voronoitreemap.j2d.Site;
import net.jafama.FastMath;
import simulation.physicalobjects.Line;
import simulation.physicalobjects.PhysicalObject;
import simulation.robot.AquaticDrone;
import simulation.robot.CISensorWrapper;
import simulation.robot.Robot;
import simulation.robot.sensor.GridSensor;
import simulation.robot.sensors.ConeTypeSensor;
import simulation.robot.sensors.Sensor;
import simulation.util.Arguments;

public class CITwoDRenderer extends TwoDRenderer {

	private static final long serialVersionUID = 2466224727957228802L;
	private static final double ENTITY_DIAMETER = 1.08;
	protected int droneID;
	protected boolean seeSensors;
	protected boolean seeEntities;
	protected boolean seeRobotFollowingTarget;
	protected boolean showVelocityVectors;
	protected boolean showRobotsPositionHistory;
	protected int coneSensorId;
	protected double coneTransparence = .1;
	protected String coneClass = "";
	protected Color[] lineColors = new Color[] { Color.RED, Color.BLUE, Color.GREEN };
	protected int colorIndex = 0;

	private HashMap<Robot, ArrayList<Vector2d>> positionsHistory = new HashMap<Robot, ArrayList<Vector2d>>();

	private ArrayList<Target> drawTargets = new ArrayList<Target>();

	public CITwoDRenderer(Arguments args) {
		super(args);

		droneID = args.getArgumentAsIntOrSetDefault("droneid", 0);
		seeSensors = args.getArgumentAsIntOrSetDefault("seeSensors", 0) == 1;
		seeEntities = args.getArgumentAsIntOrSetDefault("seeEntities", 1) == 1;
		seeRobotFollowingTarget = args.getArgumentAsIntOrSetDefault("seeRobotFollowingTarget", 1) == 1;
		coneSensorId = args.getArgumentAsIntOrSetDefault("conesensorid", -1);
		coneTransparence = args.getArgumentAsDoubleOrSetDefault("coneTransparence", coneTransparence);
		coneClass = args.getArgumentAsStringOrSetDefault("coneclass", "");
		showVelocityVectors = args.getArgumentAsIntOrSetDefault("showVelocityVectors", 0) == 1;
		showRobotsPositionHistory = args.getArgumentAsIntOrSetDefault("showRobotsPositionHistory", 0) == 1;

		addMouseListener(this);
		addMouseMotionListener(this);
		addMouseWheelListener(this);
	}

	@Override
	public synchronized void drawFrame() {
		drawTargets.clear();
		super.drawFrame();
		drawEnvironment();

		for (PhysicalObject m : simulator.getEnvironment().getAllObjects()) {
			switch (m.getType()) {
			case ROBOT:
				if (seeEntities && seeRobotFollowingTarget) {
					TargetComboCISensor sensor = (TargetComboCISensor) ((CISensorWrapper) ((AquaticDrone) m)
							.getSensorWithId(1)).getCisensor();
					Robot robot = (Robot) m;

					if (sensor.getConsideringTarget() != null) {
						int x = transformX(robot.getPosition().x + robot.getRadius() + (bigRobots ? 1 : 0));
						int y = transformY(robot.getPosition().y - robot.getDiameter() - (bigRobots ? 1 : 0));

						String name = sensor.getConsideringTarget().getName().replace("formation_target_", "t");
						Font originalFont = graphics.getFont();

						int width = graphics.getFontMetrics().stringWidth(name);
						int height = graphics.getFontMetrics().getHeight();
						graphics.setColor(Color.WHITE);
						graphics.setFont(originalFont.deriveFont(Font.BOLD));
						graphics.fillRect(x + 4, y - 20, width + 2, height - 5);

						graphics.setColor(Color.BLACK);
						graphics.drawString(name, x + 5, y - 10);
						graphics.setFont(originalFont);
					}
				}
			default:
				break;
			}
		}

		if (showRobotsPositionHistory) {
			drawRobotsPositionHistory();
		}
	}

	protected void drawEnvironment() {

		if (simulator.getEnvironment() instanceof VoronoiEnvironment) {
			VoronoiEnvironment env = (VoronoiEnvironment) simulator.getEnvironment();

			// for each site we can no get the resulting polygon of its cell.
			// note that the cell can also be empty, in this case there is no
			// polygon for the corresponding site.

			if (env.getSites() != null) {

				Graphics2D g = (Graphics2D) graphics;

				for (Site s : env.getSites()) {

					PolygonSimple polygon = s.getPolygon();

					if (polygon != null) {
						double[] x = new double[polygon.length];
						double[] y = new double[polygon.length];

						for (int i = 0; i < polygon.length; i++) {
							x[i] = transformX(polygon.getXPoints()[i]);
							y[i] = transformY(polygon.getYPoints()[i]);
						}

						PolygonSimple translated = new PolygonSimple(x, y);
						g.draw(translated);
					}
				}
			}
		}

		if (simulator.getEnvironment() instanceof GridBoundaryEnvironment) {

			GridBoundaryEnvironment env = (GridBoundaryEnvironment) simulator.getEnvironment();

			EnvironmentGrid first = env.getGrids().get(0);

			double[][] firstGrid = first.getGrid();
			double[][] drawGrid = new double[firstGrid.length][firstGrid[0].length];

			for (EnvironmentGrid g : env.getGrids()) {

				double[][] grid = g.getGrid();

				for (int y = 0; y < grid.length; y++) {
					for (int x = 0; x < grid[y].length; x++) {
						drawGrid[y][x] = Math.max(drawGrid[y][x], grid[y][x]);
					}
				}
			}

			for (int y = 0; y < drawGrid.length; y++) {
				for (int x = 0; x < drawGrid[y].length; x++) {

					mathutils.Vector2d pos = first.getCartesianPosition(x, y);

					int w = (int) (first.getResolution() * scale);

					graphics.setColor(drawGrid[y][x] > 0 ? Color.LIGHT_GRAY : Color.white);

					if (first.getDecay() == 0)
						graphics.drawRect(transformX(pos.x), transformY(pos.y) - w, w, w);
					else if (drawGrid[y][x] > 0 && simulator.getTime() - first.getDecay() < drawGrid[y][x])
						graphics.drawRect(transformX(pos.x), transformY(pos.y) - w, w, w);
				}
			}

			for (Robot r : simulator.getRobots()) {
				Sensor s = r.getSensorByType(GridSensor.class);
				if (s != null) {
					GridSensor gs = (GridSensor) s;
					gs.paint(graphics, this);
				}
			}
		}
	}

	@Override
	protected void drawEntities(Graphics graphics, Robot robot) {
		if (seeEntities) {
			Color initialColor = graphics.getColor();
			RobotCI robotci = (RobotCI) robot;
			int circleDiameter = bigRobots ? (int) Math.max(10, Math.round(ENTITY_DIAMETER * scale))
					: (int) Math.round(ENTITY_DIAMETER * scale);

			if (robot.getId() == 0)
				colorIndex = 0;

			// to prevent ConcurrentModificationExceptions
			Object[] entities = robotci.getEntities().toArray();

			for (Object o : entities) {
				Entity entity = (Entity) o;
				if (entity instanceof GeoEntity) {
					if (entity instanceof Formation) {
						Formation formation = (Formation) entity;
						for (Target t : formation.getTargets()) {
							if (!drawTargets.contains(t)) {

								if (t.isOccupied()) {
									graphics.setColor(Color.GREEN);
								} else {
									graphics.setColor(Color.ORANGE);
								}

								Vector2d pos = CoordinateUtilities.GPSToCartesian(t.getLatLon());
								int diam = (int) (t.getRadius() * 2 * scale);
								int x = transformX(pos.x) - diam / 2;
								int y = transformY(pos.y) - diam / 2;
								graphics.fillOval(x, y, diam, diam);

								drawTargetId(graphics, t);
								if (showVelocityVectors && t.getMotionData() != null) {
									MotionData motionData = (t.getMotionData());
									Vector2d targetPos = CoordinateUtilities.GPSToCartesian(t.getLatLon());
									Vector2d velocityVector = new Vector2d(
											motionData.getVelocityVector(simulator.getTime()));

									if (velocityVector.length() > 0) {
										velocityVector.setLength(velocityVector.length() * 100);
										velocityVector.add(targetPos);

										graphics.setColor(Color.BLACK);
										graphics.drawLine(transformX(targetPos.x), transformY(targetPos.y),
												transformX(velocityVector.x), transformY(velocityVector.y));

										Color color = graphics.getColor();
										graphics.setColor(Color.RED);

										int radius = diam / 10;
										int x_pos = transformX(velocityVector.x) - radius;
										int y_pos = transformY(velocityVector.y) - radius;

										graphics.fillOval(x_pos, y_pos, radius * 2, radius * 2);
										graphics.setColor(color);
									}
								}

								drawTargets.add(t);
							}
						}

					} else {
						if (entity instanceof SharedDroneLocation)
							graphics.setColor(Color.BLUE.darker());
						else if (entity instanceof RobotLocation)
							graphics.setColor(Color.GREEN.darker());
						else
							graphics.setColor(Color.yellow.darker());

						GeoEntity e = (GeoEntity) entity;
						Vector2d pos = CoordinateUtilities.GPSToCartesian(e.getLatLon());
						int x = transformX(pos.x) - circleDiameter / 2;
						int y = transformY(pos.y) - circleDiameter / 2;
						graphics.fillOval(x, y, circleDiameter, circleDiameter);
					}
				} else if (entity instanceof VirtualEntity) {
					graphics.setColor(Color.GREEN.darker());
					VirtualEntity e = (VirtualEntity) entity;
					int x = transformX(e.getX()) - circleDiameter / 2;
					int y = transformY(e.getY()) - circleDiameter / 2;
					graphics.fillOval(x, y, circleDiameter, circleDiameter);
				} else if (entity instanceof GeoFence) {
					drawGeoFence((GeoFence) entity, lineColors[colorIndex % lineColors.length]);
					colorIndex++;
				}
			}

			graphics.setColor(initialColor);
		}
	}

	protected void drawTargetId(Graphics g, Target target) {
		Vector2d position = CoordinateUtilities.GPSToCartesian(target.getLatLon());

		int x = transformX(position.x + target.getRadius());
		int y = transformY(position.y - target.getRadius());

		g.setColor(Color.WHITE);
		g.fillRect(x, y - 10, 8, 10);

		String name = target.getName().replace("formation_target_", "t");

		if (target.getOccupantID() != null) {
			name += " (" + target.getOccupantID().substring(target.getOccupantID().length() - 3) + ")";
		}

		g.setColor(Color.BLACK);
		g.drawString(name, x, y);
	}

	protected void drawGeoFence(GeoFence geo, Color c) {
		LinkedList<Waypoint> waypoints = geo.getWaypoints();

		for (int i = 1; i < waypoints.size(); i++) {

			Waypoint wa = waypoints.get(i - 1);
			Waypoint wb = waypoints.get(i);
			Vector2d va = CoordinateUtilities.GPSToCartesian(wa.getLatLon());
			Vector2d vb = CoordinateUtilities.GPSToCartesian(wb.getLatLon());

			Line l = new Line(simulator, "line" + i, va.getX(), va.getY(), vb.getX(), vb.getY());
			l.setColor(c);
			drawLine(l);
		}

		Waypoint wa = waypoints.get(waypoints.size() - 1);
		Waypoint wb = waypoints.get(0);
		Vector2d va = CoordinateUtilities.GPSToCartesian(wa.getLatLon());
		Vector2d vb = CoordinateUtilities.GPSToCartesian(wb.getLatLon());

		Line l = new Line(simulator, "line0", va.getX(), va.getY(), vb.getX(), vb.getY());
		l.setColor(c);
		drawLine(l);
	}

	@Override
	protected void drawCones(Graphics graphics, Robot robot) {
		if (seeSensors) {
			RobotCI robotCI = (RobotCI) robot;
			if (robot.getId() == droneID && (coneSensorId >= 0 || !coneClass.isEmpty())) {
				for (CISensor ciSensor : robotCI.getCISensors()) {
					if (ciSensor.getClass().getSimpleName().equals(coneClass) || ciSensor.getId() == coneSensorId) {
						if (ciSensor != null) {
							if (ciSensor instanceof ConeTypeCISensor) {
								ConeTypeCISensor coneCISensor = (ConeTypeCISensor) ciSensor;
								paintCones(graphics, robot, coneCISensor);
							} else if (ciSensor instanceof ThymioConeTypeCISensor) {
								ThymioConeTypeCISensor coneCISensor = (ThymioConeTypeCISensor) ciSensor;
								paintCones(graphics, robot, coneCISensor);
							}

						}
					}
				}

				for (Sensor s : robot.getSensors()) {
					if (s.getClass().getSimpleName().equals(coneClass) || s.getId() == coneSensorId) {
						if (s != null && s instanceof ConeTypeSensor) {
							ConeTypeSensor coneSensor = (ConeTypeSensor) s;
							paintCones(graphics, robot, coneSensor);
						}
					}
				}

			}
		}
	}

	@Override
	protected void drawRobot(Graphics graphics, Robot robot) {
		super.drawRobot(graphics, robot);

		Vector2d position = new Vector2d(robot.getPosition().x, robot.getPosition().y);

		if (positionsHistory.get(robot) == null) {
			ArrayList<Vector2d> positions = new ArrayList<Vector2d>();
			positionsHistory.put(robot, positions);
		} else {
			positionsHistory.get(robot).add(position);
		}
	};

	protected void drawRobotsPositionHistory() {
		int circleDiameter = (bigRobots ? (int) Math.max(10, Math.round(ENTITY_DIAMETER * scale))
				: (int) Math.round(ENTITY_DIAMETER * scale)) / 4;
		Color originalColor = graphics.getColor();

		for (Robot robot : positionsHistory.keySet()) {
			ArrayList<Vector2d> positions = positionsHistory.get(robot);

			for (int i = 0; i < positions.size(); i++) {
				Color color = new Color(Color.DARK_GRAY.getRed(), Color.DARK_GRAY.getGreen(), Color.DARK_GRAY.getBlue(),
						(int) (i * 150.0 / positions.size()));

				graphics.setColor(color);
				int x = transformX(positions.get(i).x) - circleDiameter / 2;
				int y = transformY(positions.get(i).y) - circleDiameter / 2;
				graphics.fillOval(x, y, circleDiameter, circleDiameter);
			}
		}

		graphics.setColor(originalColor);
	}

	private void paintCones(Graphics graphics, Robot robot, ConeTypeSensor coneSensor) {
		for (int i = 0; i < coneSensor.getAngles().length; i++) {
			double angle = coneSensor.getAngles()[i];

			double xi = robot.getPosition().getX()
					+ robot.getRadius() * FastMath.cosQuick(angle + robot.getOrientation());
			double yi = robot.getPosition().getY()
					+ robot.getRadius() * FastMath.sinQuick(angle + robot.getOrientation());

			double cutOff = coneSensor.getCutOff();
			double openingAngle = coneSensor.getOpeningAngle();

			int x1 = transformX(xi);
			int y1 = transformY(yi);

			int x3 = transformX(xi - cutOff);
			int y3 = transformY(yi + cutOff);

			int a1 = (int) (FastMath.round(FastMath
					.toDegrees(coneSensor.getSensorsOrientations()[i] + robot.getOrientation() - openingAngle / 2)));

			Graphics2D graphics2D = (Graphics2D) graphics.create();

			if (cutOff > 0) {
				Point2D p = new Point2D.Double(x1, y1);
				float radius = (float) (cutOff * scale);
				float[] dist = { 0.0f, 1.0f };

				Color dark_grey = new Color(Color.DARK_GRAY.getRed(), Color.DARK_GRAY.getGreen(),
						Color.DARK_GRAY.getBlue(), (int) (coneTransparence * 100));
				Color light_grey = new Color(Color.LIGHT_GRAY.getRed(), Color.LIGHT_GRAY.getGreen(),
						Color.LIGHT_GRAY.getBlue(), (int) (coneTransparence * 100));

				// Color[] colors = { Color.DARK_GRAY, Color.LIGHT_GRAY };
				Color[] colors = { dark_grey, light_grey };

				RadialGradientPaint rgp = new RadialGradientPaint(p, radius, dist, colors);
				graphics2D.setPaint(rgp);

				graphics2D.fillArc(x3, y3, (int) FastMath.round(cutOff * 2 * scale),
						(int) (FastMath.round(cutOff * 2 * scale)), a1,
						(int) FastMath.round(FastMath.toDegrees(openingAngle)));
			}
		}
	}

	private void paintCones(Graphics graphics, Robot robot, ConeTypeCISensor coneSensor) {
		for (int i = 0; i < coneSensor.getAngles().length; i++) {
			double angle = coneSensor.getAngles()[i];

			double xi = robot.getPosition().getX()
					+ robot.getRadius() * FastMath.cosQuick(angle + robot.getOrientation());
			double yi = robot.getPosition().getY()
					+ robot.getRadius() * FastMath.sinQuick(angle + robot.getOrientation());

			double range = coneSensor.getRange();
			double openingAngle = coneSensor.getOpeningAngle();

			int x1 = transformX(xi);
			int y1 = transformY(yi);

			int x3 = transformX(xi - range);
			int y3 = transformY(yi + range);

			int a1 = (int) (FastMath
					.round(FastMath.toDegrees(coneSensor.getAngles()[i] + robot.getOrientation() - openingAngle / 2)));

			Graphics2D graphics2D = (Graphics2D) graphics.create();

			if (range > 0) {
				Point2D p = new Point2D.Double(x1, y1);
				float radius = (float) (range * scale);
				float[] dist = { 0.0f, 1.0f };

				Color dark_grey = new Color(Color.DARK_GRAY.getRed(), Color.DARK_GRAY.getGreen(),
						Color.DARK_GRAY.getBlue(), (int) (coneTransparence * 100));
				Color light_grey = new Color(Color.LIGHT_GRAY.getRed(), Color.LIGHT_GRAY.getGreen(),
						Color.LIGHT_GRAY.getBlue(), (int) (coneTransparence * 100));

				// Color[] colors = { Color.DARK_GRAY, Color.LIGHT_GRAY };
				Color[] colors = { dark_grey, light_grey };

				RadialGradientPaint rgp = new RadialGradientPaint(p, radius, dist, colors);
				graphics2D.setPaint(rgp);

				graphics2D.fillArc(x3, y3, (int) FastMath.round(range * 2 * scale),
						(int) (FastMath.round(range * 2 * scale)), a1,
						(int) FastMath.round(FastMath.toDegrees(openingAngle)));
			}
		}
	}

	private void paintCones(Graphics graphics, Robot robot, ThymioConeTypeCISensor coneSensor) {
		for (int i = 0; i < coneSensor.getAngles().length; i++) {
			double angle = coneSensor.getAngles()[i];

			double xi = robot.getPosition().getX()
					+ robot.getRadius() * FastMath.cosQuick(angle + robot.getOrientation());
			double yi = robot.getPosition().getY()
					+ robot.getRadius() * FastMath.sinQuick(angle + robot.getOrientation());

			double range = coneSensor.getRange();
			double openingAngle = coneSensor.getOpeningAngle();

			int x1 = transformX(xi);
			int y1 = transformY(yi);

			int x3 = transformX(xi - range);
			int y3 = transformY(yi + range);

			int a1 = (int) (FastMath
					.round(FastMath.toDegrees(coneSensor.getAngles()[i] + robot.getOrientation() - openingAngle / 2)));

			Graphics2D graphics2D = (Graphics2D) graphics.create();

			if (range > 0) {
				Point2D p = new Point2D.Double(x1, y1);
				float radius = (float) (range * scale);
				float[] dist = { 0.0f, 1.0f };

				Color dark_grey = new Color(Color.DARK_GRAY.getRed(), Color.DARK_GRAY.getGreen(),
						Color.DARK_GRAY.getBlue(), (int) (coneTransparence * 100));
				Color light_grey = new Color(Color.LIGHT_GRAY.getRed(), Color.LIGHT_GRAY.getGreen(),
						Color.LIGHT_GRAY.getBlue(), (int) (coneTransparence * 100));

				// Color[] colors = { Color.DARK_GRAY, Color.LIGHT_GRAY };
				Color[] colors = { dark_grey, light_grey };

				RadialGradientPaint rgp = new RadialGradientPaint(p, radius, dist, colors);
				graphics2D.setPaint(rgp);

				graphics2D.fillArc(x3, y3, (int) FastMath.round(range * 2 * scale),
						(int) (FastMath.round(range * 2 * scale)), a1,
						(int) FastMath.round(FastMath.toDegrees(openingAngle)));
			}
		}
	}

	/*
	 * Getter and setters
	 */
	public void seeSensors(boolean seeSensors) {
		this.seeSensors = seeSensors;
	}

	public void seeEntities(boolean seeEntities) {
		this.seeEntities = seeEntities;
	}

	public void setSeeRobotFollowingTarget(boolean seeRobotFollowingTarget) {
		this.seeRobotFollowingTarget = seeRobotFollowingTarget;
	}

	public void setShowRobotsPositionHistory(boolean showRobotsPositionHistory) {
		this.showRobotsPositionHistory = showRobotsPositionHistory;
	}

	public boolean isSeeSensorEnabled() {
		return seeSensors;
	}

	public boolean isSeeEntitiesEnabled() {
		return seeEntities;
	}

	public boolean isSeeRobotTargetsEnabled() {
		return seeRobotFollowingTarget;
	}

	public boolean isShowRobotsPositionHistoryEnabled() {
		return showRobotsPositionHistory;
	}

	public void setDroneID(int droneID) {
		this.droneID = droneID;
	}

	public void setConeSensorID(int coneSensorId) {
		this.coneSensorId = coneSensorId;
	}

	public void setConeTransparency(double coneTransparence) {
		this.coneTransparence = coneTransparence;
	}

	public void setConeClass(String coneClass) {
		this.coneClass = coneClass;
	}

	public void displayVelocityVectors(boolean show) {
		this.showVelocityVectors = show;
	}

	public synchronized void resetPositionHistory() {
		positionsHistory.clear();
	}
}
