package org.jahia.test;

import junit.framework.TestCase;

import org.apache.log4j.Logger;

/**
 * Super class for Jahia tests
 * 
 * @author Guillaume Lucazeau
 * 
 */
public class JahiaTestCase extends TestCase {

	private static Logger logger = Logger.getLogger(JahiaTestCase.class);

	private String baseUrl = "http://localhost:8080";
	
	protected String getBaseServerURL() {
		 	logger.info("Base URL for tests is: " + baseUrl);
	        return baseUrl;
	    }
}
