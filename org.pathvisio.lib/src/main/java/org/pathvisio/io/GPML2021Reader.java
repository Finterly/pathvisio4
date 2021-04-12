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
package org.pathvisio.io;

import java.awt.Color;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.io.ByteArrayInputStream;

import org.bridgedb.DataSource;
import org.bridgedb.Xref;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.Namespace;
import org.jdom2.input.SAXBuilder;
import org.jdom2.input.sax.XMLReaderJDOMFactory;
import org.jdom2.input.sax.XMLReaderXSDFactory;
import org.pathvisio.debug.Logger;
import org.pathvisio.io.*;
import org.pathvisio.model.*;
import org.pathvisio.model.graphics.*;
import org.pathvisio.model.elements.*;
import org.pathvisio.model.type.*;

/**
 * This class reads a PathwayModel from an input source (GPML 2021).
 * 
 * @author finterly
 */
public class GPML2021Reader extends GpmlFormatAbstract implements GpmlFormatReader {

	public static final GPML2021Reader GPML2021READER = new GPML2021Reader("GPML2021.xsd",
			Namespace.getNamespace("http://pathvisio.org/GPML/2021"));

	protected GPML2021Reader(String xsdFile, Namespace nsGPML) {
		super(xsdFile, nsGPML);
	}

	/**
	 * Reads information from root element of Jdom document {@link Document} to the
	 * pathway model {@link PathwayModel}.
	 * 
	 * NB: Order of reading is done in such as way that referenced elements are read
	 * first. Groups are read first as other pathway elements reference groupRef.
	 * Point and DataNode elementRef are read last to ensure the Pathway Elements
	 * referenced are already instantiated.
	 * 
	 * @param pathwayModel the given pathway model.
	 * @param root         the root element of given Jdom document.
	 * @returns pathwayModel the pathway model after reading root element.
	 * @throws ConverterException
	 */
	public PathwayModel readFromRoot(PathwayModel pathwayModel, Element root) throws ConverterException {

		Pathway pathway = readPathway(root);
		pathwayModel.setPathway(pathway); // TODO, should allow instantiate pathwayModel without Pathway???

		readAuthors(pathwayModel, root);

		readAnnotations(pathwayModel, root);
		readCitations(pathwayModel, root);
		readEvidences(pathwayModel, root);

		readPathwayInfo(pathwayModel, root);
		/* read groups first */
		readGroups(pathwayModel, root);
		readLabels(pathwayModel, root);
		readShapes(pathwayModel, root);
		readDataNodes(pathwayModel, root);
		readInteractions(pathwayModel, root);
		readGraphicalLines(pathwayModel, root);
		/* read elementRefs last */
		readDataNodeElementRef(pathwayModel, root);
		readPointElementRef(pathwayModel, root);

		Logger.log.trace("End reading gpml");

		// TODO check groups have at least one pathwayElement inside?
		// TODO check at least 2 points per line element?
		// TODO handle relative and absolute coordinates
		return pathwayModel;

	}

	/**
	 * Reads pathway information from root element. Instantiates and returns the
	 * pathway object {@link Pathway}.
	 * 
	 * @param root the root element.
	 * @return pathway the pathway object.
	 * @throws ConverterException
	 */
	protected Pathway readPathway(Element root) throws ConverterException {
		String title = root.getAttributeValue("title");
		Element gfx = root.getChild("Graphics", root.getNamespace());
		double boardWidth = Double.parseDouble(gfx.getAttributeValue("boardWidth"));
		double boardHeight = Double.parseDouble(gfx.getAttributeValue("boardHeight"));
		Color backgroundColor = ColorUtils.stringToColor(gfx.getAttributeValue("backgroundColor")); // TODO optional?
		Coordinate infoBox = readInfoBox(root);
		Pathway pathway = new Pathway.PathwayBuilder(title, boardWidth, boardHeight, backgroundColor, infoBox).build();
		/* optional properties */
		Xref xref = readXref(root);
		String organism = root.getAttributeValue("organism");
		String source = root.getAttributeValue("source");
		String version = root.getAttributeValue("version");
		String license = root.getAttributeValue("license");
		if (xref != null)
			pathway.setXref(xref);
		if (organism != null)
			pathway.setOrganism(organism);
		if (source != null)
			pathway.setSource(source);
		if (version != null)
			pathway.setVersion(version);
		if (license != null)
			pathway.setLicense(license);
		return pathway;
	}

	/**
	 * Reads xref {@link Xref} information from element. Xref is required for
	 * DataNodes, Interactions, Citations and Evidences. Xref is optional for the
	 * Pathway, States, Groups, and Annotations.
	 * 
	 * @param e the element.
	 * @return xref the new xref or null if no or invalid xref information.
	 * @throws ConverterException
	 */
	protected Xref readXref(Element e) throws ConverterException {
		Element xref = e.getChild("Xref", e.getNamespace());
		if (xref != null) {
			String identifier = xref.getAttributeValue("identifier");
			String dataSource = xref.getAttributeValue("dataSource");
			if (DataSource.fullNameExists(dataSource)) {
				return new Xref(identifier, DataSource.getExistingByFullName(dataSource));
			} else if (DataSource.systemCodeExists(dataSource)) {
				return new Xref(identifier, DataSource.getByAlias(dataSource));
			} else {
				System.out.println("Invalid xref dataSource: " + dataSource);
				return null; // TODO how to handle better
//			throw new IllegalArgumentException("Invalid xref dataSource: " + dataSource);
			}
		}
		return null;
	}

