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

import java.util.List;

import org.pathvisio.model.PathwayElement;
import org.pathvisio.model.ref.CitationRef;

/**
 * This class is a parent class for all graphics which can have a
 * {@link VCitation}.
 * 
 * @author finterly
 */
public abstract class GraphicsCitable extends Graphics {

	private VCitation vCitation;

	public GraphicsCitable(VPathwayModel canvas) {
		super(canvas);
		// checkCitation(); TODO
	}

	protected VCitation getVCitation() {
		return vCitation;
	}

	protected void setVCitation(VCitation vCitation) {
		this.vCitation = vCitation;
	}

	protected VCitation createCitation() {
		return new VCitation(canvas, this, new Point2D.Double(1, -1));
	}

	/**
	 * Check for {@link VCitation} if object has {@link CitationRef}s. Create or
	 * destroy vCitation if necessary.
	 */
	public final void checkCitation(List<CitationRef> citationRefs) {
		// if object has citationRefs but no vCitation, create
		if (citationRefs.size() > 0 && vCitation == null) {
			vCitation = createCitation();
			addChild(vCitation);
		}
		// if object has no citationRefs but has vCitation, destroy
		else if (citationRefs.size() == 0 && vCitation != null) {
			vCitation.destroy();
			removeChild(vCitation);
			vCitation = null;
		}
		// if object has citationRefs and vCitation, redraw
		if (vCitation != null) {
			vCitation.markDirty();
		}
	}

}
