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
package org.pathvisio.model;

import org.pathvisio.model.graphics.FontProperty;
import org.pathvisio.model.graphics.RectProperty;
import org.pathvisio.model.graphics.ShapeStyleProperty;

/**
 * This class stores all information relevant to a Label pathway element.
 * 
 * @author finterly
 */
public class Label extends ShapedElement {

	private String textLabel;
	private String href; // optional

	/**
	 * Instantiates a Label pathway element given all possible parameters.
	 * 
	 * @param rectProperty       the centering (position) and dimension properties.
	 * @param fontProperty       the font properties, e.g. textColor, fontName...
	 * @param shapeStyleProperty the shape style properties, e.g. borderColor.
	 * @param textLabel          the text of the label.
	 * @param href               the hyperlink of the label.
	 */
	public Label(RectProperty rectProperty, FontProperty fontProperty, ShapeStyleProperty shapeStyleProperty,
			String textLabel, String href) {
		super(rectProperty, fontProperty, shapeStyleProperty);
		this.textLabel = textLabel;
		this.href = href;
	}

	/**
	 * Instantiates a Label given all possible parameters except href.
	 */
	public Label(RectProperty rectProperty, FontProperty fontProperty, ShapeStyleProperty shapeStyleProperty,
			String textLabel) {
		this(rectProperty, fontProperty, shapeStyleProperty, textLabel, null);
	}

	/**
	 * Returns the text of of the label.
	 * 
	 * @return textLabel the text of of the label.
	 * 
	 */
	public String getTextLabel() {
		return textLabel;
	}

	/**
	 * Sets the text of of the label.
	 * 
	 * @param textLabel the text of of the label.
	 * 
	 */
	public void setTextLabel(String textLabel) {
		this.textLabel = textLabel;
	}

	/**
	 * Returns the hyperlink for a Label.
	 * 
	 * @return href the hyperlink reference to a url.
	 */
	public String getHref() {
		return href;
	}

	/**
	 * Sets the hyperlink for a Label.
	 * 
	 * @param href the hyperlink reference to a url.
	 */
	public void setHref(String href) {
		this.href = href;
	}

}