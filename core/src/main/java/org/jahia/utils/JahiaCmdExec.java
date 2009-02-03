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

