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

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.font.TextAttribute;
import java.awt.geom.AffineTransform;
import java.awt.geom.Area;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.text.AttributedString;

import org.pathvisio.model.type.LineStyleType;
import org.pathvisio.model.PathwayElement;
//import org.pathvisio.model.PathwayElementEvent; TODO 
import org.pathvisio.model.ShapedElement;
import org.pathvisio.model.Rotatable;
import org.pathvisio.model.State;
import org.pathvisio.model.type.ShapeType;
import org.pathvisio.util.preferences.GlobalPreference;
import org.pathvisio.util.preferences.PreferenceManager;
import org.pathvisio.view.Adjustable;
import org.pathvisio.view.DefaultLinkAnchorDelegate;
import org.pathvisio.view.LinAlg;
import org.pathvisio.view.LinkProvider;
import org.pathvisio.view.ShapeRegistry;
import org.pathvisio.view.LinAlg.Point;
import org.pathvisio.view.model.Handle.Freedom;

/**
 * This {@link Graphics} class represents the view of a {@link Rotatable}
 * pathway elements: {@link DataNode}, {@link State}, {@link Label}, and
 * {@link Shape}. Rotation is implemented with 8 handles placed in a (rotated)
 * rectangle around the shape and a rotation handle.
 * 
 * @author unknown, finterly
 */
public abstract class GraphicsRotatable implements LinkProvider, Adjustable {

	protected Rotatable gdata = null;

	private static final double M_ROTATION_HANDLE_POSITION = 20.0;

	// Side handles
	Handle handleN;
	Handle handleE;
	Handle handleS;
	Handle handleW;
	// Corner handles
	Handle handleNE;
	Handle handleSE;
	Handle handleSW;
	Handle handleNW;
	// Rotation handle
	Handle handleR;

	Handle[] handles = new Handle[] {};

	/**
	 * Constructor for this class
	 * 
	 * @param canvas - the VPathway this Shape will be part of
	 */
	public GraphicsRotatable(VPathwayModel canvas) {
		super(canvas);
	}

	protected void createHandles() {

		if (gdata.getShapeStyleProp().getShapeType() != null && !gdata.getShapeStyleProp().getShapeType().isResizeable()
				&& !gdata.getShapeStyleProp().getShapeType().isRotatable()) {
			return; // no resizing, no handles
		} else if (gdata.getShapeStyleProp().getShapeType() != null
				&& !gdata.getShapeStyleProp().getShapeType().isResizeable()
				&& gdata.getShapeStyleProp().getShapeType().isRotatable()) {
			handleR = new Handle(Handle.Freedom.ROTATION, this, this);
			handleR.setAngle(1);
			handles = new Handle[] { handleR };
		} else if (this instanceof VState) {
			handleNE = new Handle(Handle.Freedom.NEGFREE, this, this);
			handleSE = new Handle(Handle.Freedom.FREE, this, this);
			handleSW = new Handle(Handle.Freedom.NEGFREE, this, this);
			handleNW = new Handle(Handle.Freedom.FREE, this, this);

			handleNE.setAngle(315);
			handleSE.setAngle(45);
			handleSW.setAngle(135);
			handleNW.setAngle(225);

			handles = new Handle[] { handleNE, handleSE, handleSW, handleNW, };
		} else {
			handleN = new Handle(Handle.Freedom.Y, this, this);
			handleE = new Handle(Handle.Freedom.X, this, this);
			handleS = new Handle(Handle.Freedom.Y, this, this);
			handleW = new Handle(Handle.Freedom.X, this, this);

			handleNE = new Handle(Handle.Freedom.NEGFREE, this, this);
			handleSE = new Handle(Handle.Freedom.FREE, this, this);
			handleSW = new Handle(Handle.Freedom.NEGFREE, this, this);
			handleNW = new Handle(Handle.Freedom.FREE, this, this);

			handleN.setAngle(270);
			handleE.setAngle(0);
			handleS.setAngle(90);
			handleW.setAngle(180);
			handleNE.setAngle(315);
			handleSE.setAngle(45);
			handleSW.setAngle(135);
			handleNW.setAngle(225);

			if (this instanceof VDataNode || this instanceof VLabel
					|| !gdata.getShapeStyleProp().getShapeType().isRotatable()) {
				// No rotation handle for these objects
				handles = new Handle[] { handleN, handleNE, handleE, handleSE, handleS, handleSW, handleW, handleNW, };
			} else {
				handleR = new Handle(Handle.Freedom.ROTATION, this, this);
				handleR.setAngle(1);

				handles = new Handle[] { handleN, handleNE, handleE, handleSE, handleS, handleSW, handleW, handleNW,
						handleR };
			}
		}
		setHandleLocation();
	}

