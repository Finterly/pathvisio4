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
package org.pathvisio.core.view.model.copy;

import org.pathvisio.model.element.LinePoint;
import org.pathvisio.core.preferences.GlobalPreference;
import org.pathvisio.core.preferences.PreferenceManager;
import org.pathvisio.core.view.Adjustable;
import org.pathvisio.core.view.LinAlg;
import org.pathvisio.core.view.LinAlg.Point;
import org.pathvisio.core.view.model.Handle;

/**
 * One of the two endpoints of a line. Carries a single handle.
 */
public class VPoint implements Adjustable
{
	// the handle that goes with this VPoint.
	// This Handle is created, destroyed and generally managed by Line, not by VPoint
	Handle handle;
	
	private VLine line;
	private LinePoint linePoint;
	private final VPathwayModel canvas;

	private boolean isHighlighted = false;

	public boolean isHighlighted()
	{
		return isHighlighted;
	}

	public void highlight()
	{
		if (!isHighlighted)
		{
			isHighlighted = true;
			line.markDirty();
		}
	}

	public void unhighlight()
	{
		if (isHighlighted)
		{
			isHighlighted = false;
			line.markDirty();
		}
	}

	VPoint(VPathwayModel canvas, LinePoint linePoint, VLine line) {
		this.canvas = canvas;
		this.linePoint = linePoint;
		this.line = line;
	}

	protected void unlink() {
		linePoint.setElementRef(null);
	}

	protected double getVX() { return canvas.vFromM(getLinePoint().getXY().getX()); }
	protected double getVY() { return canvas.vFromM(getLinePoint().getXY().getY()); }

	protected void setVLocation(double vx, double vy) {
		linePoint.getXY().setX(canvas.mFromV(vx));
		linePoint.getXY().setY(canvas.mFromV(vy));
	}

	protected void vMoveBy(double dx, double dy) {
		linePoint.moveBy(canvas.mFromV(dx), canvas.mFromV(dy));
	}

	public LinePoint getLinePoint() {
		return linePoint;
	}

	public VLine getLine() {
		return line;
	}

	public void adjustToHandle(Handle h, double vnewx, double vnewy)
	{
		double mcx = canvas.mFromV (vnewx);
		double mcy = canvas.mFromV (vnewy);

		if (PreferenceManager.getCurrent().getBoolean(GlobalPreference.SNAP_TO_ANGLE) ||
			canvas.isSnapModifierPressed())
		{
			// get global preference and convert to radians.
			double lineSnapStep = PreferenceManager.getCurrent().getInt(
				GlobalPreference.SNAP_TO_ANGLE_STEP) * Math.PI / 180;
			VPoint p1 = line.getStart();
			VPoint p2 = line.getEnd();
			double basex, basey;
			// base is the static point the line rotates about.
			// it is equal to the OTHER point, the one we're not moving.
			if (p1 == this)
			{
				basex = p2.getLinePoint().getXY().getX();
				basey = p2.getLinePoint().getXY().getY();
			}
			else
			{
				basex = p1.getLinePoint().getXY().getX();
				basey = p1.getLinePoint().getXY().getY();
			}
			// calculate rotation and round it off
			double rotation = Math.atan2(basey - mcy, basex - mcx);
			rotation = Math.round (rotation / lineSnapStep) * lineSnapStep;
			// project point mcx, mcy on a line with the desired angle.
			Point yr = new Point (Math.cos (rotation), Math.sin (rotation));
			Point prj = LinAlg.project(new Point (basex, basey), new Point(mcx, mcy), yr);
			mcx = prj.x;
			mcy = prj.y;
		}

		linePoint.getXY().setX(mcx);
		linePoint.getXY().setY(mcy);
	}

	protected Handle getHandle()
	{
		return handle;
	}
	
	public double getVWidth() { return 0;  }

	public double getVHeight() { return 0;  }
	
	
	protected void moveBy(double[] delta)
	{
		for(int i = 0; i < coordinates.length; i++) {
			coordinates[i] += delta[i];
		}
		fireObjectModifiedEvent(PathwayElementEvent.createCoordinatePropertyEvent(PathwayElement.this));
	}

	protected void moveTo(double[] coordinates)
	{
		this.coordinates = coordinates;
		fireObjectModifiedEvent(PathwayElementEvent.createCoordinatePropertyEvent(PathwayElement.this));
	}

	protected void moveTo(GenericPoint p)
	{
		coordinates = p.coordinates;
		fireObjectModifiedEvent(PathwayElementEvent.createCoordinatePropertyEvent(PathwayElement.this));;
	}
}
