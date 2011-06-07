package org.jahia.tools.contentgenerator;

import java.util.List;

public class FileService {

	private static FileService instance;

	private FileService() {

	}

	public static FileService getInstance() {
		if (instance == null) {
			instance = new FileService();
		}
		return instance;
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
}