	/**
	 * Reads the infobox x and y coordinate {@link Pathway#setInfoBox()}
	 * information.
	 * 
	 * @param root the root element.
	 * @return the infoBox as coordinates.
	 */
	protected Coordinate readInfoBox(Element root) {
		Element ifbx = root.getChild("InfoBox", root.getNamespace());
		double centerX = Double.parseDouble(ifbx.getAttributeValue("centerX"));
		double centerY = Double.parseDouble(ifbx.getAttributeValue("centerY"));
		return new Coordinate(centerX, centerY);
	}

	/**
	 * Reads author {@link Author} information for pathway model from root element.
	 * 
	 * @param pathwayModel the pathway model.
	 * @param root         the root element.
	 * @throws ConverterException
	 */
	protected void readAuthors(PathwayModel pathwayModel, Element root) throws ConverterException {
		Element aus = root.getChild("Authors", root.getNamespace());
		if (aus != null) {
			for (Element au : aus.getChildren("Author", aus.getNamespace())) {
				String name = au.getAttributeValue("name");
				String fullName = au.getAttributeValue("fullName");
				String email = au.getAttributeValue("email");
				Author author = new Author.AuthorBuilder(name).build();
				if (fullName != null)
					author.setFullName(fullName);
				if (email != null)
					author.setEmail(email);
				if (author != null)
					pathwayModel.addAuthor(author);
			}
		}
	}

	/**
	 * Reads annotation {@link Annotation} information for pathway model from root
	 * element.
	 * 
	 * @param pathwayModel the pathway model.
	 * @param root         the root element.
	 * @throws ConverterException
	 */
	protected void readAnnotations(PathwayModel pathwayModel, Element root) throws ConverterException {
		Element annts = root.getChild("Annotations", root.getNamespace());
		if (annts != null) {
			for (Element annt : annts.getChildren("Annotation", annts.getNamespace())) {
				String elementId = annt.getAttributeValue("elementId");
				String value = annt.getAttributeValue("value");
				AnnotationType type = AnnotationType.register(annt.getAttributeValue("type"));
				Annotation annotation = new Annotation(elementId, pathwayModel, value, type);
				/* optional properties */
				Xref xref = readXref(annt);
				String url = annt.getAttributeValue("url");
				if (xref != null)
					annotation.setXref(xref);
				if (url != null)
					annotation.setUrl(url);
				if (annotation != null)
					pathwayModel.addAnnotation(annotation);
			}
		}
	}

	/**
	 * Reads citation {@link Citation} information for pathway model from root
	 * element.
	 * 
	 * @param pathwayModel the pathway model.
	 * @param root         the root element.
	 * @throws ConverterException
	 */
	protected void readCitations(PathwayModel pathwayModel, Element root) throws ConverterException {
		Element cits = root.getChild("Citations", root.getNamespace());
		if (cits != null) {
			for (Element cit : cits.getChildren("Citation", cits.getNamespace())) {
				String elementId = cit.getAttributeValue("elementId");
				Xref xref = readXref(cit);
				Citation citation = new Citation(elementId, pathwayModel, xref);
				/* optional properties */
				String url = cit.getAttributeValue("url");
				if (url != null)
					citation.setUrl(url);
				if (citation != null)
					pathwayModel.addCitation(citation);
			}
		}
	}

	/**
	 * Reads evidence {@link Evidence} information for pathway model from root
	 * element.
	 * 
	 * @param pathwayModel the pathway model.
	 * @param root         the root element.
	 * @throws ConverterException
	 */
	protected void readEvidences(PathwayModel pathwayModel, Element root) throws ConverterException {
		Element evids = root.getChild("Evidences", root.getNamespace());
		if (evids != null) {
			for (Element evid : evids.getChildren("Evidence", evids.getNamespace())) {
				String elementId = evid.getAttributeValue("elementId");
				Xref xref = readXref(evid);
				Evidence evidence = new Evidence(elementId, pathwayModel, xref);
				/* optional properties */
				String value = evid.getAttributeValue("value");
				String url = evid.getAttributeValue("url");
				if (value != null)
					evidence.setValue(value);
				if (url != null)
					evidence.setUrl(url);
				if (evidence != null)
					pathwayModel.addEvidence(evidence);
			}
		}
	}

	/**
	 * Reads comment group (comment, dynamic property, annotationRef, citationRef)
	 * and evidencRef information {@link PathwayModel} for pathway model from root
	 * element.
	 * 
	 * @param pathwayModel the pathway model.
	 * @param root         the root element.
	 * @throws ConverterException
	 */
	protected void readPathwayInfo(PathwayModel pathwayModel, Element root) throws ConverterException {
		readPathwayComments(pathwayModel, root);
		readPathwayDynamicProperties(pathwayModel, root);
		readPathwayAnnotationRefs(pathwayModel, root);
		readPathwayCitationRefs(pathwayModel, root);
		readPathwayEvidenceRefs(pathwayModel, root);
	}

	/**
	 * Reads comment {@link Comment} information for pathway from root element.
	 * 
	 * @param pathwayModel the pathway model.
	 * @param root         the root element.
	 * @throws ConverterException
	 */
	protected void readPathwayComments(PathwayModel pathwayModel, Element root) throws ConverterException {
		for (Element cmt : root.getChildren("Comment", root.getNamespace())) {
			String source = cmt.getAttributeValue("source");
			String content = cmt.getText();
			if (content != null && !content.equals("")) {
				Comment comment = new Comment(content); // TODO needs parent pathwayModel?
				if (source != null && !source.equals(""))
					comment.setSource(source);
				pathwayModel.getPathway().addComment(new Comment(source, content));
			}
		}
	}

