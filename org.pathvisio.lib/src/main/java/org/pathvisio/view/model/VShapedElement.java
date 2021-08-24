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
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.geom.AffineTransform;
import java.awt.geom.Area;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.List;

import org.pathvisio.io.listener.PathwayElementEvent;
import org.pathvisio.io.listener.PathwayElementListener;
import org.pathvisio.view.model.VCitation;
import org.pathvisio.view.model.VElement;
import org.pathvisio.view.model.VPathwayModel;
//import org.pathvisio.core.biopax.PublicationXref;
import org.pathvisio.debug.DebugList;
import org.pathvisio.model.type.LineStyleType;
import org.pathvisio.model.*;

/**
 * This {@link Graphics} class represents the view of {@link ShapedElement}
 * pathway elements: {@link DataNode}, {@link Label}, {@link Shape}, and
 * {@link Group}.
 * 
 * @author unknown, finterly
 */
public abstract class VShapedElement extends VRotatable implements VLinkableTo, LinkProvider {

//	protected ShapedElement gdata = null; //TODO 

//	public GraphicsShapedElement(VPathwayModel canvas, PathwayElement o) { //TODO 
//		super(canvas);
//		o.addListener(this);
//		gdata = o;
//		checkCitation();
//	}

	public VShapedElement(VPathwayModel canvas) {
		super(canvas);
//		o.addListener(this);
//		gdata = o;
	}

