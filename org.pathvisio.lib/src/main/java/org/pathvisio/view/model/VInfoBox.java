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

import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.font.FontRenderContext;
import java.awt.font.TextLayout;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

import org.pathvisio.model.Pathway;

/**
 * 
 * Infobox contains the meta-information (e.g. title, organism) for a pathway
 * model. The infobox is always displayed in the top left corner at coordinate
 * (0,0).
 * 
 * view.InfoBox corresponds in some ways to
 * model.PathwayElement(ObjectType.MAPPINFO) and in some ways to
 * model.PathwayElement(ObjectType.INFOBOX). This confusion is rooted in
 * inconsistencies in GPML. This should be cleaned up one day.
 * 
 * @author unknown, finterly
 */
public class VInfoBox extends VPathwayElement {

	static final int V_SPACING = 5;
	static final int H_SPACING = 10;
	static final int INITIAL_SIZE = 200;

	public VInfoBox(VPathwayModel canvas, Pathway gdata) {
		super(canvas, gdata);
		canvas.setMappInfo(this);
		gdata.addListener(this); // TODO
	}

	/**
	 * Gets the model representation (PathwayElement) of this class
	 * 
	 * @return
	 */
	@Override
	public Pathway getPathwayElement() {
		return (Pathway) super.getPathwayElement();
	}

	// TODO
	public double getMLeft() {
		return 0;
	}

	// TODO
	public double getMTop() {
		return 0;
	}

	// Elements not stored in gpml
	String fontName = "Times New Roman"; // TODO
	boolean fontWeight = false;
	static final double M_INITIAL_FONTSIZE = 12.0;

	int sizeX = 1;
	int sizeY = 1; // Real size is calculated on first call to draw()

	@Override
	protected VCitation createCitation() {
		return new VCitation(canvas, this, new Point2D.Double(1, 0)); // TODO coordinates?
	}

	// public Point getBoardSize() { return new Point((int)gdata.getMBoardWidth(),
	// (int)gdata.getMBoardHeight()); }

	int getVFontSize() {
		return (int) (vFromM(M_INITIAL_FONTSIZE));
	}

//	protected void vMoveBy(double vdx, double vdy) {
////		markDirty();
//		setMTop(getMTop() + mFromV(vdy));
//		setMLeft(getMLeft() + mFromV(vdx));
////		markDirty();
//	}

	public void doDraw(Graphics2D g) {
		Font f = new Font(fontName, Font.PLAIN, getVFontSize());
		Font fb = new Font(f.getFontName(), Font.BOLD, f.getSize());

		if (isSelected()) {
			g.setColor(selectColor);
		}

		// Draw Name, Organism, Data-Source, Version, Author, Maintained-by, Email,
		// Availability and last modified
		String[][] text = new String[][] { { "Title: ", getPathwayElement().getTitle() },
				{ "Organism: ", getPathwayElement().getOrganism() },
//				{"Maintained by: ", gdata.getMaintainer()}, //TODO 
//				{"Email: ", gdata.getEmail()},
//				{"Availability: ", gdata.getCopyright()},
//				{"Last modified: ", gdata.getLastModified()},
//				{"Data Source: ", gdata.getMapInfoDataSource()}
		};

		int shift = 0;
		int vLeft = (int) vFromM(0); // gdata.getMLeft
		int vTop = (int) vFromM(0); // gdata.getMTop, mHeight is always 0?

		int newSizeX = sizeX;
		int newSizeY = sizeY;

		FontRenderContext frc = g.getFontRenderContext();
		for (String[] s : text) {
			if (s[1] == null || s[1].equals(""))
				continue; // Skip empty labels
			TextLayout tl0 = new TextLayout(s[0], fb, frc);
			TextLayout tl1 = new TextLayout(s[1], f, frc);
			Rectangle2D b0 = tl0.getBounds();
			Rectangle2D b1 = tl1.getBounds();
			shift += (int) Math.max(b0.getHeight(), b1.getHeight()) + V_SPACING;
			g.setFont(fb);
			tl0.draw(g, vLeft, vTop + shift);
			g.setFont(f);

			tl1.draw(g, vLeft + (int) b0.getWidth() + H_SPACING, vTop + shift);

			// add 10 for safety
			newSizeX = Math.max(newSizeX, (int) b0.getWidth() + (int) b1.getWidth() + H_SPACING + 10);
		}
		newSizeY = shift + 10; // add 10 for safety

		// if the size was incorrect, mark dirty and draw again.
		// note: we can't draw again right away because the clip rect
		// is set to a too small region.
		if (newSizeX != sizeX || newSizeY != sizeY) {
			sizeX = newSizeX;
			sizeY = newSizeY;
			markDirty();
		}
	}

	/**
	 * Previous implementation doesn't actually allow infobox to move...
	 */
	protected Shape getVShape(boolean rotate) {
		double vLeft = vFromM(getMLeft()); // gdata.getMLeft
		double vTop = vFromM(getMTop()); // gdata.getMTop, mHeight is always 0?
		double vW = sizeX;
		double vH = sizeY;
		if (vW == 1 && vH == 1) {
			vW = INITIAL_SIZE;
			vH = INITIAL_SIZE;
		}
		return new Rectangle2D.Double(vLeft, vTop, vW, vH);
	}

	@Override
	protected void setVScaleRectangle(Rectangle2D r) {
		// Do nothing, can't resize infobox
	}

	@Override
	protected int getZOrder() {
		return 0x0000; // default z-order for Infobox TODO
	}

}
