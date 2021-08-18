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
import java.awt.Shape;
import java.net.URL;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.pathvisio.model.type.ConnectorType;
import org.pathvisio.model.type.DataNodeType;
import org.pathvisio.model.type.HAlignType;
import org.pathvisio.model.type.LineStyleType;
import org.pathvisio.model.PathwayModel;
import org.pathvisio.model.graphics.Coordinate;
import org.pathvisio.model.graphics.FontProperty;
import org.pathvisio.model.graphics.LineStyleProperty;
import org.pathvisio.model.graphics.RectProperty;
import org.pathvisio.model.graphics.ShapeStyleProperty;
import org.pathvisio.model.type.ArrowHeadType;
import org.pathvisio.model.State;
import org.pathvisio.model.PathwayElement;
import org.pathvisio.core.util.Resources;
import org.pathvisio.model.Anchor;
import org.pathvisio.model.DataNode;
import org.pathvisio.model.GraphicalLine;
import org.pathvisio.model.Interaction;
import org.pathvisio.model.Label;
import org.pathvisio.model.LinePoint;
import org.pathvisio.model.type.ShapeType;
import org.pathvisio.model.type.VAlignType;
import org.pathvisio.util.ColorUtils;
import org.pathvisio.util.preferences.GlobalPreference;
import org.pathvisio.util.preferences.PreferenceManager;
import org.pathvisio.view.MIMShapes;
import org.pathvisio.view.Template;
import org.pathvisio.model.type.ArrowHeadType;

/**
 * Contains a set of templates, patterns of PathwayElements that can be added to
 * a Pathway, including default values.
 */
public abstract class DefaultTemplates {

	/* Some default colors */
	public final static Color COLOR_DEFAULT = Color.BLACK;
	public final static Color COLOR_METABOLITE = Color.BLUE;
	public final static Color COLOR_PATHWAY = new Color(20, 150, 30);
	public final static Color COLOR_LABEL = Color.DARK_GRAY;
	public final static Color COLOR_TRANSPARENT = ColorUtils.hexToColor("#00000000");

	/* Some default values */
	private static final int FONTSIZE = 12;
	private static final HAlignType HALIGN = HAlignType.CENTER;
	private static final VAlignType VALIGN = VAlignType.MIDDLE;
	private static final LineStyleType LINESTYLETYPE = LineStyleType.SOLID;
	private static final double LINEWIDTH = 1;
	private static final double ROTATION = 0;

	/* Initial sizes */
	private static final double DATANODE_WIDTH = 90; // NB: "DATANODE" used to be named "GENEPRODUCT"
	private static final double DATANODE_HEIGHT = 25;
	private static final double LABEL_WIDTH = 90;
	private static final double LABEL_HEIGHT = 25;
	private static final double LINE_LENGTH = 30;
	private static final double STATE_SIZE = 15;
	private static final double SHAPE_SIZE = 30;
	private static final double CELLCOMP_LENGTH_1 = 100;
	private static final double CELLCOMP_LENGTH_2 = 200;
	private static final double BRACE_HEIGHT = 15;
	private static final double BRACE_WIDTH = 60;

