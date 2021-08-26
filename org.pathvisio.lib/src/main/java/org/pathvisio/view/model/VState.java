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
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

import org.pathvisio.model.State;
import org.pathvisio.model.type.LineStyleType;
import org.pathvisio.events.PathwayElementListener;
import org.pathvisio.model.PathwayElement;
import org.pathvisio.model.Shape;
import org.pathvisio.model.ShapedElement;

/**
 * This class represents the view of a {@link State} PathwayElement.
 * 
 * @author unknown, finterly
 */
public class VState extends VShapedElement {

	public static final String ROTATION_KEY = "org.pathvisio.core.StateRotation";

	public VState(VPathwayModel canvas, State gdata) {
		super(canvas, gdata);
	}

	public void doDraw(Graphics2D g) {
		g.setColor(getBorderColor());
		setBorderStyle(g);
		drawShape(g);

		g.setFont(getVFont());
		drawTextLabel(g);

		drawHighlight(g);
	}

	protected Color getBorderColor(State gdata) {
		Color borderColor = gdata.getBorderColor();
		/*
		 * the selection is not colored red when in edit mode it is possible to see a
		 * color change immediately
		 */
		if (isSelected() && !canvas.isEditMode()) {
			borderColor = selectColor;
		}
		return borderColor;
	}

	protected void setBorderStyle(Graphics2D g) {
		LineStyleType ls = gdata.getBorderStyle();
		float lt = (float) vFromM(gdata.getBorderWidth());
		if (ls == LineStyleType.SOLID) {
			g.setStroke(new BasicStroke(lt));
		} else if (ls == LineStyleType.DASHED) {
			g.setStroke(
					new BasicStroke(lt, BasicStroke.CAP_SQUARE, BasicStroke.JOIN_MITER, 10, new float[] { 4, 4 }, 0));
		} else if (ls == LineStyleType.DOUBLE) {
			g.setStroke(new CompositeStroke(new BasicStroke(lt * 2), new BasicStroke(lt)));
		}
	}

	/**
	 * Return relative coordinates for the state.
	 * 
	 * @param mp a point in absolute model coordinates
	 * @returns the same point relative to the bounding box of this pathway element:
	 *          -1,-1 meaning the top-left corner, 1,1 meaning the bottom right
	 *          corner, and 0,0 meaning the center.
	 */
	protected Point2D toRelativeCoordinate(Point2D mp) {
		double relX = mp.getX();
		double relY = mp.getY();
		// get bounds of parent data node
		Rectangle2D bounds = getRBounds(((State) gdata).getDataNode()); // TODO of dataNode
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

	protected void vMoveBy(double vdx, double vdy) {
		Point2D mNewPos = new Point2D.Double(mFromV(getVCenterX() + vdx), mFromV(getVCenterY() + vdy));
		Point2D newRel = toRelativeCoordinate(mNewPos);
		double x = newRel.getX();
		double y = newRel.getY();
		if (x > 1)
			x = 1;
		if (x < -1)
			x = -1;
		if (y > 1)
			y = 1;
		if (y < -1)
			y = -1;
		((State) gdata).setRelX(x);
		((State) gdata).setRelY(y);
	}

	@Override
	public void destroy() {
		super.destroy();
	}
}
