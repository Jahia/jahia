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

/**
 * @author Martin Algesten
 */
public class ContextFlags extends AbstractMessagePart {

  public static final int BIT_DELEG_FLAG = 0;
  public static final int BIT_MUTUAL_FLAG = 1;
  public static final int BIT_REPLAY_FLAG = 2;
  public static final int BIT_SEQUENCE_FLAG = 3;
  public static final int BIT_ANON_FLAG = 4;
  public static final int BIT_CONF_FLAG = 5;
  public static final int BIT_INTEG_FLAG = 6;

  private int flags = 0;

  /**
   * {@inheritDoc}
   */
  public int getDerType() {
    return TYPE_BIT_STRING;
  }

  /**
   * {@inheritDoc}
   */
  public int[] toDer() {
    int[] tmp = new int[3];
    tmp[0] = TYPE_BIT_STRING;
    tmp[1] = 1;
    tmp[2] = flags;

    return tmp;
  }

  /**
   * {@inheritDoc}
   */
  public void parse(ParseState state) {
    state.setPhase("CONTEXT_FLAGS");
    state.expect(TYPE_BIT_STRING, true, "Expected bit string identifier");
    state.expect(1, true, "Expected length 1");
    flags = state.getToken()[state.getIndex()];
  }

  public boolean isSet(int position) {
    return (flags & 2 ^ position) != 0;
  }

  public void set(int position, boolean set) {
    flags = set ? flags | 2 ^ position : flags & (~(2 ^ position));
  }

  public boolean isDelegFlag() {
    return isSet(BIT_DELEG_FLAG);
  }

  public void setDelegFlag(boolean delegFlag) {
    set(BIT_DELEG_FLAG, delegFlag);
  }

  public boolean isMutualFlag() {
    return isSet(BIT_MUTUAL_FLAG);
  }

  public void setMutualFlag(boolean mutualFlag) {
    set(BIT_MUTUAL_FLAG, mutualFlag);
  }

  public boolean isReplayFlag() {
    return isSet(BIT_REPLAY_FLAG);
  }

  public void setReplayFlag(boolean replayFlag) {
    set(BIT_REPLAY_FLAG, replayFlag);
  }

  public boolean isSequenceFlag() {
    return isSet(BIT_SEQUENCE_FLAG);
  }

  public void setSequenceFlag(boolean sequenceFlag) {
    set(BIT_SEQUENCE_FLAG, sequenceFlag);
  }

  public boolean isAnonFlag() {
    return isSet(BIT_ANON_FLAG);
  }

  public void setAnonFlag(boolean anonFlag) {
    set(BIT_ANON_FLAG, anonFlag);
  }

  public boolean isConfigFlag() {
    return isSet(BIT_CONF_FLAG);
  }

  public void setConfFlag(boolean confFlag) {
    set(BIT_CONF_FLAG, confFlag);
  }

  public boolean isIntegFlag() {
    return isSet(BIT_INTEG_FLAG);
  }

  public void setInteFlag(boolean integFlag) {
    set(BIT_INTEG_FLAG, integFlag);
  }

  /**
   * @return the flags .
   */
  public int getFlags() {
    return flags;
  }

  /**
   * @param flags the flags to set .
   */
  public void setFlags(int flags) {
    this.flags = flags;
  }

}