	/* Default Z-order values */
	private static final int Z_ORDER_GROUP = 0x1000;
	private static final int Z_ORDER_DATANODE = 0x8000;
	private static final int Z_ORDER_STATE = 0x8000 + 10;
	private static final int Z_ORDER_LABEL = 0x7000;
	private static final int Z_ORDER_SHAPE = 0x4000;
	private static final int Z_ORDER_LINE = 0x3000;
	private static final int Z_ORDER_DEFAULT = 0x0000; // default order of uninteresting elements.

//	/**
//	 * This sets the object to a suitable default size.
//	 *
//	 * This method is intended to be called right after the object is placed on the
//	 * drawing with a click.
//	 */
//	public void setInitialSize(PathwayElement pathwayElement) {		
//		if (pathwayElement.getClass() == Shape.class) { 
//			if (shapeType == ShapeType.BRACE) {
//				setMWidth(M_INITIAL_BRACE_WIDTH);
//				setMHeight(M_INITIAL_BRACE_HEIGHT);
//			} else if (shapeType == ShapeType.MITOCHONDRIA || lineStyle == LineStyle.DOUBLE) {
//				setMWidth(M_INITIAL_CELLCOMP_WIDTH);
//				setMHeight(M_INITIAL_CELLCOMP_HEIGHT);
//			} else if (shapeType == ShapeType.SARCOPLASMICRETICULUM || shapeType == ShapeType.ENDOPLASMICRETICULUM
//					|| shapeType == ShapeType.GOLGIAPPARATUS) {
//				setMWidth(M_INITIAL_CELLCOMP_HEIGHT);
//				setMHeight(M_INITIAL_CELLCOMP_WIDTH);
//			} else {
//				setMWidth(M_INITIAL_SHAPE_SIZE);
//				setMHeight(M_INITIAL_SHAPE_SIZE);
//			}
//			break;
//		}
//		if (pathwayElement.getClass() == DataNode.class) { 
//			setMWidth(M_INITIAL_GENEPRODUCT_WIDTH);
//			setMHeight(M_INITIAL_GENEPRODUCT_HEIGHT);
//			break;
//			}
//		if (pathwayElement.getClass() == LineElement.class) { 
//			setMEndX(getMStartX() +LINE_LENGTH);
//			setMEndY(getMStartY() +LINE_LENGTH);
//			break;
//		}
//		if (pathwayElement.getClass() == State.class) { 
//			State pathwayElement = ((State) pathwayElement); 
//			pathwayElement.(M_INITIAL_STATE_SIZE);
//			pathwayElement.setHeight(M_INITIAL_STATE_SIZE);
//			break;
//		}}
//}}

	/**
	 * Abstract base for templates that only add a single PathwayElement to a
	 * Pathway
	 */
	static abstract class SingleElementTemplate implements Template {
		PathwayElement lastAdded;

		protected void addElement(PathwayElement e, PathwayModel p) {
			p.addPathwayElement(e);
			lastAdded = e;
		}

		/**
		 * Default implementation returns the view of the last added object
		 */
		public VElement getDragElement(VPathwayModel vp) {
			if (lastAdded != null) {
				Graphics g = vp.getPathwayElementView(lastAdded);
				if (g == null) {
					throw new IllegalArgumentException("Given VPathway doesn't contain last added element");
				}
				return g;
			}
			return null; // No last object
		}

		public String getDescription() {
			return "Draw new " + getName();
		}

		public URL getIconLocation() {
			return Resources.getResourceURL("new" + getName().toLowerCase() + ".gif");
		}

		public void postInsert(PathwayElement[] newElements) {
		}
	}

	/**
	 * Template for adding a Label to a Pathway
	 */
	public static class LabelTemplate extends SingleElementTemplate {

		public PathwayElement[] addElements(PathwayModel p, double mx, double my) {
			RectProperty rectProp = new RectProperty(new Coordinate(mx, my), LABEL_WIDTH, LABEL_HEIGHT);
			FontProperty fontProperty = new FontProperty(COLOR_LABEL, "Arial", false, false, false, false, FONTSIZE,
					HALIGN, VALIGN);
			// TODO borderColor, borderStyle, borderWidth, fillColor, shapeType, zOrder
			ShapeStyleProperty shapeStyleProperty = new ShapeStyleProperty(null, null, LINEWIDTH, null, ShapeType.NONE,
					Z_ORDER_LABEL);
			// rotation = 0
			Label e = new Label(rectProp, fontProperty, shapeStyleProperty, ROTATION, "Label");
			p.addLabel(e);
			lastAdded = e;
			return new PathwayElement[] { e };
		}

		public VElement getDragElement(VPathwayModel vp) {
			return null; // Don't drag label on insert
		}

		public String getName() {
			return "Label";
		}
	}

	/**
	 * Template for adding a DataNode to a Pathway. Pass a DataNodeType upon
	 * creation
	 */
	public static class DataNodeTemplate extends SingleElementTemplate {
		DataNodeType type;

