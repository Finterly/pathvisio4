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

import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

import org.pathvisio.model.Label;
import org.pathvisio.model.Shape;

/**
 * This class represents the view of a {@link Shape} PathwayElement.
 *
 * @author unknown, finterly
 */
public class VShape extends VShapedElement {
	
	Shape gdata = null;
	
	/**
	 * Constructor for this class
	 * 
	 * @param canvas - the VPathway this Shape will be part of
	 */
	public VShape(VPathwayModel canvas, Shape gdata) {
		super(canvas);
		this.gdata = gdata;
	}
	
	/**
	 * Gets the model representation (PathwayElement) of this class
	 * 
	 * @return shape
	 */
	@Override
	public Shape getPathwayElement() {
		return gdata;
	}

	@Override
	public Point2D toAbsoluteCoordinate(Point2D p) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	protected java.awt.Shape getVShape(boolean rotate) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	protected void setVScaleRectangle(Rectangle2D r) {
		// TODO Auto-generated method stub
		
	}

}

