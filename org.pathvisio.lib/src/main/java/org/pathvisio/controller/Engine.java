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
package org.pathvisio.controller;

import java.awt.Color;
import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.swing.SwingUtilities;

import org.pathvisio.debug.Logger;
import org.pathvisio.io.ConverterException;
import org.pathvisio.model.PathwayModel;
import org.pathvisio.events.PathwayExporter;
import org.pathvisio.events.PathwayImporter;
import org.pathvisio.util.FileUtils;
import org.pathvisio.util.Utils;
import org.pathvisio.view.model.VPathwayModel;
import org.pathvisio.view.model.VPathwayWrapper;

/**
 * This class manages loading, importing and exporting a PathwayModel and
 * VPathway together.
 *
 * TODO: there are some unrelated Global functions in here, but the intention is
 * to move them away in the future.
 */
public class Engine {
	/* vPathwayModel may be null */
	private VPathwayModel vPathwayModel;
	/*
	 * standalone is only used when vPathway is null. TODO: standalone below is a
	 * hack to make Converter work.
	 */
	private PathwayModel standalone = null;
	/*
	 * wrapper may also be null in case you don't need to interact with the pathway.
	 */
	private VPathwayWrapper wrapper;

	public static final String SVG_FILE_EXTENSION = "svg";
	public static final String SVG_FILTER_NAME = "Scalable Vector Graphics (*." + SVG_FILE_EXTENSION + ")";
	public static final String PATHWAY_FILE_EXTENSION = "gpml";
	public static final String PATHWAY_FILTER_NAME = "PathVisio PathwayModel (*." + PATHWAY_FILE_EXTENSION + ")";
	public static final String GENMAPP_FILE_EXTENSION = "mapp";
	public static final String GENMAPP_FILTER_NAME = "GenMAPP PathwayModel (*." + GENMAPP_FILE_EXTENSION + ")";

	/**
	 * The transparent color used in the icons for visualization of protein/mRNA
	 * data
	 */
	public static final Color TRANSPARENT_COLOR = new Color(255, 0, 255);

	/**
	 * Set this to the toolkit-specific wrapper before opening or creating a new
	 * pathway otherwise Engine can't create a vPathway.
	 */
	public void setWrapper(VPathwayWrapper value) {
		wrapper = value;
	}

	/**
	 * Returns the currently open drawing.
	 */
	public VPathwayModel getActiveVPathway() {
		return vPathwayModel;
	}

	/**
	 * Returns the currently open Pathway
	 */
	public PathwayModel getActivePathway() {
		if (vPathwayModel == null) {
			return standalone;
		} else {
			return vPathwayModel.getPathwayModel();
		}
	}

	/**
	 * Imports pathway from a file.
	 * 
	 * @param file the pathway file to import.
	 * @throws ConverterException
	 */
	public void importPathway(File file) throws ConverterException {
		Logger.log.trace("Importing pathway from " + file);

		Set<PathwayImporter> set = getPathwayImporters(file);
		if (set != null && set.size() == 1) {
			PathwayImporter importer = Utils.oneOf(set);
			PathwayModel pathwayModel = importer.doImport(file);
			pathwayModel.setSourceFile(file);
			newPathwayHelper(pathwayModel);
		} else
			throw new ConverterException(
					"Could not determine importer for '" + FileUtils.getExtension(file.toString()) + "' files");
	}

	/**
	 * After loading a pathway from disk, run createVPathway on EDT thread to
	 * prevent concurrentModificationException
	 * 
	 * @param pathwayModel the pathway model.
	 * @throws ConverterException
	 */
	private void newPathwayHelper(final PathwayModel pathwayModel) throws ConverterException {
		try {
			// switch back to EDT
			SwingUtilities.invokeAndWait(new Runnable() {
				public void run() {
					createVPathway(pathwayModel);
					fireApplicationEvent(new ApplicationEvent(pathwayModel, ApplicationEvent.Type.PATHWAY_OPENED));
					if (vPathwayModel != null) {
						fireApplicationEvent(
								new ApplicationEvent(vPathwayModel, ApplicationEvent.Type.VPATHWAY_OPENED));
					}
				}
			});
		} catch (InterruptedException e) {
			throw new ConverterException(e);
		} catch (InvocationTargetException e) {
			throw new ConverterException(e);
		}
	}

	/**
	 * Opens pathway model from memory
	 * 
	 * @param pathwayModel
	 * @throws ConverterException
	 */
	public void openPathwayFromMemory(PathwayModel pathwayModel) throws ConverterException {
		newPathwayHelper(pathwayModel);
	}

