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
