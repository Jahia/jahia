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
//  JahiaKeyGen
//  NK      18.04.2001
//
//  NK      02.05.2001  // bug severe , utilisation de Random() car system.currentTimeMillis n'est pas al�atoire comme seed !!

//

package org.jahia.utils.keygenerator;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Random;

import org.jahia.utils.Base64;
import org.jahia.utils.JahiaTools;


/**
 * Random Key Generator
 *
 * @author NK
 */


public class JahiaKeyGen
{

	
	/** 
	 * The MersenneTwisterFast instance 
	 *
	 **/
	/*
	private static MersenneTwisterFast mtfRandom = null;


	static {
		mtfRandom = new MersenneTwisterFast();
	}
	*/

	static String mLastKey = "";


	//--------------------------------------------------------------------------
    /**
     * Return a random String key.
	 *
     * @author  NK
     * @return  String a random key
     */
	public static synchronized String getKey(){
		
		MersenneTwisterFast mtfRandom = new MersenneTwisterFast();
		Random rd = new Random();
   		mtfRandom.setSeed(rd.nextLong());
		String k = Long.toString(mtfRandom.nextLong());
		while ( k.equals(mLastKey) || k.startsWith("-") ){
    		mtfRandom.setSeed(rd.nextLong());
			k = Long.toString(mtfRandom.nextLong());
		}
		mLastKey = k;
		return k;
	}


	//--------------------------------------------------------------------------
    /**
     * Return a random String key with initialization.
     * Initalize the pseudo random number generator.
     *
     * @author  NK
     * @param long the seed
     * @return  String a random key
     */
    public static synchronized String getKey(long seed){
    	
		MersenneTwisterFast mtfRandom = new MersenneTwisterFast();
    	mtfRandom.setSeed(seed);
		String k = Long.toString(mtfRandom.nextLong());
		while ( k.equals(mLastKey) || k.startsWith("-") ){
			k = Long.toString(mtfRandom.nextLong());
		}
		mLastKey = k;
		return k;
    }


	//--------------------------------------------------------------------------
    /**
     * Return a random String key limited to n characters.
	 *
     * @author  NK
     * @return  int number of char
     * @return  String a random key
     */
	public static synchronized String getKey(int n){
		
		MersenneTwisterFast mtfRandom = new MersenneTwisterFast();
		Random rd = new Random();
   		mtfRandom.setSeed(rd.nextLong());
		String k = encrypt(Long.toString(mtfRandom.nextLong()));
		while ( k.equals(mLastKey) || k.startsWith("-") || (k.length()<n) ){
    		mtfRandom.setSeed(rd.nextLong());
			k = encrypt(Long.toString(mtfRandom.nextLong()));
		}
		
		String result = JahiaTools.replacePattern(k,"+","01");
		result = JahiaTools.replacePattern(result,"/","10");
		result = JahiaTools.replacePattern(result,"=","11");
		
		mLastKey = k;
		return result.substring(0,n);

	}


    /**
     * Encrypt a specified using the SHA algorithm.
     *
     * @param  string to encrypt
     *
     * @return String the encrypted string or null on any failure
     */
    private static String encrypt (String data) {

        if (data == null) {
            return null;
        }

        if (data.length() == 0) {
            return null;
        }

        String result = null;

        try {
            MessageDigest md = MessageDigest.getInstance ("SHA-1");
            if (md != null)
            {
                md.reset ();
                md.update (data.getBytes());
                result = new String(Base64.encode (md.digest()));
            }
            md = null;
        }
        catch (NoSuchAlgorithmException ex) {
            result = null;
        }

        return result;
    }


	
}