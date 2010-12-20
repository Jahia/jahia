/**
 * This file is part of Jahia: An integrated WCM, DMS and Portal Solution
 * Copyright (C) 2002-2010 Jahia Solutions Group SA. All rights reserved.
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

package org.jahia.services.render.scripting;

import com.caucho.quercus.*;
import com.caucho.quercus.env.Env;
import com.caucho.quercus.env.QuercusValueException;
import com.caucho.quercus.env.StringValue;
import com.caucho.quercus.page.QuercusPage;
import com.caucho.quercus.servlet.QuercusServletImpl;
import com.caucho.util.L10N;
import com.caucho.vfs.Path;
import com.caucho.vfs.Vfs;
import com.caucho.vfs.WriteStream;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Enumeration;

/**
 * Customized Quercus servlet implementation, as the default doesn't let us inject our
 * variables in a flexible way :(
 *
 * @author loom
 *         Date: Jan 19, 2010
 *         Time: 12:40:19 PM
 */
public class JahiaQuercusServletImpl extends QuercusServletImpl {

    private static final L10N L = new L10N(JahiaQuercusServletImpl.class);
    
    private static final Logger log
      = org.slf4j.LoggerFactory.getLogger(JahiaQuercusServletImpl.class.getName());

    /**
     * Service.
     */
    @Override
    public void service(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        Env env = null;
        WriteStream ws = null;

        try {
          Path path = getPath(request);

          QuercusPage page;

          try {
            page = getQuercus().parse(path);
          }
          catch (FileNotFoundException ex) {
            // php/2001
            log.debug(ex.toString(), ex);

            response.sendError(HttpServletResponse.SC_NOT_FOUND);

            return;
          }


          ws = openWrite(response);

          // php/6006
          ws.setNewlineString("\n");

          QuercusContext quercus = getQuercus();

          env = quercus.createEnv(page, ws, request, response);
          quercus.setServletContext(_servletContext);

          try {
            env.start();

            // php/2030, php/2032, php/2033
            // Jetty hides server classes from web-app
            // http://docs.codehaus.org/display/JETTY/Classloading
            //
            // env.setGlobalValue("request", env.wrapJava(request));
            // env.setGlobalValue("response", env.wrapJava(response));
            // env.setGlobalValue("servletContext", env.wrapJava(_servletContext));

              /* JAHIA EXTENSION : We copy the request attribute into the environment */
              Enumeration attrNamesEnum = request.getAttributeNames();
              while (attrNamesEnum.hasMoreElements()) {
                  String currentAttributeName = (String) attrNamesEnum.nextElement();
                  if (!"".equals(currentAttributeName)) {
                      env.setGlobalValue(currentAttributeName, env.wrapJava(request.getAttribute(currentAttributeName)));
                  }
              }


            StringValue prepend
              = quercus.getIniValue("auto_prepend_file").toStringValue(env);
            if (prepend.length() > 0) {
              Path prependPath = env.lookup(prepend);

              if (prependPath == null)
                env.error(L.l("auto_prepend_file '{0}' not found.", prepend));
              else {
                QuercusPage prependPage = getQuercus().parse(prependPath);
                prependPage.executeTop(env);
              }
            }

            env.executeTop();

            StringValue append
              = quercus.getIniValue("auto_append_file").toStringValue(env);
            if (append.length() > 0) {
              Path appendPath = env.lookup(append);

              if (appendPath == null)
                env.error(L.l("auto_append_file '{0}' not found.", append));
              else {
                QuercusPage appendPage = getQuercus().parse(appendPath);
                appendPage.executeTop(env);
              }
            }
            //   return;
          }
          catch (QuercusExitException e) {
            throw e;
          }
          catch (QuercusErrorException e) {
            throw e;
          }
          catch (QuercusLineRuntimeException e) {
            log.debug(e.toString(), e);

            ws.println(e.getMessage());
            //  return;
          }
          catch (QuercusValueException e) {
            log.debug(e.toString(), e);

            ws.println(e.toString());

            //  return;
          }
          catch (Throwable e) {
            if (response.isCommitted())
              e.printStackTrace(ws.getPrintWriter());

            ws = null;

            throw e;
          }
          finally {
            if (env != null)
              env.close();

            // don't want a flush for an exception
            if (ws != null && env.getDuplex() == null)
              ws.close();
          }
        }
        catch (QuercusDieException e) {
          // normal exit
          log.debug(e.toString(), e);
        }
        catch (QuercusExitException e) {
          // normal exit
          log.debug(e.toString(), e);
        }
        catch (QuercusErrorException e) {
          // error exit
          log.debug(e.toString(), e);
        }
        catch (RuntimeException e) {
          throw e;
        }
        catch (Throwable e) {
          throw new ServletException(e);
        }
    }

    Path getPath(HttpServletRequest req) {
      String scriptPath = QuercusRequestAdapter.getPageServletPath(req);
      String pathInfo = QuercusRequestAdapter.getPagePathInfo(req);

      Path pwd = Vfs.lookup();

      Path path = pwd.lookup(req.getRealPath(scriptPath));

      if (path.isFile())
        return path;

      // XXX: include

      String fullPath;
      if (pathInfo != null)
        fullPath = scriptPath + pathInfo;
      else
        fullPath = scriptPath;

      return pwd.lookup(req.getRealPath(fullPath));
    }
    
}
