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
import java.awt.geom.AffineTransform;
import java.awt.geom.Area;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.pathvisio.core.modeltemp.PathwayElementEvent;
import org.pathvisio.model.GraphLink.LinkableFrom;
import org.pathvisio.model.LineElement;
import org.pathvisio.model.LineElement.Anchor;
import org.pathvisio.model.LineElement.LinePoint;
import org.pathvisio.model.connector.ConnectorShape;
import org.pathvisio.model.connector.ConnectorShape.Segment;
import org.pathvisio.model.connector.ConnectorShape.WayPoint;
import org.pathvisio.model.type.ArrowHeadType;
//import org.pathvisio.model.GraphLink.LinkableFrom;
import org.pathvisio.model.type.LineStyleType;
import org.pathvisio.util.preferences.GlobalPreference;
import org.pathvisio.util.preferences.PreferenceManager;
import org.pathvisio.view.model.shape.VArrowHeadType;
import org.pathvisio.view.model.shape.VDefaultShapeType;
import org.pathvisio.view.model.shape.VShapeRegistry;

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
 * 
 * @author unknown, finterly
 * 
 */
public class VLineElement extends VPathwayElement implements Adjustable, VGroupable { // TODO
	// ConnectorRestrictions
	private List<VPoint> points;
	private Map<Anchor, VAnchor> anchors = new HashMap<Anchor, VAnchor>();
	List<Handle> segmentHandles = new ArrayList<Handle>();
	ConnectorShape connectorShape;

	/**
	 * Constructor for this class
	 * 
	 * @param canvas - the VPathway this line will be part of
	 */
	public VLineElement(VPathwayModel canvas, LineElement gdata) {
		super(canvas, gdata);
		points = new ArrayList<VPoint>();
		addPoint(getPathwayElement().getStartLinePoint()); // TODO
		addPoint(getPathwayElement().getEndLinePoint()); // TODO
		setAnchors();
		getPathwayElement().getConnectorShape().recalculateShape(getPathwayElement());
//		updateSegmentHandles();
		updateCitationPosition();
	}

	/**
	 * Gets the model representation (PathwayElement) of this class
	 * 
	 * @return
	 */
	@Override
	public LineElement getPathwayElement() {
		return (LineElement) super.getPathwayElement();
	}

