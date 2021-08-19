/*******************************************************************************
 * PathVisio, a tool for data visualization and analysis using biological pathways
  * Copyright 2006-2021 BiGCaT Bioinformatics, WikiPathways
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License.  You may obtain a copy
 * of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  See the
 * License for the specific language governing permissions and limitations under
 * the License.
 ******************************************************************************/
package org.pathvisio.view.model;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.geom.AffineTransform;
import java.awt.geom.Area;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

//import org.pathvisio.model.GraphLink.LinkableFrom;
import org.pathvisio.model.type.LineStyleType;
import org.pathvisio.view.connector.ConnectorRestrictions;
import org.pathvisio.view.connector.ConnectorShape;
import org.pathvisio.view.connector.ConnectorShapeFactory;
import org.pathvisio.view.connector.ElbowConnectorShape;
import org.pathvisio.view.connector.ConnectorShape.Segment;
import org.pathvisio.view.connector.ConnectorShape.WayPoint;
import org.pathvisio.model.type.ArrowHeadType;
import org.pathvisio.model.LineElement;
import org.pathvisio.model.PathwayElement;
import org.pathvisio.model.PathwayModel;
import org.pathvisio.model.graphics.LineStyleProperty;
import org.pathvisio.model.Anchor;
import org.pathvisio.model.DataNode;
import org.pathvisio.model.Label;
import org.pathvisio.model.GraphLink.LinkableFrom;
import org.pathvisio.model.GraphLink.LinkableTo;
import org.pathvisio.model.LinePoint;
import org.pathvisio.io.listener.PathwayElementEvent;
import org.pathvisio.util.Utils;
import org.pathvisio.util.preferences.GlobalPreference;
import org.pathvisio.util.preferences.PreferenceManager;
import org.pathvisio.view.model.Adjustable;
import org.pathvisio.view.ArrowShape;
import org.pathvisio.view.ShapeRegistry;
import org.pathvisio.view.model.Handle.Freedom;
import org.pathvisio.view.model.Handle.Style;

/**
 * This class represents a Line {@link LineElement} on the pathway, or rather a
 * series of line segments that are joined together.
 *
 * It has two VPoints with a Handle. It may have zero or more anchors, each with
 * their own Handle. It has one or more segments, any segment in excess of two
 * will get a Segment Handle.
 *
 * The actual implementation of the path is done by implementations of the
 * {@link ConnectorShape} interface.
 * 
 * @see ConnectorShape
 * @see org.pathvisio.view.connector.ConnectorShapeFactory
 */
public class VLineElement extends VCitable implements Adjustable, ConnectorRestrictions { // TODO ConnectorRestrictions

	protected MLine gdata = null;

	private List<VPoint> points;

	private Map<Anchor, VAnchor> anchors = new HashMap<Anchor, VAnchor>();

	List<Handle> segmentHandles = new ArrayList<Handle>();

	ConnectorShape connectorShape;

	/**
	 * Constructor for this class
	 * 
	 * @param canvas - the VPathway this line will be part of
	 */
	public VLineElement(VPathwayModel canvas, MLine gdata) {
		super(canvas);
		gdata.addListener(this);
		this.gdata = gdata;
		checkCitation(gdata.getCitationRefs()); // TODO
		points = new ArrayList<VPoint>();
		addPoint(gdata.getStartLinePoint()); // TODO
		addPoint(gdata.getEndLinePoint()); // TODO
		setAnchors();
		getConnectorShape().recalculateShape(this); // TODO weird???
//		updateSegmentHandles();
		updateCitationPosition();
	}

	private void addPoint(LinePoint mp) {
		VPoint vp = canvas.newPoint(mp, this);
		points.add(vp);
		setHandleLocation(vp);
	}

	private MLine getMLine() {
		return (MLine) gdata;
	}

	public void createHandles() {
		createSegmentHandles();

		for (VPoint vp : points) {
			vp.handle = new Handle(Handle.Freedom.FREE, this, vp);
			// vp.handle.setCursorHint(Cursor.HAND_CURSOR);
			vp.handle.setAngle(1);
			setHandleLocation(vp);
		}
	}

	/**
	 * Create new segment handles
	 */
	private void createSegmentHandles() {
		ConnectorShape cs = getConnectorShape();
		WayPoint[] waypoints = cs.getWayPoints();

		// Destroy the old handles, just to be sure
		for (Handle h : segmentHandles)
			h.destroy();
		segmentHandles.clear();

		// Create the new handles
		for (int i = 0; i < waypoints.length; i++) {
			Handle h = new Handle(Handle.Freedom.FREE, this, this);
			h.setStyle(Handle.Style.SEGMENT);
			segmentHandles.add(h);
		}

		// Put the handles in the right place
		for (int i = 0; i < waypoints.length; i++) {
			Handle h = segmentHandles.get(i);
			h.setMLocation(waypoints[i].getX(), waypoints[i].getY());
		}
	}

