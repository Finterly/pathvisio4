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

import java.awt.Color;
import java.util.List;

import org.pathvisio.model.DataNode;
import org.pathvisio.model.DataNode.State;

/**
 * This class implements a {@link DataNode} and provides methods to resize and
 * draw it.
 * 
 * @author unknown, finterly
 */
public class VDataNode extends VShapedElement {

	private List<VState> vStates;

	public static final Color INITIAL_FILL_COLOR = Color.WHITE;

	// note: not the same as color!
	Color fillColor = INITIAL_FILL_COLOR;

	public VDataNode(VPathwayModel canvas, DataNode gdata) {
		super(canvas, gdata);
	}

	/**
	 * Gets the model representation (PathwayElement) of this class
	 * 
	 * @return
	 */
	@Override
	public DataNode getPathwayElement() {
		return (DataNode) super.getPathwayElement();
	}

	private void addState(State st) {
		VState vst = new VState(canvas, st, this);
		vStates.add(vst);
	}

}
