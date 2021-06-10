package testsForCuration;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.Namespace;
import org.jdom2.input.SAXBuilder;
import org.pathvisio.io.ConverterException;

import junit.framework.TestCase;

/**
 * Test which searches for all shape types used in GPML2013a pathways.
 * 
 * note: Anchor shapes are Circle and None. 
 * 
 * @author finterly
 */
public class FindShapeType extends TestCase {

	/**
	 * Searches for shape types used.
	 */
	public static void testShapeTypes() throws IOException, ConverterException {
		Set<String> shapeTypes = new HashSet<String>();
		Set<String> mimphos = new HashSet<String>();
		Set<String> mimint = new HashSet<String>();
		Set<String> mimdeg = new HashSet<String>();

		// Gets all organism directories
		File dirAllOrganisms = new File("C:/Users/p70073399/Documents/wikipathways-20210527-all-species/cache");
		String[] dirOrganisms = dirAllOrganisms.list(new FilenameFilter() {
			@Override
			public boolean accept(File current, String name) {
				return new File(current, name).isDirectory();
			}
		});
		System.out.println(Arrays.toString(dirOrganisms));
		for (int i = 0; i < dirOrganisms.length; i++) {
			File dirOrganism = new File(
					"C:/Users/p70073399/Documents/wikipathways-20210527-all-species/cache/" + dirOrganisms[i]);
			File[] listOfFiles = dirOrganism.listFiles(new FilenameFilter() {
				public boolean accept(File dir, String name) {
					return name.toLowerCase().endsWith(".gpml");
				}
			});
			// For all gpml of an organism:
			for (int j = 1; j < listOfFiles.length; j++) {
				File file = listOfFiles[j];
				if (file.isFile()) {
					assertTrue(file.exists());
					try {
						SAXBuilder builder = new SAXBuilder();
						Document readDoc = builder.build(file);
						Element root = readDoc.getRootElement();
						List<Element> es = root.getChildren();
						for (Element e : es) {
							Element gfx = e.getChild("Graphics", e.getNamespace());
							if (gfx != null) {
								String shapeType = gfx.getAttributeValue("ShapeType");
//								if (shapeType != null) {
//									if (shapeType.equals("mim-phosphorylated"))
//										mimphos.add(file.getName());
//									if (shapeType.equals("mim-interaction"))
//										mimint.add(file.getName());
//									if (shapeType.equals("mim-degradation"))
//										mimdeg.add(file.getName());
//								}
								shapeTypes.add(shapeType);
							}
						}
					} catch (JDOMException e) {
						e.printStackTrace();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}
//			System.out.println("Contains mim-phosphorylated");
//			for (String shapeType : mimphos) {
//				System.out.println(shapeType);
//			}
//			System.out.println("Contains mim-interaction");
//			for (String shapeType : mimint) {
//				System.out.println(shapeType);
//			}
//			System.out.println("Contains mim-degradation");
//			for (String shapeType : mimdeg) {
//				System.out.println(shapeType);
//			}
		}
		for (String shapeType : shapeTypes) {
			System.out.println(shapeType);
		}
	}
}