	protected void setVScaleRectangle(Rectangle2D r) {
		gdata.getRectProperty().setWidth(mFromV(r.getWidth()));
		gdata.getRectProperty().setHeight(mFromV(r.getHeight()));
		gdata.setMLeft(mFromV(r.getX()));
		gdata.setMTop(mFromV(r.getY()));
	}

	protected void vMoveBy(double vdx, double vdy) {
		// both setM operations fire the exact same objectModifiedEvent, one should be
		// enough
		gdata.dontFireEvents(1);
		gdata.setMLeft(gdata.getMLeft() + mFromV(vdx));
		gdata.setMTop(gdata.getMTop() + mFromV(vdy));
	}

	public Handle[] getHandles() {
		return handles;
	}

	/**
	 * Translate the given point to internal coordinate system (origin in center and
	 * axis direction rotated with this objects rotation
	 * 
	 * @param MPoint p
	 */
	private Point mToInternal(Point p) {
		Point pt = mRelativeToCenter(p);
		Point pr = LinAlg.rotate(pt, gdata.getRotation());
		return pr;
	}

	/**
	 * Translate the given coordinates to external coordinate system (of the drawing
	 * canvas)
	 * 
	 * @param x
	 * @param y
	 */
	private Point mToExternal(double x, double y) {
		Point p = new Point(x, y);
		Point pr = LinAlg.rotate(p, -gdata.getRotation());
		pr.x += gdata.getMCenterX();
		pr.y += gdata.getMCenterY();
		return pr;
	}

	/**
	 * Get the coordinates of the given point relative to this object's center
	 * 
	 * @param p
	 */
	private Point mRelativeToCenter(Point p) {
		return p.subtract(new Point(gdata.getMCenterX(), gdata.getMCenterY()));
	}

	/**
	 * Set the rotation of this object
	 * 
	 * @param angle angle of rotation in radians
	 */
	public void setRotation(double angle) {
		if (angle < 0)
			gdata.setRotation(angle + Math.PI * 2);
		else if (angle > Math.PI * 2)
			gdata.setRotation(angle - Math.PI * 2);
		else
			gdata.setRotation(angle);
	}

	public void adjustToHandle(Handle h, double vnewx, double vnewy) {
		// Rotation
		if (h == handleR) {
			Point cur = mRelativeToCenter(new Point(mFromV(vnewx), mFromV(vnewy)));

			double rotation = Math.atan2(cur.y, cur.x);
			if (PreferenceManager.getCurrent().getBoolean(GlobalPreference.SNAP_TO_ANGLE)
					|| canvas.isSnapModifierPressed()) {
				// Snap the rotation angle
				double snapStep = PreferenceManager.getCurrent().getInt(GlobalPreference.SNAP_TO_ANGLE_STEP) * Math.PI
						/ 180;
				rotation = Math.round(rotation / snapStep) * snapStep;
			}
			setRotation(rotation);
			return;
		}

		/*
		 * if point is restricted to a certain range of movement, project handle to the
		 * closest point in that range.
		 * 
		 * This is true for all handles, except for Freedom.FREE and Freedom.NEGFREE
		 * when the snap modifier is not pressed.
		 */
		Freedom freedom = h.getFreedom();
		if (!(freedom == Freedom.FREE || freedom == Freedom.NEGFREE) || canvas.isSnapModifierPressed()) {
			Point v = new Point(0, 0);
			Rectangle2D b = getVBounds();
			Point base = new Point(b.getCenterX(), b.getCenterY());
			if (freedom == Freedom.X) {
				v = new Point(1, 0);
			} else if (freedom == Freedom.Y) {
				v = new Point(0, 1);
			}
			if (freedom == Freedom.FREE) {
				v = new Point(getVWidth(), getVHeight());
			} else if (freedom == Freedom.NEGFREE) {
				v = new Point(getVWidth(), -getVHeight());
			}
			Point yr = LinAlg.rotate(v, -gdata.getRotation());
			Point prj = LinAlg.project(base, new Point(vnewx, vnewy), yr);
			vnewx = prj.x;
			vnewy = prj.y;
		}

		// Transformation
		Point iPos = mToInternal(new Point(mFromV(vnewx), mFromV(vnewy)));

		double idx = 0;
		double idy = 0;
		double idw = 0;
		double idh = 0;
		double halfw = gdata.getRectProperty().getWidth() / 2;
		double halfh = gdata.getRectProperty().getHeight() / 2;

		if (h == handleN || h == handleNE || h == handleNW) {
			idh = -(iPos.y + halfh);
			idy = -idh / 2;
		}
		if (h == handleS || h == handleSE || h == handleSW) {
			idh = (iPos.y - halfh);
			idy = idh / 2;
		}
		if (h == handleE || h == handleNE || h == handleSE) {
			idw = (iPos.x - halfw);
			idx = idw / 2;
		}
		if (h == handleW || h == handleNW || h == handleSW) {
			idw = -(iPos.x + halfw);
			idx = -idw / 2;
		}
		;

		double neww = gdata.getRectProperty().getWidth() + idw;
		double newh = gdata.getRectProperty().getHeight() + idh;

		// In case object had negative width, switch handles
		if (neww < 0) {
			setHorizontalOppositeHandle(h);
			neww = -neww;
		}
		if (newh < 0) {
			setVerticalOppositeHandle(h);
			newh = -newh;
		}

		gdata.getRectProperty().setWidth(neww);
		gdata.getRectProperty().setHeight(newh);
		Point vcr = LinAlg.rotate(new Point(idx, idy), -gdata.getRotation());
		gdata.getRectProperty().getCenterXY().setX(gdata.getRectProperty().getCenterXY().getX() + vcr.x);
		gdata.getRectProperty().getCenterXY().setY(gdata.getRectProperty().getCenterXY().getY() + vcr.y);

	}

