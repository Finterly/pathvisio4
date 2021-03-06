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
package org.pathvisio.application.exporter;

import java.awt.Dimension;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.OutputStream;
import java.io.Writer;

import org.apache.batik.dom.GenericDOMImplementation;
import org.apache.batik.svggen.SVGGraphics2D;
import org.apache.batik.transcoder.Transcoder;
import org.apache.batik.transcoder.TranscoderInput;
import org.apache.batik.transcoder.TranscoderOutput;
import org.apache.batik.transcoder.image.ImageTranscoder;
import org.apache.batik.transcoder.image.PNGTranscoder;
import org.apache.batik.transcoder.image.TIFFTranscoder;
import org.apache.fop.svg.PDFTranscoder;
import org.bridgedb.IDMapperException;
import org.pathvisio.debug.Logger;
import org.pathvisio.io.ConverterException;
import org.pathvisio.model.ImageExporter;
import org.pathvisio.view.model.VPathwayModel;
import org.pathvisio.data.DataException;
import org.pathvisio.application.gex.GexManager;
import org.pathvisio.application.visualization.VisualizationManager;
import org.pathvisio.model.PathwayModel;
import org.w3c.dom.DOMImplementation;
import org.w3c.dom.Document;

/**
 * Same as BatikImageExporter, but can also include visualized data
 * in the resulting image.
 */
public class BatikImageWithDataExporter extends ImageExporter
{
	private final VisualizationManager visualizationManager;
	private final GexManager gexManager;

	public BatikImageWithDataExporter(String type, GexManager gexManager, VisualizationManager visualizationManager)
	{
		super(type);
		this.gexManager = gexManager;
		this.visualizationManager = visualizationManager;
	}

	protected boolean dataVisible = true; // true by default

	/**
	 * Use this method to disable / enable export of the current data visualization
	 */
	public void setDataVisible (boolean value)
	{
		dataVisible = value;
	}

	public void doExport(File file, PathwayModel pathwayModel) throws ConverterException
	{
		VPathwayModel vPathwayModel = new VPathwayModel();
		vPathwayModel.fromModel(pathwayModel);

		// if data visualization is enabled, link this VPathway up to the visualization manager.
		if (dataVisible)
		{
			vPathwayModel.addVPathwayListener(visualizationManager);
			try
			{
				if (gexManager.getCachedData() != null)
					gexManager.getCachedData().syncSeed(pathwayModel.getDataNodeXrefs());
			}
			catch (IDMapperException ex)
			{
				Logger.log.error ("Could not get data", ex);
			}
			catch (DataException ex)
			{
				Logger.log.error ("Could not get data", ex);
			}
		}

		DOMImplementation domImpl = GenericDOMImplementation.getDOMImplementation();
		Document svg = domImpl.createDocument ("http://www.w3.org/2000/svg", "svg", null);

		SVGGraphics2D svgG2d = new SVGGraphics2D(svg);
		vPathwayModel.draw(svgG2d);

		//Force recalculation of size after drawing once, this allows size of text
		//to be calculated correctly
		Dimension size = vPathwayModel.calculateVSize();
		svgG2d.setSVGCanvasSize(size);

		Transcoder t = null;
		if			(getType().equals(TYPE_SVG)) {
			try {
				Writer out = new FileWriter(file);
				svgG2d.stream(out, true);
				out.flush();
				out.close();
			} catch(Exception e) {
				throw new ConverterException(e);
			}
			return;
		} else if	(getType().equals(TYPE_PNG)) {
			t = new PNGTranscoder();
		} else if	(getType().equals(TYPE_TIFF)) {
			t = new TIFFTranscoder();
		} else if	(getType().equals(TYPE_PDF)) {
			t = new PDFTranscoder();
		}
		if(t == null) noExporterException();

		svgG2d.getRoot(svg.getDocumentElement());
		t.addTranscodingHint(ImageTranscoder.KEY_BACKGROUND_COLOR, java.awt.Color.WHITE);

		try {
			TranscoderInput input = new TranscoderInput(svg);

			// Create the transcoder output.
			OutputStream ostream = new FileOutputStream(file);
			TranscoderOutput output = new TranscoderOutput(ostream);

			// Save the image.
			t.transcode(input, output);

		    // Flush and close the stream.
	        ostream.flush();
	        ostream.close();
		} catch(Exception e) {
			throw new ConverterException(e);
		}
		vPathwayModel.dispose();
	}
}