	/**
	 * Reads dynamic property {@link Pathway#setDynamicProperty()} information for
	 * pathway from root element.
	 * 
	 * @param pathwayModel the pathway model.
	 * @param root         the root element.
	 * @throws ConverterException
	 */
	protected void readPathwayDynamicProperties(PathwayModel pathwayModel, Element root) throws ConverterException {
		for (Element dp : root.getChildren("Property", root.getNamespace())) {
			String key = dp.getAttributeValue("key");
			String value = dp.getAttributeValue("value");
			pathwayModel.getPathway().setDynamicProperty(key, value);
		}
	}

	/**
	 * Reads annotation reference {@link Pathway#addAnnotationRef()} information for
	 * pathway from root element.
	 * 
	 * @param pathwayModel the pathway model.
	 * @param root         the root element.
	 * @throws ConverterException
	 */
	protected void readPathwayAnnotationRefs(PathwayModel pathwayModel, Element root) throws ConverterException {
		for (Element anntRef : root.getChildren("AnnotationRef", root.getNamespace())) {
			Annotation annotation = (Annotation) pathwayModel
					.getPathwayElement(anntRef.getAttributeValue("elementRef"));
			AnnotationRef annotationRef = new AnnotationRef(annotation);
			for (Element citRef : anntRef.getChildren("CitationRef", anntRef.getNamespace())) {
				Citation citationRef = (Citation) pathwayModel
						.getPathwayElement(citRef.getAttributeValue("elementRef"));
				if (citationRef != null)
					annotationRef.addCitationRef(citationRef);
			}
			for (Element evidRef : anntRef.getChildren("EvidenceRef", anntRef.getNamespace())) {
				Evidence evidenceRef = (Evidence) pathwayModel
						.getPathwayElement(evidRef.getAttributeValue("elementRef"));
				if (evidenceRef != null)
					annotationRef.addEvidenceRef(evidenceRef);
			}
			pathwayModel.getPathway().addAnnotationRef(annotationRef);
		}
	}

	/**
	 * Reads citation reference {@link Pathway#addCitationRef()} information for
	 * pathway model from root element.
	 * 
	 * @param pathwayModel the pathway model.
	 * @param root         the root element.
	 * @throws ConverterException
	 */
	protected void readPathwayCitationRefs(PathwayModel pathwayModel, Element root) throws ConverterException {
		for (Element citRef : root.getChildren("CitationRef", root.getNamespace())) {
			Citation citationRef = (Citation) pathwayModel.getPathwayElement(citRef.getAttributeValue("elementRef"));
			if (citationRef != null)
				pathwayModel.getPathway().addCitationRef(citationRef);
		}
	}

	/**
	 * Reads evidence reference {@link Pathway#addEvidenceRef()} information for
	 * pathway from root element.
	 * 
	 * @param pathwayModel the pathway model.
	 * @param root         the root element.
	 * @throws ConverterException
	 */
	protected void readPathwayEvidenceRefs(PathwayModel pathwayModel, Element root) throws ConverterException {
		for (Element evidRef : root.getChildren("EvidenceRef", root.getNamespace())) {
			Evidence evidenceRef = (Evidence) pathwayModel.getPathwayElement(evidRef.getAttributeValue("elementRef"));
			if (evidenceRef != null)
				pathwayModel.getPathway().addEvidenceRef(evidenceRef);
		}
	}

	/**
	 * Reads group {@link Group} information for pathway model from root element.
	 * 
	 * @param pathwayModel the pathway model.
	 * @param root         the root element.
	 * @throws ConverterException
	 */
	protected void readGroups(PathwayModel pathwayModel, Element root) throws ConverterException {
		Element grps = root.getChild("Groups", root.getNamespace());
		if (grps != null) {
			for (Element grp : grps.getChildren("Group", grps.getNamespace())) {
				String elementId = grp.getAttributeValue("elementId");
				GroupType type = GroupType.register(grp.getAttributeValue("type"));
				Element gfx = grp.getChild("Graphics", grp.getNamespace());
				RectProperty rectProperty = readRectProperty(gfx);
				FontProperty fontProperty = readFontProperty(gfx);
				ShapeStyleProperty shapeStyleProperty = readShapeStyleProperty(gfx);
				Group group = new Group(elementId, pathwayModel, rectProperty, fontProperty, shapeStyleProperty, type);
				/* read comment group, evidenceRefs */
				readElementInfo(group, grp);
				/* set optional properties */
				String textLabel = grp.getAttributeValue("textLabel");
				Xref xref = readXref(grp);
				if (xref != null)
					group.setXref(xref);
				if (textLabel != null)
					group.setTextLabel(textLabel);
				if (group != null)
					pathwayModel.addGroup(group);
			}
			/**
			 * Because a group may refer to another group not yet initialized. We read all
			 * group elements before setting groupRef.
			 */
			for (Element grp : grps.getChildren("Group", grps.getNamespace())) {
				String groupRef = grp.getAttributeValue("groupRef");
				if (groupRef != null && !groupRef.equals("")) {
					String elementId = grp.getAttributeValue("elementId");
					Group group = (Group) pathwayModel.getPathwayElement(elementId);
					group.setGroupRef((Group) group.getPathwayModel().getPathwayElement(groupRef));
				}
			}
		}
	}

