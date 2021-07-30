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
package org.pathvisio.core.view.model;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Point2D;

import org.pathvisio.model.type.AnchorShapeType;
import org.pathvisio.core.view.Adjustable;
import org.pathvisio.core.view.AnchorShape;
import org.pathvisio.core.view.LinkProvider;
import org.pathvisio.core.view.ShapeRegistry;
import org.pathvisio.core.view.model.Handle.Freedom;
import org.pathvisio.model.Anchor;
import org.pathvisio.model.LinePoint;

/**
 * VAnchor is the view representation of {@link Anchor}.
 *
 * It is stuck to a Line and can move one-dimensionally across it. It has a
 * handle so the user can drag it.
 * 
 * @author unknown, finterly
 */
public class VAnchor extends VElement implements LinkProvider, Adjustable {
	private Anchor mAnchor;
	private VLine line;
	private Handle handle;
	private double mx = Double.NaN;
	private double my = Double.NaN;
	LinkAnchor linkAnchor = null;
	static final double MIN_OUTLINE = 15; // Minimum outline diameter of 15px

	/**
	 * Instantiates a VAnchor
	 * 
	 * @param mAnchor the model Anchor.
	 * @param parent  the parent view Line.
	 */
	public VAnchor(Anchor mAnchor, VLine parent) {
		super(parent.getDrawing());
		this.mAnchor = mAnchor;
		this.line = parent;
		updatePosition();
	}

	/**
	 * Returns view x coordinate.
	 * 
	 * @return view coordinate.
	 */
	public double getVx() {
		return vFromM(mx);
	}

	/**
	 * Returns view y coordinate.
	 * 
	 * @return view coordinate.
	 */
	public double getVy() {
		return vFromM(my);
	}

	/**
	 * Returns {@link Handle} for the VAnchor.
	 * 
	 * @return handle the handle
	 */
	public Handle getHandle() {
		return handle;
	}

	/**
	 * Returns the model {@link Anchor} for the VAnchor.
	 * 
	 * @return mAnchor the model anchor.
	 */
	public Anchor getMAnchor() {
		return mAnchor;
	}

	/**
	 * Destroys the VAnchor and removed VAnchor from parent line.
	 */
	protected void destroy() {
		super.destroy();
		line.removeVAnchor(this);
	}

	/**
	 * Destroys {@link Handle} for VAnchor.
	 */
	@Override
	protected void destroyHandles() {
		if (handle != null) {
			handle.destroy();
			handle = null;
		}
	}

	/**
	 * Creates {@link Handle} for VAnchor.
	 */
	protected void createHandles() {
		handle = new Handle(Handle.Freedom.FREE, this, this);
		double lc = mAnchor.getPosition();
		Point2D position = line.vFromL(lc);
		handle.setVLocation(position.getX(), position.getY());
	}

	void updatePosition() {
		double lc = mAnchor.getPosition();

		Point2D position = line.vFromL(lc);
		if (handle != null)
			handle.setVLocation(position.getX(), position.getY());

		mx = mFromV(position.getX());
		my = mFromV(position.getY());
		// Redraw graphRefs // TODO will add methods to libGPML
		for (GraphRefContainer ref : mAnchor.getReferences()) {
			if (ref instanceof LinePoint) {
				VPoint vp = canvas.getPoint((LinePoint) ref);
				if (vp != null && vp.getLine() != line) {
					vp.getLine().recalculateConnector();
				}
			}
		}
	}

	public void adjustToHandle(Handle h, double vx, double vy) {
		double position = line.lFromV(new Point2D.Double(vx, vy));
		mAnchor.setPosition(position);
	}

	/**
	 * Returns anchor shape type.
	 * 
	 * @return shape the shape of the anchor.
	 */
	private AnchorShape getAnchorShape() {
		AnchorShape shape = ShapeRegistry.getAnchor(mAnchor.getShapeType().getName());

		if (shape != null) {
			AffineTransform f = new AffineTransform();
			double scaleFactor = vFromM(1.0);
			f.translate(getVx(), getVy());
			f.scale(scaleFactor, scaleFactor);
			Shape sh = f.createTransformedShape(shape.getShape());
			shape = new AnchorShape(sh);
		}
		return shape;
	}

	private Shape getShape() {
		AnchorShape shape = getAnchorShape();
		return shape != null ? shape.getShape() : handle.getVOutline();
	}

	protected void doDraw(Graphics2D g) {
		if (getMAnchor().getShapeType().equals(AnchorShapeType.NONE) && getMAnchor().getElementId() != null) {
			return;
		}
		Color c;

		if (isSelected()) {
			c = selectColor;
		} else {
			c = line.getPathwayElement().getLineStyleProp().getLineColor();
		}

		AnchorShape arrowShape = getAnchorShape();
		if (arrowShape != null) {
			g.setStroke(new BasicStroke());
			g.setPaint(c);
			g.fill(arrowShape.getShape());
			g.draw(arrowShape.getShape());
		}

		if (isHighlighted()) {
			Color hc = getHighlightColor();
			g.setColor(new Color(hc.getRed(), hc.getGreen(), hc.getBlue(), 128));
			g.setStroke(new BasicStroke(HIGHLIGHT_STROKE_WIDTH));
			g.draw(getShape());
		}
	}

	protected Shape calculateVOutline() {
		Shape s = getShape();
		Rectangle b = s.getBounds();
		// Create a larger shape if the given shape
		// is smaller than the minimum
		if (b.getWidth() < MIN_OUTLINE || b.getHeight() < MIN_OUTLINE) {
			s = new Ellipse2D.Double(getVx() - MIN_OUTLINE / 2, getVy() - MIN_OUTLINE / 2, MIN_OUTLINE, MIN_OUTLINE);
		}
		return s;
	}

	public LinkAnchor getLinkAnchorAt(Point2D p) {
		if (linkAnchor != null && linkAnchor.getMatchArea().contains(p)) {
			return linkAnchor;
		}
		return null;
	}

	public void hideLinkAnchors() {
		if (linkAnchor != null)
			linkAnchor.destroy();
		linkAnchor = null;
	}

	public void showLinkAnchors() {
		linkAnchor = new LinkAnchor(canvas, this, mAnchor, 0, 0);
	}

	/**
	 * Returns the zorder of the parent line
	 */
	protected int getZOrder() {
		return line.getPathwayElement().getLineStyleProp().getZOrder() + 1;
	}

}
