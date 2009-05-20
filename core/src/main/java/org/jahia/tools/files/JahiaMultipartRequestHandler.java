/**
 * Jahia Enterprise Edition v6
 *
 * Copyright (C) 2002-2009 Jahia Solutions Group. All rights reserved.
 *
 * Jahia delivers the first Open Source Web Content Integration Software by combining Enterprise Web Content Management
 * with Document Management and Portal features.
 *
 * The Jahia Enterprise Edition is delivered ON AN "AS IS" BASIS, WITHOUT WARRANTY OF ANY KIND, EITHER EXPRESSED OR
 * IMPLIED.
 *
 * Jahia Enterprise Edition must be used in accordance with the terms contained in a separate license agreement between
 * you and Jahia (Jahia Sustainable Enterprise License - JSEL).
 *
 * If you are unsure which license is appropriate for your use, please contact the sales department at sales@jahia.com.
 */
package org.jahia.tools.files;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.fileupload.FileItem;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionServlet;
import org.apache.struts.upload.FormFile;
import org.apache.struts.upload.MultipartRequestHandler;
import org.jahia.params.ParamBean;

/**
 * Jahia specific handler for the "multipart/form-data" requests used to upload
 * and handle files in Struts forms and actions.
 * 
 * @author Sergiy Shyrkov
 */
public class JahiaMultipartRequestHandler implements MultipartRequestHandler {

	/**
	 * This class implements the Struts <code>FormFile</code> interface by
	 * wrapping the Commons FileUpload <code>FileItem</code> interface. This
	 * implementation is <i>read-only</i>; any attempt to modify an instance of
	 * this class will result in an <code>UnsupportedOperationException</code>.
	 * This code is taken from the Struts code base.
	 */
	static class CommonsFormFile implements FormFile, Serializable {

		/**
		 * The <code>FileItem</code> instance wrapped by this object.
		 */
		FileItem fileItem;

		/**
		 * Constructs an instance of this class which wraps the supplied file
		 * item.
		 * 
		 * @param fileItem
		 *            The Commons file item to be wrapped.
		 */
		public CommonsFormFile(FileItem fileItem) {
			this.fileItem = fileItem;
		}

		/**
		 * Destroy all content for this form file. Implementations should remove
		 * any temporary files or any temporary file data stored somewhere
		 */
		public void destroy() {
			fileItem.delete();
		}

		/**
		 * Returns the base file name from the supplied file path. On the
		 * surface, this would appear to be a trivial task. Apparently, however,
		 * some Linux JDKs do not implement <code>File.getName()</code>
		 * correctly for Windows paths, so we attempt to take care of that here.
		 * 
		 * @param filePath
		 *            The full path to the file.
		 * @return The base file name, from the end of the path.
		 */
		protected String getBaseFileName(String filePath) {

			// First, ask the JDK for the base file name.
			String fileName = new File(filePath).getName();

			// Now check for a Windows file name parsed incorrectly.
			int colonIndex = fileName.indexOf(":");
			if (colonIndex == -1) {
				// Check for a Windows SMB file path.
				colonIndex = fileName.indexOf("\\\\");
			}
			int backslashIndex = fileName.lastIndexOf("\\");

			if (colonIndex > -1 && backslashIndex > -1) {
				// Consider this filename to be a full Windows path, and parse
				// it
				// accordingly to retrieve just the base file name.
				fileName = fileName.substring(backslashIndex + 1);
			}

			return fileName;
		}

		/**
		 * Returns the content type for this file.
		 * 
		 * @return A String representing content type.
		 */
		public String getContentType() {
			return fileItem.getContentType();
		}

		/**
		 * Returns the data for this file as a byte array. Note that this may
		 * result in excessive memory usage for large uploads. The use of the
		 * {@link #getInputStream() getInputStream} method is encouraged as an
		 * alternative.
		 * 
		 * @return An array of bytes representing the data contained in this
		 *         form file.
		 * @exception FileNotFoundException
		 *                If some sort of file representation cannot be found
		 *                for the FormFile
		 * @exception IOException
		 *                If there is some sort of IOException
		 */
		public byte[] getFileData() throws FileNotFoundException, IOException {
			return fileItem.get();
		}

