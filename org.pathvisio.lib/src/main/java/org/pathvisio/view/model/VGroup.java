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

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.geom.Area;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.List;

import org.pathvisio.model.Group;
import org.pathvisio.view.GroupPainter;
import org.pathvisio.view.VElementMouseEvent;
import org.pathvisio.view.VElementMouseListener;

/**
 * This represents the view of a {@link Group} PathwayElement. This can be drawn
 * as a shaded area, or the group can be invisible.
 *
 * Also contains the getGroupGraphics method to quickly access all Graphics'
 * that are in this group.
 * 
 * @author unknown, finterly
 */
public class VGroup extends VShapedElement implements LinkProvider, VElementMouseListener {
	public static final int FLAG_SELECTED = 1 << 0;
	public static final int FLAG_MOUSEOVER = 1 << 1;
	public static final int FLAG_ANCHORSVISIBLE = 1 << 2;

	public VGroup(VPathwayModel canvas, Group gdata) {
		super(canvas, gdata);
		canvas.addVElementMouseListener(this); // TODO????
	}

	/**
	 * Gets the model representation (PathwayElement) of this class
	 * 
	 * @return
	 */
	@Override
	public Group getPathwayElement() {
		return (Group) super.getPathwayElement();
	}

//	/**
//	 * Generates current id-ref pairs from all current groups
//	 *
//	 * @return HashMap<String, String> TODO not used anywhere...
//	 */
//	protected Map<String, String> getIdRefPairs() {
//		// idRefPairs<id, ref>
//		Map<String, String> idRefPairs = new HashMap<String, String>();
//
//		// Populate hash map of id-ref pairs for all groups
//		for (VPathwayElement vpe : canvas.getDrawingObjects()) {
//			if (vpe instanceof Graphics && vpe instanceof Group) {
//				PathwayElement pe = ((Graphics) vpe).getPathwayElement();
//				if (pe.getGroupRef() != null) {
//					idRefPairs.put(pe.getGroupId(), pe.getGroupRef());
//				}
//			}
//		}
//
//		return idRefPairs;
//	}
//
//	/**
//	 * Generates list of group references nested under this group
//	 *
//	 * @return ArrayList<String> TODO not used anywhere...
//	 */
//	protected List<String> getRefList() {
//		Map<String, Group> idRefPairs = this.getIdRefPairs();
//		List<String> refList = new ArrayList<String>();
//		String thisId = this.getPathwayElement().getElementId();
//		refList.add(thisId);
//		boolean hit = true;
//
//		while (hit) {
//			hit = false;
//			// search for hits in hash map; add to refList
//			for (String id : idRefPairs.keySet()) {
//				if (refList.contains(idRefPairs.get(id))) {
//					refList.add(id);
//					hit = true;
//				}
//			}
//			// remove hits from hash map
//			for (int i = 0; i < refList.size(); i++) {
//				idRefPairs.remove(refList.get(i));
//			}
//		}
//		return refList;
//	}

	/**
	 * Determines whether the area defined by the grouped elements contains the
	 * point specified. The elements themselves are excluded to support individual
	 * selection within a group. The ultimate effect is then selection of group by
	 * clicking the area and not the members of the group.
	 *
	 * @param point - the point to check
	 * @return True if the object contains the point, false otherwise
	 */
	protected boolean vContains(Point2D point) {
		// return false if point falls on any individual element
		for (VElement vpe : canvas.getDrawingObjects()) {
			if (vpe instanceof VPathwayObject && !(vpe instanceof VGroup) && vpe.vContains(point)) {
				return false;

			}
		}
		// return true if point within bounds of grouped objects
		if (this.getVShape(true).contains(point)) {
			return true;
		} else {
			return false;
		}
	}

	/**
	 * Returns graphics for members of a group, including nested members
	 *
	 * @return ArrayList<VPathwayObject>
	 */
	public List<VPathwayObject> getGroupGraphics() {
		List<VPathwayObject> gg = new ArrayList<VPathwayObject>();
		// return true if group object is referenced by selection
		for (VElement vpe : canvas.getDrawingObjects()) {
			if (vpe instanceof VPathwayObject && vpe != this) {
				VPathwayObject vpeg = (VPathwayObject) vpe;
				Group pe = (Group) vpeg.getPathwayElement();
				Group ref = pe.getGroupRef();
				if (ref != null && ref.equals(getPathwayElement().getElementId())) {
					gg.add(vpeg);
				}
			}
		}
		return gg;
	}

	@Override
	public void select() {
		for (VPathwayObject g : getGroupGraphics()) {
			g.select();
		}
		super.select();
	}

	@Override
	public void deselect() {
		for (VPathwayObject g : getGroupGraphics()) {
			g.deselect();
		}
		super.deselect();
	}

	@Override
	protected void vMoveBy(double dx, double dy) {
		canvas.moveMultipleElements(getGroupGraphics(), dx, dy);

		// update group outline
		markDirty();
	}

	protected void doDraw(Graphics2D g2d) {
		// Build the flags
		int flags = 0;
		if (isSelected())
			flags += FLAG_SELECTED;
		if (mouseover)
			flags += FLAG_MOUSEOVER;
		if (anchorsShowing)
			flags += FLAG_ANCHORSVISIBLE;

		// Draw the group style appearance
		GroupPainter p = GroupPainterRegistry.getPainter(((Group) gdata).getType().toString());
		p.drawGroup(g2d, this, flags);
	}

	boolean mouseover = false;

	public void vElementMouseEvent(VElementMouseEvent e) {
		if (e.getElement() == this) {
			boolean old = mouseover;
			if (e.getType() == VElementMouseEvent.TYPE_MOUSEENTER) {
				mouseover = true;
			} else if (e.getType() == VElementMouseEvent.TYPE_MOUSEEXIT) {
				mouseover = false;
			}
			if (old != mouseover) {
				markDirty();
			}
		}
	}

	public void highlight(Color c) {
		super.highlight(c);
		// Highlight the children
		for (VPathwayObject g : getGroupGraphics()) {
			g.highlight();
		}
	}

	protected Shape calculateVOutline() {
		// Include rotation and stroke
		Area a = new Area(getVShape(true));
		return a;
	}

	protected Shape getVShape(boolean rotate) {
		Rectangle2D mb = null;
		if (rotate) {
			mb = getPathwayElement().getRotatedBounds();
		} else {
			mb = getPathwayElement().getBounds();
		}
		return canvas.vFromM(mb);
	}

	protected void setVScaleRectangle(Rectangle2D r) {
		// TODO Auto-generated method stub

	}

	private LinkProvider linkAnchorDelegate = new DefaultLinkAnchorDelegate(this);
	private boolean anchorsShowing = false;

	public void showLinkAnchors() {
		anchorsShowing = true;
		linkAnchorDelegate.showLinkAnchors();
	}

	public void hideLinkAnchors() {
		anchorsShowing = false;
		linkAnchorDelegate.hideLinkAnchors();
	}

	public LinkAnchor getLinkAnchorAt(Point2D p) {
		return linkAnchorDelegate.getLinkAnchorAt(p);
	}

	@Override
	protected void destroy() {
		super.destroy();
		canvas.removeVElementMouseListener(this);
	}

	/**
	 * Use this to override default linkAnchorDelegate
	 */
	public void setLinkAnchorDelegate(LinkProvider delegate) {
		if (delegate == null)
			throw new NullPointerException("passed illegal null value for delegate");
		linkAnchorDelegate = delegate;
	}

}
