package org.pathvisio.view.model.shape;

import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.Arc2D;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;
import java.awt.geom.RoundRectangle2D;

import org.pathvisio.model.type.ShapeType;
import org.pathvisio.view.model.shape.GenMAPPShapes.Internal;

/**
 * This class stores {@link ShapeType} and their corresponding view shape types.
 * 
 * @author p70073399
 *
 */
public enum VShapeType {

	// Basic shapes
	NONE(ShapeType.NONE, null), RECTANGLE(ShapeType.RECTANGLE, new Rectangle(0, 0, 10, 10)),
	ROUNDED_RECTANGLE(ShapeType.ROUNDED_RECTANGLE, null) {
		public Shape getShape(double mw, double mh) {
			return new RoundRectangle2D.Double(0, 0, mw, mh, 20, 20);
		}
	},
	OVAL(ShapeType.OVAL, new Ellipse2D.Double(0, 0, 10, 10)),
	TRIANGLE(ShapeType.TRIANGLE, GenMAPPShapes.getRegularPolygon(3, 10, 10)), // poly in MAPP
	PENTAGON(ShapeType.PENTAGON, GenMAPPShapes.getRegularPolygon(5, 10, 10)), // poly in MAPP
	HEXAGON(ShapeType.HEXAGON, GenMAPPShapes.getRegularPolygon(6, 10, 10)), // poly in MAPP
	OCTAGON(ShapeType.OCTAGON, GenMAPPShapes.getRegularPolygon(8, 10, 10)), // poly in MAPP TODO

	// Basic line shapes
	EDGE(ShapeType.EDGE, new Line2D.Double(0, 0, 10, 10)),
	ARC(ShapeType.ARC, new Arc2D.Double(0, 0, 10, 10, 0, -180, Arc2D.OPEN)),
	BRACE(ShapeType.BRACE, GenMAPPShapes.getPluggableShape(Internal.BRACE)),

	// Cellular components with special shape
	MITOCHONDRIA(ShapeType.MITOCHONDRIA, GenMAPPShapes.getPluggableShape(Internal.MITOCHONDRIA)),
	SARCOPLASMICRETICULUM(ShapeType.SARCOPLASMIC_RETICULUM,
			GenMAPPShapes.getPluggableShape(Internal.SARCOPLASMICRETICULUM)),
	ENDOPLASMICRETICULUM(ShapeType.ENDOPLASMIC_RETICULUM,
			GenMAPPShapes.getPluggableShape(Internal.ENDOPLASMICRETICULUM)),
	GOLGIAPPARATUS(ShapeType.GOLGI_APPARATUS, GenMAPPShapes.getPluggableShape(Internal.GOLGIAPPARATUS)),

	// Cellular components (rarely used)
	NUCLEOLUS(ShapeType.NUCLEOLUS, null), VACUOLE(ShapeType.VACUOLE, null), LYSOSOME(ShapeType.LYSOSOME, null),
	CYTOSOL(ShapeType.CYTOSOL, null),

	// Cellular components with basic shapes
	EXTRACELLULAR(ShapeType.EXTRACELLULAR, null) { // Rounded rectangle
		public Shape getShape(double mw, double mh) {
			return new RoundRectangle2D.Double(0, 0, mw, mh, 20, 20);
		}
	},
	CELL(ShapeType.CELL, null) { // Rounded rectangle
		public Shape getShape(double mw, double mh) {
			return new RoundRectangle2D.Double(0, 0, mw, mh, 20, 20);
		}
	},
	NUCLEUS(ShapeType.NUCLEUS, new Ellipse2D.Double(0, 0, 10, 10)), // Oval
	ORGANELLE(ShapeType.ORGANELLE, null) { // Rounded rectangle
		public Shape getShape(double mw, double mh) {
			return new RoundRectangle2D.Double(0, 0, mw, mh, 20, 20);
		}
	},
	VESICLE(ShapeType.VESICLE, new Ellipse2D.Double(0, 0, 10, 10)), // Oval

	// Deprecated since GPML2013a?
	MEMBRANE(ShapeType.MEMBRANE, null), // Rounded rectangle
	CELLA(ShapeType.CELLA, null), // Oval
	RIBOSOME(ShapeType.RIBOSOME, null), // Hexagon
	ORGANA(ShapeType.ORGANA, null), // Oval
	ORGANB(ShapeType.ORGANB, null), // Oval
	ORGANC(ShapeType.ORGANC, null), // Oval
	PROTEINB(ShapeType.PROTEINB, null), // Hexagon

	// Special Shape //TODO 
	CORONAVIRUS(ShapeType.register("Coronavirus"), GenMAPPShapes.getPluggableShape(Internal.ENDOPLASMICRETICULUM));

	private final ShapeType shapeType;
	private final Shape shape;

	private VShapeType(ShapeType shapeType, Shape shape) {
		this.shapeType = shapeType;
		this.shape = shape;
	}

	public Shape getVShapeType(ShapeType shapeType) {

	}

	public Shape getShape(double mw, double mh) {
		// now scale the path so it has proper w and h.
		Rectangle r = shape.getBounds();
		AffineTransform at = new AffineTransform();
		at.translate(-r.x, -r.y);
		at.scale(mw / r.width, mh / r.height);
		return at.createTransformedShape(shape);
	}

}
