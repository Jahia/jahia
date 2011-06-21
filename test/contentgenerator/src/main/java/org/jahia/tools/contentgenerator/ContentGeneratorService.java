package org.jahia.tools.contentgenerator;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Iterator;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;
import org.apache.maven.plugin.MojoExecutionException;
import org.jahia.tools.contentgenerator.bo.ArticleBO;
import org.jahia.tools.contentgenerator.bo.ExportBO;
import org.jahia.tools.contentgenerator.bo.GroupBO;
import org.jahia.tools.contentgenerator.bo.SiteBO;
import org.jahia.tools.contentgenerator.bo.UserBO;
import org.jahia.tools.contentgenerator.properties.ContentGeneratorCst;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;
import org.w3c.dom.DOMException;

public class ContentGeneratorService {

	public static ContentGeneratorService instance;

	private static final Logger logger = Logger.getLogger(ContentGeneratorService.class.getName());

	public static int currentPageIndex = 0;

	public static int currentFileIndex = 0;

	private ContentGeneratorService() {
	}

	public static ContentGeneratorService getInstance() {
		if (instance == null) {
			instance = new ContentGeneratorService();
		}
		return instance;
	}

	/**
	 * Generates an XML files containing the pages tree and that can be imported
	 * into a Jahia website
	 * 
	 * @param export
	 */
	public void generatePages(ExportBO export) throws MojoExecutionException, IOException {
		if (!ContentGeneratorCst.VALUE_NONE.equals(export.getAddFilesToPage()) && export.getFileNames().isEmpty()) {
			throw new MojoExecutionException(
					"Directory containing files to include is empty, use jahia-cg:generate-files first");
		}

		List<ArticleBO> articles = DatabaseService.getInstance().selectArticles(export, export.getTotalPages());

		PageService pageService = new PageService();
		pageService.createTopPages(export, articles);
	}

	/**
	 * Generates text files that can be used as attachments, with random content
	 * from the articles database
	 * 
	 * @param export
	 * @param articles
	 * @throws MojoExecutionException
	 */
	public void generateFiles(ExportBO export) throws MojoExecutionException {
		logger.info("Jahia files generator starts");

		Integer numberOfFilesToGenerate = export.getNumberOfFilesToGenerate();
		if (numberOfFilesToGenerate == null) {
			throw new MojoExecutionException("numberOfFilesToGenerate parameter is null");
		}

		List<ArticleBO> articles = DatabaseService.getInstance().selectArticles(export,
				export.getNumberOfFilesToGenerate());
		int indexArticle = 0;
		File outputFile;

		for (int i = 0; i < numberOfFilesToGenerate; i++) {
			if (indexArticle == articles.size()) {
				indexArticle = 0;
			}

			String newFileName = "file." + i + ".txt";
			String newFilePath = export.getFilesDirectory() + System.getProperty("file.separator") + newFileName;
			outputFile = new File(newFilePath);

			try {
				FileUtils.writeStringToFile(outputFile, articles.get(indexArticle).getContent());
			} catch (IOException e) {
				throw new MojoExecutionException("Can't create new file: " + e.getMessage());
			}

			indexArticle++;
		}
	}

	/**
	 * Generates pages and then creates a ZIP archive containing those pages,
	 * the files needed for attachments and site.properties
	 * 
	 * @param export
	 * @return Absolute path of ZIP file created
	 * @throws MojoExecutionException
	 * @throws ParserConfigurationException
	 * @throws DOMException
	 */
	public String generateSite(ExportBO export) throws MojoExecutionException, DOMException,
			ParserConfigurationException {
		String globalArchivePath = null;
		String sep = System.getProperty("file.separator");

		// as we create a full site we will need a home page
		export.setRootPageName(ContentGeneratorCst.ROOT_PAGE_NAME);
		SiteBO site = new SiteBO();
		site.setSiteKey(export.getSiteKey());

		try {
			SiteService siteService = new SiteService();

			generatePages(export);

			logger.debug("Pages generated, now site");
			List<File> filesToZip = new ArrayList<File>();

			// create temporary dir in output dir (siteKey)
			File tempOutputDir = siteService.createSiteDirectory(export.getSiteKey(), new File(export.getOutputDir()));

			// create properties file
			File propertiesFile = siteService.createPropertiesFile(export.getSiteKey(), tempOutputDir);
			filesToZip.add(propertiesFile);

			// create tree dirs for files attachments (if files are not at
			// "none")
			File tempXmlFile = null;
			if (!ContentGeneratorCst.VALUE_NONE.equals(export.getAddFilesToPage())) {
				FileService fileService = new FileService();
				File filesDirectory = siteService.createFilesDirectoryTree(export.getSiteKey(), tempOutputDir);
				filesToZip.add(new File(tempOutputDir + "/content"));

				// get all files available in the pool dir
				List<File> filesToCopy = fileService.getFilesAvailable(export.getFilesDirectory());

				// if there are more files available than pages created, we copy
				// only the total of files that have been used
				if (filesToCopy.size() > export.getTotalPages()) {
					filesToCopy = filesToCopy.subList(0, export.getTotalPages() - 1);
				}

				fileService.copyFilesForAttachment(filesToCopy, filesDirectory);

				// generates XML code for files
				tempXmlFile = new File(export.getOutputDir() + sep + "jcrFiles.xml");
				fileService.createAndPopulateFilesXmlFile(tempXmlFile, filesToCopy);
			}

			// 2 - copy pages => repository.xml
			File repositoryFile = siteService.createAndPopulateRepositoryFile(tempOutputDir, site,
					export.getOutputFile(), tempXmlFile);

			// Add XML Groups
			UserGroupService userGroupService = new UserGroupService();
			// TODO: params nbUsers / nbGroups
			Integer nbUsers = Integer.valueOf(12);
			Integer nbGroups = Integer.valueOf(5);
			List<GroupBO> groups = userGroupService.generateUsersAndGroups(nbUsers, nbGroups);
			Element groupsNode = userGroupService.generateJcrGroups(export.getSiteKey(), groups);

			// TODO: transformer le repository String en repository global JDOM
			Document repositoryDoc = readXmlFile(repositoryFile);
			repositoryDoc = siteService.insertGroupsIntoSiteRepository(repositoryDoc, export.getSiteKey(), groupsNode);

			OutputService os = new OutputService();
			os.writeJdomDocumentToFile(repositoryDoc, repositoryFile);
			filesToZip.add(repositoryFile);

			String zipFileName = export.getSiteKey() + ".zip";
			File siteArchive = os.createSiteArchive(zipFileName, tempOutputDir.getParentFile().getAbsolutePath(), filesToZip);

			// Users archive
			filesToZip.clear();
			File tmpUsers = new File(tempOutputDir + sep + "tmpUsers");
			tmpUsers.mkdir();
			
			List<UserBO> users = new ArrayList<UserBO>();
			for (Iterator<GroupBO> iterator = groups.iterator(); iterator.hasNext();) {
				GroupBO group = iterator.next();
				users.addAll(group.getUsers());
			}
			File repositoryUsers = new File(tmpUsers + sep + "repository.xml");
			Document usersRepositoryDocument = userGroupService.createUsersRepository(users);
			os.writeJdomDocumentToFile(usersRepositoryDocument, repositoryUsers);

			File contentUsers = userGroupService.createFileTreeForUsers(users, tmpUsers);
			
			filesToZip.add(repositoryUsers);
			filesToZip.add(contentUsers);
			String usersZipFileName = "users.zip";
			File usersArchive = os.createSiteArchive(usersZipFileName, tmpUsers.getParentFile().getAbsolutePath(), filesToZip);
			
			// Global site archive
			filesToZip.clear();
			filesToZip.add(siteArchive);
			filesToZip.add(usersArchive);
			File globalArchive = os.createSiteArchive(zipFileName, tempOutputDir.getParentFile().getAbsolutePath(), filesToZip);
			globalArchivePath = globalArchive.getAbsolutePath();
			
		} catch (IOException e) {
			throw new MojoExecutionException("Exception while creating the website ZIP archive: " + e);
		}
		return globalArchivePath;
	}

