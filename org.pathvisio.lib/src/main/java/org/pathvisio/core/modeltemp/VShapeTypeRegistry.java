package org.pathvisio.core.modeltemp;

import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.Arc2D;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;
import java.awt.geom.RoundRectangle2D;
import java.util.Map;
import java.util.TreeMap;

import org.pathvisio.model.type.ShapeType;
import org.pathvisio.view.model.shape.VShapeTypeCatalog.Internal;

/**
 * 
 * @author finterly
 */
public abstract class VShapeTypeRegistry {

	public static Map<String, Shape> shapeMap = new TreeMap<String, Shape>(String.CASE_INSENSITIVE_ORDER);
	
	private static Shape rectangle = new Rectangle(0, 0, 10, 10);
	private static Shape roundedRectangle = new RoundRectangle2D.Double(0, 0, 10, 10, 20, 20);
	private static Shape ellipse = new Ellipse2D.Double(0, 0, 10, 10);

	static {
		// Basic shapes
		shapeMap.put(ShapeType.NONE.getName(), null);
		shapeMap.put(ShapeType.RECTANGLE.getName(), rectangle);
		shapeMap.put(ShapeType.ROUNDED_RECTANGLE.getName(), roundedRectangle);
		shapeMap.put(ShapeType.OVAL.getName(), ellipse);
		shapeMap.put(ShapeType.TRIANGLE.getName(), VShapeTypeCatalog.getRegularPolygon(3, 10, 10));
		shapeMap.put(ShapeType.PENTAGON.getName(), VShapeTypeCatalog.getRegularPolygon(5, 10, 10));
		shapeMap.put(ShapeType.HEXAGON.getName(), VShapeTypeCatalog.getRegularPolygon(6, 10, 10));
		shapeMap.put(ShapeType.OCTAGON.getName(), VShapeTypeCatalog.getRegularPolygon(8, 10, 10));

		// Basic line shapes
		shapeMap.put(ShapeType.EDGE.getName(), new Line2D.Double(0, 0, 10, 10));
		shapeMap.put(ShapeType.ARC.getName(), new Arc2D.Double(0, 0, 10, 10, 0, -180, Arc2D.OPEN));
		shapeMap.put(ShapeType.BRACE.getName(), VShapeTypeCatalog.getPluggableShape(Internal.BRACE));

		// Cellular components with special shape
		shapeMap.put(ShapeType.MITOCHONDRIA.getName(), VShapeTypeCatalog.getPluggableShape(Internal.MITOCHONDRIA));
		shapeMap.put(ShapeType.SARCOPLASMIC_RETICULUM.getName(),
				VShapeTypeCatalog.getPluggableShape(Internal.SARCOPLASMICRETICULUM));
		shapeMap.put(ShapeType.ENDOPLASMIC_RETICULUM.getName(),
				VShapeTypeCatalog.getPluggableShape(Internal.ENDOPLASMICRETICULUM));
		shapeMap.put(ShapeType.GOLGI_APPARATUS.getName(), VShapeTypeCatalog.getPluggableShape(Internal.GOLGIAPPARATUS));

		// Cellular components (rarely used)
		shapeMap.put(ShapeType.NUCLEOLUS.getName(), null);
		shapeMap.put(ShapeType.VACUOLE.getName(), null);
		shapeMap.put(ShapeType.LYSOSOME.getName(), null);
		shapeMap.put(ShapeType.CYTOSOL.getName(), null);

		// Cellular components with basic shapes
		shapeMap.put(ShapeType.EXTRACELLULAR.getName(), roundedRectangle); // Rounded rectangle
		shapeMap.put(ShapeType.CELL.getName(), roundedRectangle); // Rounded rectangle
		shapeMap.put(ShapeType.NUCLEUS.getName(), ellipse); // Oval
		shapeMap.put(ShapeType.ORGANELLE.getName(), roundedRectangle); // Rounded rectangle
		shapeMap.put(ShapeType.VESICLE.getName(), ellipse);// Oval

		// Deprecated since GPML2013a? TODO 
		shapeMap.put(ShapeType.MEMBRANE.getName(), null); // Rounded rectangle
		shapeMap.put(ShapeType.CELLA.getName(), null); // Oval
		shapeMap.put(ShapeType.RIBOSOME.getName(), null); // Hexagon
		shapeMap.put(ShapeType.ORGANA.getName(), null); // Oval
		shapeMap.put(ShapeType.ORGANB.getName(), null); // Oval
		shapeMap.put(ShapeType.ORGANC.getName(), null); // Oval
		shapeMap.put(ShapeType.PROTEINB.getName(), null); // Hexagon

		// Special Shapes //TODO
		shapeMap.put("Coronavirus", VShapeTypeCatalog.getPluggableShape(Internal.CORONAVIRUS));
	}

	
	/**
	 * Looks up the {@link ShapeType} corresponding to the string name.
	 */
	public static ShapeType getShapeType(String value) {
		return ShapeType.fromName(value);
	}

	
	public static Shape getShape(String shapeType, double mw, double mh) {
		Shape shape = shapeMap.get(shapeType);
		// now scale the path so it has proper w and h.
		Rectangle r = shape.getBounds();
		AffineTransform at = new AffineTransform();
		at.translate(-r.x, -r.y);
		at.scale(mw / r.width, mh / r.height);
		return at.createTransformedShape(shape);
	}
	
	public void addShape(ShapeType shapeType, Shape shape) {

	}
	



}
