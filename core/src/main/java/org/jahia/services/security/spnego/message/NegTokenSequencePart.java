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
/*
 * Copyright 2006 Taglab Limited
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License
 */
package org.jahia.services.security.spnego.message;

/**
 * @author Martin Algesten
 */
public class NegTokenSequencePart extends AbstractMessagePart {

  private int seqNo = 0; // 0, 1, 2, 3, 4...

  private MessagePart messagePart;

  public NegTokenSequencePart() {
  }

  public NegTokenSequencePart(int seqNo, MessagePart messagePart) {
    this.seqNo = seqNo;
    this.messagePart = messagePart;
  }

  /**
   * 0xa0, 0xa1, 0xa2, 0xa3...
   * {@inheritDoc}
   */
  public int getDerType() {
    return 0xa0 + seqNo;
  }

  /**
   * {@inheritDoc} 
   */
  public int[] toDer() {
    return wrap(getDerType(), messagePart.toDer());
  }

  /**
   * {@inheritDoc} 
   */
  public void parse(ParseState state) {
    seqNo = 0xff & state.getToken()[state.getIndex()];
    state.setIndex(state.getIndex() + 1);
    if (seqNo < 0xa0)
      state.addMessage("Expected sequence number > 0xa0: " + seqNo);
    seqNo = seqNo - 0xa0;
    state.parseDerLength(); // Can't verify.
    messagePart.parse(state);
  }

  public void setSeqNo(int seqNo) {
    this.seqNo = seqNo;
  }

  public int getSeqNo() {
    return seqNo;
  }

  public MessagePart getMessagePart() {
    return messagePart;
  }

  public void setMessagePart(MessagePart messagePart) {
    this.messagePart = messagePart;
  }

}