	private void addPoint(LinePoint mp) {
		VPoint vp = canvas.newPoint(mp, this);
		points.add(vp);
		setHandleLocation(vp);
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
			List<LinePoint> points = getPathwayElement().getLinePoints();
			if (points.size() - 2 != (waypoints.length)) {
				// Recreate points from segments
				points = new ArrayList<LinePoint>();
				points.add(getPathwayElement().getStartLinePoint());
				for (int i = 0; i < waypoints.length; i++) {
					LinePoint p = getPathwayElement().new LinePoint(ArrowHeadType.UNDIRECTED, waypoints[i].getX(),
							waypoints[i].getY());
					points.add(p);
				}
				points.add(getPathwayElement().getEndLinePoint());
				getPathwayElement().dontFireEvents(1);
				getPathwayElement().setLinePoints(points);
			}
			points.get(index + 1).moveTo(mFromV(vx), mFromV(vy));
		}
	}

	private List<Handle> getSegmentHandles() {
		return segmentHandles;
	}

	private ConnectorShape getConnectorShape() {
		return getPathwayElement().getConnectorShape();
	}

	/**
	 * Get the connector shape translated to view coordinates allowing for Line
	 * Ending This allows the line to be drawn only upto the point where the line
	 * ending starts
	 */
	public Shape getVConnectorAdjusted() {

		// call to getLineEndingWidth
		double startGap = getGap(getPathwayElement().getStartLineType());
		double endGap = getGap(getPathwayElement().getEndLineType());

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
			gap = VShapeRegistry.getArrow("Default").getGap();
		} else if (type.getName().equals("Line")) {
			gap = 0;
		} else {
			gap = VShapeRegistry.getArrow(type.getName()).getGap();
		}
		return gap;

	}

	public void doDraw(Graphics2D g) {
		Color c = getLineColor();
		g.setColor(c);
		setLineStyle(g);

		Shape l = getVConnectorAdjusted();

		VArrowHeadType[] heads = getVHeadsAdjusted();
		VArrowHeadType hs = heads[0];
		VArrowHeadType he = heads[1];

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
			if (o instanceof VDataNode || o instanceof VShape)
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
	public VArrowHeadType[] getVHeads() {
		Segment[] segments = getConnectorShape().getSegments();

		VArrowHeadType he = getVHead(segments[segments.length - 1].getMStart(), segments[segments.length - 1].getMEnd(),
				getPathwayElement().getEndLineType());
		VArrowHeadType hs = getVHead(segments[0].getMEnd(), segments[0].getMStart(),
				getPathwayElement().getStartLineType());
		return new VArrowHeadType[] { hs, he };
	}

	/**
	 * Returns the properly sized and rotated arrowheads which have been adjusted
	 * for Line ending thickness
	 * 
	 * @return An array with two arrowheads, for the start and end respectively
	 */
	public VArrowHeadType[] getVHeadsAdjusted() {
		Segment[] segments = getConnectorShape().getSegments();

		// last segment in the Connector Shape
		double lineEndingWidth = getGap(getPathwayElement().getEndLineType());
		Point2D adjustedSegmentEnd = segments[segments.length - 1].calculateNewEndPoint(lineEndingWidth);
		VArrowHeadType he = getVHead(segments[segments.length - 1].getMStart(), adjustedSegmentEnd,
				getPathwayElement().getEndLineType());

		// first segment in the connector shape
		double lineStartingWidth = getGap(getPathwayElement().getStartLineType());
		Point2D adjustedSegmentStart = segments[0].calculateNewStartPoint(lineStartingWidth);
		VArrowHeadType hs = getVHead(segments[0].getMEnd(), adjustedSegmentStart,
				getPathwayElement().getStartLineType());
		return new VArrowHeadType[] { hs, he };
	}

	protected Shape getVShape(boolean rotate) {
		Shape l = getVConnectorAdjusted();

		VArrowHeadType[] heads = getVHeadsAdjusted();
		VArrowHeadType hs = heads[0];
		VArrowHeadType he = heads[1];

		float thickness = (float) vFromM(getPathwayElement().getLineWidth());
		if (getPathwayElement().getLineStyle() == LineStyleType.DOUBLE)
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
		List<Anchor> manchors = getPathwayElement().getAnchors();
		for (Anchor ma : manchors) {
			if (!anchors.containsKey(ma)) {
				anchors.put(ma, new VAnchor(ma, this));
			}
		}
		// Check for deleted anchors
		for (Anchor ma : anchors.keySet()) {
			if (!manchors.contains(ma)) {
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
		getPathwayElement().removeAnchor(va.getAnchor());
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
		Point2D r = getPathwayElement().toRelativeCoordinate(p);
		getVCitation().setRPosition(r);
	}

	protected void swapPoint(VPoint pOld, VPoint pNew) {
		int i = points.indexOf(pOld);
		if (i > -1) {
			points.remove(pOld);
			points.add(i, pNew);
		}
	}

	protected void drawHead(Graphics2D g, VArrowHeadType head, Color c) {
		if (head != null) {
			// reset stroked line to solid, but use given thickness
			g.setStroke(new BasicStroke((float) vFromM(getPathwayElement().getLineWidth())));
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
				//TODO 
				VDefaultShapeType.getPluggableGraphic(VDefaultShapeType.Internal.DEFAULT_ARROWHEAD, g);
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
	protected VArrowHeadType getVHead(Point2D mP1, Point2D mP2, ArrowHeadType type) {
		double xs = vFromM(mP1.getX());
		double ys = vFromM(mP1.getY());
		double xe = vFromM(mP2.getX());
		double ye = vFromM(mP2.getY());

		VArrowHeadType h;
		if (type == null) {
			h = VShapeRegistry.getArrow("Default");
		} else if (type.getName().equals("Line")) {
			h = null;
		} else {
			h = VShapeRegistry.getArrow(type.getName());
		}

		if (h != null) {
			AffineTransform f = new AffineTransform();
			double scaleFactor = vFromM(1.0 + 0.3 * getPathwayElement().getLineWidth());
			f.rotate(Math.atan2(ye - ys, xe - xs), xe, ye);
			f.translate(xe, ye);
			f.scale(scaleFactor, scaleFactor);
			Shape sh = f.createTransformedShape(h.getShape());
			h = new VArrowHeadType(sh, h.getFillType());
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

	/**
	 * Get the x-coordinate of the center point of this object adjusted to the
	 * current zoom factor
	 * 
	 * @return the center x-coordinate
	 */
	public double getVCenterX() {
		return vFromM(getPathwayElement().getCenterX());
	}

	/**
	 * Get the y-coordinate of the center point of this object adjusted to the
	 * current zoom factor
	 *
	 * @return the center y-coordinate
	 */
	public double getVCenterY() {
		return vFromM(getPathwayElement().getCenterY());
	}

	/**
	 * Get the width of this object adjusted to the current zoom factor, but not
	 * taking into account rotation
	 * 
	 * @note if you want the width of the rotated object's boundary, use
	 *       {@link #getVShape(true)}.getWidth();
	 * @return
	 */
	public double getVWidth() {
		return vFromM(getPathwayElement().getWidth());
	}

	/**
	 * Get the height of this object adjusted to the current zoom factor, but not
	 * taking into account rotation
	 * 
	 * @note if you want the height of the rotated object's boundary, use
	 *       {@link #getVShape(true)}.getY();
	 * @return
	 */
	public double getVHeight() {
		return vFromM(getPathwayElement().getHeight());
	}

	/**
	 * Get the x-coordinate of the left side of this object adjusted to the current
	 * zoom factor, but not taking into account rotation
	 * 
	 * @note if you want the left side of the rotated object's boundary, use
	 *       {@link #getVShape(true)}.getX();
	 * @return
	 */
	public double getVLeft() {
		return vFromM(getPathwayElement().getLeft());
	}

	/**
	 * Get the y-coordinate of the top side of this object adjusted to the current
	 * zoom factor, but not taking into account rotation
	 * 
	 * @note if you want the top side of the rotated object's boundary, use
	 *       {@link #getVShape(true)}.getY();
	 * @return
	 */
	public double getVTop() {
		return vFromM(getPathwayElement().getTop());
	}

	protected void vMoveWayPointsBy(double vdx, double vdy) {
		List<LinePoint> mps = getPathwayElement().getLinePoints();
		for (int i = 1; i < mps.size() - 1; i++) {
			mps.get(i).moveBy(mFromV(vdx), mFromV(vdy));
		}
	}

	protected void vRecalculatePoints(double vdx, double vdy) {
		for (VPoint p : points) {
			p.setVLocation(p.getVX() + canvas.mFromV(vdx), p.getVY() + canvas.mFromV(vdy));
		}
	}

	// TODO Not
	/**
	 * Not sure what this is TODO I think it is ... if you move a line....you need
	 * to move its points And then also recalculate any lines attached to this line
	 * anchor..
	 */
	protected void vMoveBy(double vdx, double vdy) {
		// move LinePoints directly, not every LinePoint is represented
		// by a VPoint but we want to move them all.
		for (LinePoint p : getPathwayElement().getLinePoints()) {
			p.moveBy(canvas.mFromV(vdx), canvas.mFromV(vdy));
		}
		// Redraw graphRefs
		for (Anchor anchor : getPathwayElement().getAnchors()) {
			for (LinkableFrom ref : anchor.getLinkableFroms()) {
				if (ref instanceof LinePoint) {
					VPoint vp = canvas.getPoint((LinePoint) ref);
					if (vp != null) {
						vp.getLine().recalculateConnector();
					}
				}
			}
		}
//		for (GraphRefContainer ref : gdata.getReferences()) {
//			if (ref instanceof LinePoint) {
//				VPoint vp = canvas.getPoint((LinePoint) ref);
//				if (vp != null) {
//					vp.getLine().recalculateConnector();
//				}
//			}
//		}
	}

	private void setHandleLocation(VPoint vp) {
		if (vp.handle == null)
			return;
		LinePoint mp = vp.getLinePoint();
		vp.handle.setMLocation(mp.getX(), mp.getY());
	}

	public void recalculateConnector() {
		getConnectorShape().recalculateShape(getPathwayElement());
		updateAnchorPositions();
		updateCitationPosition();
		for (VPoint vp : points)
			setHandleLocation(vp);
		markDirty();
	}

	public void gmmlObjectModified(PathwayElementEvent e) {
		getConnectorShape().recalculateShape(getPathwayElement());

		WayPoint[] wps = getConnectorShape().getWayPoints();
		List<LinePoint> mps = getPathwayElement().getLinePoints();
		if (wps.length == mps.size() - 2 && getConnectorShape().hasValidWaypoints(getPathwayElement())) {
			getPathwayElement().adjustWayPointPreferences(wps);
		} else {
			getPathwayElement().resetWayPointPreferences();
		}

		updateSegmentHandles();
		markDirty();
		for (VPoint p : points) {
			setHandleLocation(p);
		}
		if (getPathwayElement().getAnchors().size() != anchors.size()) {
			setAnchors();
		}
		checkCitation();
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

		for (LinePoint p : getPathwayElement().getLinePoints()) {
			canvas.pointsMtoV.remove(p);
		}
		List<VAnchor> remove = new ArrayList<VAnchor>(anchors.values());
		for (VAnchor a : remove) {
			a.destroy();
		}
	}

	/**
	 * Returns the x-coordinate of the start point of this line, adjusted to the
	 * current zoom factor
	 * 
	 * @return
	 */
	protected double getVStartX() {
		return (int) (vFromM(getPathwayElement().getStartLinePointX()));
	}

	/**
	 * Returns the y-coordinate of the start point of this line, adjusted to the
	 * current zoom factor
	 * 
	 * @return
	 */
	protected double getVStartY() {
		return (int) (vFromM(getPathwayElement().getStartLinePointY()));
	}

	/**
	 * Returns the x-coordinate of the end point of this line, adjusted to the
	 * current zoom factor
	 * 
	 * @return
	 */
	protected double getVEndX() {
		return (int) (vFromM(getPathwayElement().getEndLinePointX()));
	}

	/**
	 * Returns the y-coordinate of the end point of this line, adjusted to the
	 * current zoom factor
	 * 
	 * @return
	 */
	protected double getVEndY() {
		return (int) (vFromM(getPathwayElement().getEndLinePointY()));
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
		return getPathwayElement().getZOrder();
	}

	protected Color getLineColor() {
		Color linecolor = getPathwayElement().getLineColor();
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
		LineStyleType ls = getPathwayElement().getLineStyle();
		float lt = (float) vFromM(getPathwayElement().getLineWidth());
		if (ls == LineStyleType.SOLID) {
			g.setStroke(new BasicStroke(lt));
		} else if (ls == LineStyleType.DASHED) {
			g.setStroke(
					new BasicStroke(lt, BasicStroke.CAP_SQUARE, BasicStroke.JOIN_MITER, 10, new float[] { 4, 4 }, 0));
		} else if (ls == LineStyleType.DOUBLE) {
			g.setStroke(new CompositeStroke(new BasicStroke(lt * 2), new BasicStroke(lt)));
		}
	}

}
