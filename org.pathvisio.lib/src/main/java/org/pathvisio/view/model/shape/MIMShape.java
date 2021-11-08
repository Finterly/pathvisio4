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
package org.pathvisio.view.model.shape;

import java.awt.Shape;
import java.awt.geom.Ellipse2D;
import java.awt.geom.GeneralPath;
import java.awt.geom.Rectangle2D;

import org.pathvisio.model.type.ArrowHeadType;

/**
 * Collection of Shapes and ArrowHeads used in the Molecular Interaction Map -
 * style of pathways
 */
public class MIMShape {

	public static final ArrowHeadType MIM_NECESSARY_STIMULATION = ArrowHeadType.register("mim-necessary-stimulation");
	public static final ArrowHeadType MIM_BINDING = ArrowHeadType.register("mim-binding");
	public static final ArrowHeadType MIM_CONVERSION = ArrowHeadType.register("mim-conversion");
	public static final ArrowHeadType MIM_TRANSLOCATION = ArrowHeadType.register("mim-translocation");
	public static final ArrowHeadType MIM_STIMULATION = ArrowHeadType.register("mim-stimulation");
	public static final ArrowHeadType MIM_MODIFICATION = ArrowHeadType.register("mim-modification");
	public static final ArrowHeadType MIM_CATALYSIS = ArrowHeadType.register("mim-catalysis");
	public static final ArrowHeadType MIM_INHIBITION = ArrowHeadType.register("mim-inhibition");
	public static final ArrowHeadType MIM_CLEAVAGE = ArrowHeadType.register("mim-cleavage");
	public static final ArrowHeadType MIM_COVALENT_BOND = ArrowHeadType.register("mim-covalent-bond");
	public static final ArrowHeadType MIM_BRANCHING_LEFT = ArrowHeadType.register("mim-branching-left");
	public static final ArrowHeadType MIM_BRANCHING_RIGHT = ArrowHeadType.register("mim-branching-right");
	public static final ArrowHeadType MIM_TRANSLATION = ArrowHeadType.register("mim-transcription-translation");
	public static final ArrowHeadType MIM_GAP = ArrowHeadType.register("mim-gap");

	public static void registerShapes() {
		VShapeRegistry.registerArrow(MIM_NECESSARY_STIMULATION.getName(), getMIMNecessary(),
				VArrowHeadType.FillType.OPEN, ARROWWIDTH);
		VShapeRegistry.registerArrow(MIM_BINDING.getName(), getMIMBinding(), VArrowHeadType.FillType.CLOSED);
		VShapeRegistry.registerArrow(MIM_CONVERSION.getName(), getMIMConversion(), VArrowHeadType.FillType.CLOSED,
				ARROWWIDTH);
		VShapeRegistry.registerArrow(MIM_TRANSLOCATION.getName(), getMIMTranslocation(), VArrowHeadType.FillType.CLOSED,
				ARROWWIDTH);
		VShapeRegistry.registerArrow(MIM_STIMULATION.getName(), getMIMStimulation(), VArrowHeadType.FillType.OPEN,
				ARROWWIDTH);
		VShapeRegistry.registerArrow(MIM_MODIFICATION.getName(), getMIMBinding(), VArrowHeadType.FillType.CLOSED);
		VShapeRegistry.registerArrow(MIM_CATALYSIS.getName(), getMIMCatalysis(), VArrowHeadType.FillType.OPEN,
				CATALYSIS_DIAM + CATALYSIS_GAP);
		VShapeRegistry.registerArrow(MIM_CLEAVAGE.getName(), getMIMCleavage(), VArrowHeadType.FillType.WIRE,
				CLEAVAGE_FIRST);
		VShapeRegistry.registerArrow(MIM_BRANCHING_LEFT.getName(), getMIMBranching(LEFT), VArrowHeadType.FillType.OPEN,
				BRANCH_LOCATION);
		VShapeRegistry.registerArrow(MIM_BRANCHING_RIGHT.getName(), getMIMBranching(RIGHT),
				VArrowHeadType.FillType.OPEN, BRANCH_LOCATION);
		VShapeRegistry.registerArrow(MIM_INHIBITION.getName(), getMIMInhibition(), VArrowHeadType.FillType.OPEN,
				TBARWIDTH + TBAR_GAP);
		VShapeRegistry.registerArrow(MIM_COVALENT_BOND.getName(), getMIMCovalentBond(), VArrowHeadType.FillType.OPEN);
		VShapeRegistry.registerArrow(MIM_TRANSLATION.getName(), getMIMTranslation(), VArrowHeadType.FillType.WIRE,
				ARROWWIDTH + ARROWHEIGHT);
		VShapeRegistry.registerArrow(MIM_GAP.getName(), getMIMGap(), VArrowHeadType.FillType.OPEN, 10);
	}

	static private java.awt.Shape getMIMCovalentBond() {
		GeneralPath path = new GeneralPath();
		path.moveTo(0, -7);
		path.lineTo(0, 7);

		path.moveTo(0, -7);
		path.lineTo(8, -7);

		path.moveTo(0, 7);
		path.lineTo(8, 7);

		return path;
	}