	/**
	 * Update the segment handles, if the ConnectorShape has changed so much that
	 * the number of segment handles doesn't match the number of segments anymore,
	 * number of segments, they will be destroyed and recreated.
	 */
	private void updateSegmentHandles() {
		ConnectorShape cs = getConnectorShape();
		WayPoint[] waypoints = cs.getWayPoints();

		// Destroy and recreate the handles if the number
		// doesn't match the waypoints number
		if (waypoints.length != segmentHandles.size()) {
			// clear and create from scratch
			createSegmentHandles();
		} else {
			// just adjust the positions
			for (int i = 0; i < waypoints.length; i++) {
				Handle h = segmentHandles.get(i);
				h.setMLocation(waypoints[i].getX(), waypoints[i].getY());
			}
		}
	}

	/**
	 * Updates the segment preferences to the new handle position
	 */
	public void adjustToHandle(Handle h, double vx, double vy) {
		WayPoint[] waypoints = getConnectorShape().getWayPoints();
		int index = segmentHandles.indexOf(h);
		if (index > -1) {
			List<LinePoint> points = gdata.getLinePoints();
			if (points.size() - 2 != (waypoints.length)) {
				// Recreate points from segments
				points = new ArrayList<LinePoint>();
				points.add(gdata.getStartLinePoint());
				for (int i = 0; i < waypoints.length; i++) {
					LinePoint p = gdata.new LinePoint(waypoints[i].getX(), waypoints[i].getY());
					points.add(p);
				}
				points.add(gdata.getEndLinePoint());
				gdata.dontFireEvents(1);
				gdata.setLinePoints(points);
			}
			points.get(index + 1).moveTo(mFromV(vx), mFromV(vy));
		}
	}

	private List<Handle> getSegmentHandles() {
		return segmentHandles;
	}

	private ConnectorShape getConnectorShape() {
		return getMLine().getConnectorShape();
	}

	/**
	 * Get the connector shape translated to view coordinates allowing for Line
	 * Ending This allows the line to be drawn only upto the point where the line
	 * ending starts
	 */
	public Shape getVConnectorAdjusted() {

		// call to getLineEndingWidth
		double startGap = getGap(gdata.getStartLineType());
		double endGap = getGap(gdata.getEndLineType());

		// From the segments
		Shape s = getConnectorShape().calculateAdjustedShape(startGap, endGap);

		AffineTransform t = new AffineTransform();
		double scale = vFromM(1);
		t.setToScale(scale, scale);
		return t.createTransformedShape(s);
	}

	/**
	 * returns the gap that goes with the specified LineType If no line ending, the
	 * method returns 0
	 */
	private double getGap(ArrowHeadType type) {

		double gap = 0;
		if (type == null) {
			gap = ShapeRegistry.getArrow("Default").getGap(); // TODO...
		} else if (type.getName().equals("Line")) {
			gap = 0;
		} else {
			gap = ShapeRegistry.getArrow(type.getName()).getGap();
		}
		return gap;

	}

	public void doDraw(Graphics2D g) {
		Color c = getLineColor();
		g.setColor(c);
		setLineStyle(g);

		Shape l = getVConnectorAdjusted();

		ArrowShape[] heads = getVHeadsAdjusted();
		ArrowShape hs = heads[0];
		ArrowShape he = heads[1];

		g.draw(l);
		drawHead(g, he, c);
		drawHead(g, hs, c);
		if (isHighlighted()) {
			Color hc = getHighlightColor();
			g.setColor(new Color(hc.getRed(), hc.getGreen(), hc.getBlue(), 128));
			g.setStroke(new BasicStroke(HIGHLIGHT_STROKE_WIDTH));
			g.draw(l);
			if (he != null)
				g.draw(he.getShape());
			if (hs != null)
				g.draw(hs.getShape());
		}

		// highlight unlinked points, after pressing Ctrl+L
		for (VPoint vp : points) {
			if (vp.isHighlighted()) {
				int size = 8;
				g.setColor(PreferenceManager.getCurrent().getColor(GlobalPreference.COLOR_HIGHLIGHTED));
				g.fill(new Rectangle2D.Double(vp.getVX() - size / 2, vp.getVY() - size / 2, size, size));
			}
		}
	}

	/** Overridden, to unhighlight VPoints as well */
	@Override
	public void unhighlight() {
		super.unhighlight();
		for (VPoint vp : points)
			vp.unhighlight();
	}

	/**
	 * Be careful to prevent infinite recursion when Line.getVOutline triggers
	 * recalculation of a connector.
	 *
	 * For now, only check crossing of geneproducts and shapes.
	 */
	public Shape mayCross(Point2D point) {
		Shape shape = null;
		for (VElement o : canvas.getDrawingObjects()) {
			if (o instanceof VDataNode || o instanceof Shape) // TODO VShape?
				if (o.vContains(point)) {
					shape = o.getVOutline();
				}
		}

		return shape;
	}

	public Point2D getStartPoint() {
		return new Point2D.Double(getVStartX(), getVStartY());
	}

	public Point2D getEndPoint() {
		return new Point2D.Double(getVEndX(), getVEndY());
	}

	public Shape calculateVOutline() {
		return getVShape(true);
	}

