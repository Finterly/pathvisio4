/*******************************************************************************
 * PathVisio, a tool for data visualization and analysis using biological pathways
 * Copyright 2006-2019 BiGCaT Bioinformatics
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
package org.pathvisio.view.model.shape;

import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.geom.AffineTransform;

public class AbstractShape {
	
	private String name;
	private boolean isResizeable;
	private boolean isRotatable;
	private Shape sh;

	public AbstractShape(Shape sh, String name, boolean isResizeable, boolean isRotatable) {
		this.name = name;
		this.sh = sh;
		this.isRotatable = isRotatable;
		this.isResizeable = isResizeable;
		VShapeRegistry.registerShape(this);
	}

	public AbstractShape(Shape sh, String name) {
		this(sh, name, true, true);
	}

	public String getName() {
		return name;
	}

	public Shape getShape(double mw, double mh) {
		// now scale the path so it has proper w and h.
		Rectangle r = sh.getBounds();
		AffineTransform at = new AffineTransform();
		at.translate(-r.x, -r.y);
		at.scale(mw / r.width, mh / r.height);
		return at.createTransformedShape(sh);
	}

	public boolean isResizeable() {
		return isResizeable;
	}

	public boolean isRotatable() {
		return isRotatable;
	}

	public String toString() {
		return name;
	}

}
