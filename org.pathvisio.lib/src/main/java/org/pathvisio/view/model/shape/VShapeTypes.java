package org.pathvisio.view.model.shape;

/**
 * 
 * @author finterly
 */
public class VShapeTypes {

	static void registerShapes() {
		// Basic shapes
		ShapesRegistry.registerShape(VShapeType.NONE);
		ShapesRegistry.registerShape(VShapeType.RECTANGLE);
		ShapesRegistry.registerShape(VShapeType.ROUNDED_RECTANGLE);
		ShapesRegistry.registerShape(VShapeType.OVAL);
		ShapesRegistry.registerShape(VShapeType.TRIANGLE);
		ShapesRegistry.registerShape(VShapeType.PENTAGON);
		ShapesRegistry.registerShape(VShapeType.HEXAGON);
		ShapesRegistry.registerShape(VShapeType.OCTAGON);

		// Basic line shapes
		ShapesRegistry.registerShape(VShapeType.EDGE);
		ShapesRegistry.registerShape(VShapeType.ARC);
		ShapesRegistry.registerShape(VShapeType.BRACE);

		// Cellular components with special shape
		ShapesRegistry.registerShape(VShapeType.MITOCHONDRIA);
		ShapesRegistry.registerShape(VShapeType.SARCOPLASMIC_RETICULUM);
		ShapesRegistry.registerShape(VShapeType.ENDOPLASMIC_RETICULUM);
		ShapesRegistry.registerShape(VShapeType.GOLGI_APPARATUS);

		// Cellular components (rarely used)
		ShapesRegistry.registerShape(VShapeType.NUCLEOLUS);
		ShapesRegistry.registerShape(VShapeType.VACUOLE);
		ShapesRegistry.registerShape(VShapeType.LYSOSOME);
		ShapesRegistry.registerShape(VShapeType.CYTOSOL);

		// Cellular components with basic shape
		ShapesRegistry.registerShape(VShapeType.EXTRACELLULAR);
		ShapesRegistry.registerShape(VShapeType.CELL); // Rounded rectangle
		ShapesRegistry.registerShape(VShapeType.NUCLEUS); // Oval
		ShapesRegistry.registerShape(VShapeType.ORGANELLE); // Rounded rectangle
		ShapesRegistry.registerShape(VShapeType.VESICLE); // Oval

		// Deprecated since GPML2013a?
		ShapesRegistry.registerShape(VShapeType.MEMBRANE); // Rounded rectangle
		ShapesRegistry.registerShape(VShapeType.CELLA); // Oval
		ShapesRegistry.registerShape(VShapeType.RIBOSOME); // Hexagon
		ShapesRegistry.registerShape(VShapeType.ORGANA); // Oval
		ShapesRegistry.registerShape(VShapeType.ORGANB); // Oval
		ShapesRegistry.registerShape(VShapeType.ORGANC); // Oval
		ShapesRegistry.registerShape(VShapeType.PROTEINB); // Hexagon

		// Special Shapes //TODO
		ShapesRegistry.registerShape(VShapeType.CORONAVIRUS);
	}

}
