package org.jahia.tools.contentgenerator;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.io.FileUtils;

/**
 * Class to handle files used as attachments in Jahia pages
 * 
 * @author Guillaume Lucazeau
 * 
 */
public class FileService {
	
	String sep;

	public FileService() {
		sep = System.getProperty("file.separator");
	}

	/**
	 * Returns a file name randomly picked in the list of file names available
	 * (from the pool directory specified in parameter)
	 * 
	 * @param availableFileNames
	 * @return file name chosen
	 */
	public String getFileName(List<String> availableFileNames) {
		String fileName = null;

		if (ContentGeneratorService.currentFileIndex == availableFileNames.size()) {
			ContentGeneratorService.currentFileIndex = 0;
		}
		fileName = availableFileNames.get(ContentGeneratorService.currentFileIndex);
		ContentGeneratorService.currentFileIndex++;
		return fileName;
	}
	
	/**
	 * Returns a list of the files that can be used as attachments
	 * Return only filename as String, sorted alphabetically
	 * @param filesDirectoryPath
	 *            directory containing the files that will be uploaded into the
	 *            Jahia repository and can be used as attachments
	 * @return list of file names
	 * @todo: get a list of file actually used as attachment and provide them as a zip
	 */
	public List<String> getFileNamesAvailable(File filesDirectory) {
		List<String> fileNames = new ArrayList<String>();
		File[] files = filesDirectory.listFiles();
		Arrays.sort(files);
		
		for (int i = 0; i < files.length; i++) {
			if (files[i].isFile()) {
				fileNames.add(files[i].getName());
			}
		}
		return fileNames;
	}
	
	/**
	 * Returns a list of File available for attachment
	 * @param filesDirectory
	 * @return Files available
	 */
	public List<File> getFilesAvailable(File filesDirectory) {
		List<String> filenames = getFileNamesAvailable(filesDirectory);
		List<File> fileList = new ArrayList<File>();
		File f;
	
		for (Iterator<String> iterator = filenames.iterator(); iterator.hasNext();) {
			String fileName = (String) iterator.next();
			f = new File(filesDirectory + sep + fileName);
			fileList.add(f);
		}
		return fileList;
	}
	
	/**
	 * 
	 * @param filesToCopy
	 * @param destDir
	 * @returns List of new files (copies)
	 * @throws IOException
	 *             if source or destination is invalid
	 * @throws IOException
	 *             if an IO error occurs during copying
	 */
	public List<File> copyFilesForAttachment(List<File> filesToCopy, File destDir) throws IOException {
		List<File> newFiles = new ArrayList<File>();
		File oldFile;
		File newFile;
		for (Iterator<File> iterator = filesToCopy.iterator(); iterator.hasNext();) {
			oldFile = (File) iterator.next();
			
			String fileName = oldFile.getName();
			
			// creates a new directory for each file, with the same name
			File newDirForFile = new File(destDir + sep + getFileNameWithoutExtension(fileName));
			newDirForFile.mkdir();
			
			FileUtils.copyFileToDirectory(oldFile, newDirForFile);
			newFile = new File(newDirForFile + sep + oldFile.getName());
			newFiles.add(newFile);
		}
		return newFiles;
	}
	
	private String getFileNameWithoutExtension(String fileName) {
		return fileName.substring(0, fileName.lastIndexOf('.'));
	}
}
