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
import java.awt.Stroke;
import java.awt.geom.Area;
import java.awt.geom.Rectangle2D;
import java.util.List;

//import org.pathvisio.core.biopax.PublicationXref;
import org.pathvisio.debug.DebugList;
import org.pathvisio.events.PathwayElementEvent;
import org.pathvisio.events.PathwayElementListener;
import org.pathvisio.model.PathwayObject;

/**
 * This class is a parent class for all graphics that can be added to a
 * VPathwayModel.
 */
public abstract class VPathwayObject extends VElement implements PathwayElementListener {

	protected PathwayObject gdata = null;

	/**
	 * List of children, everything that moves when this element is dragged.
	 * Includes Citation and State.
	 */
	private List<VElement> children = new DebugList<VElement>();

	public VPathwayObject(VPathwayModel canvas, PathwayObject gdata) {
		super(canvas);
		gdata.addListener(this);
		this.gdata = gdata;
	}

	/**
	 * Gets the model representation (PathwayElement) of this class
	 * 
	 * @return
	 */
	public PathwayObject getPathwayElement() {
		return gdata;
	}

	public void markDirty() {
		super.markDirty();
		for (VElement child : children)
			child.markDirty();
	}

	boolean listen = true;

	public void gmmlObjectModified(PathwayElementEvent e) {
		if (listen) {
			markDirty(); // mark everything dirty
//			checkCitation(); TODO
		}
	}

	public Area createVisualizationRegion() {
		return new Area(getVBounds());
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

	public List<VElement> getChildren() {
		return children;
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
