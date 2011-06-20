package org.jahia.tools.contentgenerator;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.NotImplementedException;
import org.apache.log4j.Logger;
import org.jahia.tools.contentgenerator.bo.SiteBO;
import org.jahia.tools.contentgenerator.properties.ContentGeneratorCst;
import org.jdom.Document;
import org.jdom.Element;

public class SiteService {
	Logger logger = Logger.getLogger(SiteService.class.getName());
	String sep;

	public SiteService() {
		sep = System.getProperty("file.separator");
	}

	public File createAndPopulateRepositoryFile(File tempOutputDir, SiteBO site, File pagesFile, File filesFile)
			throws IOException {
		File repositoryFile = new File(tempOutputDir + sep + "repository.xml");

		StringBuffer sb = new StringBuffer();
		sb.append(site.getHeader());

		if (filesFile != null) {
			String files = FileUtils.readFileToString(filesFile);
			sb.append(files);
		}

		String pages = FileUtils.readFileToString(pagesFile);
		sb.append(pages);

		sb.append(site.getFooter());

		FileUtils.writeStringToFile(repositoryFile, sb.toString());

		return repositoryFile;
	}

	/**
	 * Creates temporary directory where we will put all resources to zip If the
	 * directory already exists, we empty it
	 * 
	 * @param siteKey
	 * @param destDir
	 * @return new directory created
	 * @throws IOException
	 *             can not delete dir
	 */
	public File createSiteDirectory(String siteKey, File destDir) throws IOException {
		File tempOutputDir = new File(destDir + sep + siteKey);
		if (tempOutputDir.exists()) {
			FileUtils.deleteDirectory(tempOutputDir);
		}
		tempOutputDir.mkdir();
		logger.debug("temp directory for site export: " + tempOutputDir.getAbsolutePath());
		return tempOutputDir;
	}

	/**
	 * Copies the XML file generated into a temporary dir and renames it to
	 * "repository.xml" (or other name defined in the constant)
	 * 
	 * @param pagesFile
	 * @return new File created
	 * @throws IOException
	 */
	public File copyPagesFile(File pagesFile, File tempOutputDir) throws IOException {
		FileUtils.copyFileToDirectory(pagesFile, tempOutputDir);
		File copy = new File(tempOutputDir + sep + pagesFile.getName());
		File renamedCopy = new File(tempOutputDir + sep + ContentGeneratorCst.REPOSITORY_FILENAME);
		copy.renameTo(renamedCopy);
		logger.debug("new file containing pages: " + renamedCopy);
		return renamedCopy;
	}

	/**
	 * Create the tree that will contains all files used as attachments in the
	 * new site
	 * 
	 * @param siteKey
	 * @param tempOutputDir
	 * @return new File created
	 * @throws IOException
	 *             one dir can not be created
	 */
	public File createFilesDirectoryTree(String siteKey, File tempOutputDir) throws IOException {
		String treePath = tempOutputDir + sep + "content" + sep + "sites" + sep + siteKey + sep + "files" + sep
				+ "contributed";
		File treeFile = new File(treePath);
		FileUtils.forceMkdir(treeFile);
		return treeFile;
	}

	public File createUsersFile(Integer nbUsers) {
		throw new NotImplementedException();
	}

	/**
	 * Creates properties for the site created and write them to the default
	 * properties file (creates it as well)
	 * 
	 * @param siteKey
	 * @param tempOutputDir
	 * @return
	 * @throws FileNotFoundException
	 *             file
	 * @throws IOException
	 */
	public File createPropertiesFile(String siteKey, File tempOutputDir) throws FileNotFoundException, IOException {
		Properties siteProp = new Properties();

		siteProp.setProperty("sitetitle", siteKey);
		siteProp.setProperty("siteservername", ContentGeneratorCst.SITE_SERVER_NAME_DEFAULT);
		siteProp.setProperty("sitekey", siteKey);
		siteProp.setProperty("description", ContentGeneratorCst.DESCRIPTION_DEFAULT);
		siteProp.setProperty("templatePackageName", ContentGeneratorCst.TEMPLATE_SET_DEFAULT);
		siteProp.setProperty("mixLanguage", Boolean.FALSE.toString());
		siteProp.setProperty("defaultLanguage", "en");
		siteProp.setProperty("installedModules.1", ContentGeneratorCst.TEMPLATE_SET_DEFAULT);
		siteProp.setProperty("language.en.activated", Boolean.TRUE.toString());
		siteProp.setProperty("language.en.mandatory", Boolean.TRUE.toString());

		String sep = System.getProperty("file.separator");
		File propFile = new File(tempOutputDir + sep + ContentGeneratorCst.SITE_PROPERTIES_FILENAME);

		siteProp.store(new FileOutputStream(propFile), ContentGeneratorCst.DESCRIPTION_DEFAULT);

		return propFile;
	}
	
	public Document insertGroupsIntoSiteRepository(Document repository, String siteKey, Element groups) {
		// @todo:siteKey
		Element siteNode = repository.getRootElement().getChild("sites").getChild(siteKey);
		siteNode.addContent(groups);
		return repository;
	}
}