	/**
	 * Returns the properly sized and rotated arrowheads
	 * 
	 * @return An array with two arrowheads, for the start and end respectively
	 */
	public ArrowShape[] getVHeads() {
		Segment[] segments = getConnectorShape().getSegments();

		ArrowShape he = getVHead(segments[segments.length - 1].getMStart(), segments[segments.length - 1].getMEnd(),
				gdata.getEndLineType());
		ArrowShape hs = getVHead(segments[0].getMEnd(), segments[0].getMStart(), gdata.getStartLineType());
		return new ArrowShape[] { hs, he };
	}

	/**
	 * Returns the properly sized and rotated arrowheads which have been adjusted
	 * for Line ending thickness
	 * 
	 * @return An array with two arrowheads, for the start and end respectively
	 */
	public ArrowShape[] getVHeadsAdjusted() {
		Segment[] segments = getConnectorShape().getSegments();

		// last segment in the Connector Shape
		double lineEndingWidth = getGap(gdata.getEndLineType());
		Point2D adjustedSegmentEnd = segments[segments.length - 1].calculateNewEndPoint(lineEndingWidth);
		ArrowShape he = getVHead(segments[segments.length - 1].getMStart(), adjustedSegmentEnd, gdata.getEndLineType());

		// first segment in the connector shape
		double lineStartingWidth = getGap(gdata.getStartLineType());
		Point2D adjustedSegmentStart = segments[0].calculateNewStartPoint(lineStartingWidth);
		ArrowShape hs = getVHead(segments[0].getMEnd(), adjustedSegmentStart, gdata.getStartLineType());
		return new ArrowShape[] { hs, he };
	}

	protected Shape getVShape(boolean rotate) {
		Shape l = getVConnectorAdjusted();

		ArrowShape[] heads = getVHeadsAdjusted();
		ArrowShape hs = heads[0];
		ArrowShape he = heads[1];

		float thickness = (float) vFromM(gdata.getLineStyleProp().getLineWidth());
		if (gdata.getLineStyleProp().getLineStyle() == LineStyleType.DOUBLE)
			thickness *= 4;
		BasicStroke bs = new BasicStroke(thickness);

		Area total = new Area(bs.createStrokedShape(l));
		if (hs != null)
			total.add(new Area(bs.createStrokedShape(hs.getShape())));
		if (he != null)
			total.add(new Area(bs.createStrokedShape(he.getShape())));

		return total;
	}

	private void setAnchors() {
		// Check for new anchors
		List<Anchor> Anchors = gdata.getAnchors();
		for (Anchor ma : Anchors) {
			if (!anchors.containsKey(ma)) {
				anchors.put(ma, new VAnchor(ma, this));
			}
		}
		// Check for deleted anchors
		for (Anchor ma : anchors.keySet()) {
			if (!Anchors.contains(ma)) {
				anchors.get(ma).destroy();
			}
		}
	}

	protected Collection<VAnchor> getVAnchors() {
		return anchors.values();
	}

	public void markDirty() {
		super.markDirty();
		for (VAnchor va : anchors.values()) {
			va.markDirty();
		}
	}

	void removeVAnchor(VAnchor va) {
		anchors.remove(va.getAnchor());
		gdata.removeAnchor(va.getAnchor());
	}

	private void updateAnchorPositions() {
		for (VAnchor va : anchors.values()) {
			va.updatePosition();
		}
	}

	private void updateCitationPosition() {
		if (getVCitation() == null)
			return;
		Point2D p = getConnectorShape().fromLineCoordinate(0.7);
		p.setLocation(p.getX() - 5, p.getY());
		Point2D r = gdata.toRelativeCoordinate(p);
		getVCitation().setRPosition(r);
	}

	protected void swapPoint(VPoint pOld, VPoint pNew) {
		int i = points.indexOf(pOld);
		if (i > -1) {
			points.remove(pOld);
			points.add(i, pNew);
		}
	}

	protected void drawHead(Graphics2D g, ArrowShape head, Color c) {
		if (head != null) {
			// reset stroked line to solid, but use given thickness
			g.setStroke(new BasicStroke((float) vFromM(gdata.getLineStyleProp().getLineWidth())));
			switch (head.getFillType()) {
			case OPEN:
				g.setPaint(Color.WHITE);
				g.fill(head.getShape());
				g.setColor(c);
				g.draw(head.getShape());
				break;
			case CLOSED:
				g.setPaint(c);
				g.fill(head.getShape());
				break;
			case WIRE:
				g.setColor(c);
				g.draw(head.getShape());
				break;
			default:
				assert (false);
			}
		}
	}

