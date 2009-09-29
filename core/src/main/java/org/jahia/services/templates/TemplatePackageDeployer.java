/**
 * This file is part of Jahia: An integrated WCM, DMS and Portal Solution
 * Copyright (C) 2002-2009 Jahia Solutions Group SA. All rights reserved.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.
 *
 * As a special exception to the terms and conditions of version 2.0 of
 * the GPL (or any later version), you may redistribute this Program in connection
 * with Free/Libre and Open Source Software ("FLOSS") applications as described
 * in Jahia's FLOSS exception. You should have received a copy of the text
 * describing the FLOSS exception, and it is also available here:
 * http://www.jahia.com/license
 *
 * Commercial and Supported Versions of the program
 * Alternatively, commercial and supported versions of the program may be used
 * in accordance with the terms contained in a separate written agreement
 * between you and Jahia Solutions Group SA. If you are unsure which license is appropriate
 * for your use, please contact the sales department at sales@jahia.com.
 */
package org.jahia.services.templates;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import org.apache.commons.collections.map.ListOrderedMap;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.DirectoryFileFilter;
import org.apache.commons.io.filefilter.SuffixFileFilter;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.jahia.data.templates.JahiaTemplatesPackage;
import org.jahia.exceptions.JahiaException;
import org.jahia.exceptions.JahiaTemplateServiceException;
import org.jahia.settings.SettingsBean;
import org.jahia.utils.zip.JahiaArchiveFileHandler;

/**
 * Template package deployer service.
 * 
 * @author Sergiy Shyrkov
 */
class TemplatePackageDeployer {

	public interface WatchdogCallback {
		void onChange(File file);
	}

	static class FolderWatcher extends TimerTask {
		private Map<String, Long> timestamps = new HashMap<String, Long>();
		private File monitoredFolder;
		private WatchdogCallback callback;

		FolderWatcher(File folderToWatch, WatchdogCallback callback) {
			super();
			this.monitoredFolder = folderToWatch;
			this.callback = callback;
			initTimestamps();
		}

		private void initTimestamps() {
			String[] existingFiles = getPackageFiles(monitoredFolder);
			for (String pkgFileName : existingFiles) {
				timestamps.put(pkgFileName, new File(monitoredFolder, pkgFileName).lastModified());
			}
		}

		@Override
		public void run() {
			boolean changesDetected = false;
			File changedFile = null;

			String[] existingFiles = getPackageFiles(monitoredFolder);
			for (String pkgFileName : existingFiles) {
				File pkgFile = new File(monitoredFolder, pkgFileName);
				if (timestamps.containsKey(pkgFileName)) {
					if (timestamps.get(pkgFileName) < pkgFile.lastModified()) {
						changesDetected = true;
						changedFile = pkgFile;
						timestamps.put(pkgFileName, pkgFile.lastModified());
					}
				} else {
					changesDetected = true;
					changedFile = pkgFile;
					timestamps.put(pkgFileName, pkgFile.lastModified());
				}
			}
			if (changesDetected) {
				callback.onChange(changedFile);
			}
		}

	}

	private static Logger logger = Logger.getLogger(TemplatePackageDeployer.class);

	/**
	 * The templates classes Root Path *
	 */
	private static final String TEMPLATES_CLASSES_ROOT_FOLDER = "jahiatemplates";

	private TemplatePackageRegistry templatePackageRegistry;

	private SettingsBean settingsBean;

	private Timer watchdog;

	/**
	 * Deploys a template package.
	 * 
	 * @param packageHandler
	 *            the template package handler object
	 * @throws JahiaTemplateServiceException
	 */
	private void deployPackage(JahiaTemplatesPackageHandler packageHandler) throws JahiaTemplateServiceException {

		JahiaTemplatesPackage tmplPack = packageHandler.getPackage();

		logger.info("Start deploying new template package '" + tmplPack.getName() + "'");

		File tmplRootFolder = new File(settingsBean.getJahiaTemplatesDiskPath(), tmplPack.getRootFolder());

		if (tmplRootFolder.exists()) {
			throw new JahiaTemplateServiceException("Unable to deploy template package '" + tmplPack.getName()
			        + "'. Folder '" + tmplPack.getRootFolder() + "' already exists");
		}

		tmplRootFolder.mkdirs();
		try {

			packageHandler.unzip(tmplRootFolder.getAbsolutePath());

			// extract the classes file
			if (tmplPack.hasClasses()) {
				File tmpFile = new File(tmplRootFolder, tmplPack.getClassesFile());
				if (tmpFile.exists() && tmpFile.isFile()) {
					JahiaArchiveFileHandler arch = new JahiaArchiveFileHandler(tmpFile.getAbsolutePath());
					if (tmplPack.getClassesRoot() == null) {
						arch.unzip(settingsBean.getClassDiskPath() + File.separator + TEMPLATES_CLASSES_ROOT_FOLDER);
					} else {
						arch.unzip(settingsBean.getClassDiskPath() + File.separator + tmplPack.getClassesRoot());
					}
				}
			}
			if (tmplPack.isWar()) {
				File classesFolder = new File(tmplRootFolder, "WEB-INF/classes");
				if (classesFolder.exists()) {
					FileUtils.copyDirectory(classesFolder, new File(settingsBean.getClassDiskPath()));
				}
				FileUtils.deleteDirectory(new File(tmplRootFolder, "WEB-INF"));
			}
		} catch (JahiaException je) {
			logger.error(je);
			throw new JahiaTemplateServiceException(je.getMessage(), je);

		} catch (IOException ioe) {
			logger.error(ioe);
			throw new JahiaTemplateServiceException("Failed creating JahiaArchiveFileHandler on classes file", ioe);
		}

		// overwrite template deployment descriptor
		TemplateDeploymentDescriptorHelper.serialize(tmplPack, tmplRootFolder);

		logger.info("Package '" + tmplPack.getName() + "' successfully deployed");
	}