		public DataNodeTemplate(DataNodeType type) {
			this.type = type;
		}

		public PathwayElement[] addElements(PathwayModel p, double mx, double my) {
			// custom default graphics for data nodes
			Color color = COLOR_DEFAULT;
			ShapeType shapeType = ShapeType.RECTANGLE;
			boolean fontWeight = false;
			// Default colors for different types
			if (type.equals(DataNodeType.METABOLITE)) {
				color = COLOR_METABOLITE;
				shapeType = ShapeType.ROUNDED_RECTANGLE; // TODO rectangle?
			} else if (type.equals(DataNodeType.PATHWAY)) {
				color = COLOR_PATHWAY;
				fontWeight = true;
				shapeType = ShapeType.NONE; // TODO rounded rectangle?
			}
			RectProperty rectProp = new RectProperty(new Coordinate(mx, my), DATANODE_WIDTH, DATANODE_HEIGHT);
			// textColor, fontName, fontWeight, fontStyle, fontDecoration, fontStrikethru,
			// fontSize, hAlign, vAlign
			FontProperty fontProperty = new FontProperty(color, "Arial", fontWeight, false, false, false, FONTSIZE,
					HALIGN, VALIGN);
			// borderColor, borderStyle, borderWidth, fillColor, shapeType, zOrder
			ShapeStyleProperty shapeStyleProperty = new ShapeStyleProperty(color, LINESTYLETYPE, LINEWIDTH, Color.WHITE,
					shapeType, Z_ORDER_LABEL);
			// rotation = 0
			DataNode e = new DataNode(rectProp, fontProperty, shapeStyleProperty, ROTATION, type.toString(), type);
			// TODO????
			if (PreferenceManager.getCurrent().getBoolean(GlobalPreference.DATANODES_ROUNDED)) {
				e.getShapeStyleProp().setShapeType(ShapeType.ROUNDED_RECTANGLE);
			}
			p.addDataNode(e);
			lastAdded = e;
			return new PathwayElement[] { e };
		}

		public VElement getDragElement(VPathwayModel vp) {
			VDataNode g = (VDataNode) super.getDragElement(vp);
			return g.handleSE;
		}

		public String getName() {
			return type.toString();
		}
	}

	/**
	 * Template for adding a Shape (including Cellular Compartment Shapes) to a
	 * Pathway. Pass a ShapeType upon creation.
	 */
	public static class ShapeTemplate extends SingleElementTemplate {
		ShapeType type;

		Set<ShapeType> CELL_COMPONENT_SET = new HashSet<>(Arrays.asList(ShapeType.CELL, ShapeType.NUCLEUS,
				ShapeType.ENDOPLASMIC_RETICULUM, ShapeType.GOLGI_APPARATUS, ShapeType.MITOCHONDRIA,
				ShapeType.SARCOPLASMIC_RETICULUM, ShapeType.ORGANELLE, ShapeType.VESICLE));

		public ShapeTemplate(ShapeType type) {
			this.type = type;
		}

		public PathwayElement[] addElements(PathwayModel p, double mx, double my) {
			double width = getInitialSize(type)[0];
			double height = getInitialSize(type)[1];
			LineStyleType borderStyleType = getInitialBorderStyle(type);
			Color color;
			double borderWidth;
			if (CELL_COMPONENT_SET.contains(type)) {
				color = Color.lightGray;
				borderWidth = 3;
			} else {
				color = COLOR_DEFAULT;
				borderWidth = LINEWIDTH;
			}
//			e.setDynamicProperty(CellularComponentType.CELL_COMPONENT_KEY, ccType.toString());
			RectProperty rectProp = new RectProperty(new Coordinate(mx, my), width, height);
			// textColor, fontName, fontWeight, fontStyle, fontDecoration, fontStrikethru,
			// fontSize, hAlign, vAlign
			FontProperty fontProperty = new FontProperty(color, "Arial", false, false, false, false, FONTSIZE, HALIGN,
					VALIGN);
			// borderColor, borderStyle, borderWidth, fillColor, shapeType, zOrder
			ShapeStyleProperty shapeStyleProperty = new ShapeStyleProperty(color, borderStyleType, borderWidth,
					Color.WHITE, type, Z_ORDER_LABEL);
			org.pathvisio.model.Shape e = new org.pathvisio.model.Shape(rectProp, fontProperty, shapeStyleProperty,
					ROTATION);
			p.addShape(e);
			lastAdded = e;
			// brace
//			gdata.setOrientation(OrientationType.RIGHT);
			return new PathwayElement[] { e };
		}

