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

 package org.jahia.operations.valves;

import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.jahia.exceptions.JahiaException;
import org.jahia.exceptions.JahiaSessionExpirationException;
import org.jahia.params.ProcessingContext;
import org.jahia.pipelines.PipelineException;
import org.jahia.pipelines.valves.Valve;
import org.jahia.pipelines.valves.ValveContext;

/**
 * <p>Title: </p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2004</p>
 * <p>Company: Jahia Ltd</p>
 * @author Serge Huber
 * @version 1.0
 */

public class OperationModeCheckValve implements Valve {
    public OperationModeCheckValve () {
    }

    public void invoke (Object context, ValveContext valveContext)
        throws PipelineException {
        ProcessingContext processingContext = (ProcessingContext) context;
        try {
            if ("core".equals(processingContext.getEngine())) {
                if (processingContext.getPage() == null &&
                    (processingContext.getOperationMode().equals(ProcessingContext.EDIT)
                     || processingContext.getOperationMode().equals(ProcessingContext.COMPARE))) {
                    // try to switch to NORMAL Mode
                    // force to recompute all locales be removing session locale
                    // and switching to NOMAL MODE
                    processingContext.getSessionState().removeAttribute(ProcessingContext.
                        SESSION_LOCALE);
                    processingContext.flushLocaleListCache();
                    List<Locale> locales = processingContext.getLocales(true);
                    Map pageTitles = processingContext.getContentPage().getTitles(true);
                    Locale locale = null;
                    String lang = null;
                    for (int i = 0; i < locales.size(); i++) {
                        locale = locales.get(i);
                        if (pageTitles.containsKey(locale.toString())) {
                            lang = locale.toString();
                            break;
                        }
                    }

                    if (lang != null) {
                        processingContext.getSessionState().setAttribute(ProcessingContext.
                            SESSION_LOCALE,
                            locale);
                        processingContext.setUser(processingContext.getUser());
                        processingContext.changePage(processingContext.getContentPage());
                    }
                }
            }
        } catch (JahiaSessionExpirationException jsee) {
            throw new PipelineException(jsee);
        } catch (JahiaException je) {
            throw new PipelineException(je);
        }

        valveContext.invokeNext(context);

    }

    public void initialize () {
    }

}
