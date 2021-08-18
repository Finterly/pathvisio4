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

import org.pathvisio.model.PathwayElement;
import org.pathvisio.model.DataNode;

/**
 * This class implements a {@link DataNode} and provides methods to resize and
 * draw it.
 */
public class VDataNode extends VRotatable {

	protected DataNode dataNode = null;

	public static final Color INITIAL_FILL_COLOR = Color.WHITE;

	// note: not the same as color!
	Color fillColor = INITIAL_FILL_COLOR;

	public VDataNode(VPathwayModel canvas, DataNode dataNode) {
		super(canvas);
		this.dataNode = dataNode;
	}

	/**
	 * Gets the model representation (PathwayElement) of this class
	 * 
	 * @return dataNode
	 */
	@Override
	public DataNode getPathwayElement() {
		return dataNode;
	}

}