	/**
	 * Opens a pathway from a gpml file
	 */
	public void openPathway(File pathwayFile) throws ConverterException {
		String pwf = pathwayFile.toString();

		// initialize new JDOM gpml representation and read the file
		final PathwayModel pathwayModel = new PathwayModel();
		pathwayModel.readFromXml(new File(pwf), true);
		// Only set the pathway field after the data is loaded
		// (Exception thrown on error, this part will not be reached)
		newPathwayHelper(pathwayModel);
	}

	/**
	 * Opens a pathway from a url.
	 * 
	 * @param url
	 * @return f the file.
	 * @throws ConverterException
	 */
	public File openPathway(URL url) throws ConverterException {
		// TODO insert in recent pathways
		String protocol = url.getProtocol();
		File f = null;
		if (protocol.equals("file")) {
			f = new File(url.getFile());
			openPathway(f);
		} else {
			try {
				f = File.createTempFile("urlPathway", "." + Engine.PATHWAY_FILE_EXTENSION);
				FileUtils.downloadFile(url, f);
				openPathway(f);
			} catch (Exception e) {
				throw new ConverterException(e);
			}
		}
		return f;
	}

	/**
	 * Saves the given pathway to a file. 
	 * 
	 * @param p      The pathway to save
	 * @param toFile The file to save to
	 * @throws ConverterException
	 */
	public void savePathway(PathwayModel p, File toFile) throws ConverterException {
		// make sure there are no problems with references.
		// p.fixReferences(); TODO not needed anymore?
		p.writeToXml(toFile, true);
		fireApplicationEvent(new ApplicationEvent(p, ApplicationEvent.Type.PATHWAY_SAVE));
	}

	/**
	 * Saves the currently active pathway to a file. 
	 * 
	 * @param toFile The file to save to
	 * @throws ConverterException
	 */
	public void savePathway(File toFile) throws ConverterException {
		savePathway(getActivePathway(), toFile);
	}

	/**
	 * Opposite of createVPathway. Disposes of a vPathwayModel by setting it to
	 * null.
	 */
	public void disposeVPathway() {
		assert (vPathwayModel != null);
		// signal destruction of vPathway
		fireApplicationEvent(new ApplicationEvent(vPathwayModel, ApplicationEvent.Type.VPATHWAY_DISPOSED));
		vPathwayModel.dispose();
		vPathwayModel = null;
	}

	/**
	 * Tries to make a vPathway, replacing pathway with a new one.
	 *
	 * @param p the pathway model.
	 */
	public void createVPathway(PathwayModel p) {
		if (wrapper == null) {
			standalone = p;
		} else {
			double zoom = 100;
			if (hasVPathwayModel()) {
				// save zoom Level
				zoom = getActiveVPathway().getPctZoom();

				disposeVPathway();
			}
			vPathwayModel = wrapper.createVPathwayModel();
			vPathwayModel.registerKeyboardActions(this);
			vPathwayModel.activateUndoManager(this);
			vPathwayModel.fromModel(p);

			vPathwayModel.setPctZoom(zoom);
			fireApplicationEvent(new ApplicationEvent(vPathwayModel, ApplicationEvent.Type.VPATHWAY_CREATED));
		}
	}

	/**
	 * This method is used by the undo manager. Replaces a pathway model.
	 * 
	 * @param p the pathway model.
	 */
	public void replacePathway(PathwayModel p) {
		vPathwayModel.replacePathway(p);
		fireApplicationEvent(new ApplicationEvent(vPathwayModel, ApplicationEvent.Type.VPATHWAY_CREATED));
	}

	/**
	 * Creates a new {@link PathwayModel} and view {@link VPathwayModel}. Pathway
	 * model has {@link Pathway} initialized with default values.
	 */
	public void newPathway() {
		PathwayModel p = new PathwayModel();
		createVPathway(p);
		fireApplicationEvent(new ApplicationEvent(p, ApplicationEvent.Type.PATHWAY_NEW));
		if (vPathwayModel != null) {
			fireApplicationEvent(new ApplicationEvent(vPathwayModel, ApplicationEvent.Type.VPATHWAY_NEW));
		}
	}

	/**
	 * Find out whether a VPathwayModel is currently available or not
	 * 
	 * @return true if a VPathwayModel is currently available, false if not
	 */
	public boolean hasVPathwayModel() {
		return vPathwayModel != null;
	}