	/**
	 * Will return the arrowhead suitable for an arrow pointing from p1 to p2 (so
	 * the tip of the arrowhead will be at p2).
	 * 
	 * @param mP1 The start point in model coordinates
	 * @param mP2 The end point in model coordinates
	 * @return The ArrowShape in view coordinates
	 */
	protected ArrowShape getVHead(Point2D mP1, Point2D mP2, ArrowHeadType type) {
		double xs = vFromM(mP1.getX());
		double ys = vFromM(mP1.getY());
		double xe = vFromM(mP2.getX());
		double ye = vFromM(mP2.getY());

		ArrowShape h;
		if (type == null) {
			h = ShapeRegistry.getArrow("Default");
		} else if (type.getName().equals("Line")) {
			h = null;
		} else {
			h = ShapeRegistry.getArrow(type.getName());
		}

		if (h != null) {
			AffineTransform f = new AffineTransform();
			double scaleFactor = vFromM(1.0 + 0.3 * gdata.getLineStyleProp().getLineWidth());
			f.rotate(Math.atan2(ye - ys, xe - xs), xe, ye);
			f.translate(xe, ye);
			f.scale(scaleFactor, scaleFactor);
			Shape sh = f.createTransformedShape(h.getShape());
			h = new ArrowShape(sh, h.getFillType());
		}
		return h;
	}

	/**
	 * Sets the line start and end to the coordinates specified
	 * <DL>
	 * <B>Parameters</B>
	 * <DD>Double x1 - new startx
	 * <DD>Double y1 - new starty
	 * <DD>Double x2 - new endx
	 * <DD>Double y2 - new endy
	 */
	private void setVLine(double vx1, double vy1, double vx2, double vy2) {
		getStart().setVLocation(vx1, vy1);
		getEnd().setVLocation(vx2, vy2);
	}

	protected void setVScaleRectangle(Rectangle2D r) {
		setVLine(r.getX(), r.getY(), r.getX() + r.getWidth(), r.getY() + r.getHeight());
	}

	public List<VPoint> getPoints() {
		return points;
	}

	public VPoint getStart() {
		return points.get(0);
	}

	public VPoint getEnd() {
		return points.get(points.size() - 1);
	}

	public double getVCenterX() {
		return vFromM(gdata.getMCenterX());
	}

	public double getVCenterY() {
		return vFromM(gdata.getMCenterY());
	}

	public double getVLeft() {
		return vFromM(gdata.getMLeft());
	}

	public double getVWidth() {
		return vFromM(gdata.getMWidth());
	}

	public double getVHeight() {
		return vFromM(gdata.getMHeight());
	}

	public double getVTop() {
		return vFromM(gdata.getMTop());
	}

	protected void vMoveWayPointsBy(double vdx, double vdy) {
		List<LinePoint> mps = gdata.getLinePoints();
		for (int i = 1; i < mps.size() - 1; i++) {
			mps.get(i).moveBy(mFromV(vdx), mFromV(vdy));
		}
	}

	protected void vRecalculatePoints(double vdx, double vdy) {
		for (VPoint p : points) {
			p.setVLocation(p.getVX() + canvas.mFromV(vdx), p.getVY() + canvas.mFromV(vdy));
		}
	}

	protected void vMoveBy(double vdx, double vdy) {
		// move LinePoints directly, not every LinePoint is represented
		// by a VPoint but we want to move them all.
		for (LinePoint p : gdata.getLinePoints()) {
			p.moveBy(canvas.mFromV(vdx), canvas.mFromV(vdy));
		}
		// Redraw graphRefs
		for (LinkableFrom ref : gdata.getReferences()) { // TODO ....
			if (ref instanceof LinePoint) {
				VPoint vp = canvas.getPoint((LinePoint) ref);
				if (vp != null) {
					vp.getLine().recalculateConnector();
				}
			}
		}
	}

	private void setHandleLocation(VPoint vp) {
		if (vp.handle == null)
			return;
		LinePoint mp = vp.getLinePoint();
		vp.handle.setMLocation(mp.getXY().getX(), mp.getXY().getY());
	}

	public void recalculateConnector() {
		getConnectorShape().recalculateShape(getMLine());
		updateAnchorPositions();
		updateCitationPosition();
		for (VPoint vp : points)
			setHandleLocation(vp);
		markDirty();
	}

	public void gmmlObjectModified(PathwayElementEvent e) {
		getConnectorShape().recalculateShape(getMLine());

		WayPoint[] wps = getConnectorShape().getWayPoints();
		List<LinePoint> mps = gdata.getLinePoints();
		if (wps.length == mps.size() - 2 && getConnectorShape().hasValidWaypoints(getMLine())) {
			getMLine().adjustWayPointPreferences(wps);
		} else {
			getMLine().resetWayPointPreferences();
		}

		updateSegmentHandles();
		markDirty();
		for (VPoint p : points) {
			setHandleLocation(p);
		}
		if (gdata.getAnchors().size() != anchors.size()) {
			setAnchors();
		}
		checkCitation(gdata.getCitationRefs());
		updateAnchorPositions();
		updateCitationPosition();
	}

	protected void destroyHandles() {
		// Point handles will be destroyed by VPoints

		for (Handle h : getSegmentHandles()) {
			h.destroy();
		}
		for (VPoint p : points) {
			if (p.handle != null)
				p.handle.destroy();
		}
	}