	/**
	 * Reads label {@link Label} information for pathway model from root element.
	 * 
	 * @param pathwayModel the pathway model.
	 * @param root         the root element.
	 * @throws ConverterException
	 */
	protected void readLabels(PathwayModel pathwayModel, Element root) throws ConverterException {
		Element lbs = root.getChild("Labels", root.getNamespace());
		if (lbs != null) {
			for (Element lb : lbs.getChildren("Label", lbs.getNamespace())) {
				String elementId = lb.getAttributeValue("elementId");
				String textLabel = lb.getAttributeValue("textLabel");
				Element gfx = lb.getChild("Graphics", lb.getNamespace());
				RectProperty rectProperty = readRectProperty(gfx);
				FontProperty fontProperty = readFontProperty(gfx);
				ShapeStyleProperty shapeStyleProperty = readShapeStyleProperty(gfx);
				Label label = new Label(elementId, pathwayModel, rectProperty, fontProperty, shapeStyleProperty,
						textLabel);
				/* read comment group, evidenceRefs */
				readElementInfo(label, lb);
				/* set optional properties */
				String href = lb.getAttributeValue("href");
				String groupRef = lb.getAttributeValue("groupRef");
				if (href != null)
					label.setHref(href);
				if (groupRef != null && !groupRef.equals(""))
					label.setGroupRef((Group) label.getPathwayModel().getPathwayElement(groupRef));
				if (label != null)
					pathwayModel.addLabel(label);
			}
		}
	}

	/**
	 * Reads shape {@link Shape} information for pathway model from root element.
	 * 
	 * @param pathwayModel the pathway model.
	 * @param root         the root element.
	 * @throws ConverterException
	 */
	protected void readShapes(PathwayModel pathwayModel, Element root) throws ConverterException {
		Element shps = root.getChild("Shapes", root.getNamespace());
		if (shps != null) {
			for (Element shp : shps.getChildren("Shape", shps.getNamespace())) {
				String elementId = shp.getAttributeValue("elementId");
				Element gfx = shp.getChild("Graphics", shp.getNamespace());
				RectProperty rectProperty = readRectProperty(gfx);
				FontProperty fontProperty = readFontProperty(gfx);
				ShapeStyleProperty shapeStyleProperty = readShapeStyleProperty(gfx);
				double rotation = Double.parseDouble(gfx.getAttributeValue("rotation"));
				Shape shape = new Shape(elementId, pathwayModel, rectProperty, fontProperty, shapeStyleProperty,
						rotation);
				/* read comment group, evidenceRefs */
				readElementInfo(shape, shp);
				/* set optional properties */
				String textLabel = shp.getAttributeValue("textLabel");
				String groupRef = shp.getAttributeValue("groupRef");
				if (textLabel != null)
					shape.setTextLabel(textLabel);
				if (groupRef != null && !groupRef.equals(""))
					shape.setGroupRef((Group) shape.getPathwayModel().getPathwayElement(groupRef));
				if (shape != null)
					pathwayModel.addShape(shape);
			}
		}
	}

	/**
	 * Reads data node {@link DataNode} information for pathway model from root
	 * element.
	 * 
	 * @param pathwayModel the pathway model.
	 * @param root         the root element.
	 * @throws ConverterException
	 */
	protected void readDataNodes(PathwayModel pathwayModel, Element root) throws ConverterException {
		Element dns = root.getChild("DataNodes", root.getNamespace());
		if (dns != null) {
			for (Element dn : dns.getChildren("DataNode", dns.getNamespace())) {
				String elementId = dn.getAttributeValue("elementId");
				Element gfx = dn.getChild("Graphics", dn.getNamespace());
				RectProperty rectProperty = readRectProperty(gfx);
				FontProperty fontProperty = readFontProperty(gfx);
				ShapeStyleProperty shapeStyleProperty = readShapeStyleProperty(gfx);
				String textLabel = dn.getAttributeValue("textLabel");
				DataNodeType type = DataNodeType.register(dn.getAttributeValue("type"));
				Xref xref = readXref(dn);
				DataNode dataNode = new DataNode(elementId, pathwayModel, rectProperty, fontProperty,
						shapeStyleProperty, textLabel, type, xref);
				/* read comment group, evidenceRefs */
				readElementInfo(dataNode, dn);
				/* read states */
				readStates(dataNode, dn);
				/* set optional properties */
				String groupRef = dn.getAttributeValue("groupRef");
				if (groupRef != null && !groupRef.equals(""))
					dataNode.setGroupRef((Group) pathwayModel.getPathwayElement(groupRef));
				if (dataNode != null)
					pathwayModel.addDataNode(dataNode);
			}
		}
	}