	private void setHorizontalOppositeHandle(Handle h) {
		Handle opposite = null;
		if (h == handleE)
			opposite = handleW;
		else if (h == handleW)
			opposite = handleE;
		else if (h == handleNE)
			opposite = handleNW;
		else if (h == handleSE)
			opposite = handleSW;
		else if (h == handleNW)
			opposite = handleNE;
		else if (h == handleSW)
			opposite = handleSE;
		else
			opposite = h;
		canvas.setPressedObject(opposite);
	}

	private void setVerticalOppositeHandle(Handle h) {
		Handle opposite = null;
		if (h == handleN)
			opposite = handleS;
		else if (h == handleS)
			opposite = handleN;
		else if (h == handleNE)
			opposite = handleSE;
		else if (h == handleSE)
			opposite = handleNE;
		else if (h == handleNW)
			opposite = handleSW;
		else if (h == handleSW)
			opposite = handleNW;
		else
			opposite = h;
		canvas.setPressedObject(opposite);
	}

	/**
	 * Sets the handles at the correct location;
	 * 
	 * @param ignore the position of this handle will not be adjusted
	 */
	protected void setHandleLocation() {
		Point p;
		if (gdata.getShapeStyleProp().getShapeType() == null || gdata.getShapeType().isResizeable()) {

			if (handleN != null) {
				p = mToExternal(0, -gdata.getRectProperty().getHeight() / 2);
				handleN.setMLocation(p.x, p.y);
				p = mToExternal(gdata.getRectProperty().getWidth() / 2, 0);
				handleE.setMLocation(p.x, p.y);
				p = mToExternal(0, gdata.getRectProperty().getHeight() / 2);
				handleS.setMLocation(p.x, p.y);
				p = mToExternal(-gdata.getRectProperty().getWidth() / 2, 0);
				handleW.setMLocation(p.x, p.y);
			}

			p = mToExternal(gdata.getRectProperty().getWidth() / 2, -gdata.getRectProperty().getHeight() / 2);
			handleNE.setMLocation(p.x, p.y);
			p = mToExternal(gdata.getRectProperty().getWidth() / 2, gdata.getRectProperty().getHeight() / 2);
			handleSE.setMLocation(p.x, p.y);
			p = mToExternal(-gdata.getRectProperty().getWidth() / 2, gdata.getRectProperty().getHeight() / 2);
			handleSW.setMLocation(p.x, p.y);
			p = mToExternal(-gdata.getRectProperty().getWidth() / 2, -gdata.getRectProperty().getHeight() / 2);
			handleNW.setMLocation(p.x, p.y);
		}
		if ((gdata.getShapeStyleProperty().getShapeType() == null || gdata.getShapeType().isRotatable())
				&& (handleR != null)) {
			p = mToExternal(gdata.getRectProperty().getWidth() / 2 + M_ROTATION_HANDLE_POSITION, 0);
			handleR.setMLocation(p.x, p.y);
		}

		for (Handle h : getHandles())
			h.rotation = gdata.getRotation();
	}