	protected void destroy() {
		super.destroy();
		gdata.removeListener(this);
		for (VElement child : getChildren()) {
			child.destroy();
		}

		for (LinePoint p : gdata.getLinePoints()) {
			canvas.pointsMtoV.remove(p);
		}
		List<VAnchor> remove = new ArrayList<VAnchor>(anchors.values());
		for (VAnchor a : remove) {
			a.destroy();
		}
		getChildren().clear();
		setVCitation(null);

		// View should not remove its model
//		Pathway parent = lineElement.getParent();
//		if(parent != null) parent.remove(lineElement);
	}

	/**
	 * Returns the x-coordinate of the start point of this line, adjusted to the
	 * current zoom factor
	 * 
	 * @return
	 */
	protected double getVStartX() {
		return (int) (vFromM(gdata.getMStartX()));
	}

	/**
	 * Returns the y-coordinate of the start point of this line, adjusted to the
	 * current zoom factor
	 * 
	 * @return
	 */
	protected double getVStartY() {
		return (int) (vFromM(gdata.getMStartY()));
	}

	/**
	 * Returns the x-coordinate of the end point of this line, adjusted to the
	 * current zoom factor
	 * 
	 * @return
	 */
	protected double getVEndX() {
		return (int) (vFromM(gdata.getMEndX()));
	}

	/**
	 * Returns the y-coordinate of the end point of this line, adjusted to the
	 * current zoom factor
	 * 
	 * @return
	 */
	protected double getVEndY() {
		return (int) (vFromM(gdata.getMEndY()));
	}

	/**
	 * Translate a line coordinate (1-dimensional) to a view coordinate
	 * 
	 * @param l The line coordinate
	 */
	public Point2D vFromL(double l) {
		Point2D m = getConnectorShape().fromLineCoordinate(l);
		return new Point2D.Double(vFromM(m.getX()), vFromM(m.getY()));
	}

	/**
	 * Translate a view coordinate (2-dimensional) to a line coordinate
	 * (1-dimensional)
	 */
	public double lFromV(Point2D v) {
		Point2D m = new Point2D.Double(mFromV(v.getX()), mFromV(v.getY()));
		return getConnectorShape().toLineCoordinate(m);
	}

	/**
	 * Get the segment on which the given line coordinate lies
	 */
	public Segment getSegment(double lc) {
		Segment[] segments = getConnectorShape().getSegments();
		double length = 0;
		for (Segment s : segments) {
			length += s.getMLength();
		}
		double end = 0;
		for (Segment s : segments) {
			end += s.getMLength();
			if (lc <= end) {
				return s;
			}
		}
		return segments[segments.length];

	}

	/*-----------------------------Graphics methods ---------------------------*/

	/**
	 * Get the rectangle that represents the bounds of the shape's direct
	 * translation from model to view, without taking into account rotation. Default
	 * implementation is equivalent to <code>getVShape(false).getBounds2D();</code>
	 */
	protected Rectangle2D getVScaleRectangle() {
		return getVShape(false).getBounds2D();
	}

	/**
	 * Returns the z-order from the model
	 */
	protected int getZOrder() {
		return gdata.getLineStyleProp().getZOrder();
	}

	protected Color getLineColor() {
		Color linecolor = gdata.getLineStyleProp().getLineColor();
		/*
		 * the selection is not colored red when in edit mode it is possible to see a
		 * color change immediately
		 */
		if (isSelected() && !canvas.isEditMode()) {
			linecolor = selectColor;
		}
		return linecolor;
	}

	protected void setLineStyle(Graphics2D g) {
		LineStyleType ls = gdata.getLineStyleProp().getLineStyle();
		float lt = (float) vFromM(gdata.getLineStyleProp().getLineWidth());
		if (ls == LineStyleType.SOLID) {
			g.setStroke(new BasicStroke(lt));
		} else if (ls == LineStyleType.DASHED) {
			g.setStroke(
					new BasicStroke(lt, BasicStroke.CAP_SQUARE, BasicStroke.JOIN_MITER, 10, new float[] { 4, 4 }, 0));
		} else if (ls == LineStyleType.DOUBLE) {
			g.setStroke(new CompositeStroke(new BasicStroke(lt * 2), new BasicStroke(lt)));
		}
	}

	private class MLine extends LineElement implements ConnectorRestrictions {
		ConnectorShape shape;

		public MLine(LineStyleProperty ot) {
			super(ot);
		}

		/**
		 * the Connector Shape for this line - the connector shape can calculate a Shape
		 * based on the connector type (straight, elbow or curved) and possibly way
		 * points
		 */
		public ConnectorShape getConnectorShape() {
			String type = getLineStyleProp().getConnectorType().getName();

			// Recreate the ConnectorShape when it's null or when the type
			// doesn't match the implementing class
			if (shape == null || !shape.getClass().equals(ConnectorShapeFactory.getImplementingClass(type))) {
				shape = ConnectorShapeFactory.createConnectorShape(getLineStyleProp().getConnectorType().getName());
				shape.recalculateShape(this);
			}
			return shape;
		}

		/**
		 * returns the center x coordinate of the bounding box around (start, end)
		 */
		public double getMCenterX() {
			double start = getMStartX();
			double end = getMEndX();
			return start + (end - start) / 2;
		}

