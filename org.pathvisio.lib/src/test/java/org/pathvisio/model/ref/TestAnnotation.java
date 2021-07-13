package org.pathvisio.model.ref;

import org.pathvisio.model.PathwayModel;
import org.pathvisio.model.element.DataNode;
import org.pathvisio.model.type.AnnotationType;

import junit.framework.TestCase;

/**
 * Tests for Annotation class.
 * 
 * @author p70073399
 */
public class TestAnnotation extends TestCase {

	/**
	 */
	public static void testAnnotationWithAnnotationRef() {

		PathwayModel p1 = new PathwayModel();

		assert (p1.getAnnotations().isEmpty());

		Annotation a1 = new Annotation("value", AnnotationType.ONTOLOGY);
		Citation c1 = new Citation(new UrlRef(null, null));
		p1.addAnnotation(a1);
		p1.addCitation(c1);
		AnnotationRef ar1 = new AnnotationRef(a1);
		CitationRef cr1 = new CitationRef(c1);
		ar1.addCitationRef(cr1);
		System.out.println("Annotation has AnnotationRefs " + a1.getAnnotationRefs());
		System.out.println("Citation has CitationRefs " + c1.getCitationRefs());
		System.out.println("AnnotationRef has CitationRefs " + ar1.getCitationRefs());
		
		// data node has annotationRef which has a citationRef
		DataNode d1 = new DataNode(null, null, null, "d1", null);
		p1.addDataNode(d1);
		d1.addAnnotationRef(ar1); 
		System.out.println("DataNode has AnnotationRefs " + d1.getAnnotationRefs());

		
		assertEquals(ar1.getAnnotatable(), d1);
		assertEquals(ar1.getAnnotation(), a1);
		System.out.println("PathwayModel contains PathwayElements " + p1.getPathwayElements());

//		 a1.removeAnnotationRef(ar1);
		 ar1.unsetAnnotation();
//		 d1.removeAnnotationRef(ar1);
		
		assertNull(ar1.getAnnotatable());
		assertNull(ar1.getAnnotation());
		System.out.println("DataNode has AnnotationRef " + d1.getAnnotationRefs());
		System.out.println("Annotation has AnnotationRef " + a1.getAnnotationRefs());
		System.out.println("PathwayModel contains PathwayElements " + p1.getPathwayElements());

	}

}