		public VElement getDragElement(VPathwayModel vp) {
			GraphicsShape s = (GraphicsShape) super.getDragElement(vp);
			return s.handleSE;
		}

		public String getName() {
			return type.toString();
		}

		public double[] getInitialSize(ShapeType type) {
			if (type.equals(ShapeType.BRACE)) {
				return new double[] { BRACE_WIDTH, BRACE_HEIGHT };
			} else if (type.equals(ShapeType.MITOCHONDRIA) || type.equals(ShapeType.CELL)
					|| type.equals(ShapeType.NUCLEUS) || type.equals(ShapeType.ORGANELLE)) {
				return new double[] { CELLCOMP_LENGTH_2, CELLCOMP_LENGTH_1 };
			} else if (type.equals(ShapeType.SARCOPLASMIC_RETICULUM) || type.equals(ShapeType.ENDOPLASMIC_RETICULUM)
					|| type.equals(ShapeType.GOLGI_APPARATUS)) {
				return new double[] { CELLCOMP_LENGTH_1, CELLCOMP_LENGTH_2 };
			} else {
				return new double[] { SHAPE_SIZE, SHAPE_SIZE };
			}
		}

		public LineStyleType getInitialBorderStyle(ShapeType type) {
			// set borderStyle depending on shape type
			if (type.equals(ShapeType.CELL) || type.equals(ShapeType.NUCLEUS) || type.equals(ShapeType.ORGANELLE)) {
				return LineStyleType.DOUBLE;
			} else if (type.equals(ShapeType.CYTOSOL) || type.equals(ShapeType.EXTRACELLULAR)
					|| type.equals(ShapeType.MEMBRANE)) {
				return LineStyleType.DASHED; // TODO membrane/cytosol never implemented?
			} else {
				return LINESTYLETYPE; // solid
			}
		}
	}

	/**
	 * Template for adding an Interaction line to a Pathway.
	 */
	public static class InteractionTemplate extends SingleElementTemplate {
		LineStyleType style;
		ArrowHeadType startType;
		ArrowHeadType endType;
		ConnectorType connectorType;
		String name;

		public InteractionTemplate(String name, LineStyleType style, ArrowHeadType startType, ArrowHeadType endType,
				ConnectorType connectorType) {
			this.style = style;
			this.startType = startType;
			this.endType = endType;
			this.connectorType = connectorType;
			this.name = name;
		}

		public PathwayElement[] addElements(PathwayModel p, double mx, double my) {
			LinePoint startLinePoint = new LinePoint(startType, new Coordinate(mx, my));
			LinePoint endLinePoint = new LinePoint(endType, new Coordinate(mx, my));
			// lineColor, lineStyle, lineWidth, connectorType
			LineStyleProperty lineStyleProp = new LineStyleProperty(Color.BLACK, style, LINEWIDTH, connectorType);
			Interaction e = new Interaction(lineStyleProp);
			e.addLinePoint(startLinePoint);
			e.addLinePoint(endLinePoint);
			p.addInteraction(e);
			lastAdded = e;
			return new PathwayElement[] { e };
		}

		public VElement getDragElement(VPathwayModel vp) {
			VLine l = (VLine) super.getDragElement(vp);
			return l.getEnd().getHandle();
		}

		public String getName() {
			return name;
		}
	}