	/**
	 * TODO should absolute x and y be optional?
	 * 
	 * Reads state {@link State} information for data node from element.
	 * 
	 * @param dataNode the data node object {@link DataNode}.
	 * @param dn       the data node element.
	 * @throws ConverterException
	 */
	protected void readStates(DataNode dataNode, Element dn) throws ConverterException {
		Element sts = dn.getChild("States", dn.getNamespace());
		if (sts != null) {
			for (Element st : sts.getChildren("State", sts.getNamespace())) {
				String elementId = st.getAttributeValue("elementId");
				String textLabel = st.getAttributeValue("textLabel");
				StateType type = StateType.register(st.getAttributeValue("type"));
				Element gfx = st.getChild("Graphics", st.getNamespace());
				double relX = Double.parseDouble(gfx.getAttributeValue("relX"));
				double relY = Double.parseDouble(gfx.getAttributeValue("relY"));
				double width = Double.parseDouble(gfx.getAttributeValue("width"));
				double height = Double.parseDouble(gfx.getAttributeValue("height"));
				FontProperty fontProperty = readFontProperty(gfx);
				ShapeStyleProperty shapeStyleProperty = readShapeStyleProperty(gfx);
				State state = new State(elementId, dataNode.getPathwayModel(), dataNode, textLabel, type, relX, relY,
						width, height, fontProperty, shapeStyleProperty);
				/* read comment group, evidenceRefs */
				readElementInfo(state, st);
				/* set optional properties */
				Xref xref = readXref(st);
				if (xref != null)
					state.setXref(xref);
				if (state != null)
					dataNode.addState(state);
			}
		}
	}

	/**
	 * Reads interaction {@link Interaction} information for pathway model from root
	 * element.
	 * 
	 * @param pathwayModel the pathway model.
	 * @param root         the root element.
	 * @throws ConverterException
	 */
	protected void readInteractions(PathwayModel pathwayModel, Element root) throws ConverterException {
		Element ias = root.getChild("Interactions", root.getNamespace());
		if (ias != null) {
			for (Element ia : ias.getChildren("Interaction", ias.getNamespace())) {
				String elementId = ia.getAttributeValue("elementId");
				Element gfx = ia.getChild("Graphics", ia.getNamespace());
				LineStyleProperty lineStyleProperty = readLineStyleProperty(gfx);
				Xref xref = readXref(ia);
				Interaction interaction = new Interaction(elementId, pathwayModel, lineStyleProperty, xref);
				/* read comment group, evidenceRefs */
				readLineElement(interaction, ia);
				if (interaction != null) {
					if (interaction.getPoints().size() < 2) {
						System.out.println("Interaction elementId:" + elementId + "has" + interaction.getPoints().size()
								+ " points,  must have at least 2 points");// TODO error!
					}
					pathwayModel.addInteraction(interaction);
				}
			}
		}
	}

	/**
	 * Reads graphical line {@link GraphicalLine} information for pathway model from
	 * root element.
	 * 
	 * @param pathwayModel the pathway model.
	 * @param root         the root element.
	 * @throws ConverterException
	 */
	protected void readGraphicalLines(PathwayModel pathwayModel, Element root) throws ConverterException {
		Element glns = root.getChild("GraphicaLines", root.getNamespace());
		if (glns != null) {
			for (Element gln : glns.getChildren("GraphicaLine", glns.getNamespace())) {
				String elementId = gln.getAttributeValue("elementId");
				Element gfx = gln.getChild("Graphics", gln.getNamespace());
				LineStyleProperty lineStyleProperty = readLineStyleProperty(gfx);
				GraphicalLine graphicalLine = new GraphicalLine(elementId, pathwayModel, lineStyleProperty);
				readLineElement(graphicalLine, gln);
				if (graphicalLine != null)
					if (graphicalLine.getPoints().size() < 2) {
						System.out.println("GraphicalLine elementId:" + elementId + "has"
								+ graphicalLine.getPoints().size() + " points,  must have at least 2 points");// TODO //
																												// error!
					}
				pathwayModel.addGraphicalLine(graphicalLine);
			}
		}
	}

	/**
	 * Reads line element {@link LineElement} information for interaction or
	 * graphical line from element.
	 * 
	 * @param lineElement the line element object.
	 * @param ln          the line element.
	 * @throws ConverterException
	 */
	protected void readLineElement(LineElement lineElement, Element ln) throws ConverterException {
		readElementInfo(lineElement, ln); // comment group and evidenceRef
		Element wyps = ln.getChild("Waypoints", ln.getNamespace());
		readPoints(lineElement, wyps);
		readAnchors(lineElement, wyps);
		/* set optional properties */
		String groupRef = ln.getAttributeValue("groupRef");
		if (groupRef != null && !groupRef.equals(""))
			lineElement.setGroupRef((Group) lineElement.getPathwayModel().getPathwayElement(groupRef));
	}

	/**
	 * Reads point {@link Point} information for line element from element.
	 * 
	 * @param lineElement the line element object.
	 * @param wyps        the waypoints element.
	 * @throws ConverterException
	 */
	protected void readPoints(LineElement lineElement, Element wyps) throws ConverterException {
		for (Element pt : wyps.getChildren("Point", wyps.getNamespace())) {
			String elementId = pt.getAttributeValue("elementId");
			ArrowHeadType arrowHead = ArrowHeadType.register(pt.getAttributeValue("arrowHead"));
			Coordinate xy = new Coordinate(Double.parseDouble(pt.getAttributeValue("x")),
					Double.parseDouble(pt.getAttributeValue("y")));
			Point point = new Point(elementId, lineElement.getPathwayModel(), arrowHead, xy);
			if (point != null) // set elementRef and optional properties later
				lineElement.addPoint(point);
		}
	}