	private void deployTemplatePackage(JahiaTemplatesPackageHandler packageHandler)
	        throws JahiaTemplateServiceException {
		JahiaTemplatesPackage pkg = packageHandler.getPackage();
		logger.info("Template package '" + pkg.getName() + "' found in file " + packageHandler.getFile());
		File tmplRootFolder = new File(settingsBean.getJahiaTemplatesDiskPath(), pkg.getRootFolder());
		if (tmplRootFolder.exists()) {
			if (FileUtils.isFileNewer(packageHandler.getFile(), tmplRootFolder)) {
				logger.debug("Older version of the template package '" + pkg.getName()
				        + "' already deployed. Deleting it.");
				try {
					FileUtils.deleteDirectory(tmplRootFolder);
				} catch (IOException e) {
					logger.error("Unable to delete the template set directory " + tmplRootFolder
					        + ". Skipping deployment.", e);
				}
			} else {
				logger.info("Template package '" + pkg.getName() + "' already deployed. Skipping.");
			}
		}
		if (!tmplRootFolder.exists()) {
			deployPackage(packageHandler);
		}
	}

	private boolean isValidPackage(JahiaTemplatesPackage pkg) {
		if (StringUtils.isEmpty(pkg.getName())) {
			logger.warn("Template package name '" + pkg.getName() + "' is not valid. Setting it to 'templates'.");
			pkg.setName("templates");
		}
		if (StringUtils.isEmpty(pkg.getRootFolder())) {
			String folderName = pkg.getName().replace(' ', '_').toLowerCase();
			logger.warn("Template package root folder '" + pkg.getRootFolder() + "' is not valid. Setting it to '"
			        + folderName + "'.");
			pkg.setRootFolder(folderName);
		}
		return true;
	}

	public void registerTemplatePackages() {
		File templatesRoot = new File(settingsBean.getJahiaTemplatesDiskPath());
		logger.info("Scanning templates directory (" + templatesRoot + ") for deployed packages...");
		if (templatesRoot.isDirectory()) {
			String[] dirs = templatesRoot.list(DirectoryFileFilter.DIRECTORY);

			List<JahiaTemplatesPackageHandler> remaining = new ArrayList<JahiaTemplatesPackageHandler>();

			for (int i = 0; i < dirs.length; i++) {
				File templateDir = new File(settingsBean.getJahiaTemplatesDiskPath(), dirs[i]);

				logger.debug("Checking directory: " + dirs[i]);
				if (JahiaTemplatesPackageHandler.isValidTemplatesDirectory(templateDir.getAbsolutePath())) {
					try {
						logger.debug("Reading the templates set under " + dirs[i]);
						JahiaTemplatesPackageHandler packageHandler = new JahiaTemplatesPackageHandler(templateDir);
						JahiaTemplatesPackage pkg = packageHandler.getPackage();
						if (pkg != null) {
							logger.debug("Template package found: " + pkg.getName());
							if (isValidPackage(pkg)) {
								remaining.add(packageHandler);
							}
						} else {
							logger.warn("Unable to read template package from the directory " + templateDir);
						}
					} catch (JahiaException ex) {
						logger.warn("Unable to read the templates deployment descriptor under " + templateDir, ex);
					}
				}
			}

			ListOrderedMap toDeploy = getOrderedPackages(remaining);
			for (Iterator<?> iterator = toDeploy.values().iterator(); iterator.hasNext();) {
				JahiaTemplatesPackageHandler handler = (JahiaTemplatesPackageHandler) iterator.next();
				templatePackageRegistry.register(handler.getPackage());
			}
		}
		logger.info("...finished scanning templates directory. Found "
		        + templatePackageRegistry.getAvailablePackagesCount() + " template packages.");
	}