	/**
	 * Template for adding a Graphical line to a Pathway.
	 */
	public static class GraphicalLineTemplate extends SingleElementTemplate {
		LineStyleType style;
		ArrowHeadType startType;
		ArrowHeadType endType;
		ConnectorType connectorType;
		String name;

		public GraphicalLineTemplate(String name, LineStyleType style, ArrowHeadType startType, ArrowHeadType endType,
				ConnectorType connectorType) {
			this.style = style;
			this.startType = startType;
			this.endType = endType;
			this.connectorType = connectorType;
			this.name = name;
		}

		public PathwayElement[] addElements(PathwayModel p, double mx, double my) {
			LinePoint startLinePoint = new LinePoint(startType, new Coordinate(mx, my));
			LinePoint endLinePoint = new LinePoint(endType, new Coordinate(mx, my));
			// lineColor, lineStyle, lineWidth, connectorType
			LineStyleProperty lineStyleProp = new LineStyleProperty(Color.BLACK, style, LINEWIDTH, connectorType);
			GraphicalLine e = new GraphicalLine(lineStyleProp);
			e.addLinePoint(startLinePoint);
			e.addLinePoint(endLinePoint);
			p.addGraphicalLine(e);
			lastAdded = e;
			return new PathwayElement[] { e };
		}

		public VElement getDragElement(VPathwayModel vp) {
			VLine l = (VLine) super.getDragElement(vp);
			return l.getEnd().getHandle();
		}

		public String getName() {
			return name;
		}
	}

	/**
	 * Template for an interaction, two datanodes with a connecting line.
	 */
	public static class InteractionDataNodeTemplate implements Template {
		final static int OFFSET_LINE = 5;
		DataNode lastStartNode;
		DataNode lastEndNode;
		Interaction lastLine;

		ArrowHeadType endType;
		ArrowHeadType startType;

		LineStyleType lineStyle;

		public InteractionDataNodeTemplate() {
			endType = ArrowHeadType.UNDIRECTED;
			startType = ArrowHeadType.UNDIRECTED;
			lineStyle = LineStyleType.SOLID;
		}

		public PathwayElement[] addElements(PathwayModel p, double mx, double my) {
			// Add two GeneProduct DataNodes, connected by a line
			Template dnt = new DataNodeTemplate(DataNodeType.GENEPRODUCT);
			lastStartNode = (DataNode) dnt.addElements(p, mx, my)[0];
//			lastStartNode.setInitialSize();
			lastEndNode = (DataNode) dnt.addElements(p, mx + 2 * lastStartNode.getRectProp().getWidth(), my)[0];
//			lastEndNode.setInitialSize();

			Template lnt = new InteractionTemplate("defaultline", lineStyle, startType, endType,
					ConnectorType.STRAIGHT);
			lastLine = (Interaction) lnt.addElements(p, mx, my)[0];
			lastLine.getStartLinePoint().linkTo(lastStartNode, 1, 0);
			lastLine.getEndLinePoint().linkTo(lastEndNode, -1, 0);

			return new PathwayElement[] { lastLine, lastStartNode, lastEndNode };
		}

		public VElement getDragElement(VPathwayModel vp) {
			return null;
		}

		public String getName() {
			return "interaction";
		}

		public String getDescription() {
			return "Draw new " + getName();
		}

		public URL getIconLocation() {
			return Resources.getResourceURL("new" + getName().toLowerCase() + ".gif");
		}

		public void postInsert(PathwayElement[] newElements) {
		}
	}

	/**
	 * Template for an inhibition interaction, two datanodes with a MIM_INHIBITION
	 * line.
	 */
	public static class InhibitionInteractionTemplate extends InteractionTemplate {
		@Override
		public PathwayElement[] addElements(PathwayModel p, double mx, double my) {
			super.addElements(p, mx, my);
			lastLine.setEndLineType(MIMShapes.MIM_INHIBITION);
			return new PathwayElement[] { lastLine, lastStartNode, lastEndNode };
		}

		@Override
		public String getName() {
			return "inhibition interaction";
		}
	}