	/**
	 * Reads anchor {@link Anchor} information for line element from element.
	 * 
	 * @param lineElement the line element object.
	 * @param wyps        the waypoints element.
	 * @throws ConverterException
	 */
	protected void readAnchors(LineElement lineElement, Element wyps) throws ConverterException {
		for (Element an : wyps.getChildren("Anchor", wyps.getNamespace())) {
			String elementId = an.getAttributeValue("elementId");
			double position = Double.parseDouble(an.getAttributeValue("position"));
			Coordinate xy = new Coordinate(Double.parseDouble(an.getAttributeValue("x")),
					Double.parseDouble(an.getAttributeValue("y")));
			AnchorType shapeType = AnchorType.register(an.getAttributeValue("shapeType"));
			Anchor anchor = new Anchor(elementId, lineElement.getPathwayModel(), position, xy, shapeType);
			if (anchor != null)
				lineElement.addAnchor(anchor);
		}
	}

	/**
	 * Reads elementRef {@link DataNode#setElementRef()} for pathway model
	 * datanodes.
	 * 
	 * @param pathwayModel the pathway model.
	 * @param root         the root element.
	 * @throws ConverterException
	 */
	protected void readDataNodeElementRef(PathwayModel pathwayModel, Element root) throws ConverterException {
		Element dns = root.getChild("DataNodes", root.getNamespace());
		for (Element dn : dns.getChildren("DataNode", dns.getNamespace())) {
			String elementRef = dn.getAttributeValue("elementRef");
			if (elementRef != null && !elementRef.equals("")) {
				PathwayElement elemRf = pathwayModel.getPathwayElement(elementRef);
				if (elemRf != null) {
					String elementId = dn.getAttributeValue("elementId");
					DataNode dataNode = (DataNode) pathwayModel.getPathwayElement(elementId);
					dataNode.setElementRef(elemRf);
				}
			}
		}
	}

	/**
	 * Reads elementRef {@link Point#setElementRef()} for pathway model points.
	 * 
	 * @param pathwayModel the pathway model.
	 * @param root         the root element.
	 * @throws ConverterException
	 */
	protected void readPointElementRef(PathwayModel pathwayModel, Element root) throws ConverterException {
		List<String> lnElementNames = Collections.unmodifiableList(Arrays.asList("Interactions", "GraphicalLines"));
		List<String> lnElementName = Collections.unmodifiableList(Arrays.asList("Interaction", "GraphicalLine"));
		for (int i = 0; i < lnElementNames.size(); i++) {
			Element ias = root.getChild(lnElementNames.get(i), root.getNamespace());
			if (ias != null) {
				for (Element ia : ias.getChildren(lnElementName.get(i), ias.getNamespace())) {
					Element wyps = ia.getChild("Waypoints", ia.getNamespace());
					for (Element pt : wyps.getChildren("Point", wyps.getNamespace())) {
						String elementRef = pt.getAttributeValue("elementRef");
						if (elementRef != null && !elementRef.equals("")) {
							PathwayElement elemRf = pathwayModel.getPathwayElement(elementRef);
							if (elemRf != null) {
								String elementId = pt.getAttributeValue("elementId");
								Point point = (Point) pathwayModel.getPathwayElement(elementId);
								point.setElementRef(elemRf);
								point.setRelX(Double.parseDouble(pt.getAttributeValue("relX")));
								point.setRelY(Double.parseDouble(pt.getAttributeValue("relY")));
							}
						}
					}
				}
			}
		}
	}

	/**
	 * Reads comment group (comment, dynamic property, annotationRef, citationRef)
	 * and elementRef {@link ElementInfo} information, , for pathway element from
	 * element.
	 * 
	 * @param elementInfo the element info pathway element object.
	 * @param e           the pathway element element.
	 * @throws ConverterException
	 */
	private void readElementInfo(ElementInfo elementInfo, Element e) throws ConverterException {
		readComments(elementInfo, e);
		readDynamicProperties(elementInfo, e);
		readAnnotationRefs(elementInfo, e);
		readCitationRefs(elementInfo, e);
		readEvidenceRefs(elementInfo, e);
	}

	/**
	 * Reads comment {@link Comment} information for pathway element from element.
	 * 
	 * @param elementInfo the element info pathway element object.
	 * @param e           the pathway element element.
	 * @throws ConverterException
	 */
	protected void readComments(ElementInfo elementInfo, Element e) throws ConverterException {
		for (Element cmt : e.getChildren("Comment", e.getNamespace())) {
			String source = cmt.getAttributeValue("source");
			String content = cmt.getText();
			if (content != null && !content.equals("")) {
				Comment comment = new Comment(content); // TODO needs parent pathwayModel?
				if (source != null && !source.equals(""))
					comment.setSource(source);
				elementInfo.addComment(new Comment(source, content));
			}
		}
	}

	/**
	 * Reads dynamic property {@link ElementInfo#setDynamicProperty()} information
	 * for pathway element from element.
	 * 
	 * @param elementInfo the element info pathway element object .
	 * @param e           the pathway element element.
	 * @throws ConverterException
	 */
	protected void readDynamicProperties(ElementInfo elementInfo, Element e) throws ConverterException {
		for (Element dp : e.getChildren("Property", e.getNamespace())) {
			String key = dp.getAttributeValue("key");
			String value = dp.getAttributeValue("value");
			elementInfo.setDynamicProperty(key, value);
		}
	}

