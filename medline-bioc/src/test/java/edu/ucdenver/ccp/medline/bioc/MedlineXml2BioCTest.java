package edu.ucdenver.ccp.medline.bioc;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.GZIPInputStream;

import javax.xml.bind.JAXBException;
import javax.xml.stream.XMLStreamException;

import org.junit.Test;

import com.pengyifan.bioc.BioCCollection;
import com.pengyifan.bioc.BioCDocument;
import com.pengyifan.bioc.BioCPassage;
import com.pengyifan.bioc.io.BioCCollectionReader;

import edu.ucdenver.ccp.common.collections.CollectionsUtil;
import edu.ucdenver.ccp.common.test.DefaultTestCase;
import edu.ucdenver.ccp.medline.xml.MedlineXmlParserTest;

public class MedlineXml2BioCTest extends DefaultTestCase {
	private static final String SAMPLE_FILE_NAME = "medline-xml-sample-2016.xml";
	private static final String ABSTRACT_1 = "OBJECTIVES: To examine the relationship between speech intelligibilities among the similar level of hearing loss and threshold elevation of the auditory brainstem response (ABR).\n"
			+ "METHODS: The relationship between maximum speech intelligibilities among similar levels of hearing loss and relative threshold elevation of the click-evoked ABR (ABR threshold - pure tone average at 2,000 and 4,000 Hz) was retrospectively reviewed in patients with sensorineural hearing loss (SNHL) other than apparent retrocochlear lesions as auditory neuropathy, vestibular schwannoma and the other brain lesions.\n"
			+ "RESULTS: Comparison of the speech intelligibilities in subjects with similar levels of hearing loss found that the variation in maximum speech intelligibility was significantly correlated with the threshold elevation of the ABR.\n"
			+ "CONCLUSION: The present results appear to support the idea that variation in maximum speech intelligibility in patients with similar levels of SNHL may be related to the different degree of dysfunctions of the inner hair cells and/or cochlear nerves in addition to those of outer hair cells.";
	private static final String ABSTRACT_2 = "Trauma to soft tissues is an important consideration in cases of seatbelt injury, as soft-tissue injury can cause shock. Careful observation for hours in the clinic at least, along with appropriate imaging studies, is necessary if signs of a seatbelt injury exist. ";
	private static final Map<String, String> pmid2AbstractMap = new HashMap<String, String>() {
		{
			put("10787327", ABSTRACT_1);
			put("10847728", ABSTRACT_2);
			put("10970178", null);
		}
	};
	private static final Map<String, String> pmid2TitleMap = new HashMap<String, String>() {
		{
			put("10787327",
					"Effect of calcium chromate dust, influenza virus, and 100 R whole-body x radiation on lung tumor incidence in mice.");
			put("10847728", "Electron microscopic study of chronic desquamative gingivitis.");
			put("10970178",
					"Purification and characterization of a histidine-binding protein from Salmonella typhimurium LT-2 and its relationship to the histidine permease system.");
		}
	};

	@Test
	public void testXml2BioCConversion() throws IOException, XMLStreamException, JAXBException {
		File sampleMedlineXmlFile = copyClasspathResourceToTemporaryFile(MedlineXmlParserTest.class, SAMPLE_FILE_NAME);
		File baseOutputDirectory = folder.newFolder();
		MedlineXml2BioC.processMedlineXmlFile(sampleMedlineXmlFile, baseOutputDirectory);

		List<File> expectedOutputFileList = CollectionsUtil.createList(new File(baseOutputDirectory,
				"10/787/10787327.bioc.xml.gz"), new File(baseOutputDirectory, "10/847/10847728.bioc.xml.gz"), new File(
				baseOutputDirectory, "10/970/10970178.bioc.xml.gz"));

		/* check that expected output files exist */
		for (File f : expectedOutputFileList) {
			assertTrue(f.exists());
			BioCCollectionReader cr = new BioCCollectionReader(new GZIPInputStream(new FileInputStream(f)));
			BioCCollection collection = cr.readCollection();
			assertEquals(1, collection.getDocmentCount());
			BioCDocument document = collection.getDocument(0);
			String id = document.getID();
			assertEquals(2, document.getPassageCount());
			BioCPassage titlePassage = document.getPassage(0);
			if (titlePassage.getText().isPresent()) {
				String titleText = titlePassage.getText().get();
				assertEquals(pmid2TitleMap.get(id), titleText);
			} else {
				assertNull(pmid2TitleMap.get(id));
			}
			BioCPassage abstractPassage = document.getPassage(1);
			if (abstractPassage.getText().isPresent()) {
				String abstractText = abstractPassage.getText().get();
				assertEquals(pmid2AbstractMap.get(id), abstractText);
			} else {
				assertNull(pmid2AbstractMap.get(id));
			}
			cr.close();
		}

	}

}
