/**
 * 
 * This file is part of Jahia: An integrated WCM, DMS and Portal Solution
 * Copyright (C) 2002-2009 Jahia Limited. All rights reserved.
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
 * in Jahia's FLOSS exception. You should have recieved a copy of the text
 * describing the FLOSS exception, and it is also available here:
 * http://www.jahia.com/license"
 * 
 * Commercial and Supported Versions of the program
 * Alternatively, commercial and supported versions of the program may be used
 * in accordance with the terms contained in a separate written agreement
 * between you and Jahia Limited. If you are unsure which license is appropriate
 * for your use, please contact the sales department at sales@jahia.com.
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
