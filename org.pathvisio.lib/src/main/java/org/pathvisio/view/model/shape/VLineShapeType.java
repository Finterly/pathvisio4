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

import java.awt.Polygon;
import java.awt.Shape;
import java.awt.geom.Arc2D;
import java.awt.geom.Ellipse2D;
import java.awt.geom.GeneralPath;
import java.awt.geom.Rectangle2D;

import org.pathvisio.model.type.AnchorShapeType;

/**
 * This class defines and registers shapes for anchors and arrowheads
 * 
 * {@link VArrowHeadType}, including: Oval, Rectangle, Arrow, TBar. Shapes are
 * defined and registered in the static section of this class.
 * 
 * @author unknown
 */
class VLineShapeType {

	static void registerShapes() {
		
		//register arrow heads
		VShapeRegistry.registerArrow("Arrow", getArrowHead(), VArrowHeadType.FillType.CLOSED);
		VShapeRegistry.registerArrow("TBar", getTBar(), VArrowHeadType.FillType.OPEN, TBARWIDTH + TBAR_GAP);
		VShapeRegistry.registerArrow("LigandRound", getLRound(), VArrowHeadType.FillType.CLOSED);
		VShapeRegistry.registerArrow("ReceptorRound", getRRound(), VArrowHeadType.FillType.WIRE);
		VShapeRegistry.registerArrow("Receptor", getReceptor(), VArrowHeadType.FillType.WIRE);
		VShapeRegistry.registerArrow("ReceptorSquare", getReceptorSquare(), VArrowHeadType.FillType.WIRE);
		VShapeRegistry.registerArrow("LigandSquare", getLigand(), VArrowHeadType.FillType.CLOSED);

		// register anchors
		VShapeRegistry.registerAnchor(AnchorShapeType.SQUARE.getName(), getAnchorSquare()); // TODO
		VShapeRegistry.registerAnchor(AnchorShapeType.CIRCLE.getName(), getAnchorCircle());
		VShapeRegistry.registerAnchor(AnchorShapeType.NONE.getName(), getAnchorNone());
	}

	/**
	 * These are all model coordinates:
	 */
	private static final int ARROWHEIGHT = 4;
	private static final int ARROWWIDTH = 9;
	private static final int TBARHEIGHT = 15;
	private static final int TBARWIDTH = 1;
	private static final int TBAR_GAP = 6;
	private static final int LRDIAM = 11;
	private static final int RRDIAM = LRDIAM + 3;
	private static final int LIGANDWIDTH = 8;
	private static final int LIGANDHEIGHT = 11;
	private static final int RECEPWIDTH = LIGANDWIDTH + 2;
	private static final int RECEPHEIGHT = LIGANDHEIGHT + 2;

	private static final int ANCHOR_DEFAULT_SIZE = 3;
	private static final int ANCHOR_CIRCLE_SIZE = 8;

	private static Shape getArrowHead() {
		int[] xpoints = new int[] { 0, -ARROWWIDTH, -ARROWWIDTH };
		int[] ypoints = new int[] { 0, -ARROWHEIGHT, ARROWHEIGHT };
		return new Polygon(xpoints, ypoints, 3);
	}

	private static Shape getTBar() {
		return new Rectangle2D.Double(0, -TBARHEIGHT / 2, TBARWIDTH, TBARHEIGHT);
	}

	private static Shape getLRound() {
		return new Ellipse2D.Double(-LRDIAM / 2, -LRDIAM / 2, LRDIAM, LRDIAM);
	}

	private static Shape getRRound() {
		return new Arc2D.Double(0, -RRDIAM / 2, RRDIAM, RRDIAM, 90, 180, Arc2D.OPEN);
	}

	private static Shape getReceptorSquare() {
		GeneralPath rec = new GeneralPath();
		rec.moveTo(RECEPWIDTH, RECEPHEIGHT / 2);
		rec.lineTo(0, RECEPHEIGHT / 2);
		rec.lineTo(0, -RECEPHEIGHT / 2);
		rec.lineTo(RECEPWIDTH, -RECEPHEIGHT / 2);
		return rec;
	}

	private static Shape getReceptor() {
		GeneralPath rec = new GeneralPath();
		rec.moveTo(RECEPWIDTH, RECEPHEIGHT / 2);
		rec.lineTo(0, 0);
		rec.lineTo(RECEPWIDTH, -RECEPHEIGHT / 2);
		return rec;
	}

	private static Shape getLigand() {
		return new Rectangle2D.Double(-LIGANDWIDTH, -LIGANDHEIGHT / 2, LIGANDWIDTH, LIGANDHEIGHT);
	}

	private static Shape getAnchorSquare() { // TODO
		return new Rectangle2D.Double(-ANCHOR_DEFAULT_SIZE / 2, -ANCHOR_DEFAULT_SIZE / 2, ANCHOR_DEFAULT_SIZE,
				ANCHOR_DEFAULT_SIZE);
	}

	private static Shape getAnchorCircle() {
		return new Ellipse2D.Double(-ANCHOR_CIRCLE_SIZE / 2, -ANCHOR_CIRCLE_SIZE / 2, ANCHOR_CIRCLE_SIZE,
				ANCHOR_CIRCLE_SIZE);
	}

	private static Shape getAnchorNone() {
		return new Rectangle2D.Double(-ANCHOR_DEFAULT_SIZE / 2, -ANCHOR_DEFAULT_SIZE / 2, ANCHOR_DEFAULT_SIZE,
				ANCHOR_DEFAULT_SIZE);
	}

}