	/**
	 * Reads annotationRef {@link ElementInfo#addAnnotationRef()} information for
	 * pathway element from element.
	 * 
	 * @param elementInfo the element info pathway element object.
	 * @param e           the pathway element element.
	 * @throws ConverterException
	 */
	protected void readAnnotationRefs(ElementInfo elementInfo, Element e) throws ConverterException {
		for (Element anntRef : e.getChildren("AnnotationRef", e.getNamespace())) {
			Annotation annotation = (Annotation) elementInfo.getPathwayModel()
					.getPathwayElement(anntRef.getAttributeValue("elementRef"));
			AnnotationRef annotationRef = new AnnotationRef(annotation);
			for (Element citRef : anntRef.getChildren("CitationRef", anntRef.getNamespace())) {
				Citation citationRef = (Citation) elementInfo.getPathwayModel()
						.getPathwayElement(citRef.getAttributeValue("elementRef"));
				if (citationRef != null)
					annotationRef.addCitationRef(citationRef);
			}
			for (Element evidRef : anntRef.getChildren("EvidenceRef", anntRef.getNamespace())) {
				Evidence evidenceRef = (Evidence) elementInfo.getPathwayModel()
						.getPathwayElement(evidRef.getAttributeValue("elementRef"));
				if (evidenceRef != null)
					annotationRef.addEvidenceRef(evidenceRef);
			}
			elementInfo.addAnnotationRef(annotationRef);
		}
	}

	/**
	 * Reads citationRef {@link ElementInfo#addCitationRef()} information for
	 * pathway element from element.
	 * 
	 * @param elementInfo the element info pathway element object.
	 * @param e           the pathway element element.
	 * @throws ConverterException
	 */
	protected void readCitationRefs(ElementInfo elementInfo, Element e) throws ConverterException {
		for (Element citRef : e.getChildren("CitationRef", e.getNamespace())) {
			Citation citationRef = (Citation) elementInfo.getPathwayModel()
					.getPathwayElement(citRef.getAttributeValue("elementRef"));
			if (citationRef != null) {
				elementInfo.addCitationRef(citationRef);
			}
		}
	}

	/**
	 * Reads evidenceRef {@link ElementInfo#addEvidenceRef()} information for
	 * pathway element from element.
	 * 
	 * @param elementInfo the element info pathway element object.
	 * @param e           the pathway element element.
	 * @throws ConverterException
	 */
	protected void readEvidenceRefs(ElementInfo elementInfo, Element e) throws ConverterException {
		for (Element evidRef : e.getChildren("EvidenceRef", e.getNamespace())) {
			Evidence evidenceRef = (Evidence) elementInfo.getPathwayModel()
					.getPathwayElement(evidRef.getAttributeValue("elementRef"));
			if (evidenceRef != null)
				elementInfo.addEvidenceRef(evidenceRef);
		}
	}

	/**
	 * Reads rect property {@link RectProperty} information. Jdom handles schema
	 * default values.
	 * 
	 * @param gfx the parent graphics element.
	 * @throws ConverterException
	 */
	protected RectProperty readRectProperty(Element gfx) throws ConverterException {
		double centerX = Double.parseDouble(gfx.getAttributeValue("centerX"));
		double centerY = Double.parseDouble(gfx.getAttributeValue("centerY"));
		double width = Double.parseDouble(gfx.getAttributeValue("width"));
		double height = Double.parseDouble(gfx.getAttributeValue("height"));
		return new RectProperty(new Coordinate(centerX, centerY), width, height);
	}

	/**
	 * Reads font property {@link FontProperty} information. Jdom handles schema
	 * default values.
	 * 
	 * @param gfx the parent graphics element.
	 * @throws ConverterException
	 */
	protected FontProperty readFontProperty(Element gfx) throws ConverterException {
		Color textColor = ColorUtils.stringToColor(gfx.getAttributeValue("textColor"));
		String fontName = gfx.getAttributeValue("fontName");
		boolean fontWeight = gfx.getAttributeValue("fontWeight").equals("Bold");
		boolean fontStyle = gfx.getAttributeValue("fontStyle").equals("Italic");
		boolean fontDecoration = gfx.getAttributeValue("fontDecoration").equals("Underline");
		boolean fontStrikethru = gfx.getAttributeValue("fontStrikethru").equals("Strikethru");
		int fontSize = Integer.parseInt(gfx.getAttributeValue("fontSize"));
		HAlignType hAlignType = HAlignType.fromName(gfx.getAttributeValue("hAlign"));
		VAlignType vAlignType = VAlignType.fromName(gfx.getAttributeValue("vAlign"));
		return new FontProperty(textColor, fontName, fontWeight, fontStyle, fontDecoration, fontStrikethru, fontSize,
				hAlignType, vAlignType);
	}

	/**
	 * Reads shape style property {@link ShapeStyleProperty} information. Jdom
	 * handles schema default values.
	 * 
	 * @param gfx the parent graphics element.
	 * @throws ConverterException
	 */
	protected ShapeStyleProperty readShapeStyleProperty(Element gfx) throws ConverterException {
		Color borderColor = ColorUtils.stringToColor(gfx.getAttributeValue("borderColor"));
		LineStyleType borderStyle = LineStyleType.register(gfx.getAttributeValue("borderStyle"));
		double borderWidth = Double.parseDouble(gfx.getAttributeValue("borderWidth"));
		Color fillColor = ColorUtils.stringToColor(gfx.getAttributeValue("fillColor"));
		ShapeType shapeType = ShapeType.register(gfx.getAttributeValue("shapeType"));
		String zOrder = gfx.getAttributeValue("zOrder");
		ShapeStyleProperty shapeStyleProperty = new ShapeStyleProperty(borderColor, borderStyle, borderWidth, fillColor,
				shapeType);
		if (zOrder != null) {
			shapeStyleProperty.setZOrder(Integer.parseInt(zOrder));
		}
		return shapeStyleProperty;
	}