	/**
	 * Template for a stimulation interaction, two datanodes with a MIM_STIMULATION
	 * line.
	 */
	public static class StimulationInteractionTemplate extends InteractionTemplate {
		@Override
		public PathwayElement[] addElements(PathwayModel p, double mx, double my) {
			super.addElements(p, mx, my);
			lastLine.setEndLineType(MIMShapes.MIM_STIMULATION);
			return new PathwayElement[] { lastLine, lastStartNode, lastEndNode };
		}

		@Override
		public String getName() {
			return "stimulation interaction";
		}
	}

	/**
	 * Template for a phosphorylation interaction, two Protein Datanodes with a
	 * MIM_MODIFICATION line.
	 */

	public static class PhosphorylationTemplate extends InteractionTemplate {
		// static final double OFFSET_CATALYST = 50;
		PathwayElement lastPhosphorylation;
		// PathwayElement lastPhosLine;

		public PathwayElement[] addElements(PathwayModel p, double mx, double my) {
			super.addElements(p, mx, my);
			lastStartNode.setDataNodeType(DataNodeType.PROTEIN);
			lastEndNode.setDataNodeType(DataNodeType.PROTEIN);
			lastStartNode.setTextLabel("Protein");
			lastEndNode.setTextLabel("P-Protein");
			lastLine.setEndLineType(MIMShapes.MIM_MODIFICATION);

			PathwayElement elt = PathwayElement.createPathwayElement(ObjectType.STATE);
			elt.setInitialSize();
			elt.setTextLabel("P");
			((MState) elt).linkTo(lastEndNode, 1.0, 1.0);
			elt.setShapeType(ShapeType.OVAL);
			p.add(elt);
			elt.setGeneratedElementId();

			return new PathwayElement[] { lastStartNode, lastEndNode, lastLine };
		}

		public String getName() {
			return "Phosphorylation";
		}
	}

	/**
	 * Template for a reaction, two Metabolites with a connecting arrow, and a
	 * GeneProduct (enzyme) pointing to an anchor on that arrow.
	 */
	public static class ReactionTemplate extends InteractionTemplate {
		static final double OFFSET_CATALYST = 50;
		PathwayElement lastCatalyst;
		PathwayElement lastCatLine;

		public PathwayElement[] addElements(PathwayModel p, double mx, double my) {
			super.addElements(p, mx, my);
			Template dnt = new DataNodeTemplate(DataNodeType.GENEPRODUCT);
			lastCatalyst = dnt.addElements(p, mx + lastStartNode.getMWidth(), my - OFFSET_CATALYST)[0];
			lastCatalyst.setInitialSize();
			lastCatalyst.setTextLabel("Catalyst");

			lastStartNode.setDataNodeType(DataNodeType.METABOLITE);
			lastStartNode.setColor(COLOR_METABOLITE);
			lastStartNode.setShapeType(ShapeType.ROUNDED_RECTANGLE);
			lastStartNode.setTextLabel("Substrate");

			lastEndNode.setDataNodeType(DataNodeType.METABOLITE);
			lastEndNode.setColor(COLOR_METABOLITE);
			lastEndNode.setShapeType(ShapeType.ROUNDED_RECTANGLE);
			lastEndNode.setTextLabel("Product");

			lastLine.setEndLineType(MIMShapes.MIM_CONVERSION);
			MAnchor anchor = lastLine.addMAnchor(0.5);

			Template lnt = new InteractionTemplate("line", LineStyleType.SOLID, ArrowHeadType.UNDIRECTED,
					ArrowHeadType.UNDIRECTED, ConnectorType.STRAIGHT);
			lastCatLine = lnt.addElements(p, mx, my)[0];

			lastCatLine.getMStart().linkTo(lastCatalyst, 0, 1);
			lastCatLine.getMEnd().linkTo(anchor, 0, 0);
			lastCatLine.setEndLineType(MIMShapes.MIM_CATALYSIS);

			return new PathwayElement[] { lastStartNode, lastEndNode, lastLine, lastCatalyst };
		}