		/**
		 * returns the center y coordinate of the bounding box around (start, end)
		 */
		public double getMCenterY() {
			double start = getMStartY();
			double end = getMEndY();
			return start + (end - start) / 2;
		}

		/**
		 * returns the left x coordinate of the bounding box around (start, end)
		 */
		public double getMLeft() {
			double start = getMStartX();
			double end = getMEndX();
			return Math.min(start, end);
		}

		/**
		 * returns the width of the bounding box around (start, end)
		 */
		public double getMWidth() {
			double start = getMStartY();
			double end = getMEndX();
			return Math.abs(start - end);
		}

		/**
		 * returns the height of the bounding box around (start, end)
		 */
		public double getMHeight() {
			double start = getMStartY();
			double end = getMEndY();
			return Math.abs(start - end);
		}

		/**
		 * returns the top y coordinate of the bounding box around (start, end)
		 */
		public double getMTop() {
			double start = getMStartY();
			double end = getMEndY();
			return Math.min(start, end);
		}

		/**
		 * Sets the position of the top side of the rectangular bounds of the line
		 */
		public void setMTop(double v) {
			if (getDirectionY() > 0) {
				setMStartY(v);
			} else {
				setMEndY(v);
			}
		}

		/**
		 * Sets the position of the left side of the rectangular bounds of the line
		 */
		public void setMLeft(double v) {
			if (getDirectionX() > 0) {
				setMStartX(v);
			} else {
				setMEndX(v);
			}
		}

		/**
		 * Sets the x position of the center of the line. This makes the line move as a
		 * whole
		 */
		public void setMCenterX(double v) {
			double dx = v - getMCenterX();
			setMStartX(getMStartX() + dx);
			setMEndX(getMEndX() + dx);
		}

		/**
		 * Sets the y position of the center of the line. This makes the line move as a
		 * whole.
		 */
		public void setMCenterY(double v) {
			double dy = v - getMCenterY();
			setMStartY(getMStartY() + dy);
			setMEndY(getMEndY() + dy);
		}

		/** returns the sign of end.x - start.x */
		private int getDirectionX() {
			return (int) Math.signum(getMEndX() - getMStartX());
		}

		/** returns the sign of end.y - start.y */
		private int getDirectionY() {
			return (int) Math.signum(getMEndY() - getMStartY());
		}

		/** converts end point from LinePoint to Point2D */
		public Point2D getEndPoint() {
			return toPoint2D(getEndLinePoint());
		}

		/** converts start point from LinePoint to Point2D */
		public Point2D getStartPoint() {
			return toPoint2D(getStartLinePoint());
		}

		/** converts all points from LinePoint to Point2D */
		public List<Point2D> getPoints() {
			List<Point2D> pts = new ArrayList<Point2D>();
			for (LinePoint p : getLinePoints()) {
				pts.add(toPoint2D(p));
			}
			return pts;
		}

		/**
		 * Returns the element that the start of this line is connected to. Returns null
		 * if there isn't any.
		 */
		private LinkableTo getStartElement() {
			PathwayModel parent = getPathwayModel();
			if (parent != null) {
				return getStartLinePoint().getElementRef();
			}
			return null;
		}

		/**
		 * Returns the element that the end of this line is connected to. Returns null
		 * if there isn't any.
		 */
		private LinkableTo getEndElement() {
			PathwayModel parent = getPathwayModel();
			if (parent != null) {
				return getEndLinePoint().getElementRef();
			}
			return null;
		}

		/**
		 * Calculate on which side of a PathwayElement (SIDE_NORTH, SIDE_EAST,
		 * SIDE_SOUTH or SIDE_WEST) the start of this line is connected to.
		 *
		 * If the start is not connected to anything, returns SIDE_WEST
		 */
		public int getStartSide() {
			int side = SIDE_WEST;

			LinkableTo e = getStartElement();
			if (e != null) {
				if (e instanceof PathwayElement) {
					side = getSide(getStartLinePoint().getRelX(), getStartLinePoint().getRelY());
				} else if (e instanceof Anchor) {
					side = getAttachedLineDirection((Anchor) e);
				}
			}
			return side;
		}

		/**
		 * Calculate on which side of a PathwayElement (SIDE_NORTH, SIDE_EAST,
		 * SIDE_SOUTH or SIDE_WEST) the end of this line is connected to.
		 *
		 * If the end is not connected to anything, returns SIDE_EAST
		 */
		public int getEndSide() {
			int side = SIDE_EAST;

			LinkableTo e = getEndElement();
			if (e != null) {
				if (e instanceof PathwayElement) {
					side = getSide(getEndLinePoint().getRelX(), getEndLinePoint().getRelY());
				} else if (e instanceof Anchor) {
					side = getAttachedLineDirection((Anchor) e);
				}
			}
			return side;
		}

