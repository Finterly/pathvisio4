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

import org.pathvisio.model.DataNode;
import org.pathvisio.model.Label;
import org.pathvisio.model.PathwayElement;

/**
 * This class represents the view of a {@link Label} pathway element.
 */
public class VLabel extends GraphicsShape {
	
	Label label = null;
	/**
	 * Constructor for this class
	 * 
	 * @param canvas - the VPathway this label will be part of
	 */
	public VLabel(VPathwayModel canvas, Label o) {
		super(canvas);
	}

	
	@Override 
	protected void destroy() {
		super.destroy();
		gdata.removeListener(this); //TODO 
		for (VElement child : getChildren()) {
			child.destroy();
		}
		getChildren().clear();
		setVCitation(null);

		// View should not remove its model
//		Pathway parent = gdata.getParent();
//		if(parent != null) parent.remove(gdata);
	}


	/**
	 * Gets the model representation (PathwayElement) of this class
	 * 
	 * @return label
	 */
	@Override
	public Label getPathwayElement() {
		return label;
	}
	
	
}