	/**
	 * Goes through the template set archives in the in the shared templates
	 * folder to check if there are any new or updated files, which needs to be
	 * deployed to the templates folder. Does not register template set package
	 * itself.
	 */
	public void deploySharedTemplatePackages() {
		File sharedTemplates = new File(settingsBean.getJahiaSharedTemplatesDiskPath());

		logger.info("Scanning shared templates directory (" + sharedTemplates
		        + ") for new or updated template set packages ...");

		String[] jarFiles = getPackageFiles(sharedTemplates);

		// iterate over found JAR/WAR files and deploy them
		List<JahiaTemplatesPackageHandler> remaining = new ArrayList<JahiaTemplatesPackageHandler>();

		for (int i = 0; i < jarFiles.length; i++) {
			File templateJar = new File(sharedTemplates, jarFiles[i]);
			try {
				JahiaTemplatesPackageHandler packageHandler = new JahiaTemplatesPackageHandler(templateJar);
				JahiaTemplatesPackage pkg = packageHandler.getPackage();
				if (pkg != null) {
					if (isValidPackage(pkg)) {
						remaining.add(packageHandler);
					}
				} else {
					logger
					        .error("Unable to read the templates package from the file: "
					                + templateJar.getAbsolutePath());
				}
			} catch (IllegalArgumentException ex) {
				logger.error("Unable to read the templates deployment descriptor from " + templateJar.getPath(), ex);
			} catch (JahiaException ex) {
				logger.error("Unable to read the templates deployment descriptor from " + templateJar.getPath(), ex);
			}
		}

		ListOrderedMap toDeploy = getOrderedPackages(remaining);
		for (Iterator<?> iterator = toDeploy.values().iterator(); iterator.hasNext();) {
			JahiaTemplatesPackageHandler handler = (JahiaTemplatesPackageHandler) iterator.next();
			try {
				deployTemplatePackage(handler);
			} catch (JahiaException e) {
				logger.error("Error deploying package from JAR file '" + handler.getPackage().getName()
				        + "'. Skipping it.", e);
			}
		}

		logger.info("...finished scanning shared templates directory.");
	}

	private ListOrderedMap getOrderedPackages(List<JahiaTemplatesPackageHandler> remaining) {
		ListOrderedMap toDeploy = new ListOrderedMap();
		while (!remaining.isEmpty()) {
			List<JahiaTemplatesPackageHandler> newRemaining = new ArrayList<JahiaTemplatesPackageHandler>();
			for (JahiaTemplatesPackageHandler handler : remaining) {
				JahiaTemplatesPackage pack = handler.getPackage();
				if (pack.getExtends() == null || toDeploy.containsKey(pack.getExtends())) {
					toDeploy.put(pack.getName(), handler);
				} else {
					newRemaining.add(handler);
				}
			}
			if (newRemaining.equals(remaining)) {
				logger.error("Cannot deploy packages " + newRemaining + ", unresolved dependencies");
				break;
			} else {
				remaining = newRemaining;
			}
		}
		return toDeploy;
	}

	private static String[] getPackageFiles(File sharedTemplatesFolder) {
		String[] packageFiles = ArrayUtils.EMPTY_STRING_ARRAY;

		if (!sharedTemplatesFolder.exists()) {
			sharedTemplatesFolder.mkdirs();
		}

		if (sharedTemplatesFolder.exists() && sharedTemplatesFolder.isDirectory()) {
			packageFiles = sharedTemplatesFolder.list(new SuffixFileFilter(new String[] { ".jar", ".war" }));
		}
		return packageFiles;
	}

	public void setSettingsBean(SettingsBean settingsBean) {
		this.settingsBean = settingsBean;
	}

	public void setTemplatePackageRegistry(TemplatePackageRegistry tmplPackageRegistry) {
		templatePackageRegistry = tmplPackageRegistry;
	}

	public void startWatchdog(WatchdogCallback callback) {
		long interval = settingsBean.isDevelopmentMode() ? 5000 : settingsBean.getTemplatesObserverInterval();
		if (interval <= 0) {
			return;
		}

		logger.info("Starting template packages watchdog with interval " + interval + " ms. Monitoring the folder "
		        + settingsBean.getJahiaSharedTemplatesDiskPath());

		stopWatchdog();
		watchdog = new Timer(true);
		watchdog.schedule(new FolderWatcher(new File(settingsBean.getJahiaSharedTemplatesDiskPath()), callback),
		        interval, interval);
	}

	public void stopWatchdog() {
		if (watchdog != null) {
			watchdog.cancel();
		}
	}

}