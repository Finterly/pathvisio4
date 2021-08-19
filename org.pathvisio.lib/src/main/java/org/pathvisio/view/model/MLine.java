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

import java.awt.Shape;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.List;

import org.pathvisio.model.LineElement;
import org.pathvisio.model.LinePoint;
import org.pathvisio.model.graphics.LineStyleProperty;
import org.pathvisio.model.type.ArrowHeadType;
import org.pathvisio.model.type.LineStyleType;
import org.pathvisio.view.connector.ConnectorRestrictions;
import org.pathvisio.view.connector.ConnectorShape;
import org.pathvisio.view.connector.ConnectorShapeFactory;
import org.pathvisio.view.connector.ConnectorShape.WayPoint;
import org.pathvisio.core.util.Utils;

/**
 * MLine - basically a PathwayElement, but overrides some methods to calculate
 * coordinates dynamically. For example getMCenterX().
 *
 * For other shapes, the centerX coordinate is stored in GPML. Lines however
 * store the end-points, the center can be calculated based on that.
 */
public class MLine extends LineElement implements ConnectorRestrictions {
	
	
	
	
	public MLine(LineStyleProperty lineStyleProperty) {
		super(lineStyleProperty);
	}
	
	

	
	//------------------------ here -------------------//

	public LinePoint getMStart() {
		return getStartLinePoint();
	}

	public LinePoint getMEnd() {
		return getEndLinePoint();
	}
	public List<LinePoint> getMPoints() {
		return getLinePoints();
	}
	
	
	
	public void setMStart(LinePoint p) {
		getMStart().moveTo(p);
	}



	public void setMEnd(LinePoint p) {
		getMEnd().moveTo(p);
	}

	public double getMStartX() {
		return getMStart().getXY().getX();
	}
	
	public double getMStartY() {
		return getMStart().getXY().getY();
	}

	public void setMStartX(double v) {
		getMStart().setX(v);
	}


	public void setMStartY(double v) {
		getMStart().setY(v);
	}

	public double getMEndX() {
		return mPoints.get(mPoints.size() - 1).getX();
	}

	public void setMEndX(double v) {
		getMEnd().setX(v);
	}

	public double getMEndY() {
		return getMEnd().getY();
	}

	public void setMEndY(double v) {
		getMEnd().setY(v);
	}

	protected ArrowHeadType endLineType = ArrowHeadType.UNDIRECTED;
	protected ArrowHeadType startLineType = ArrowHeadType.UNDIRECTED;


	
	
	
	




//TODO: end of new elements
	protected List<MAnchor> anchors = new ArrayList<MAnchor>();

	/**
	 * Get the anchors for this line.
	 * 
	 * @return A list with the anchors, or an empty list, if no anchors are defined
	 */
	public List<MAnchor> getMAnchors() {
		return anchors;
	}

	/**
	 * Add a new anchor to this line at the given position.
	 * 
	 * @param position The relative position on the line, between 0 (start) to 1
	 *                 (end).
	 */
	public MAnchor addMAnchor(double position) {
		if (position < 0 || position > 1) {
			throw new IllegalArgumentException("Invalid position value '" + position + "' must be between 0 and 1");
		}
		MAnchor anchor = new MAnchor(position);
		anchors.add(anchor);
		// No property for anchor, use LINESTYLE as dummy property to force redraw on
		// line
		fireObjectModifiedEvent(PathwayElementEvent.createSinglePropertyEvent(this, StaticProperty.LINESTYLE));
		return anchor;
	}

	/**
	 * Remove the given anchor
	 */
	public void removeMAnchor(MAnchor anchor) {
		if (anchors.remove(anchor)) {
			// No property for anchor, use LINESTYLE as dummy property to force redraw on
			// line
			fireObjectModifiedEvent(PathwayElementEvent.createSinglePropertyEvent(this, StaticProperty.LINESTYLE));
		}
	}
	
	
	
	//------------------------ here -------------------//

	
	



	



	


	

	


	







}