		/**
		 * Returns the (client-side) file name for this file.
		 * 
		 * @return The client-size file name.
		 */
		public String getFileName() {
			return getBaseFileName(fileItem.getName());
		}

		/**
		 * Returns the size, in bytes, of this file.
		 * 
		 * @return The size of the file, in bytes.
		 */
		public int getFileSize() {
			return (int) fileItem.getSize();
		}

		/**
		 * Get an InputStream that represents this file. This is the preferred
		 * method of getting file data.
		 * 
		 * @exception FileNotFoundException
		 *                If some sort of file representation cannot be found
		 *                for the FormFile
		 * @exception IOException
		 *                If there is some sort of IOException
		 */
		public InputStream getInputStream() throws FileNotFoundException,
		        IOException {
			return fileItem.getInputStream();
		}

		/**
		 * Sets the content type for this file.
		 * <p>
		 * NOTE: This method is not supported in this implementation.
		 * 
		 * @param contentType
		 *            A string representing the content type.
		 */
		public void setContentType(String contentType) {
			throw new UnsupportedOperationException(
			        "The setContentType() method is not supported.");
		}

		/**
		 * Sets the (client-side) file name for this file.
		 * <p>
		 * NOTE: This method is not supported in this implementation.
		 * 
		 * @param fileName
		 *            The client-side name for the file.
		 */
		public void setFileName(String fileName) {
			throw new UnsupportedOperationException(
			        "The setFileName() method is not supported.");
		}

		/**
		 * Sets the size, in bytes, for this file.
		 * <p>
		 * NOTE: This method is not supported in this implementation.
		 * 
		 * @param filesize
		 *            The size of the file, in bytes.
		 */
		public void setFileSize(int filesize) {
			throw new UnsupportedOperationException(
			        "The setFileSize() method is not supported.");
		}

		/**
		 * Returns the (client-side) file name for this file.
		 * 
		 * @return The client-size file name.
		 */
		public String toString() {
			return getFileName();
		}
	}

	/**
	 * The combined text and file request parameters.
	 */
	private Hashtable elementsAll;

	/**
	 * The file request parameters.
	 */
	private Hashtable elementsFile;

	/**
	 * The text request parameters.
	 */
	private Hashtable elementsText;

	/**
	 * The action mapping with which this handler is associated.
	 */
	private ActionMapping mapping;

	/**
	 * The servlet with which this handler is associated.
	 */
	private ActionServlet servlet;

	public void finish() {
		rollback();
	}

	public Hashtable getAllElements() {
		return elementsAll;
	}

	public Hashtable getFileElements() {
		return elementsFile;
	}

	public ActionMapping getMapping() {
		return mapping;
	}

	public ActionServlet getServlet() {
		return servlet;
	}

	public Hashtable getTextElements() {
		return elementsText;
	}

	public void handleRequest(HttpServletRequest request)
	        throws ServletException {

		ParamBean ctx = (ParamBean) request
		        .getAttribute("org.jahia.params.ParamBean");

		if (ctx == null)
			throw new IllegalArgumentException(
			        "Unable find the ParamBean object in the request scope");

		FileUpload fileUpload = ctx.getFileUpload();
		elementsText = new Hashtable(fileUpload.getParameterMap());

		Map fileItems = fileUpload.getFileItems();
		elementsFile = new Hashtable(fileItems.size());
		for (Iterator iterator = fileItems.entrySet().iterator(); iterator
		        .hasNext();) {
			Map.Entry item = (Map.Entry) iterator.next();
			elementsFile.put(item.getKey(), new CommonsFormFile((FileItem) item
			        .getValue()));
		}

		elementsAll = new Hashtable(elementsText.size() + elementsFile.size());
		elementsAll.putAll(elementsText);
		elementsAll.putAll(elementsFile);
	}

	public void rollback() {
		Iterator iter = elementsFile.values().iterator();

		while (iter.hasNext()) {
			FormFile formFile = (FormFile) iter.next();

			formFile.destroy();
		}
	}

	public void setMapping(ActionMapping mapping) {
		this.mapping = mapping;
	}

	public void setServlet(ActionServlet servlet) {
		this.servlet = servlet;
	}

}