	protected Shape calculateVOutline() {
		// Include rotation and stroke
		Area a = new Area(getShape(true, true));
		return a;
	}

	protected Shape getVShape(boolean rotate) {
		return getShape(rotate, false); // Get the shape without border
	}

	/**
	 * Returns the shape that should be drawn
	 * 
	 * @parameter rotate whether to take into account rotation or not
	 * @parameter stroke whether to include the stroke or not
	 * @return
	 */
	protected Shape getShape(boolean rotate, boolean stroke) {
		if (stroke) {
			return getShape(rotate, (float) gdata.getShapeStyleProperty().getBorderWidth());
		} else {
			return getShape(rotate, 0);
		}
	}

	public Shape getShape() {
		return getShape(false, 0);
	}

	/**
	 * Returns the shape that should be drawn
	 * 
	 * @parameter rotate whether to take into account rotation or not
	 * @parameter sw the width of the stroke to include
	 * @return
	 */
	protected java.awt.Shape getShape(boolean rotate, float sw) {
		double mx = gdata.getMLeft();
		double my = gdata.getMTop();
		double mw = gdata.getRectProperty().getWidth();
		double mh = gdata.getRectProperty().getHeight();
		double mcx = gdata.getRectProperty().getCenterXY().getX();
		double mcy = gdata.getRectProperty().getCenterXY().getY();

		java.awt.Shape s = null;

		if (gdata.getShapeStyleProp().getShapeType() == null || gdata.getShapeStyleProp().getShapeType() == ShapeType.NONE) {
			s = ShapeRegistry.DEFAULT_SHAPE.getShape(mw, mh);
		} else {
			s = gdata.getShapeStyleProp().getShapeType().getShape(mw, mh);
		}

		AffineTransform t = new AffineTransform();
		t.scale(canvas.getZoomFactor(), canvas.getZoomFactor());

		if (rotate) {
			t.rotate(gdata.getRotation(), mcx, mcy);
		}
		t.translate(mx, my);
		s = t.createTransformedShape(s);

		if (sw > 0)
			if (mw * mh > 0) // Workaround, batik balks if the shape is zero sized.
			{
				if (gdata.getShapeStyleProp().getBorderStyle() == LineStyleType.DOUBLE) {
					// correction factor for composite stroke
					sw = (float) (gdata.getShapeStyleProp().getBorderWidth() * 4);
				}
				Stroke stroke = new BasicStroke(sw);
				s = stroke.createStrokedShape(s);
			}
		return s;
	}

	public void gmmlObjectModified(PathwayElementEvent e) {
		markDirty(); // mark everything dirty
		checkCitation();
		if (handles.length > 0)
			setHandleLocation();
	}

	LinkProvider linkAnchorDelegate = new DefaultLinkAnchorDelegate(this);

	/**
	 * Use this to override default linkAnchorDelegate
	 */
	public void setLinkAnchorDelegate(LinkProvider delegate) {
		if (delegate == null)
			throw new NullPointerException("passed illegal null value for delegate");
		linkAnchorDelegate = delegate;
	}

	public void showLinkAnchors() {
		linkAnchorDelegate.showLinkAnchors();
	}

	public void hideLinkAnchors() {
		linkAnchorDelegate.hideLinkAnchors();
	}

	public LinkAnchor getLinkAnchorAt(Point2D p) {
		return linkAnchorDelegate.getLinkAnchorAt(p);
	}

	@Override
	protected void destroyHandles() {
		for (Handle h : handles) {
			h.destroy();
		}
		handles = new Handle[] {};
	}

	protected void doDraw(Graphics2D g2d) {
		g2d.setColor(getLineColor());
		setLineStyle(g2d);
		drawShape(g2d);

		// return to normal stroke
		g2d.setStroke(new BasicStroke());

		g2d.setFont(getVFont());
		drawTextLabel(g2d);

		drawHighlight(g2d);
	}