		public String getName() {
			return "reaction";
		}
	}

	/**
	 * Template for a reaction, two Metabolites with a connecting arrow, and a
	 * GeneProduct (enzyme) pointing to an anchor on that arrow.
	 */
	public static class ReversibleReactionTemplate extends InteractionTemplate {
		static final double OFFSET_CATALYST = 50;
		PathwayElement lastCatalyst;
		PathwayElement lastCatalyst2;
		PathwayElement lastCatLine;
		PathwayElement lastCatLine2;
		PathwayElement lastReverseLine;

		public PathwayElement[] addElements(PathwayModel p, double mx, double my) {
			super.addElements(p, mx, my);
			Template dnt = new DataNodeTemplate(DataNodeType.PROTEIN);
			lastCatalyst = dnt.addElements(p, mx + lastStartNode.getMWidth(), my - OFFSET_CATALYST)[0];
			lastCatalyst.setInitialSize();
			lastCatalyst.setTextLabel("Catalyst 1");

			lastCatalyst2 = dnt.addElements(p, mx + lastStartNode.getMWidth(), my + OFFSET_CATALYST)[0];
			lastCatalyst2.setInitialSize();
			lastCatalyst2.setTextLabel("Catalyst 2");

			lastStartNode.setDataNodeType(DataNodeType.METABOLITE);
			lastStartNode.setColor(COLOR_METABOLITE);
			lastStartNode.setShapeType(ShapeType.ROUNDED_RECTANGLE);
			lastStartNode.setTextLabel("Metabolite 1");

			lastEndNode.setDataNodeType(DataNodeType.METABOLITE);
			lastEndNode.setColor(COLOR_METABOLITE);
			lastEndNode.setShapeType(ShapeType.ROUNDED_RECTANGLE);
			lastEndNode.setTextLabel("Metabolite 2");
			lastLine.setEndLineType(MIMShapes.MIM_CONVERSION);

			Anchor anchor = lastLine.addAnchor(0.5);

			Template lnt = new InteractionTemplate("line", LineStyleType.SOLID, ArrowHeadType.UNDIRECTED,
					ArrowHeadType.UNDIRECTED, ConnectorType.STRAIGHT);
			lastCatLine = lnt.addElements(p, mx, my)[0];

			lastCatLine.getMStart().linkTo(lastCatalyst, 0, 1);
			lastCatLine.getMEnd().linkTo(anchor, 0, 0);
			lastCatLine.setEndLineType(MIMShapes.MIM_CATALYSIS);

			Template rev = new InteractionTemplate("line", LineStyleType.SOLID, ArrowHeadType.UNDIRECTED,
					ArrowHeadType.UNDIRECTED, ConnectorType.STRAIGHT);
			lastReverseLine = rev.addElements(p, mx, my)[0];

			lastReverseLine.getMStart().linkTo(lastEndNode, -1, 0.5);
			lastReverseLine.getMEnd().linkTo(lastStartNode, 1, 0.5);
			lastReverseLine.setEndLineType(MIMShapes.MIM_CONVERSION);

			Anchor anchor2 = lastReverseLine.addAnchor(0.5);

			Template lnt2 = new InteractionTemplate("line", LineStyleType.SOLID, ArrowHeadType.UNDIRECTED,
					ArrowHeadType.UNDIRECTED, ConnectorType.STRAIGHT);
			lastCatLine2 = lnt2.addElements(p, mx, my)[0];

			lastCatLine2.getMStart().linkTo(lastCatalyst2, 0, -1);
			lastCatLine2.getMEnd().linkTo(anchor2, 0, 0);
			lastCatLine2.setEndLineType(MIMShapes.MIM_CATALYSIS);

			return new PathwayElement[] { lastStartNode, lastEndNode, lastLine, lastCatalyst, lastCatalyst2 }; // These
																												// elements
			// are
			// selected
			// in
			// PV,
			// so
			// users
			// can
			// move
			// them
			// around.
		}

		public String getName() {
			return "ReversibleReaction";
		}
	}

}
