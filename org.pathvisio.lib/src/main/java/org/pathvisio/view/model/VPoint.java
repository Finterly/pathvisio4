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

import org.pathvisio.model.LineElement.LinePoint;
import org.pathvisio.util.preferences.GlobalPreference;
import org.pathvisio.util.preferences.PreferenceManager;
import org.pathvisio.view.LinAlg;
import org.pathvisio.view.LinAlg.Point;

/**
 * One of the two endpoints of a line. Carries a single handle.
 * 
 * @author unknown, finterly
 */
public class VPoint implements Adjustable {

	private final VPathwayModel canvas;
	private LinePoint linePoint;
	private VLineElement vLine;
	private boolean isHighlighted = false;
	/**
	 * The handle that goes with this VPoint. This Handle is created, destroyed and
	 * generally managed by Line, not by VPoint
	 */
	Handle handle;

	/**
	 * Instantiates a VPoint.
	 * 
	 * @param canvas    the VPathwayModel this object belongs to.
	 * @param linePoint the LinePoint pathway element this object corresponds to.
	 * @param line      the VLine this objects belongs to.
	 */
	VPoint(VPathwayModel canvas, LinePoint linePoint, VLineElement line) {
		this.canvas = canvas;
		this.linePoint = linePoint;
		this.vLine = line;
	}

	/**
	 * Gets the model representation (PathwayElement) of this class
	 * 
	 * @return linePoint the LinePoint pathway element this object corresponds to.
	 */
	public LinePoint getPathwayElement() {
		return linePoint;
	}

	public boolean isHighlighted() {
		return isHighlighted;
	}

	public void highlight() {
		if (!isHighlighted) {
			isHighlighted = true;
			vLine.markDirty();
		}
	}

	public void unhighlight() {
		if (isHighlighted) {
			isHighlighted = false;
			vLine.markDirty();
		}
	}

	protected void unlink() {
		linePoint.unlink();
	}

	protected double getVX() {
		return canvas.vFromM(linePoint.getX());
	}

	protected double getVY() {
		return canvas.vFromM(linePoint.getY());
	}

	protected void setVLocation(double vx, double vy) {
		linePoint.setX(canvas.mFromV(vx));
		linePoint.setY(canvas.mFromV(vy));
	}

	protected void vMoveBy(double dx, double dy) {
		linePoint.moveBy(canvas.mFromV(dx), canvas.mFromV(dy));
	}

	public LinePoint getLinePoint() {
		return linePoint;
	}

	public VLineElement getLine() {
		return vLine;
	}

	public void adjustToHandle(Handle h, double vnewx, double vnewy) {
		double mcx = canvas.mFromV(vnewx);
		double mcy = canvas.mFromV(vnewy);

		if (PreferenceManager.getCurrent().getBoolean(GlobalPreference.SNAP_TO_ANGLE)
				|| canvas.isSnapModifierPressed()) {
			// get global preference and convert to radians.
			double lineSnapStep = PreferenceManager.getCurrent().getInt(GlobalPreference.SNAP_TO_ANGLE_STEP) * Math.PI
					/ 180;
			VPoint p1 = vLine.getStart();
			VPoint p2 = vLine.getEnd();
			double basex, basey;
			// base is the static point the line rotates about.
			// it is equal to the OTHER point, the one we're not moving.
			if (p1 == this) {
				basex = p2.getLinePoint().getX();
				basey = p2.getLinePoint().getY();
			} else {
				basex = p1.getLinePoint().getX();
				basey = p1.getLinePoint().getY();
			}
			// calculate rotation and round it off
			double rotation = Math.atan2(basey - mcy, basex - mcx);
			rotation = Math.round(rotation / lineSnapStep) * lineSnapStep;
			// project point mcx, mcy on a line with the desired angle.
			Point yr = new Point(Math.cos(rotation), Math.sin(rotation));
			Point prj = LinAlg.project(new Point(basex, basey), new Point(mcx, mcy), yr);
			mcx = prj.x;
			mcy = prj.y;
		}

		linePoint.setX(mcx);
		linePoint.setY(mcy);
	}

	protected Handle getHandle() {
		return handle;
	}

	public double getVWidth() {
		return 0;
	}

	public double getVHeight() {
		return 0;
	}


}
