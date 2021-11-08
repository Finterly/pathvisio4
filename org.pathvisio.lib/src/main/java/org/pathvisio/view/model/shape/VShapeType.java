package org.pathvisio.view.model.shape;

import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.Arc2D;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;
import java.awt.geom.RoundRectangle2D;

import org.pathvisio.model.type.ShapeType;
import org.pathvisio.view.model.shape.VShapeTypeCatalog.Internal;

/**
 * Class for VShapeType
 * 
 * @author finterly
 */
public class VShapeType {

	// ================================================================================
	// Static variables
	// ================================================================================
	private static Shape rectangle = new Rectangle(0, 0, 10, 10);
	private static Shape roundedRectangle = new RoundRectangle2D.Double(0, 0, 10, 10, 20, 20);
	private static Shape ellipse = new Ellipse2D.Double(0, 0, 10, 10);

	// ================================================================================
	// Instantiates and registers shapes
	// ================================================================================

	// Basic shapes
	public static final VShapeType NONE = new VShapeType(ShapeType.NONE, null);
	public static final VShapeType RECTANGLE = new VShapeType(ShapeType.RECTANGLE, rectangle);
	public static final VShapeType ROUNDED_RECTANGLE = new VShapeType(ShapeType.ROUNDED_RECTANGLE, roundedRectangle);
	public static final VShapeType OVAL = new VShapeType(ShapeType.OVAL, ellipse);
	public static final VShapeType TRIANGLE = new VShapeType(ShapeType.TRIANGLE,
			VShapeTypeCatalog.getRegularPolygon(3, 10, 10));
	public static final VShapeType PENTAGON = new VShapeType(ShapeType.PENTAGON,
			VShapeTypeCatalog.getRegularPolygon(5, 10, 10));
	public static final VShapeType HEXAGON = new VShapeType(ShapeType.HEXAGON,
			VShapeTypeCatalog.getRegularPolygon(6, 10, 10));
	public static final VShapeType OCTAGON = new VShapeType(ShapeType.OCTAGON,
			VShapeTypeCatalog.getRegularPolygon(8, 10, 10));

	// Basic line shapes
	public static final VShapeType EDGE = new VShapeType(ShapeType.EDGE, new Line2D.Double(0, 0, 10, 10));;
	public static final VShapeType ARC = new VShapeType(ShapeType.ARC,
			new Arc2D.Double(0, 0, 10, 10, 0, -180, Arc2D.OPEN));
	public static final VShapeType BRACE = new VShapeType(ShapeType.BRACE,
			VShapeTypeCatalog.getPluggableShape(Internal.BRACE));

	// Cellular components with special shape
	public static final VShapeType MITOCHONDRIA = new VShapeType(ShapeType.MITOCHONDRIA,
			VShapeTypeCatalog.getPluggableShape(Internal.MITOCHONDRIA));
	public static final VShapeType SARCOPLASMIC_RETICULUM = new VShapeType(ShapeType.SARCOPLASMIC_RETICULUM,
			VShapeTypeCatalog.getPluggableShape(Internal.SARCOPLASMICRETICULUM));
	public static final VShapeType ENDOPLASMIC_RETICULUM = new VShapeType(ShapeType.ENDOPLASMIC_RETICULUM,
			VShapeTypeCatalog.getPluggableShape(Internal.ENDOPLASMICRETICULUM));
	public static final VShapeType GOLGI_APPARATUS = new VShapeType(ShapeType.GOLGI_APPARATUS,
			VShapeTypeCatalog.getPluggableShape(Internal.GOLGIAPPARATUS));

	// Cellular components (rarely used)
	public static final VShapeType NUCLEOLUS = new VShapeType(ShapeType.NUCLEOLUS, null);
	public static final VShapeType VACUOLE = new VShapeType(ShapeType.VACUOLE, null);
	public static final VShapeType LYSOSOME = new VShapeType(ShapeType.LYSOSOME, null);
	public static final VShapeType CYTOSOL = new VShapeType(ShapeType.CYTOSOL, null);

	// Cellular components with basic shape
	public static final VShapeType EXTRACELLULAR = new VShapeType(ShapeType.EXTRACELLULAR, roundedRectangle);
	public static final VShapeType CELL = new VShapeType(ShapeType.CELL, roundedRectangle);
	public static final VShapeType NUCLEUS = new VShapeType(ShapeType.NUCLEUS, ellipse);
	public static final VShapeType ORGANELLE = new VShapeType(ShapeType.ORGANELLE, roundedRectangle);
	public static final VShapeType VESICLE = new VShapeType(ShapeType.VESICLE, ellipse);

	// Deprecated since GPML2013a?
	public static final VShapeType MEMBRANE = new VShapeType(ShapeType.MEMBRANE, null); // Rounded rectangle
	public static final VShapeType CELLA = new VShapeType(ShapeType.CELLA, null); // Oval
	public static final VShapeType RIBOSOME = new VShapeType(ShapeType.RIBOSOME, null); // Hexagon
	public static final VShapeType ORGANA = new VShapeType(ShapeType.ORGANA, null); // Oval
	public static final VShapeType ORGANB = new VShapeType(ShapeType.ORGANB, null); // Oval
	public static final VShapeType ORGANC = new VShapeType(ShapeType.ORGANC, null); // Oval
	public static final VShapeType PROTEINB = new VShapeType(ShapeType.PROTEINB, null); // Hexagon

	// Special shapes
	public static final VShapeType CORONAVIRUS = new VShapeType(ShapeType.CORONAVIRUS,
			VShapeTypeCatalog.getPluggableShape(Internal.CORONAVIRUS)); // TODO
	public static final VShapeType DNA = new VShapeType(ShapeType.DNA, null); // TODO
	public static final VShapeType CELL_ICON = new VShapeType(ShapeType.CELL_ICON, null); // TODO

	// ================================================================================
	// Variables
	// ================================================================================
	private ShapeType shapeType;
	private Shape shape;
	private final boolean isResizeable;
	private final boolean isRotatable;

	// ================================================================================
	// Constructors
	// ================================================================================
	/**
	 * The constructor is private. ShapeType cannot be directly instantiated. Use
	 * create() method to instantiate ShapeType.
	 * 
	 * @param name the string key of this ShapeType. // isResizeable if true object
	 *             is resizeable.
	 * @throws NullPointerException if name is null.
	 */
	private VShapeType(ShapeType shapeType, Shape shape) {
		this(shapeType, shape, true, true);
	}

	private VShapeType(ShapeType shapeType, Shape shape, boolean isResizeable, boolean isRotatable) {
		this.shapeType = shapeType;
		this.isResizeable = isResizeable;
		this.isRotatable = isRotatable;
		this.shape = shape;
		VShapeRegistry.registerShape(shapeType.getName(), this); // adds this name and ShapeType to map.
	}

	public ShapeType getShapeType() {
		return shapeType;
	}

	/**
	 * @param shapeTypeName
	 * @param mw
	 * @param mh
	 * @return
	 */
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

}
