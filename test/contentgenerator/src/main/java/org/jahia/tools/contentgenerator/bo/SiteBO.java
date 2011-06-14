package org.jahia.tools.contentgenerator.bo;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;

public class SiteBO {

	private String siteKey;

	private File usersFile;

	private File pagesFile;

	public String getSiteKey() {
		return siteKey;
	}

	public void setSiteKey(String siteKey) {
		this.siteKey = siteKey;
	}

	public File getUsersFile() {
		return usersFile;
	}

	public void setUsersFile(File usersFile) {
		this.usersFile = usersFile;
	}

	public File getPagesFile() {
		return pagesFile;
	}

	public void setPagesFile(File repositoryFile) {
		this.pagesFile = repositoryFile;
	}

	public String getHeader() {
		StringBuffer sb = new StringBuffer();

		sb.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?> \n");
		sb.append("<content xmlns:jcr=\"http://www.jcp.org/jcr/1.0\" xmlns:nt=\"http://www.jcp.org/jcr/nt/1.0\" xmlns:jnt=\"http://www.jahia.org/jahia/nt/1.0\" xmlns:test=\"http://www.apache.org/jackrabbit/test\" xmlns:sv=\"http://www.jcp.org/jcr/sv/1.0\" xmlns:jmix=\"http://www.jahia.org/jahia/mix/1.0\" xmlns:j=\"http://www.jahia.org/jahia/1.0\" xmlns:mix=\"http://www.jcp.org/jcr/mix/1.0\" xmlns:rep=\"internal\">\n");
		sb.append("<sites jcr:primaryType=\"jnt:virtualsitesFolder\">\n");
		sb.append("<" + this.siteKey + " jcr:primaryType=\"jnt:virtualsite\">\n");
		return sb.toString();
	}
	
	public String getFooter() {
		StringBuffer sb = new StringBuffer();
		
		sb.append("</" + this.siteKey + ">\n");
		sb.append("</sites>\n");
		sb.append("</content>\n");
		return sb.toString();
	}
}