	public Point2D toAbsoluteCoordinate(Point2D p, ShapedElement gdata) {
		double x = p.getX();
		double y = p.getY();
		Rectangle2D bounds = getMBounds(gdata);
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
	public Point2D toRelativeCoordinate(Point2D mp, ShapedElement gdata) {
		double relX = mp.getX();
		double relY = mp.getY();
		Rectangle2D bounds = getRBounds(gdata);
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
	
	/**
	 * Get the rectangular bounds of the object without rotation taken into account
	 */
	public Rectangle2D getMBounds(ShapedElement gdata) {
		return new Rectangle2D.Double(getMLeft(gdata), getMTop(gdata), gdata.getRectProp().getWidth(),
				gdata.getRectProp().getHeight());
	}

	/**
	 * TODO is in VRotatable...
	 * Get the rectangular bounds of the object after rotation is applied
	 */
	public Rectangle2D getRBounds(ShapedElement gdata) {
		Double rotation = gdata.getRotation();
		if (gdata.getClass() == ShapedElement.class) {
			Rectangle2D bounds = getMBounds((ShapedElement) gdata);
			AffineTransform t = new AffineTransform();
			t.rotate(rotation, ((ShapedElement) gdata).getRectProp().getCenterXY().getX(),
					((ShapedElement) gdata).getRectProp().getCenterXY().getY());
			bounds = t.createTransformedShape(bounds).getBounds2D();
			return bounds;
		}
	}
	
	/**
	 * Get the x-coordinate of the center point of this object adjusted to the
	 * current zoom factor
	 * 
	 * @return the center x-coordinate
	 */
	public double getVCenterX(ShapedElement gdata) {
		return vFromM(gdata.getRectProp().getCenterXY().getX());
	}

	/**
	 * Get the y-coordinate of the center point of this object adjusted to the
	 * current zoom factor
	 *
	 * @return the center y-coordinate
	 */
	public double getVCenterY(ShapedElement gdata) {
		return vFromM(gdata.getRectProp().getCenterXY().getY());
	}

	/**
	 * Get the x-coordinate of the left side of this object adjusted to the current
	 * zoom factor, but not taking into account rotation
	 * 
	 * @note if you want the left side of the rotated object's boundary, use
	 *       {@link #getVShape(true)}.getX();
	 * @return
	 */
	public double getVLeft(ShapedElement gdata) {
		return vFromM(getMLeft(gdata));
	}

	/**
	 * Get the y-coordinate of the top side of this object adjusted to the current
	 * zoom factor, but not taking into account rotation
	 * 
	 * @note if you want the top side of the rotated object's boundary, use
	 *       {@link #getVShape(true)}.getY();
	 * @return
	 */
	public double getVTop(ShapedElement gdata) {
		return vFromM(getMTop(gdata));
	}

	/**
	 * Get the width of this object adjusted to the current zoom factor, but not
	 * taking into account rotation
	 * 
	 * @note if you want the width of the rotated object's boundary, use
	 *       {@link #getVShape(true)}.getWidth();
	 * @return
	 */
	public double getVWidth(ShapedElement gdata) {
		return vFromM(gdata.getRectProp().getWidth());
	}

	/**
	 * Get the height of this object adjusted to the current zoom factor, but not
	 * taking into account rotation
	 * 
	 * @note if you want the height of the rotated object's boundary, use
	 *       {@link #getVShape(true)}.getY();
	 * @return
	 */
	public double getVHeight(ShapedElement gdata) {
		return vFromM(gdata.getRectProp().getHeight());
	}

	/**
	 * Get the direct view to model translation of this shape
	 * 
	 * @param rotate Whether to take into account rotation or not
	 * @return
	 */
	abstract protected Shape getVShape(boolean rotate);

	/*----------------- Convenience methods from Model -----------------*/

	// startx for shapes TODO
	public double getMLeft(ShapedElement gdata) {
		return gdata.getRectProp().getCenterXY().getX() - gdata.getRectProp().getWidth() / 2;
	}

	// starty for shapes TODO
	public double getMTop(ShapedElement gdata) {
		return gdata.getRectProp().getCenterXY().getY() - gdata.getRectProp().getHeight() / 2;
	}

	/**
	 * Get the rectangle that represents the bounds of the shape's direct
	 * translation from model to view, without taking into account rotation. Default
	 * implementation is equivalent to <code>getVShape(false).getBounds2D();</code>
	 */
	protected Rectangle2D getVScaleRectangle() {
		return getVShape(false).getBounds2D();
	}

	/**
	 * Scales the object to the given rectangle, by taking into account the rotation
	 * (given rectangle will be rotated back before scaling)
	 * 
	 * @param r
	 */
	protected abstract void setVScaleRectangle(Rectangle2D r);

	/**
	 * Default implementation returns the rotated shape. Subclasses may override
	 * (e.g. to include the stroke)
	 * 
	 * @see {@link VElement#calculateVOutline()}
	 */
	protected Shape calculateVOutline() {
		return getVShape(true);
	}

	/**
	 * Returns the fontstyle to create a java.awt.Font
	 * 
	 * @return the fontstyle, or Font.PLAIN if no font is available
	 */
	public int getVFontStyle(ShapedElement gdata) {
		int style = Font.PLAIN;
		if (gdata.getFontProp().getFontName() != null) {
			if (gdata.getFontProp().getFontWeight()) {
				style |= Font.BOLD;
			}
			if (gdata.getFontProp().getFontStyle()) {
				style |= Font.ITALIC;
			}
		}
		return style;
	}

	/**
	 * Returns the z-order from the model
	 */
	protected int getZOrder(ShapedElement gdata) {
		return gdata.getShapeStyleProp().getZOrder();
	}

	protected Color getBorderColor(ShapedElement gdata) {
		Color borderColor = gdata.getShapeStyleProp().getBorderColor();
		/*
		 * the selection is not colored red when in edit mode it is possible to see a
		 * color change immediately
		 */
		if (isSelected() && !canvas.isEditMode()) {
			borderColor = selectColor;
		}
		return borderColor;
	}

	protected void setBorderStyle(Graphics2D g, ShapedElement gdata) {
		LineStyleType ls = gdata.getShapeStyleProp().getBorderStyle();
		float lt = (float) vFromM(gdata.getShapeStyleProp().getBorderWidth());
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