	private Map<String, Set<PathwayExporter>> exporters = new HashMap<String, Set<PathwayExporter>>();
	private Map<String, Set<PathwayImporter>> importers = new HashMap<String, Set<PathwayImporter>>();

	/**
	 * Adds a {@link PathwayExporter} that handles export of GPML to another file
	 * format.
	 * 
	 * @param export
	 */
	public void addPathwayExporter(PathwayExporter export) {
		for (String ext : export.getExtensions()) {
			Utils.multimapPut(exporters, ext.toLowerCase(), export);
		}
	}

	/**
	 * Removes a {@link PathwayExporter} that handles export of GPML to another file
	 * format.
	 * 
	 * @param export
	 */
	public void removePathwayExporter(PathwayExporter export) {
		for (String ext : export.getExtensions()) {
			if (exporters.containsKey(ext)) {
				if (exporters.get(ext).size() == 1) {
					exporters.remove(ext);
				} else {
					exporters.get(ext).remove(export);
				}
			}
		}
	}

	/**
	 * Adds a {@link PathwayImporter} that handles import of GPML to another file
	 * format.
	 * 
	 * @param importer
	 */
	public void addPathwayImporter(PathwayImporter importer) {
		for (String ext : importer.getExtensions()) {
			Utils.multimapPut(importers, ext.toLowerCase(), importer);
		}
	}

	/**
	 * Removes a {@link PathwayImporter} that handles import of GPML to another file
	 * format.
	 * 
	 * @param importer
	 */
	public void removePathwayImporter(PathwayImporter importer) {
		for (String ext : importer.getExtensions()) {
			if (importers.containsKey(ext)) {
				if (importers.get(ext).size() == 1) {
					importers.remove(ext);
				} else {
					importers.get(ext).remove(importer);
				}
			}
		}
	}

	/**
	 * Finds a suitable exporter for the given filename.
	 * 
	 * @returns null if no suitable exporter could be found.
	 */
	public Set<PathwayExporter> getPathwayExporters(File f) {
		return exporters.get(FileUtils.getExtension(f.toString()).toLowerCase());
	}

	/**
	 * Find exporters suitable for a given file. In case multiple importers match
	 * the file extension, the files may be inspected.
	 * 
	 * @returns set, null if no suitable importer could be found.
	 */
	public Set<PathwayImporter> getPathwayImporters(File f) {
		Set<PathwayImporter> set = new HashSet<PathwayImporter>();
		// deep copy, so that we can safely modify our set
		set.addAll(importers.get(FileUtils.getExtension(f.toString()).toLowerCase()));
		if (set != null && set.size() > 1) {
			Iterator<PathwayImporter> i = set.iterator();
			while (i.hasNext()) {
				PathwayImporter j = i.next();
				if (!j.isCorrectType(f))
					i.remove();
			}
		}
		return set;
	}

	/**
	 * Returns all registered pathway exporters.
	 * 
	 * @returns all registered pathway exporters
	 */
	public Set<PathwayExporter> getPathwayExporters() {
		return Utils.multimapValues(exporters);
	}

	/**
	 * Returns all registered pathway importers.
	 * 
	 * @returns all registered pathway importers
	 */
	public Set<PathwayImporter> getPathwayImporters() {
		return Utils.multimapValues(importers);
	}

	/**
	 * 
	 */
	private List<ApplicationEventListener> applicationEventListeners = new ArrayList<ApplicationEventListener>();

	/**
	 * Adds an {@link ApplicationEventListener}, that will be notified if a property
	 * changes that has an effect throughout the program (e.g. opening a pathway)
	 * 
	 * @param l the {@link ApplicationEventListener} to add
	 */
	public void addApplicationEventListener(ApplicationEventListener l) {
		if (l == null)
			throw new NullPointerException();
		applicationEventListeners.add(l);
	}

	/**
	 * Removes an {@link ApplicationEventListener}, that will be notified if a
	 * property changes that has an effect throughout the program (e.g. opening a
	 * pathway)
	 * 
	 * @param l the {@link ApplicationEventListener} to add
	 */
	public void removeApplicationEventListener(ApplicationEventListener l) {
		applicationEventListeners.remove(l);
	}

	/**
	 * Fires a {@link ApplicationEvent} to notify all
	 * {@link ApplicationEventListener}s registered to this class
	 * 
	 * @param e
	 */
	private void fireApplicationEvent(ApplicationEvent e) {
		for (ApplicationEventListener l : applicationEventListeners)
			l.applicationEvent(e);
	}

	/**
	 * Implement this if you want to receive events upon opening / closing pathways
	 */
	public interface ApplicationEventListener {
		public void applicationEvent(ApplicationEvent e);
	}

