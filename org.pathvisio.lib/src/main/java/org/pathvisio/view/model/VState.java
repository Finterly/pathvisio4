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

import org.pathvisio.model.DataNode.State;

/**
 * This class represents the view of a {@link State} PathwayElement.
 * 
 * @author unknown, finterly
 */
public class VState extends VShapedElement {

	private VDataNode vDataNode;

	public VState(VPathwayModel canvas, State gdata, VDataNode vDataNode) {
		super(canvas, gdata);
		this.vDataNode = vDataNode;
	}

	/**
	 * Gets the model representation (PathwayElement) of this class
	 * 
	 * @return
	 */
	@Override
	public State getPathwayElement() {
		return (State) super.getPathwayElement();
	}

	protected void vMoveBy(double vdx, double vdy) {
		Point2D mNewPos = new Point2D.Double(mFromV(getVCenterX() + vdx), mFromV(getVCenterY() + vdy));
		Point2D newRel = getPathwayElement().getDataNode().toRelativeCoordinate(mNewPos);
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

}