	/**
	 * Calculates the number of pages needed, used to know how much articles we
	 * will need
	 * 
	 * @param nbPagesTopLevel
	 * @param nbLevels
	 * @param nbPagesPerLevel
	 * @return number of pages needed
	 */
	public Integer getTotalNumberOfPagesNeeded(Integer nbPagesTopLevel, Integer nbLevels, Integer nbPagesPerLevel) {
		Double nbPages = new Double(0);
		for (double d = nbLevels; d > 0; d--) {
			nbPages += Math.pow(nbPagesPerLevel.doubleValue(), d);
		}
		nbPages = nbPages * nbPagesTopLevel + nbPagesTopLevel;

		return new Integer(nbPages.intValue());
	}

	/**
	 * Format a date for inclusion in JCR XML file If date is null, current date
	 * is used Format used: http://www.day.com/specs/jcr/1.0/6.2.5.1_Date.html
	 * 
	 * @param date
	 * @return formated date
	 */
	public String getDateForJcrImport(Date date) {
		GregorianCalendar gc = (GregorianCalendar) GregorianCalendar.getInstance();
		if (date != null) {
			gc.setTime(date);
		}
		StringBuffer sbNewDate = new StringBuffer();
		// 2011-04-01T17:39:59.265+02:00
		sbNewDate.append(gc.get(Calendar.YEAR));
		sbNewDate.append("-");
		sbNewDate.append(gc.get(Calendar.MONTH));
		sbNewDate.append("-");
		sbNewDate.append(gc.get(Calendar.DAY_OF_MONTH));
		sbNewDate.append("T");
		sbNewDate.append(gc.get(Calendar.HOUR_OF_DAY));
		sbNewDate.append(":");
		sbNewDate.append(gc.get(Calendar.MINUTE));
		sbNewDate.append(":");
		sbNewDate.append(gc.get(Calendar.SECOND));
		sbNewDate.append(".");
		sbNewDate.append(gc.get(Calendar.MILLISECOND));
		sbNewDate.append(gc.get(Calendar.ZONE_OFFSET));
		return sbNewDate.toString();
	}

	/**
	 * Add dates and common attributes to JCR elements: - lastPublished -
	 * lastPublishedBy - published: true - created - createdBy: "system" -
	 * lastModified - lastModifiedBy: "root"
	 * 
	 * @param element
	 * @return element with new attribute
	 */
	public Element addJcrAttributes(Element element, String jcrDate) {
		element.setAttribute("lastPublished", jcrDate, ContentGeneratorCst.NS_J);
		element.setAttribute("lastPublishedBy", "root", ContentGeneratorCst.NS_J);
		element.setAttribute("published", Boolean.TRUE.toString(), ContentGeneratorCst.NS_J);
		element.setAttribute("created", jcrDate, ContentGeneratorCst.NS_JCR);
		element.setAttribute("createdBy", "system", ContentGeneratorCst.NS_JCR);
		element.setAttribute("lastModified", jcrDate, ContentGeneratorCst.NS_JCR);
		element.setAttribute("lastModifiedBy", "root", ContentGeneratorCst.NS_JCR);

		return element;
	}

	// @TODO a supprimer
	private Document readXmlFile(File xmlFile) {
		SAXBuilder builder = new SAXBuilder();

		Document document = null;
		try {
			document = builder.build(xmlFile);
		} catch (JDOMException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return document;
	}
}