		private int getAttachedLineDirection(Anchor anchor) {
			int side;
			double pos = anchor.getPosition();
			MLine attLine = ((MLine) anchor.getLineElement());
			if (attLine.getConnectorShape() instanceof ElbowConnectorShape) {
				ConnectorShape.Segment attSeg = findAnchorSegment(attLine, pos);
				int orientationX = Utils.getDirectionX(attSeg.getMStart(), attSeg.getMEnd());
				int orientationY = Utils.getDirectionY(attSeg.getMStart(), attSeg.getMEnd());
				side = getSide(orientationY, orientationX);
			} else {
				side = getOppositeSide(getSide(getMEndX(), getMEndY(), getMStartX(), getMStartY()));
				if (attLine.almostPerfectAlignment(side)) {
					side = getClockwisePerpendicularSide(side);
				}
			}
			return side;
		}

		private ConnectorShape.Segment findAnchorSegment(MLine attLine, double pos) {
			ConnectorShape.Segment[] segments = attLine.getConnectorShape().getSegments();
			Double totLength = 0.0;
			ConnectorShape.Segment attSeg = null;
			for (ConnectorShape.Segment segment : segments) {
				totLength = totLength + segment.getMLength();
			}
			Double currPos;
			Double segSum = 0.0;
			for (ConnectorShape.Segment segment : segments) {
				segSum = segSum + segment.getMLength();
				currPos = segSum / totLength;
				attSeg = segment;
				if (currPos > pos) {
					break;
				}
			}
			return attSeg;
		}

		/**
		 * Check if either the line segment has less than or equal to 10 degree
		 * alignment with the side passed
		 * 
		 * @param startLine
		 * @param endLine
		 * @return true if <= 10 degree alignment else false
		 */
		private boolean almostPerfectAlignment(int side) {
			int MAXOFFSET = 30; /*
								 * cutoff point where we see a shallow angle still as either horizontal or
								 * vertical
								 */
			// X axis
			if ((side == SIDE_EAST) || (side == SIDE_WEST)) {
				double angleDegree = (180 / Math.PI)
						* Math.atan2(Math.abs(getStartPoint().getY() - getEndPoint().getY()),
								Math.abs(getStartPoint().getX() - getEndPoint().getX()));
				if (angleDegree <= MAXOFFSET)
					return true;
			} else {// north south or Y axis
				double angleDegree = (180 / Math.PI)
						* Math.atan2(Math.abs(getStartPoint().getX() - getEndPoint().getX()),
								Math.abs(getStartPoint().getY() - getEndPoint().getY()));
				if (angleDegree <= MAXOFFSET)
					return true;
			}
			return false;
		}

		/**
		 * Returns the Perpendicular for a SIDE_* constant (e.g. SIDE_EAST <->
		 * SIDE_WEST)
		 */
		private int getClockwisePerpendicularSide(int side) {
			switch (side) {
			case SIDE_EAST:
				return SIDE_SOUTH;
			case SIDE_WEST:
				return SIDE_NORTH;
			case SIDE_NORTH:
				return SIDE_EAST;
			case SIDE_SOUTH:
				return SIDE_WEST;
			}
			return -1;
		}

		public void adjustWayPointPreferences(WayPoint[] waypoints) {
			List<LinePoint> LinePoints = getLinePoints();
			for (int i = 0; i < waypoints.length; i++) {
				WayPoint wp = waypoints[i];
				LinePoint mp = LinePoints.get(i + 1);
				if (mp.getXY().getX() != wp.getX() || mp.getXY().getY() != wp.getY()) {
					dontFireEvents(1);
					mp.moveTo(wp.getX(), wp.getY());
				}
			}
		}

		public void resetWayPointPreferences() {
			List<LinePoint> mps = getLinePoints();
			while (mps.size() > 2) {
				mps.remove(mps.size() - 2);
			}
		}

		/**
		 * Get the preferred waypoints, to which the connector must draw it's path. The
		 * waypoints returned by this method are preferences and the connector shape may
		 * decide not to use them if they are invalid.
		 */
		public WayPoint[] getWayPointPreferences() {
			List<LinePoint> pts = getLinePoints();
			WayPoint[] wps = new WayPoint[pts.size() - 2];
			for (int i = 0; i < wps.length; i++) {
				wps[i] = new WayPoint(toPoint2D(pts.get(i + 1)));
			}
			return wps;
		}

		/**
		 * Get the side of the given pathway element to which the x and y coordinates
		 * connect
		 * 
		 * @param x The x coordinate
		 * @param y The y coordinate
		 * @param e The element to find the side of
		 * @return One of the SIDE_* constants
		 */
		private static int getSide(double x, double y, double cx, double cy) {
			return getSide(x - cx, y - cy);
		}

		private static int getSide(double relX, double relY) {
			int direction = 0;
			if (Math.abs(relX) > Math.abs(relY)) {
				if (relX > 0) {
					direction = SIDE_EAST;
				} else {
					direction = SIDE_WEST;
				}
			} else {
				if (relY > 0) {
					direction = SIDE_SOUTH;
				} else {
					direction = SIDE_NORTH;
				}
			}
			return direction;
		}

