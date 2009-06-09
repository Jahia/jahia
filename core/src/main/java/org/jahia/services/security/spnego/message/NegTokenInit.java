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
package org.jahia.services.security.spnego.message;

import java.util.LinkedList;

/**
 * @author Martin Algesten
 */
public class NegTokenInit extends AbstractSequence<NegTokenSequencePart> {

  /**
   * {@inheritDoc}
   */
  @Override
  public int getDerType() {
    return TYPE_NEG_TOKEN_INIT;
  }

  public static final int INDEX_MECH_TYPES = 0;
  public static final int INDEX_REQ_FLAGS = 1;
  public static final int INDEX_MECH_TOKEN = 2;
  public static final int INDEX_MECH_LIST_MIC = 3;

  private MechTypeList mechTypes;
  private ContextFlags contextFlags;
  private OctetString mechToken;
  private OctetString mechListMIC;

  /**
   * {@inheritDoc}
   */
  @Override
  protected LinkedList<NegTokenSequencePart> getParts() {

    LinkedList<NegTokenSequencePart> tmp = new LinkedList<NegTokenSequencePart>();

    if (mechTypes != null)
      tmp.add(new NegTokenSequencePart(INDEX_MECH_TYPES, mechTypes));

    if (contextFlags != null)
      tmp.add(new NegTokenSequencePart(INDEX_REQ_FLAGS, contextFlags));

    if (mechToken != null)
      tmp.add(new NegTokenSequencePart(INDEX_MECH_TOKEN, mechToken));

    if (mechListMIC != null)
      tmp.add(new NegTokenSequencePart(INDEX_MECH_LIST_MIC, mechListMIC));

    return tmp;

  }

  /**
   * {@inheritDoc}
   */
  @Override
  public int[] toDer() {
    int[] tmp = super.toDer();
    return wrap(TYPE_NEG_TOKEN_INIT, tmp);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void parse(ParseState state) {
    state.setPhase("NEG_TOKEN_INIT");
    state.expect(TYPE_NEG_TOKEN_INIT, true, "Expected NegTokenInit identifier");
    state.parseDerLength(); // I can't really verify it.
    super.parse(state);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  protected NegTokenSequencePart createInstance(ParseState state) {
    NegTokenSequencePart part = new NegTokenSequencePart();
    if (state.expect(0xa0, false, null)) {
      mechTypes = new MechTypeList();
      part.setMessagePart(mechTypes);
    } else if (state.expect(0xa1, false, null)) {
      contextFlags = new ContextFlags();
      part.setMessagePart(contextFlags);
    } else if (state.expect(0xa2, false, null)) {
      mechToken = new OctetString();
      part.setMessagePart(mechToken);
    } else if (state.expect(0xa3, false, null)) {
      mechListMIC = new OctetString();
      part.setMessagePart(mechListMIC);
    } else {
      state.addMessage("Unexpected message part sequence no: "
          + state.getToken()[state.getIndex()]);
    }
    return part;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  protected void setParts(LinkedList<NegTokenSequencePart> parts) {
    for (NegTokenSequencePart part : parts) {
      switch (part.getSeqNo()) {
        case INDEX_MECH_TYPES:
          mechTypes = (MechTypeList) part.getMessagePart();
          break;
        case INDEX_REQ_FLAGS:
          contextFlags = (ContextFlags) part.getMessagePart();
          break;
        case INDEX_MECH_TOKEN:
          mechToken = (OctetString) part.getMessagePart();
          break;
        case INDEX_MECH_LIST_MIC:
          mechListMIC = (OctetString) part.getMessagePart();
          break;
        default:
          throw new RuntimeException("Unexpected sequence number: "
              + part.getSeqNo());
      }
    }
  }

  /**
   * @return context flags.
   */
  public ContextFlags getContextFlags() {
    return contextFlags;
  }

  /**
   * @param contextFlags the context flags.
   */
  public void setContextFlags(ContextFlags contextFlags) {
    this.contextFlags = contextFlags;
  }

  /**
   * @return mechanism list.
   */
  public OctetString getMechListMIC() {
    return mechListMIC;
  }

  /**
   * @param mechListMIC the mechanism list.
   */
  public void setMechListMIC(OctetString mechListMIC) {
    this.mechListMIC = mechListMIC;
  }

  /**
   * @return mechanism token.
   */
  public OctetString getMechToken() {
    return mechToken;
  }

  /**
   * @param mechToken the mechanism token.
   */
  public void setMechToken(OctetString mechToken) {
    this.mechToken = mechToken;
  }

  public MechTypeList getMechTypes() {
    return mechTypes;
  }

  public void setMechTypes(MechTypeList mechTypes) {
    this.mechTypes = mechTypes;
  }

}
