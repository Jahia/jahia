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
//
//
//  JahiaCmdExec
//
//  NK      13.01.2001
//
//

package org.jahia.utils;


import java.io.File;

import org.jahia.exceptions.JahiaException;

/**
 * Execute an External Command Line
 *
 * @author Khue ng
 */
public class JahiaCmdExec {

   protected String m_Cmd = "";

   /**
    * Constructor
    *
    * @param (String) cmd, the command line
    */
   public JahiaCmdExec(String cmd) {

      m_Cmd = cmd;

   }


   /**
    * Execute the command in the current working dir
    *
    * @exception (JahiaException) on error
    */
   public void execute() throws JahiaException {

      try {

         Runtime.getRuntime().exec(m_Cmd);

      } catch (Exception e) {

         String errMsg = "Failed executing command line " + m_Cmd ;
         JahiaConsole.println("JahiaCmdExec::execute()", errMsg + "\n" + e.toString());
         throw new JahiaException ("JahiaCmdExec", errMsg ,
                                    JahiaException.SERVICE_ERROR, JahiaException.WARNING_SEVERITY, e);

      }
   }


   /**
    * Execute the command in a specified working dir
    *
    * @param (File) workingDir , The abstract File object to the new working dir
    * @exception (JahiaException) on error
    */
   public void execute(File workingDir) throws JahiaException {

      try {
         Runtime.getRuntime().exec(m_Cmd,new String[]{""},workingDir);

      } catch (Exception e) {

         String errMsg = "Failed executing command line " + m_Cmd ;
         JahiaConsole.println("JahiaCmdExec::execute()", errMsg + "\n" + e.toString());
         throw new JahiaException ("JahiaCmdExec", errMsg ,
                                    JahiaException.SERVICE_ERROR, JahiaException.WARNING_SEVERITY, e);

      }
   }


}

