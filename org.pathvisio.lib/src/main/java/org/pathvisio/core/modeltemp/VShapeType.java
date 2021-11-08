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

import org.pathvisio.debug.Logger;
import org.pathvisio.model.type.ShapeType;
import org.pathvisio.view.model.shape.VShapeTypeCatalog.Internal;

/**
 * 
 * @author finterly
 */
public class VShapeType {

	static void registerShapes() {

		// register arrow heads
		VShapeRegistry.registerShape(ShapeType.NONE.getName(), null);
		VShapeRegistry.registerShape("TBar", getTBar(), VArrowHeadType.FillType.OPEN, TBARWIDTH + TBAR_GAP);
		VShapeRegistry.registerShape("LigandRound", getLRound(), VArrowHeadType.FillType.CLOSED);
		VShapeRegistry.registerShape("ReceptorRound", getRRound(), VArrowHeadType.FillType.WIRE);
		VShapeRegistry.registerShape("Receptor", getReceptor(), VArrowHeadType.FillType.WIRE);
		VShapeRegistry.registerShape("ReceptorSquare", getReceptorSquare(), VArrowHeadType.FillType.WIRE);
		VShapeRegistry.registerShape("LigandSquare", getLigand(), VArrowHeadType.FillType.CLOSED);
	}

	public static final Map<String, VShapeType> shapeMap = new TreeMap<String, VShapeType>(
			String.CASE_INSENSITIVE_ORDER);

	private static Shape rectangle = new Rectangle(0, 0, 10, 10);
	private static Shape roundedRectangle = new RoundRectangle2D.Double(0, 0, 10, 10, 20, 20);
	private static Shape ellipse = new Ellipse2D.Double(0, 0, 10, 10);

	// Basic shapes
	public static final VShapeType NONE = new VShapeType(ShapeType.NONE.getName(), null);
	public static final VShapeType RECTANGLE = new VShapeType(ShapeType.RECTANGLE.getName(), rectangle);
	public static final VShapeType ROUNDED_RECTANGLE = new VShapeType(ShapeType.ROUNDED_RECTANGLE.getName(),
			roundedRectangle);
	public static final VShapeType OVAL = new VShapeType(ShapeType.OVAL.getName(), ellipse);
	public static final VShapeType TRIANGLE = new VShapeType(ShapeType.TRIANGLE.getName(),
			VShapeTypeCatalog.getRegularPolygon(3, 10, 10));
	public static final VShapeType PENTAGON = new VShapeType(ShapeType.PENTAGON.getName(),
			VShapeTypeCatalog.getRegularPolygon(5, 10, 10));
	public static final VShapeType HEXAGON = new VShapeType(ShapeType.HEXAGON.getName(),
			VShapeTypeCatalog.getRegularPolygon(6, 10, 10));
	public static final VShapeType OCTAGON = new VShapeType(ShapeType.OCTAGON.getName(),
			VShapeTypeCatalog.getRegularPolygon(8, 10, 10));

	// Basic line shapes
	public static final VShapeType EDGE = new VShapeType(ShapeType.EDGE.getName(), new Line2D.Double(0, 0, 10, 10));
	public static final VShapeType ARC = new VShapeType(ShapeType.ARC.getName(),
			new Arc2D.Double(0, 0, 10, 10, 0, -180, Arc2D.OPEN));
	public static final VShapeType BRACE = new VShapeType(ShapeType.BRACE.getName(),
			VShapeTypeCatalog.getPluggableShape(Internal.BRACE));

	// Cellular components with special shape
	public static final VShapeType MITOCHONDRIA = new VShapeType(ShapeType.MITOCHONDRIA.getName(),
			VShapeTypeCatalog.getPluggableShape(Internal.MITOCHONDRIA));
	public static final VShapeType SARCOPLASMIC_RETICULUM = new VShapeType(ShapeType.SARCOPLASMIC_RETICULUM.getName(),
			VShapeTypeCatalog.getPluggableShape(Internal.SARCOPLASMICRETICULUM));
	public static final VShapeType ENDOPLASMIC_RETICULUM = new VShapeType(ShapeType.ENDOPLASMIC_RETICULUM.getName(),
			VShapeTypeCatalog.getPluggableShape(Internal.ENDOPLASMICRETICULUM));
	public static final VShapeType GOLGI_APPARATUS = new VShapeType(ShapeType.GOLGI_APPARATUS.getName(),
			VShapeTypeCatalog.getPluggableShape(Internal.GOLGIAPPARATUS));

	// Cellular components (rarely used)
	public static final VShapeType NUCLEOLUS = new VShapeType(ShapeType.NUCLEOLUS.getName(), null);
	public static final VShapeType VACUOLE = new VShapeType(ShapeType.VACUOLE.getName(), null);
	public static final VShapeType LYSOSOME = new VShapeType(ShapeType.LYSOSOME.getName(), null);
	public static final VShapeType CYTOSOL = new VShapeType(ShapeType.CYTOSOL.getName(), null);

