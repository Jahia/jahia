package org.jahia.tools.contentgenerator.junit;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.jahia.tools.contentgenerator.OutputService;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class OutputServiceTest extends ContentGeneratorTestCase {

	private String sep;
	private File testDir;
	private String testFilename = "test.txt";
	private File testFile;
	private OutputService os;

	@Before
	public void setUp() {
		sep = System.getProperty("file.separator");
		String sTempDir = System.getProperty("java.io.tmpdir");
		File tempDir = new File(sTempDir + sep + "cg-test");
		tempDir.mkdir();
		
		os = new OutputService();
		
		testFile = new File(testDir + sep + testFilename);
	}
	
	@After
	public void tearDown() {
		testFile.delete();
	}
	
	@Test
	public void testInitOutputFile() {
		try {			
			os.initOutputFile(testFile);
			assertTrue(testFile.exists());
			assertTrue(FileUtils.readFileToString(testFile).isEmpty());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Test
	public void testInitOutputFileAlreadyExists() {
		String s = "aaa\\nbbb\\nccc\\nddd\\neee\\nfff\\nggg\\nhhh";
		try {
			FileUtils.writeStringToFile(testFile, s);
			assertEquals(s, FileUtils.readFileToString(testFile));
			
			os.initOutputFile(testFile);
			assertTrue(testFile.exists());
			assertTrue(FileUtils.readFileToString(testFile).isEmpty());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	@Test
	public void testAppendStringToFile() {
		String s1 = "abcdef\\n";
		String s2 = "123456";
		
		try {
			testFile.createNewFile();

			FileUtils.writeStringToFile(testFile, s1);
			
			os.appendStringToFile(testFile, s2);
			
			String s3 = s1 + s2;
			assertEquals(s3, FileUtils.readFileToString(testFile));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}

}