	/**
	 * Reads line style property {@link LineStyleProperty} information. Jdom handles
	 * schema default values.
	 * 
	 * @param gfx the parent graphics element.
	 * @throws ConverterException
	 */
	protected LineStyleProperty readLineStyleProperty(Element gfx) throws ConverterException {
		Color lineColor = ColorUtils.stringToColor(gfx.getAttributeValue("lineColor"));
		LineStyleType lineStyle = LineStyleType.register(gfx.getAttributeValue("lineStyle"));
		double lineWidth = Double.parseDouble(gfx.getAttributeValue("lineWidth"));
		ConnectorType connectorType = ConnectorType.register(gfx.getAttributeValue("connectorType"));
		String zOrder = gfx.getAttributeValue("zOrder");
		LineStyleProperty lineStyleProperty = new LineStyleProperty(lineColor, lineStyle, lineWidth, connectorType);
		if (zOrder != null) {
			lineStyleProperty.setZOrder(Integer.parseInt(zOrder));
		}
		return lineStyleProperty;
	}

	/*---------------------------------------------------------------------------*/

	/*--------------------THESE METHODS MOVED TO GpmlFormatAbstract.java-------------------------------*/

	/*------------------------------------MY METHODS --------------------------------------*/

//	public void readFromRoot(Element root, PathwayModel pathwayModel) throws ConverterException {
//		// TODO Auto-generated method stub
//		
//	}
//
//	public PathwayModel readFromXml(InputStream is) throws ConverterException {
//		PathwayModel pathwayModel = null;
//		try {
//			XMLReaderJDOMFactory schemafactory = new XMLReaderXSDFactory(xsdFile); // schema
//			SAXBuilder builder = new SAXBuilder(schemafactory);
//			Document doc = builder.build(is);
//			Element root = doc.getRootElement();
//			System.out.println("Root: " + doc.getRootElement());
//			pathwayModel = readFromRoot(pathwayModel, root);
//		} catch (JDOMException e) {
//			throw new ConverterException(e);
//		} catch (IOException e) {
//			throw new ConverterException(e);
//		} catch (Exception e) {
//			throw new ConverterException(e); // TODO e.printStackTrace()?
//		}
//		return pathwayModel;// TODO do we want to return pathway or not?
//	}
//
//	/**
//	 * Read the JDOM document from the file specified
//	 * 
//	 * @param file the file from which the JDOM document should be read.
//	 * @throws ConverterException
//	 */
//	public PathwayModel readFromXml(File file) throws ConverterException {
//		InputStream is;
//		try {
//			is = new FileInputStream(file);
//		} catch (FileNotFoundException e) {
//			throw new ConverterException(e);
//		}
//		return readFromXml(is);
//	}
//
//	/**
//	 * Read the JDOM document from the file specified
//	 * 
//	 * @param s      the string input.
//	 * @param string the file from which the JDOM document should be read.
//	 * @throws ConverterException
//	 */
//	public PathwayModel readFromXml(String str) throws ConverterException {
//		if (str == null)
//			return null;
//		InputStream is;
//		try {
//			is = stringToInputStream(str);// TODO does this work?
//		} catch (Exception e) {
//			throw new ConverterException(e);
//		}
//		return readFromXml(is);
//	}
//
//	// METHOD FROM UTILS
//	public static InputStream stringToInputStream(String str) {
//		if (str == null)
//			return null;
//		InputStream is = null;
//		try {
//			is = new ByteArrayInputStream(str.getBytes(StandardCharsets.UTF_8));
//		} catch (Exception ex) {
//		}
//		return is;
//	}

	// TODO PROBLEM CASTING?
//	protected void readGroupRefs(PathwayModel pathwayModel, Element root) {
//		List<String> shpElements = Collections
//				.unmodifiableList(Arrays.asList("DataNodes", "Labels", "Shapes", "Groups"));
//		List<String> shpElement = Collections.unmodifiableList(Arrays.asList("DataNode", "Label", "Shape", "Group"));
//		for (int i = 0; i < shpElements.size(); i++) {
//			Element grps = root.getChild(shpElements.get(i), root.getNamespace());
//			for (Element grp : grps.getChildren(shpElement.get(i), grps.getNamespace())) {
//				String groupRef = grp.getAttributeValue("groupRef");
//				if (groupRef != null && !groupRef.equals("")) {
//					String elementId = grp.getAttributeValue("elementId");
//					ShapedElement shapedElement = (ShapedElement) pathwayModel.getPathwayElement(elementId);
//					shapedElement.setGroupRef((Group) pathwayModel.getPathwayElement(groupRef));
//				}
//			}
//		}
//		List<String> lnElements = Collections.unmodifiableList(Arrays.asList("Interactions", "GraphicalLines"));
//		List<String> lnElement = Collections.unmodifiableList(Arrays.asList("Interaction", "GraphicalLine"));
//		for (int i = 0; i < shpElements.size(); i++) {
//			Element grps = root.getChild(lnElements.get(i), root.getNamespace());
//			for (Element grp : grps.getChildren(lnElement.get(i), grps.getNamespace())) {
//				String groupRef = grp.getAttributeValue("groupRef");
//				if (groupRef != null && !groupRef.equals("")) {
//					String elementId = grp.getAttributeValue("elementId");
//					LineElement lineElement = (LineElement) pathwayModel.getPathwayElement(elementId);
//					lineElement.setGroupRef((Group) pathwayModel.getPathwayElement(groupRef));
//				}
//			}
//		}
//	}

}
