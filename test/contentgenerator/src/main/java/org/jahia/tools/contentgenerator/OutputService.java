package org.jahia.tools.contentgenerator;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.Iterator;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;
import org.jahia.tools.contentgenerator.bo.PageBO;
import org.jdom.Document;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;

/**
 * Class used to write data into output files/directories
 * 
 * @author Guillaume Lucazeau
 * 
 */
public class OutputService {
	Logger logger = Logger.getLogger(OutputService.class.getName());
	String sep;

	public OutputService() {
		sep = System.getProperty("file.separator");
	}

	public void initOutputFile(File f) throws IOException {
		// if file already exist, we empty it
		FileUtils.writeStringToFile(f, "");
	}

	public void appendStringToFile(File f, String s) throws IOException {
		OutputStreamWriter fwriter = new OutputStreamWriter(new FileOutputStream(f, true), "UTF-8");
		BufferedWriter fOut = new BufferedWriter(fwriter);
		fOut.write(s);
		fOut.close();
	}

	public void appendPagesToFile(File f, List<PageBO> listePages) throws IOException {
		for (Iterator<PageBO> iterator = listePages.iterator(); iterator.hasNext();) {
			PageBO page = iterator.next();
			appendPageToFile(f, page);
		}
	}

	public void appendPageToFile(File f, PageBO page) throws IOException {
		appendStringToFile(f, page.toString());
	}

	public void appendPathToFile(File f, List<String> paths) throws IOException {
		for (Iterator<String> iterator = paths.iterator(); iterator.hasNext();) {
			String path = iterator.next();
			path = path + ",";
			appendStringToFile(f, path);
		}
	}

	public File createSiteArchive(String archiveName, String outputPath, List<File> filesToArchive) {
		File newZipArchive = null;
		try {
			// Creates the ZIP file
			ZipOutputStream zipOutput = new ZipOutputStream(new FileOutputStream(outputPath + sep + archiveName));

			// Process each file or directory to add
			for (Iterator<File> iterator = filesToArchive.iterator(); iterator.hasNext();) {
				File f = iterator.next();
				zipFile(f, zipOutput);
			}
			zipOutput.close();
			newZipArchive = new File(outputPath + sep + archiveName);
		} catch (IOException e) {
			logger.error("Can not create ZIP file: " + e);
		}
		return newZipArchive;
	}

	/**
	 * Zip a file, get the filename as ZIP entry name
	 * 
	 * @param f
	 * @param out
	 * @throws IOException
	 */
	private void zipFile(File f, ZipOutputStream out) throws IOException {
		zipFile(f, f.getName(), out);
	}

	/**
	 * Zip files and directories Call itself for sub files/sub directories
	 * 
	 * @param f
	 * @param fileName
	 * @param out
	 * @throws IOException
	 */
	// name is the name for the file
	private void zipFile(File f, String fileName, ZipOutputStream out) throws IOException {

		if (f.isDirectory()) {
			File[] files = f.listFiles();
			if (files != null) {
				for (File subFile : files) {
					String childName = fileName + sep + subFile.getName();
					zipFile(subFile, childName, out);
				}
			}
		} else {
			FileInputStream in = new FileInputStream(f);
			byte[] buf = new byte[1024];

			// Add ZIP entry to output stream.
			out.putNextEntry(new ZipEntry(fileName));

			// Transfer bytes from the file to the ZIP file
			int len;
			while ((len = in.read(buf)) > 0) {
				out.write(buf, 0, len);
			}

			// Complete the entry
			out.closeEntry();
			in.close();
		}
	}

	public void writeJdomDocumentToFile(Document doc, File file) throws IOException {
		XMLOutputter out = new XMLOutputter();
		java.io.FileWriter writer = new java.io.FileWriter(file);

		out.setFormat(Format.getPrettyFormat());
		
		out.output(doc, writer);
		writer.flush();
		writer.close();
	}
	
	public void writeInputStreamToFile(InputStream is, File f) throws IOException {

		OutputStream out = new FileOutputStream(f);
 
		int read=0;
		byte[] bytes = new byte[1024];
 
		while((read = is.read(bytes))!= -1){
			out.write(bytes, 0, read);
		}
 
		is.close();
		out.flush();
		out.close();	
	}
}
