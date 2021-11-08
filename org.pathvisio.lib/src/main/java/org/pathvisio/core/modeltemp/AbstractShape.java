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
package org.pathvisio.core.model;


import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.geom.AffineTransform;

import org.pathvisio.core.view.ShapeRegistry;

/**
 * This class.
 * 
 * @author unknown, finterly
 */
public class AbstractShape implements IShape {
	private String name;
	private String mappName;
	private boolean isResizeable;
	private boolean isRotatable;
	private Shape sh;

	/**
	 * Constructor to define a new AbstractShape.
	 * 
	 * @param sh           the shape.
	 * @param name         the string.
	 * @param mappName     the string.
	 * @param isResizeable the boolean.
	 * @param isRotatable  the boolean.
	 */
	public AbstractShape(Shape sh, String name, String mappName, boolean isResizeable, boolean isRotatable) {
		this.name = name;
		this.sh = sh;
		this.mappName = mappName;
		this.isRotatable = isRotatable;
		this.isResizeable = isResizeable;
		VShapeRegistry.registerShape(this);
	}

	/**
	 * Constructor to define a new AbstractShape.
	 * 
	 * @param sh   the shape.
	 * @param name the string.
	 */
	public AbstractShape(Shape sh, String name) {
		this(sh, name, name, true, true);
	}

	/**
	 * Gets mappName.
	 * 
	 * @return mappName.
	 */
	public String getMappName() {
		return mappName;
	}

	/**
	 * Gets name.
	 * 
	 * @return name.
	 */
	public String getName() {
		return name;
	}

	/**
	 * Gets shape.
	 * 
	 * @param mw.
	 * @param mh.
	 * @return shape object that defines the geometry of transformed shape sh.
	 */
	public Shape getShape(double mw, double mh) {
		// now scale the path so it has proper w and h.
		Rectangle r = sh.getBounds();
		AffineTransform at = new AffineTransform();
		at.translate(-r.x, -r.y);
		at.scale(mw / r.width, mh / r.height);
		return at.createTransformedShape(sh);
	}

	/**
	 * @return isResizeable the boolean.
	 */
	public boolean isResizeable() {
		return isResizeable;
	}

	/**
	 * @return isRotatable the boolean.
	 */
	public boolean isRotatable() {
		return isRotatable;
	}

	/**
	 * @return name the string.
	 */
	public String toString() {
		return name;
	}

}
