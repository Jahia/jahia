package org.jahia.tools.contentgenerator.bo;

import java.io.File;

public class ExportBO {

	private File outputFile;
	
	private String outputDir;
	
	private String outputFilename;
	
	private Integer nbPagesTopLevel;
	
	private Integer nbSubLevels;
	
	private Integer nbSubPagesPerPage;
	
	private Integer totalPages; 
	
	private Integer maxArticleIndex;
	
	public File getOutputFile() {
		return outputFile;
	}

	public void setOutputFile(File outputFile) {
		this.outputFile = outputFile;
	}

	public String getOutputDir() {
		return outputDir;
	}

	public void setOutputDir(String outputDir) {
		this.outputDir = outputDir;
	}

	public String getOutputFilename() {
		return outputFilename;
	}

	public void setOutputFilename(String outputFilename) {
		this.outputFilename = outputFilename;
	}

	public Integer getNbPagesTopLevel() {
		return nbPagesTopLevel;
	}

	public void setNbPagesTopLevel(Integer nbPagesTopLevel) {
		this.nbPagesTopLevel = nbPagesTopLevel;
	}

	public Integer getNbSubLevels() {
		return nbSubLevels;
	}

	public void setNbSubLevels(Integer nbSubLevels) {
		this.nbSubLevels = nbSubLevels;
	}

	public Integer getNbSubPagesPerPage() {
		return nbSubPagesPerPage;
	}

	public void setNbSubPagesPerPage(Integer nbSubPagesPerPage) {
		this.nbSubPagesPerPage = nbSubPagesPerPage;
	}
	
	public Integer getTotalPages() {
		return totalPages;
	}

	public void setTotalPages(Integer totalPages) {
		this.totalPages = totalPages;
	}

	public Integer getMaxArticleIndex() {
		return maxArticleIndex;
	}

	public void setMaxArticleIndex(Integer maxArticleIndex) {
		this.maxArticleIndex = maxArticleIndex;
	}

	public ExportBO() {
		
	}
	
	public String toString() {
		StringBuffer sb = new StringBuffer();
		sb.append("<!-- export information -->\n");
		sb.append("<!-- top level pages: " + this.getNbPagesTopLevel() + " -->\n");
		sb.append("<!-- sub levels: " + this.getNbSubLevels() + " -->\n");
		sb.append("<!-- sub pages per page: " + this.getNbSubPagesPerPage() + " -->\n");
		sb.append("<!-- total pages: " + this.getTotalPages() + " -->\n");
		return sb.toString();
	}
}
