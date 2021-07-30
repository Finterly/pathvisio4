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

import java.awt.Color;

import org.pathvisio.model.PathwayElement;
import org.pathvisio.core.view.model.VDataNode;
import org.pathvisio.model.DataNode;

/**
 * This class implements a geneproduct and provides methods to resize and draw
 * it. //TODO: rename this class to DataNode
 */
public class CDataNode extends ControllerShape {

	private DataNode model;
	private VDataNode view;

	public CDataNode(DataNode model, VDataNode view){
	          this.model = model;
	          this.view = view;
	       }

	public void setCourseName(String name) {
		model.setName(name);
	}

	public String getCourseName() {
		return model.getName();
	}

	public void setCourseId(String id) {
		model.setId(id);
	}

	public String getCourseId() {
		return model.getId();
	}

	public void setCourseCategory(String category) {
		model.setCategory(category);
	}

	public String getCourseCategory() {
		return model.getCategory();
	}

	public void updateView() {
		view.printCourseDetails(model.getName(), model.getId(), model.getCategory());
	}

}
