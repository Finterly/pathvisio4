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
package org.pathvisio.view.model.shape;

import java.awt.Shape;
import java.awt.geom.GeneralPath;
import java.util.HashMap;
import java.util.Map;

import org.pathvisio.debug.Logger;
import org.pathvisio.view.model.shape.VShapeTypeCatalog.Internal;

/**
 * The Shape registry stores all arrow heads and shapes. The shape registry
 * initializes itself, by calling registerShape() on BasicShapes, GenMAPPShapes
 * and MIMShapes.
 * 
 * @author unknown, finterly
 */
/**
 * @author p70073399
 *
 */
public class VShapeRegistry {

	public static final Shape DEFAULT_SHAPE = VShapeTypeCatalog.getPluggableShape(Internal.DEFAULT);
	private static VArrowHeadType defaultArrow = null;
	private static VAnchorShapeType defaultAnchor = null;

	private static Map<String, VArrowHeadType> arrowMap = new HashMap<String, VArrowHeadType>();
	private static Map<String, VShapeType> shapeMap = new HashMap<String, VShapeType>();
	private static Map<String, VAnchorShapeType> anchorMap = new HashMap<String, VAnchorShapeType>();
//	private static Map<String, IShape> mappMappings = new HashMap<String, IShape>();

	static {
		GeneralPath temp = new GeneralPath();
		temp.moveTo(13.629531, 19.93828);
		// TODO
		defaultArrow = new VArrowHeadType(temp, VArrowHeadType.FillType.OPEN);

		VLineShapeType.registerShapes();
		MIMShape.registerShapes();
	}

	public static VShapeType registerShape(String shapeTypeName, VShapeType vShapeType) {
		if (shapeMap.containsKey(shapeTypeName)) {
			return shapeMap.get(shapeTypeName);
		} else {
			Logger.log.trace("Registered shape type " + shapeTypeName);
			shapeMap.put(shapeTypeName, vShapeType);
			return vShapeType;
		}
	}

	/**
	 * looks up the ShapeType corresponding to that name.
	 */
	public static VShapeType getVShapeType(String value) {
		return shapeMap.get(value);
	}

	/**
	 * Register an arrow shape
	 * 
	 * @param key              The key used to identify the arrow shape
	 * @param sh               The shape used to draw the stroke
	 * @param fillType         The fill type, see {@link VArrowHeadType}
	 * @param lineEndingLength The line ending width
	 */
	static public void registerArrow(String key, Shape sh, VArrowHeadType.FillType fillType, int lineEndingLength) {
		// pass in zero as the gap between line line ending and anchor
		arrowMap.put(key, new VArrowHeadType(sh, fillType, lineEndingLength));
	}

	/**
	 * Register an arrow shape.
	 * 
	 * @param key      The key used to identify the arrow shape
	 * @param sh       The shape used to draw the stroke and fill (in case fillType
	 *                 is open or closed)
	 * @param fillType The fill type, see {@link VArrowHeadType}
	 */
	static public void registerArrow(String key, Shape sh, VArrowHeadType.FillType fillType) {
		arrowMap.put(key, new VArrowHeadType(sh, fillType));
	}

	/**
	 * Register an anchor shape.
	 * 
	 * @param key
	 * @param sh
	 */
	static public void registerAnchor(String key, Shape sh) {
		anchorMap.put(key, new VAnchorShapeType(sh));
	}

	/**
	 * Returns a named arrow head. The shape is normalized so that it fits with a
	 * line that goes along the positive x-axis. The tip of the arrow head is in
	 * 0,0.
	 * 
	 * NB: here we return a reference to the object on the registry itself, we
	 * should really return a clone, although in practice this is not a problem
	 * since we do an affine transform immediately after. TODO 
	 */
	public static VArrowHeadType getArrow(String name) {
		VArrowHeadType sh = arrowMap.get(name);
		if (sh == null) {
			sh = defaultArrow;
		}
		return sh;
	}

	/**
	 * Returns an anchor shape
	 */
	public static VAnchorShapeType getAnchor(String name) {
		VAnchorShapeType sh = anchorMap.get(name);
		if (sh == null) {
			sh = defaultAnchor;
		}
		return sh;
	}

}
