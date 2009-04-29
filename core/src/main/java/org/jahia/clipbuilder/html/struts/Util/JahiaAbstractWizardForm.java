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
 package org.jahia.clipbuilder.html.struts.Util;

import org.apache.struts.action.*;
import java.lang.reflect.*;
import org.jahia.clipbuilder.html.bean.*;

/**
 *  Description of the Class
 *
 *@author    Tlili Khaled
 */
public abstract class JahiaAbstractWizardForm extends ActionForm {

	private static org.apache.log4j.Logger logger = org.apache.log4j.Logger.getLogger(JahiaAbstractWizardForm.class);


	/**
	 *  Sets the AllPropertiesToNull attribute of the ClipperForm object
	 */
	public void setAllPropertiesToNull() {
		try {
			// test if the source and the target has same class
			Method[] methods = getClass().getMethods();
			for (int i = 0; i < methods.length; i++) {
				Method m = methods[i];
				Class declarationClass = m.getDeclaringClass();
				// don't process method in the super classes
				if (declarationClass.equals(getClass())) {
					String label = m.getName();
					logger.debug("[ Method " + label + " is in process. ]");
					// process only the get method
					if (label.substring(0, 3).equalsIgnoreCase("set")) {
						//execute the setMethod on the target form
						String methodToCall = "set" + label.substring(3);
						String[] values = {null};
						Class[] paramType = {String.class};
						this.getClass().getMethod(methodToCall, paramType).invoke((Object)this, (Object[])values);
					}
				}
			}

		}
		catch (NoSuchMethodException ex) {
			logger.error(" [ Error " + ex.toString() + " ] ", ex);
		}
		catch (InvocationTargetException ex) {
			logger.error(" [ Error " + ex.toString() + " ] ", ex);
		}
		catch (IllegalArgumentException ex) {
			logger.error(" [ Error " + ex.toString() + " ] ", ex);
		}
		catch (IllegalAccessException ex) {
			logger.error(" [ Error " + ex.getMessage() + " ] ", ex);
		}
		catch (SecurityException ex) {
			logger.error(" [ Error " + ex.getMessage() + " ] ", ex);
		}
	}


	/**
	 *  Gets the Id attribute of the ClipperAction object
	 *
	 *@return    The Id value or -1 if not found
	 */
	public abstract int getId();


	/**
	 *  Gets the NextFormId attribute of the ClipperForm object
	 *
	 *@return    The NextFormId value
	 */
	public int getNextFormId() {
		int id = this.getId();
		if (id < JahiaClipBuilderConstants.MANAGE || id > JahiaClipBuilderConstants.PREVIEW) {
			return -1;
		}

		return id + 1;
	}


	/**
	 *  Description of the Method
	 *
	 *@param  cBean  Description of Parameter
	 */
	public abstract void loadFromClipperBean(ClipperBean cBean);

}
