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
package org.pathvisio.core.controller.model;

import org.pathvisio.core.model.PathwayElementListener;
import org.pathvisio.core.view.model.VPathwayElement;
import org.pathvisio.model.PathwayElement;

/**
 * This class is a parent class for all graphics that can be added to a
 * VPathway.
 */
public abstract class Controller implements PathwayElementListener {

	private PathwayElement mPathwayElement;
	private VPathwayElement vPathwayElement;

	public Controller(PathwayElement mPathwayElement, VPathwayElement vPathwayElement) {
		this.mPathwayElement = mPathwayElement;
		this.vPathwayElement = vPathwayElement;
	}

	public void setElementId(String elementId) {
		mPathwayElement.setElementId(elementId);
	}

	public String getElementId() {
		return mPathwayElement.getElementId();
	}

	public void updateView() {
		vPathwayElement.setElementId(mPathwayElement.getElementId());
	}
}
