package org.pathvisio.core.modeltemp;

import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.Arc2D;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;
import java.awt.geom.RoundRectangle2D;

import org.pathvisio.debug.Logger;
import org.pathvisio.model.type.AnchorShapeType;
import org.pathvisio.model.type.ShapeType;
import org.pathvisio.view.model.shape.VShapeTypeCatalog.Internal;

/**
 * 
 * @author finterly
 */
public class VShapeTypes {

	private static Shape rectangle = new Rectangle(0, 0, 10, 10);
	private static Shape roundedRectangle = new RoundRectangle2D.Double(0, 0, 10, 10, 20, 20);
	private static Shape ellipse = new Ellipse2D.Double(0, 0, 10, 10);

	static void registerShapes() {

		// register arrow heads
		VShapeRegistry.registerShape("Arrow", getArrowHead(), VArrowHeadType.FillType.CLOSED);
		VShapeRegistry.registerArrow("TBar", getTBar(), VArrowHeadType.FillType.OPEN, TBARWIDTH + TBAR_GAP);
		VShapeRegistry.registerArrow("LigandRound", getLRound(), VArrowHeadType.FillType.CLOSED);
		VShapeRegistry.registerArrow("ReceptorRound", getRRound(), VArrowHeadType.FillType.WIRE);
		VShapeRegistry.registerArrow("Receptor", getReceptor(), VArrowHeadType.FillType.WIRE);
		VShapeRegistry.registerArrow("ReceptorSquare", getReceptorSquare(), VArrowHeadType.FillType.WIRE);
		VShapeRegistry.registerArrow("LigandSquare", getLigand(), VArrowHeadType.FillType.CLOSED);

		// register anchors
		VShapeRegistry.registerAnchor(AnchorShapeType.SQUARE.getName(), getAnchorSquare()); // TODO
		VShapeRegistry.registerAnchor(AnchorShapeType.CIRCLE.getName(), getAnchorCircle());
		VShapeRegistry.registerAnchor(AnchorShapeType.NONE.getName(), getAnchorNone());
	}

	// Basic shapes
	public static final VShapeTypes NONE = new VShapeTypes();
	public static final VShapeTypes RECTANGLE = new VShapeTypes(ShapeType.RECTANGLE.getName(), rectangle);
	public static final VShapeTypes ROUNDED_RECTANGLE = new VShapeTypes(ShapeType.ROUNDED_RECTANGLE.getName(),
			roundedRectangle);
	public static final VShapeTypes OVAL = new VShapeTypes(ShapeType.OVAL.getName(), ellipse);
	public static final VShapeTypes TRIANGLE = new VShapeTypes(ShapeType.TRIANGLE.getName(),
			VShapeTypeCatalog.getRegularPolygon(3, 10, 10));
	public static final VShapeTypes PENTAGON = new VShapeTypes(ShapeType.PENTAGON.getName(),
			VShapeTypeCatalog.getRegularPolygon(5, 10, 10));
	public static final VShapeTypes HEXAGON = new VShapeTypes(ShapeType.HEXAGON.getName(),
			VShapeTypeCatalog.getRegularPolygon(6, 10, 10));
	public static final VShapeTypes OCTAGON = new VShapeTypes(ShapeType.OCTAGON.getName(),
			VShapeTypeCatalog.getRegularPolygon(8, 10, 10));

	// Basic line shapes
	public static final VShapeTypes EDGE = new VShapeTypes(ShapeType.EDGE.getName(), new Line2D.Double(0, 0, 10, 10));
	public static final VShapeTypes ARC = new VShapeTypes(ShapeType.ARC.getName(),
			new Arc2D.Double(0, 0, 10, 10, 0, -180, Arc2D.OPEN));
	public static final VShapeTypes BRACE = new VShapeTypes(ShapeType.BRACE.getName(),
			VShapeTypeCatalog.getPluggableShape(Internal.BRACE));

