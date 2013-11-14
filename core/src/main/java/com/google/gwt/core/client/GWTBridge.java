package com.google.gwt.core.client;

import com.google.gwt.core.shared.*;

/**
 * This class is used for fixing class not found com.google.gwt.core.client.GWTBridge;
 *
 * This is only needed for GXT 2 to work. Once we remove GXT2 or it will be fixed, this can be removed also.
 */

public abstract class GWTBridge extends com.google.gwt.core.shared.GWTBridge {
}