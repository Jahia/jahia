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

 package org.jahia.utils;

import java.io.FileInputStream;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Properties;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;


/**
 * Class ActionHandler: determines the method to invoke for an Application
 * (typically a Web Application), given an action.
 *
 * @author  Stephane Boury, Jerome Tamiotti
 * @version 1.1
 */
public class ActionHandler {

    private static org.apache.log4j.Logger logger =
            org.apache.log4j.Logger.getLogger(ActionHandler.class);

    /* The class on which to call the methods */
    private Class actionClass;

    /**
	 * An instance of the class on which to call the methods
	 * This instance is null here because the class is static,
	 * and the parameter of 'invokeMethod' method will be ignored
	 */
    private Object instanceOfClass = null;


    /* The Map containing the method names */
	private Properties methods;

    /**
	 * Class constructor: builds the Map by reading a flat file.
	 *
	 * @param propsFile     the full path to the file containing pairs of
	 * "action - method name"
	 * @param theClass      the name of the Class on which to call the methods
	 */
    public ActionHandler(String propsFile, String theClass) {

        methods = new Properties();
		try {
			FileInputStream fis = new FileInputStream(propsFile);
			methods.load(fis);
			fis.close();
		}
		catch (IOException e) {
			logger.error("ActionHandler-ActionHandler", e);
		}

        try {
            actionClass = Class.forName(theClass);
        } catch (ClassNotFoundException ex) {
            logger.error("ActionHandler-ActionHandler", ex);
        } catch (Exception ex) {
            logger.error("ActionHandler-ActionHandler", ex);
        }
    }

    /**
     * Calls the appropriate method given the action.
     *
     * @param action    the action that triggers the method to call
     * @param request   the request parameter for the method
     */
    public Object call(String action, HttpServletRequest request) throws NullPointerException {

        String theMethod = methods.getProperty(action);
		if (theMethod == null) { //method not found in list
			logger.error("No method found for action " + action);
			throw new NullPointerException();
		}
        try {
			Class theParams[] = {Class.forName("javax.servlet.http.HttpServletRequest")};
            Method thisMethod = actionClass.getDeclaredMethod(theMethod, theParams);
			Object args[] = {request};
            return thisMethod.invoke(instanceOfClass, args);

        } catch(ClassNotFoundException cnfe) {
            logger.error("ActionHandler-call", cnfe);
			throw new NullPointerException();

		} catch(NoSuchMethodException nsme) {
            logger.error("ActionHandler-call", nsme);
			throw new NullPointerException();

		} catch(IllegalAccessException iae) {
            logger.error("ActionHandler-call", iae);
			throw new NullPointerException();

		} catch(InvocationTargetException ite) {
            logger.error("Target exception", ite.getTargetException());
            logger.error("ActionHandler-call", ite);

			throw new NullPointerException();
		}
    }


    /**
     * Calls the appropriate method given the action.
     *
     * @param action     the action that triggers the method to call
     * @param request    the request parameter for the method
     * @param response   the response parameter for the method
     */
    public Object call( Object instanceOfClass, String action, HttpServletRequest request, HttpServletResponse response) throws NullPointerException {

        String theMethod = methods.getProperty(action);
		if (theMethod == null) { //method not found in list
			logger.error("No method found for action " + action);
			throw new NullPointerException();
		}
        try {
			Class theParams[] = {Class.forName("javax.servlet.http.HttpServletRequest"),Class.forName("javax.servlet.http.HttpServletResponse")};
            Method thisMethod = actionClass.getDeclaredMethod(theMethod, theParams);
			Object args[] = {request,response};

            return thisMethod.invoke(instanceOfClass, args);

        } catch(ClassNotFoundException cnfe) {
            logger.error("Class not found", cnfe);
			throw new NullPointerException();

		} catch(NoSuchMethodException nsme) {
            logger.error("No such method", nsme);
			throw new NullPointerException();

		} catch(IllegalAccessException iae) {
            logger.error("Illegal access exception", iae);
			throw new NullPointerException();

		} catch(InvocationTargetException ite) {
            logger.error("Invocation error", ite);
            if (ite.getTargetException() != null) {
                logger.error("Root cause", ite.getTargetException());
            }
			throw new NullPointerException();
		}
    }

}