	// Cellular components with basic shape
	public static final VShapeType EXTRACELLULAR = new VShapeType(ShapeType.EXTRACELLULAR.getName(), roundedRectangle);
	public static final VShapeType CELL = new VShapeType(ShapeType.CELL.getName(), roundedRectangle); // Rounded
																										// rectangle
	public static final VShapeType NUCLEUS = new VShapeType(ShapeType.NUCLEUS.getName(), ellipse); // Oval
	public static final VShapeType ORGANELLE = new VShapeType(ShapeType.ORGANELLE.getName(), roundedRectangle); // Rounded
	// rectangle
	public static final VShapeType VESICLE = new VShapeType(ShapeType.VESICLE.getName(), ellipse); // Oval

	// Deprecated since GPML2013a?
	public static final VShapeType MEMBRANE = new VShapeType(ShapeType.MEMBRANE.getName(), null); // Rounded rectangle
	public static final VShapeType CELLA = new VShapeType(ShapeType.CELLA.getName(), null); // Oval
	public static final VShapeType RIBOSOME = new VShapeType(ShapeType.RIBOSOME.getName(), null); // Hexagon
	public static final VShapeType ORGANA = new VShapeType(ShapeType.ORGANA.getName(), null); // Oval
	public static final VShapeType ORGANB = new VShapeType(ShapeType.ORGANB.getName(), null); // Oval
	public static final VShapeType ORGANC = new VShapeType(ShapeType.ORGANC.getName(), null); // Oval
	public static final VShapeType PROTEINB = new VShapeType(ShapeType.PROTEINB.getName(), null); // Hexagon

	// Special Shapes //TODO
	public static final VShapeType CORONAVIRUS = new VShapeType("Coronavirus",
			VShapeTypeCatalog.getPluggableShape(Internal.CORONAVIRUS));

	private String shapeTypeName;
	private Shape shape;
	private final boolean isResizeable;
	private final boolean isRotatable;

	/**
	 * Constructor for VShapeType
	 * 
	 * @param shapeTypeName the string name for the corresponding shapeType.
	 * @param shape         the corresponding shape.
	 * @param isResizeable
	 * @param isRotatable
	 */
	private VShapeType(String shapeTypeName, Shape shape, boolean isResizeable, boolean isRotatable) {
		this.shape = shape;
		this.isResizeable = isResizeable;
		this.isRotatable = isRotatable;
		if (shapeTypeName == null) {
			throw new NullPointerException();
		}
		this.shapeTypeName = shapeTypeName;
		shapeMap.put(this.shapeTypeName, this); // adds to map
	}

	/**
	 * The constructor is private. ShapeType cannot be directly instantiated. Use
	 * create() method to instantiate ShapeType. TODO
	 */
	private VShapeType(String shapeTypeName, Shape shape) {
		this(shapeTypeName, shape, true, true);
	}

	/**
	 * Returns a VShapeType from a given string identifier name. If the VShapeType
	 * doesn't exist yet, it is created to extend the enum. The method makes sure
	 * that the same object is not added twice.
	 * 
	 * @param shapeTypeName the string key.
	 * @param shape         the shape.
	 * @return the VShapeType for given name. If name does not exist, creates and
	 *         returns a new VShapeType.
	 */
	public static VShapeType register(String shapeTypeName, Shape shape) {
		if (shapeMap.containsKey(shapeTypeName)) {
			return shapeMap.get(shapeTypeName);
		} else {
			Logger.log.trace("Registered shape for shape type " + shapeTypeName);
			return new VShapeType(shapeTypeName, shape);
		}
	}

	public String getShapeTypeName() {
		return shapeTypeName;
	}

	public ShapeType getShapeType() {
		return ShapeType.fromName(shapeTypeName);
	}

	public Shape getShape(double mw, double mh) {
		// now scale the path so it has proper w and h.
		Rectangle r = shape.getBounds();
		AffineTransform at = new AffineTransform();
		at.translate(-r.x, -r.y);
		at.scale(mw / r.width, mh / r.height);
		return at.createTransformedShape(shape);
	}

	public boolean isResizeable() {
		return isResizeable;
	}

	public boolean isRotatable() {
		return isRotatable;
	}

//	/**
//	 * Returns the names of all registered ShapeTypes as a list.
//	 * 
//	 * @return names the names of all registered ShapeTypes in the order of
//	 *         insertion.
//	 */
//	static public List<String> getNames() {
//		List<String> names = new ArrayList<>(nameToShapeType.keySet());
//		return names;
//	}
//
//	/**
//	 * Returns the data node type values of all ShapeTypes as a list.
//	 * 
//	 * @return shapeTypes the list of all registered ShapeTypes.
//	 */
//	static public List<ShapeType> getValues() {
//		List<ShapeType> shapeTypes = new ArrayList<>(nameToShapeType.values());
//		return shapeTypes;
//	}
//
//	/**
//	 * Returns a string representation of this ShapeType.
//	 * 
//	 * @return name the identifier of this ShapeType.
//	 */
//	public String toString() {
//		return name;
//	}

}
