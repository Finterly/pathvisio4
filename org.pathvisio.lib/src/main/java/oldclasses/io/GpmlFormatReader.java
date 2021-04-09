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
package oldclasses.io;

import org.jdom2.Element;
import org.pathvisio.io.ConverterException;
import org.pathvisio.io.gpml2021.GPMLVersion;
import org.pathvisio.model.*;

public interface GpmlFormatReader extends GPMLVersion {
	
	public PathwayElement mapElement(Element e) throws ConverterException;

	public void readFromRoot(Element root, PathwayModel pwyModel) throws ConverterException;
}