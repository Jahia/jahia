/**
 * This file is part of Jahia: An integrated WCM, DMS and Portal Solution
 * Copyright (C) 2002-2009 Jahia Solutions Group SA. All rights reserved.
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
/* Copyright 2006 Taglab Limited
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License
 */
package org.jahia.services.security.spnego;

import org.apache.commons.codec.binary.Hex;
import org.apache.log4j.Logger;
import org.jahia.services.security.spnego.message.ApplicationConstructedObject;
import org.jahia.services.security.spnego.message.NegTokenTarg;
import org.jahia.services.security.spnego.message.ParseState;

/**
 * Parser for convering <code>byte[]</code> spnego tokens into nice objects.
 * @author Martin Algesten
 */
public class SpnegoParser {

  /**
   * Logger.
   */
  static final Logger LOG = Logger.getLogger("ws.security.SpnegoParser");

  /**
   * Parses an incoming NegTokenInit.
   * @param token the byte array to parse.
   * @return the object if no errors were detected or null if there were errors.
   */
  public ApplicationConstructedObject parseInitToken(byte[] token) {

    ParseState state = new ParseState(token);
    ApplicationConstructedObject result = new ApplicationConstructedObject();

    if (LOG.isDebugEnabled()) {
      LOG.debug("Token dump: " + dump(token));
    }

    try {
      result.parse(state);
    } catch (Exception e) {
      LOG.error("Failed to parse: " + e.getMessage());
      if (LOG.isDebugEnabled()) {
        LOG.debug("Failed to parse", e);
      }
    }

    for (String message : state.getMessages()) {
      LOG.info(message);
    }

    if (state.getMessages().size() == 0) {
      return result;
    } else {
      return null;
    }

  }

  /**
   * Parses a NegTokenTarg.
   * @param token the byte array token.
   * @return the parsed token if no error were encountered, or null if there
   *         were errors.
   */
  public NegTokenTarg parseTargToken(byte[] token) {

    ParseState state = new ParseState(token);

    NegTokenTarg result = new NegTokenTarg();

    if (LOG.isDebugEnabled()) {
      LOG.debug("Token dump: " + dump(token));
    }

    try {
      result.parse(state);
    } catch (Exception e) {
      LOG.error("Failed to parse: " + e.getMessage());
      if (LOG.isDebugEnabled()) {
        LOG.debug("Failed to parse", e);
      }
    }

    for (String message : state.getMessages()) {
      LOG.info(message);
    }

    if (state.getMessages().size() == 0) {
      return result;
    } else {
      return null;
    }

  }

  /**
   * Dumps the given token in a nice string.
   * @param token the source bytes.
   * @return the result String.
   */
  protected String dump(byte[] token) {

    char[] dump = Hex.encodeHex(token);

    StringBuilder buf = new StringBuilder();

    buf.append("0 : ");

    for (int i = 0; i < dump.length; i += 2) {
      buf.append("0x");
      buf.append(dump[i]);
      buf.append(dump[i + 1]);
      buf.append(" ");
      if (i > 0 && i % 30 == 0) {
        buf.append("\n" + (i / 2 + 1) + ": ");
      }
    }

    return buf.toString();

  }

}
