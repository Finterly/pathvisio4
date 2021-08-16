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
public abstract class GraphicsShapedElement extends GraphicsCitable {

//	protected ShapedElement gdata = null; //TODO 


//	public GraphicsShapedElement(VPathwayModel canvas, PathwayElement o) { //TODO 
//		super(canvas);
//		o.addListener(this);
//		gdata = o;
//		checkCitation();
//	}

	public GraphicsShapedElement(VPathwayModel canvas) {
		super(canvas);
//		o.addListener(this);
//		gdata = o;
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

	// startx for shapes TODO
	public double getMLeft(ShapedElement gdata) {
		return gdata.getRectProp().getCenterXY().getX() - gdata.getRectProp().getWidth() / 2;
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

	// starty for shapes TODO
	public double getMTop(ShapedElement gdata) {
		return gdata.getRectProp().getCenterXY().getY() - gdata.getRectProp().getHeight() / 2;
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
	 * Get the rectangular bounds of the object without rotation taken into accound
	 */
	public Rectangle2D getBounds(ShapedElement gdata) {
		return new Rectangle2D.Double(getMLeft(gdata), getMTop(gdata), gdata.getRectProp().getWidth(),
				gdata.getRectProp().getHeight());

	}

	/**
	 * Get the direct view to model translation of this shape
	 * 
	 * @param rotate Whether to take into account rotation or not
	 * @return
	 */
	abstract protected Shape getVShape(boolean rotate);

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

	protected void destroy(ShapedElement gdata) {
		super.destroy();
		gdata.removeListener(this);
		for (VElement child : children) {
			child.destroy();
		}
		children.clear();
		citation = null;

		// View should not remove its model
//		Pathway parent = gdata.getParent();
//		if(parent != null) parent.remove(gdata);
	}

	/**
	 * Returns the z-order from the model
	 */
	protected int getZOrder(ShapedElement gdata) {
		return gdata.getShapeStyleProp().getZOrder();
	}

	protected Color getLineColor(ShapedElement gdata) {
		Color linecolor = gdata.getShapeStyleProp().getBorderColor();
		/*
		 * the selection is not colored red when in edit mode it is possible to see a
		 * color change immediately
		 */
		if (isSelected() && !canvas.isEditMode()) {
			linecolor = selectColor;
		}
		return linecolor;
	}

	protected void setLineStyle(Graphics2D g, ShapedElement gdata) {
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

	public void addChild(VElement elt) {
		children.add(elt);
	}

	public void removeChild(VElement elt) {
		children.remove(elt);
	}

}

/**
 * Generates double line stroke, e.g., for cellular compartment shapes.
 *
 */
final class CompositeStroke implements Stroke {
	private Stroke stroke1, stroke2;

	public CompositeStroke(Stroke stroke1, Stroke stroke2) {
		this.stroke1 = stroke1;
		this.stroke2 = stroke2;
	}

	public Shape createStrokedShape(Shape shape) {
		return stroke2.createStrokedShape(stroke1.createStrokedShape(shape));
	}
}
