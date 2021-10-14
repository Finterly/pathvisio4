package org.pathvisio.view.model.shape;

import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.Arc2D;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;
import java.util.Map;
import java.util.TreeMap;

import org.pathvisio.model.type.ShapeType;
import org.pathvisio.view.model.shape.GenMAPPShapes.Internal;

/**
 * 
 * @author finterly
 */
public abstract class VShapeTypeRegistry {

	public static Map<ShapeType, Shape> shapeTypeToShape = new TreeMap<ShapeType, Shape>();
	static {
		// Basic shapes
		shapeTypeToShape.put(ShapeType.NONE, null);
		shapeTypeToShape.put(ShapeType.RECTANGLE, new Rectangle(0, 0, 10, 10));
		shapeTypeToShape.put(ShapeType.ROUNDED_RECTANGLE, null); // ROUNDED RECTANGLE TODO
//		ROUNDED_RECTANGLE(ShapeType.ROUNDED_RECTANGLE, null) {
//			public Shape getShape(double mw, double mh) {
//				return new RoundRectangle2D.Double(0, 0, mw, mh, 20, 20);
//			}
//		},
		shapeTypeToShape.put(ShapeType.OVAL, new Ellipse2D.Double(0, 0, 10, 10));
		shapeTypeToShape.put(ShapeType.TRIANGLE, GenMAPPShapes.getRegularPolygon(3, 10, 10));
		shapeTypeToShape.put(ShapeType.PENTAGON, GenMAPPShapes.getRegularPolygon(5, 10, 10));
		shapeTypeToShape.put(ShapeType.HEXAGON, GenMAPPShapes.getRegularPolygon(6, 10, 10));
		shapeTypeToShape.put(ShapeType.OCTAGON, GenMAPPShapes.getRegularPolygon(8, 10, 10));

		// Basic line shapes
		shapeTypeToShape.put(ShapeType.EDGE, new Line2D.Double(0, 0, 10, 10));
		shapeTypeToShape.put(ShapeType.ARC, new Arc2D.Double(0, 0, 10, 10, 0, -180, Arc2D.OPEN));
		shapeTypeToShape.put(ShapeType.BRACE, GenMAPPShapes.getPluggableShape(Internal.BRACE));

		// Cellular components with special shape
		shapeTypeToShape.put(ShapeType.MITOCHONDRIA, GenMAPPShapes.getPluggableShape(Internal.MITOCHONDRIA));
		shapeTypeToShape.put(ShapeType.SARCOPLASMIC_RETICULUM,
				GenMAPPShapes.getPluggableShape(Internal.SARCOPLASMICRETICULUM));
		shapeTypeToShape.put(ShapeType.ENDOPLASMIC_RETICULUM,
				GenMAPPShapes.getPluggableShape(Internal.ENDOPLASMICRETICULUM));
		shapeTypeToShape.put(ShapeType.GOLGI_APPARATUS, GenMAPPShapes.getPluggableShape(Internal.GOLGIAPPARATUS));

		// Cellular components (rarely used)
		shapeTypeToShape.put(ShapeType.NUCLEOLUS, null);
		shapeTypeToShape.put(ShapeType.VACUOLE, null);
		shapeTypeToShape.put(ShapeType.LYSOSOME, null);
		shapeTypeToShape.put(ShapeType.CYTOSOL, null);

		// Cellular components with basic shapes
		shapeTypeToShape.put(ShapeType.EXTRACELLULAR, null); // Rounded rectangle
		shapeTypeToShape.put(ShapeType.CELL, null); // Rounded rectangle
		shapeTypeToShape.put(ShapeType.NUCLEUS, new Ellipse2D.Double(0, 0, 10, 10)); // Oval
		shapeTypeToShape.put(ShapeType.ORGANELLE, null); // Rounded rectangle
		shapeTypeToShape.put(ShapeType.VESICLE, new Ellipse2D.Double(0, 0, 10, 10));// Oval

		// Deprecated since GPML2013a?
		shapeTypeToShape.put(ShapeType.MEMBRANE, null); // Rounded rectangle
		shapeTypeToShape.put(ShapeType.CELLA, null); // Oval
		shapeTypeToShape.put(ShapeType.RIBOSOME, null); // Hexagon
		shapeTypeToShape.put(ShapeType.ORGANA, null); // Oval
		shapeTypeToShape.put(ShapeType.ORGANB, null); // Oval
		shapeTypeToShape.put(ShapeType.ORGANC, null); // Oval
		shapeTypeToShape.put(ShapeType.PROTEINB, null); // Hexagon

		// Special Shapes //TODO
		shapeTypeToShape.put(ShapeType.register("Coronavirus"), GenMAPPShapes.getPluggableShape(Internal.CORONAVIRUS));
	}

	public void addShape(ShapeType shapeType, Shape shape) {

	}

	public static Shape getShape(ShapeType shapeType, double mw, double mh) {
		Shape shape = shapeTypeToShape.get(shapeType);
		// now scale the path so it has proper w and h.
		Rectangle r = shape.getBounds();
		AffineTransform at = new AffineTransform();
		at.translate(-r.x, -r.y);
		at.scale(mw / r.width, mh / r.height);
		return at.createTransformedShape(shape);
	}

}