	// Cleavage line ending constants
	static final int CLEAVAGE_FIRST = 10;
	static final int CLEAVAGE_SECOND = 20;
	static final int CLEAVAGE_GAP = CLEAVAGE_SECOND - CLEAVAGE_FIRST;

	// Branch line ending constants
	private static final int LEFT = 0;
	private static final int RIGHT = 1;
	private static final int BRANCH_LOCATION = 8;
	private static final int BRANCHTHICKNESS = 1;

	// method to create the MIM Branch RIGHT and LEFT line endings
	// a 4 sided structure with small thickness works better tha
	// a line.(Maybe the affine trasform has a issue with a line
	// as opposed to a thin quadrilateral)
	static private java.awt.Shape getMIMBranching(int direction) {
		if (direction == RIGHT) {
			GeneralPath path = new GeneralPath();
			path.moveTo(0, 0);
			path.lineTo(BRANCH_LOCATION, -BRANCH_LOCATION);
			path.lineTo(BRANCH_LOCATION, -BRANCH_LOCATION + BRANCHTHICKNESS);
			path.lineTo(BRANCHTHICKNESS, 0);
			path.closePath();
			return path;
		} else {
			GeneralPath path = new GeneralPath();
			path.moveTo(0, 0);
			path.lineTo(BRANCH_LOCATION, BRANCH_LOCATION);
			path.lineTo(BRANCH_LOCATION, BRANCH_LOCATION - BRANCHTHICKNESS);
			path.lineTo(BRANCHTHICKNESS, 0);
			path.closePath();
			return path;
		}
	}

	// method to create the MIM Cleavage lie ending
	static private java.awt.Shape getMIMCleavage() {
		GeneralPath path = new GeneralPath();
		path.moveTo(0, 0);
		path.lineTo(0, -CLEAVAGE_FIRST);
		path.lineTo(CLEAVAGE_SECOND, CLEAVAGE_FIRST);
		return path;
	}

	static final int CATALYSIS_DIAM = 8;
	static final int CATALYSIS_GAP = CATALYSIS_DIAM / 4;
	static final int CATALYSIS_GAP_HEIGHT = 6;

	// create the ellipse for catalysis line ending
	static private java.awt.Shape getMIMCatalysis() {
		return new Ellipse2D.Double(0, -CATALYSIS_DIAM / 2, CATALYSIS_DIAM, CATALYSIS_DIAM);
	}

	private static final int ARROWHEIGHT = 4;
	private static final int ARROWWIDTH = 9;
	private static final int ARROW_NECESSARY_CROSSBAR = 6;

	private static GeneralPath getArrowShapedPath() {
		GeneralPath path = new GeneralPath();
		path.moveTo(0, -ARROWHEIGHT);
		path.lineTo(ARROWWIDTH, 0);
		path.lineTo(0, ARROWHEIGHT);
		path.closePath();
		return path;
	}

	static private java.awt.Shape getMIMStimulation() {
		return getArrowShapedPath();
	}

	static private java.awt.Shape getMIMBinding() {
		GeneralPath path = new GeneralPath();
		path.moveTo(0, 0);
		path.lineTo(-ARROWWIDTH, -ARROWHEIGHT);
		path.lineTo(-ARROWWIDTH / 2, 0);
		path.lineTo(-ARROWWIDTH, ARROWHEIGHT);
		path.closePath();
		return path;
	}

	static private java.awt.Shape getMIMConversion() {
		return getArrowShapedPath();
	}

	static private java.awt.Shape getMIMTranslocation() {
		return getArrowShapedPath();
	}

	static private java.awt.Shape getMIMNecessary() {
		GeneralPath path = getArrowShapedPath();
		path.moveTo(-ARROW_NECESSARY_CROSSBAR, -ARROWHEIGHT);
		path.lineTo(-ARROW_NECESSARY_CROSSBAR, ARROWHEIGHT);
		return path;
	}

	final static int TAIL = ARROWWIDTH / 2;

	static private Shape getMIMTranslation() {
		GeneralPath path = new GeneralPath();
		path.moveTo(-TAIL, 0);
		path.lineTo(-TAIL, ARROWHEIGHT * 2);
		path.lineTo(TAIL, ARROWHEIGHT * 2);
		path.lineTo(TAIL, ARROWHEIGHT * 3);
		path.lineTo(TAIL + ARROWWIDTH, ARROWHEIGHT * 2);
		path.lineTo(TAIL, ARROWHEIGHT);
		path.lineTo(TAIL, ARROWHEIGHT * 2);
		return path;
	}

	static private java.awt.Shape getMIMGap() {
		GeneralPath path = new GeneralPath();
		path.moveTo(0, 0);
		path.moveTo(0, 5);
		return path;
	}

	// copied from BasicShapes for T-Bar
	private static final int TBARHEIGHT = 15;
	private static final int TBARWIDTH = 1;
	private static final int TBAR_GAP = 6;

	// copied from BasicShapes.getTBar()
	private static Shape getMIMInhibition() {
		return new Rectangle2D.Double(0, -TBARHEIGHT / 2, TBARWIDTH, TBARHEIGHT);
	}

}