	protected void drawTextLabel(Graphics2D g) {
		int margin = (int) vFromM(5);
		Rectangle area = getVShape(true).getBounds();
		String label = gdata.getTextLabel();
		if (label != null && !"".equals(label)) {
			// Split by newline, to enable multi-line labels
			String[] lines = label.split("\n");

			FontMetrics fm = g.getFontMetrics();
			int lh = fm.getHeight();
			int yoffset = area.y + fm.getAscent();
			switch (gdata.getFontProp().getVAlign()) {
			case MIDDLE:
				yoffset += (area.height - (lines.length * lh)) / 2;
				break;
			case TOP:
				yoffset += margin;
				break;
			case BOTTOM:
				yoffset += area.height - margin - (lines.length * lh);
			}

			for (int i = 0; i < lines.length; i++) {
				if (lines[i].equals(""))
					continue; // Can't have attributed string with 0 length
				AttributedString ats = getVAttributedString(lines[i]);
				if (!gdata.getHref().equals("")) {
					ats.addAttribute(TextAttribute.UNDERLINE, TextAttribute.UNDERLINE_ON);
				}
				Rectangle2D tb = fm.getStringBounds(ats.getIterator(), 0, lines[i].length(), g);

				int xoffset = area.x;
				switch (gdata.getFontProp().getHAlign()) {
				case CENTER:
					xoffset += (int) (area.width / 2) - (int) (tb.getWidth() / 2);
					break;
				case LEFT:
					xoffset += margin;
					break;
				case RIGHT:
					xoffset += area.width - margin - tb.getWidth();
					break;
				}

				g.drawString(ats.getIterator(), xoffset, yoffset + (int) (i * tb.getHeight()));
			}

		}
	}

	private AttributedString getVAttributedString(String text) {
		AttributedString ats = new AttributedString(text);
		if (gdata.getFontProp().getFontStrikethru()) {
			ats.addAttribute(TextAttribute.STRIKETHROUGH, TextAttribute.STRIKETHROUGH_ON);
		}
		if (gdata.getFontProp().getFontDecoration()) {
			ats.addAttribute(TextAttribute.UNDERLINE, TextAttribute.UNDERLINE_ON);
		}

		ats.addAttribute(TextAttribute.FONT, getVFont());
		return ats;
	}

	protected Font getVFont() {
		String name = gdata.getFontProp().getFontName();
		int style = getVFontStyle();
		return new Font(name, style, 12).deriveFont((float) vFromM(gdata.getFontProp().getFontSize()));
	}

	protected void drawShape(Graphics2D g) {
		Color fillcolor = gdata.getShapeStyleProp().getFillColor();

		if (!hasOutline())
			return; // nothing to draw.

		java.awt.Shape shape = getShape(true, false);

		if (gdata.getShapeStyleProp().getShapeType() == ShapeType.BRACE || gdata.getShapeStyleProp().getShapeType() == ShapeType.ARC) {
			// don't fill arcs or braces
			// TODO: this exception should disappear in the future,
			// when we've made sure all pathways on wikipathways have
			// transparent arcs and braces
		} else {
			// fill the rest
			if (!gdata.isTransparent()) {
				g.setColor(fillcolor);
				g.fill(shape);
			}
		}
		g.setColor(getLineColor());
		g.draw(shape);
	}

	private boolean hasOutline() {
		return (!(gdata.getShapeStyleProp().getShapeType() == null || gdata.getShapeStyleProp().getShapeType() == ShapeType.NONE));
	}

	/**
	 * Draw a translucent marker around the shape so that it stands out. Used e.g.
	 * to indicate search results. Highlightcolor is customizeable.
	 */
	protected void drawHighlight(Graphics2D g) {
		if (isHighlighted()) {
			Color hc = getHighlightColor();
			g.setColor(new Color(hc.getRed(), hc.getGreen(), hc.getBlue(), 128));

			if (hasOutline()) {
				// highlight the outline
				java.awt.Shape shape = getShape(true, false);
				g.setStroke(new BasicStroke(HIGHLIGHT_STROKE_WIDTH));
				g.draw(shape);
			} else {
				// outline invisible, fill the entire area
				g.setStroke(new BasicStroke());
				Rectangle2D r = new Rectangle2D.Double(getVLeft(), getVTop(), getVWidth(), getVHeight());
				g.fill(r);
			}
		}
	}

	/**
	 * {@inheritDoc} GraphicsShape overrides vContains, because the base
	 * implementation only considers a hit with the outline, which makes it hard to
	 * grab with the mouse.
	 */
	@Override
	protected boolean vContains(Point2D point) {
		// first use getVBounds as a rough approximation
		if (getVBounds().contains(point)) {
			// if the shape is transparent, only check against the outline
			if (gdata.isTransparent()) {
				return getVOutline().contains(point);
			} else {
				// otherwise check against the whole shape
				return getVShape(true).contains(point);
			}
		} else {
			return false;
		}
	}

}