	String appName = "Application name undefined";

	/**
	 * Returns full application name, including version No.
	 * 
	 * @return appName the application name.
	 */
	public String getApplicationName() {
		return appName;
	}

	/**
	 * Sets full application name to give string value.
	 * 
	 * @param value the string value for application name.
	 */
	public void setApplicationName(String appName) {
		this.appName = appName;
	}

	/**
	 * Fire a close event TODO: move APPLICATION_CLOSE to other place
	 */
	public void close() {
		ApplicationEvent e = new ApplicationEvent(this, ApplicationEvent.Type.APPLICATION_CLOSE);
		fireApplicationEvent(e);
	}

	private boolean disposed = false;

	/**
	 * Frees all resources (such as listeners) held by this class. Owners of this
	 * class must explicitly dispose of it to clean up.
	 */
	public void dispose() {
		assert (!disposed);
		if (vPathwayModel != null)
			disposeVPathway();
		applicationEventListeners.clear();
		disposed = true;
	}

	/**
	 * Returns the subversion revision at the time of building //TODO not needed? 
	 * 
	 * @return revision
	 */
	public static String getRevision() {
		return "";
	}

	/**
	 * Returns the current PathVisio version
	 * 
	 * @return revision 
	 */
	public static String getVersion() {
		return Revision.VERSION;
	}

}

//// TODO: No reason to keep this in engine, it doesn't act on active pathway
///**
// * Exports given pathway to file. This function doesn't act on the active
// * pathway.
// * 
// * @param pathway pathway to export
// * @param file    file to write to.
// * @returns a list of warnings that occurred during export, or an empty list if
// *          there were none.
// */
//public List<String> exportPathway(File file, PathwayModel pathwayModel) throws ConverterException {
//	Logger.log.trace("Exporting pathway to " + file);
//
//	Set<PathwayExporter> set = getPathwayExporters(file);
//
//	if (set != null && set.size() == 1) {
//		PathwayExporter exporter = Utils.oneOf(set);
//		exporter.doExport(file, pathwayModel);
//		return exporter.getWarnings();
//	} else
//		throw new ConverterException(
//				"Could not determine exporter for '" + FileUtils.getExtension(file.toString()) + "' files");
//
//}
//
//// TODO: No reason to keep this in engine, it doesn't act on active pathway
///**
// * Exports given pathway to file. This function doesn't act on the active
// * pathway.
// * 
// * @param pathway pathway to export
// * @param file    file to write to.
// * @returns a list of warnings that occurred during export, or an empty list if
// *          there were none.
// */
//public List<String> exportPathway(File file, PathwayModel pathwayModel, int zoom) throws ConverterException {
//	Logger.log.trace("Exporting pathway to " + file);
//
//	Set<PathwayExporter> set = getPathwayExporters(file);
//
//	if (set != null && set.size() == 1) {
//		PathwayExporter exporter = Utils.oneOf(set);
//		exporter.doExport(file, pathwayModel, zoom);
//		return exporter.getWarnings();
//	} else
//		throw new ConverterException(
//				"Could not determine exporter for '" + FileUtils.getExtension(file.toString()) + "' files");
//
//}

//// TODO: No reason to keep this in engine, it doesn't act on active pathway
///**
// * Exports given pathway to file. This function doesn't act on the active
// * pathway.
// * 
// * @param pathway pathway to export
// * @param file    file to write to.
// * @returns a list of warnings that occurred during export, or an empty list if
// *          there were none.
// */
//public List<String> exportPathway(File file, PathwayModel pathwayModel, String exporterName)
//		throws ConverterException {
//	Logger.log.trace("Exporting pathway to " + file);
//
//	Set<PathwayExporter> set = getPathwayExporters(file);
//	try {
//		for (PathwayExporter pExporter : set) {
//
//			if (pExporter.getName().equals(exporterName)) {
//				System.out.println(pExporter.getName());
//				pExporter.doExport(file, pathwayModel);
//				return pExporter.getWarnings();
//			}
//		}
//	} catch (Exception e) {
//		// TODO Auto-generated catch block
//		e.printStackTrace();
//	}
//	return null;
////	if (set != null && set.size() == 1)
////	{
////		PathwayExporter exporter = Utils.oneOf(set);
////		exporter.doExport(file, pathway);
////		return exporter.getWarnings();
////	}
////	else
////		throw new ConverterException( "Could not determine exporter for '" + FileUtils.getExtension(file.toString()) +  "' files" );
//
//}
