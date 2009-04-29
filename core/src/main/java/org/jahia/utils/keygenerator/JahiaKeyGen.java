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