		/**
		 * Returns the opposite for a SIDE_* constant (e.g. SIDE_EAST <-> SIDE_WEST)
		 */
		private int getOppositeSide(int side) {
			switch (side) {
			case SIDE_EAST:
				return SIDE_WEST;
			case SIDE_WEST:
				return SIDE_EAST;
			case SIDE_NORTH:
				return SIDE_SOUTH;
			case SIDE_SOUTH:
				return SIDE_NORTH;
			}
			return -1;
		}

		/**
		 * Check if the connector may cross this point Optionally, returns a shape that
		 * defines the boundaries of the area around this point that the connector may
		 * not cross. This method can be used for advanced connectors that route along
		 * other objects on the drawing
		 * 
		 * @return A shape that defines the boundaries of the area around this point
		 *         that the connector may not cross. Returning null is allowed for
		 *         implementing classes.
		 */
		public Shape mayCross(Point2D point) {
			PathwayModel parent = getPathwayModel();
			Rectangle2D rect = null;
			if (parent != null) {
				for (PathwayElement e : parent.getPathwayElements()) { // TODO
					if (e.getClass() == org.pathvisio.model.Shape.class || e.getClass() == DataNode.class
							|| e.getClass() == Label.class) {
						Rectangle2D b = getMBounds(); // TODO ...
						if (b.contains(point)) {
							if (rect == null)
								rect = b;
							else
								rect.add(b);
						}
					}
				}
			}
			return rect;
		}

		/* ------------------------------------------------ */

		@Override
		public Point2D getStartPoint2D() {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public Point2D getEndPoint2D() {
			// TODO Auto-generated method stub
			return null;
		}

		/* -------------------Pathway Element----------------------------- */
		public double getMStartX() {
			return getStartLinePoint().getXY().getX();
		}

		public void setMStartX(double v) {
			getStartLinePoint().getXY().setX(v);
		}

		public double getMStartY() {
			return getStartLinePoint().getXY().getY();
		}

		public void setMStartY(double v) {
			getStartLinePoint().getXY().setY(v);
		}

		public double getMEndX() {
			return getEndLinePoint().getXY().getX();
		}

		public void setMEndX(double v) {
			getEndLinePoint().getXY().setX(v);
		}

		public double getMEndY() {
			return getEndLinePoint().getXY().getY();
		}

		public void setMEndY(double v) {
			getEndLinePoint().getXY().setY(v);
		}

		public Point2D toPoint2D(LinePoint linePoint) {
			return new Point2D.Double(linePoint.getXY().getX(), linePoint.getXY().getY());
		}

		/**
		 * Get the rectangular bounds of the object after rotation is applied
		 */
		public Rectangle2D getRBounds() {
			Rectangle2D bounds = getMBounds();
			AffineTransform t = new AffineTransform();
			t.rotate(0, getMCenterX(), getMCenterY()); // TODO getRotation() always 0?
			bounds = t.createTransformedShape(bounds).getBounds2D();
			return bounds;
		}

		/**
		 * Get the rectangular bounds of the object without rotation taken into account
		 */
		public Rectangle2D getMBounds() {
			return new Rectangle2D.Double(getMLeft(), getMTop(), 0, getMHeight());
		}

		public Point2D toAbsoluteCoordinate(Point2D p) {
			double x = p.getX();
			double y = p.getY();
			Rectangle2D bounds = getRBounds();
			// Scale
			if (bounds.getWidth() != 0)
				x *= bounds.getWidth() / 2;
			if (bounds.getHeight() != 0)
				y *= bounds.getHeight() / 2;
			// Translate
			x += bounds.getCenterX();
			y += bounds.getCenterY();
			return new Point2D.Double(x, y);
		}

		/**
		 * @param mp a point in absolute model coordinates
		 * @returns the same point relative to the bounding box of this pathway element:
		 *          -1,-1 meaning the top-left corner, 1,1 meaning the bottom right
		 *          corner, and 0,0 meaning the center.
		 */
		public Point2D toRelativeCoordinate(Point2D mp) {
			double relX = mp.getX();
			double relY = mp.getY();
			Rectangle2D bounds = getRBounds();
			// Translate
			relX -= bounds.getCenterX();
			relY -= bounds.getCenterY();
			// Scalebounds.getCenterX();
			if (relX != 0 && bounds.getWidth() != 0)
				relX /= bounds.getWidth() / 2;
			if (relY != 0 && bounds.getHeight() != 0)
				relY /= bounds.getHeight() / 2;
			return new Point2D.Double(relX, relY);
		}
		/* -------------------Conveniency methods?----------------------------- */

		public ArrowHeadType getStartLineType() {
			ArrowHeadType startLineType = getStartLineType();
			return startLineType == null ? ArrowHeadType.UNDIRECTED : startLineType;
		}

		public ArrowHeadType getEndLineType() {
			ArrowHeadType endLineType = getEndLineType();
			return endLineType == null ? ArrowHeadType.UNDIRECTED : endLineType;
		}

		public void setStartLineType(ArrowHeadType value) {
			getStartLinePoint().setArrowHead(value);
		}

		public void setEndLineType(ArrowHeadType value) {
			getEndLinePoint().setArrowHead(value);
		}

	}
}
