package org.pathvisio.view.model;

import org.pathvisio.model.LineElement;
import org.pathvisio.model.LinePoint;
import org.pathvisio.model.graphics.LineStyleProperty;
import org.pathvisio.view.connector.ConnectorRestrictions;

public class MPoint extends LinePoint implements ConnectorRestrictions {

	
	/**
	 * Link to an object. Current absolute coordinates will be converted to relative
	 * coordinates based on the object to link to.
	 */
	public void linkTo(GraphIdContainer idc) {
		Point2D rel = idc.toRelativeCoordinate(toPoint2D());
		linkTo(idc, rel.getX(), rel.getY());
	}

	/**
	 * Link to an object using the given relative coordinates
	 */
	public void linkTo(GraphIdContainer idc, double relX, double relY) {
		String id = idc.getGraphId();
		if (id == null)
			id = idc.setGeneratedGraphId();
		setGraphRef(idc.getGraphId());
		setRelativePosition(relX, relY);
	}

	/**
	 * note that this may be called any number of times when this point is already
	 * unlinked
	 */
	public void unlink() {
		if (graphRef != null) {
			if (getPathway() != null) {
				Point2D abs = getAbsolute();
				moveTo(abs.getX(), abs.getY());
			}
			relativeSet = false;
			setGraphRef(null);
			fireObjectModifiedEvent(PathwayElementEvent.createCoordinatePropertyEvent(PathwayElement.this));
		}
	}

	public Point2D toAbsoluteCoordinate(Point2D p) {
		return new Point2D.Double(p.getX() + getX(), p.getY() + getY());
	}

	public Point2D toRelativeCoordinate(Point2D p) {
		return new Point2D.Double(p.getX() - getX(), p.getY() - getY());
	}

	/**
	 * Find out if this point is linked to an object. Returns true if a graphRef
	 * exists and is not an empty string
	 */
	public boolean isLinked() {
		String ref = getGraphRef();
		return ref != null && !"".equals(ref);
	}
}