	// Cellular components with special shape
	public static final VShapeTypes MITOCHONDRIA = new VShapeTypes(ShapeType.MITOCHONDRIA.getName(),
			VShapeTypeCatalog.getPluggableShape(Internal.MITOCHONDRIA));
	public static final VShapeTypes SARCOPLASMIC_RETICULUM = new VShapeTypes(ShapeType.SARCOPLASMIC_RETICULUM.getName(),
			VShapeTypeCatalog.getPluggableShape(Internal.SARCOPLASMICRETICULUM));
	public static final VShapeTypes ENDOPLASMIC_RETICULUM = new VShapeTypes(ShapeType.ENDOPLASMIC_RETICULUM.getName(),
			VShapeTypeCatalog.getPluggableShape(Internal.ENDOPLASMICRETICULUM));
	public static final VShapeTypes GOLGI_APPARATUS = new VShapeTypes(ShapeType.GOLGI_APPARATUS.getName(),
			VShapeTypeCatalog.getPluggableShape(Internal.GOLGIAPPARATUS));

	// Cellular components (rarely used)
	public static final VShapeTypes NUCLEOLUS = new VShapeTypes(ShapeType.NUCLEOLUS.getName(), null);
	public static final VShapeTypes VACUOLE = new VShapeTypes(ShapeType.VACUOLE.getName(), null);
	public static final VShapeTypes LYSOSOME = new VShapeTypes(ShapeType.LYSOSOME.getName(), null);
	public static final VShapeTypes CYTOSOL = new VShapeTypes(ShapeType.CYTOSOL.getName(), null);

	// Cellular components with basic shape
	public static final VShapeTypes EXTRACELLULAR = new VShapeTypes(ShapeType.EXTRACELLULAR.getName(),
			roundedRectangle);
	public static final VShapeTypes CELL = new VShapeTypes(ShapeType.CELL.getName(), roundedRectangle); // Rounded
																										// rectangle
	public static final VShapeTypes NUCLEUS = new VShapeTypes(ShapeType.NUCLEUS.getName(), ellipse); // Oval
	public static final VShapeTypes ORGANELLE = new VShapeTypes(ShapeType.ORGANELLE.getName(), roundedRectangle); // Rounded
	// rectangle
	public static final VShapeTypes VESICLE = new VShapeTypes(ShapeType.VESICLE.getName(), ellipse); // Oval

	// Deprecated since GPML2013a?
	public static final VShapeTypes MEMBRANE = new VShapeTypes(ShapeType.MEMBRANE.getName(), null); // Rounded rectangle
	public static final VShapeTypes CELLA = new VShapeTypes(ShapeType.CELLA.getName(), null); // Oval
	public static final VShapeTypes RIBOSOME = new VShapeTypes(ShapeType.RIBOSOME.getName(), null); // Hexagon
	public static final VShapeTypes ORGANA = new VShapeTypes(ShapeType.ORGANA.getName(), null); // Oval
	public static final VShapeTypes ORGANB = new VShapeTypes(ShapeType.ORGANB.getName(), null); // Oval
	public static final VShapeTypes ORGANC = new VShapeTypes(ShapeType.ORGANC.getName(), null); // Oval
	public static final VShapeTypes PROTEINB = new VShapeTypes(ShapeType.PROTEINB.getName(), null); // Hexagon

	// Special Shapes //TODO
	public static final VShapeTypes CORONAVIRUS = new VShapeTypes("Coronavirus",
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
	private VShapeTypes(String shapeTypeName, Shape shape, boolean isResizeable, boolean isRotatable) {
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
	private VShapeTypes(String shapeTypeName, Shape shape) {
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
	public static VShapeTypes register(String shapeTypeName, Shape shape) {
		if (shapeMap.containsKey(shapeTypeName)) {
			return shapeMap.get(shapeTypeName);
		} else {
			Logger.log.trace("Registered shape for shape type " + shapeTypeName);
			return new VShapeTypes(shapeTypeName, shape);
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
