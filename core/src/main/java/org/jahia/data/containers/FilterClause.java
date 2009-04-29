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
//
//
// 27.05.2002 NK added in Jahia


package org.jahia.data.containers;

import java.io.Serializable;

import org.apache.log4j.Logger;



/**
 * Hold information about a filtering clause.
 *
 * @see ContainerFilterBean
 * @author Khue Nguyen <a href="mailto:khue@jahia.org">khue@jahia.org</a>
 */
class FilterClause implements Serializable {
    
    private static final transient Logger logger = Logger.getLogger(FilterClause.class);

	/** The lower comparator or simple comparator **/
	private String lowerComp;
	/** The upper comparator **/
	private String upperComp;
	/** The array of values **/
	private String[] values;
	/** The valid state of this clause instance **/
	private boolean isValid = false;
	/** If this clause a range clause ( with two comparators ) **/
	private boolean isRangeClause = false;


	//--------------------------------------------------------------------------
	/**
	 * Constructor for a simple comparison clause with a single value.
	 *
	 * @param String comparator, the comparator
	 * @param String value, the value
	 */
	public FilterClause(String comp, String value){
		if ( value != null && comp != null )
		{

			String[] values = {value};
			this.values = values;
			this.lowerComp = comp;
			this.isValid = true;
		}
	}

	//--------------------------------------------------------------------------
	/**
	 * Constructor for a simple comparison clause with a multiple values.
	 *
	 * @param String comparator, the comparator
	 * @param String[] values, the values
	 */
	public FilterClause(String comp, String[] values){
		if ( (values != null) && (values.length>0)&& (comp != null) )
		{
			this.values = values;
			this.lowerComp = comp;
			this.isValid = true;
		}
	}

	//--------------------------------------------------------------------------
	/**
	 * Constructor for a range comparison clause.
	 *
	 * @param String lowerComp, the lower comparator
	 * @param String upperComp, the upper comparator
	 * @param String lowerValue, the lower value
	 * @param String upperValue, the upper value
	 */
	public FilterClause( String lowerComp,
						 String upperComp,
						 String lowerVal,
						 String upperVal ){

		if ( (lowerVal != null)
			 && (upperVal != null)
			 && (lowerComp != null)
			 && (upperComp != null) )
		{
			String[] values = {lowerVal,upperVal};
			this.values = values;
			this.lowerComp = lowerComp;
			this.upperComp = upperComp;
			this.isValid = true;
			this.isRangeClause = true;
		}
	}

	/**
	 * Return true if this clause is valid.
	 */
	public boolean isValid(){
		return this.isValid;
	}

	/**
	 * Return true if this clause is a range clause ( with two comparators )
	 */
	public boolean isRangeClause(){
		return this.isRangeClause;
	}

	/**
	 * Return the values.
	 */
	public String[] getValues(){
		return this.values;
	}

	/**
	 * Return the single value (= the first value [0] in the values array).
	 */
	public String getValue(){
		return this.values[0];
	}

	/**
	 * return the lower value (= the first value [0] in the values array).
	 */
	public String getLowerValue(){
		return this.values[0];
	}

	/**
	 * return the upper value (= the second value [1] in the values array).
	 */
	public String getUpperValue(){
		return this.values[1];
	}

	/**
	 * return the comparator for a simple clause.
	 */
	public String getComp(){
		return this.lowerComp;
	}

	/**
	 * return the lower comparator for a range clause.
	 */
	public String getLowerComp(){
		return this.lowerComp;
	}

	/**
	 * return the upper comparator for a range clause.
	 */
	public String getUpperComp(){
		return this.upperComp;
	}

	//--------------------------------------------------------------------------
	/**
	 * Return true if the given Long value match this clause
	 * Here, the clause's values are converted to long and a number comparison is performed
	 *
	 * @param value
     * @param numberFormat , @see NumberFormats
	 * @return boolean , the comparison result.
	 */
	public boolean compareNumber(String value, String numberFormat){
		if ( !isValid ){
			return false;
		}

		boolean result = false;

		try {
			String comp = getComp();
            String v = null;
            // SCSE: check for IS_NULL and NOT_NULL clauses
            if ( isNullOrNotNullClause() ){
              result = compareNullOrNotNull(value);
            } else if ( isRangeClause() ) {
                v = getValue();
                result = compare(value,v,comp,numberFormat);
                if ( result )
                {
                    v = getUpperValue();
                    result = compare(value,v,getUpperComp(),numberFormat);
                }
            } else {
                String[] vals = this.getValues();
                int size = vals.length;
                for ( int i=0; i<size; i++ ){
                    v = vals[i];
                    result = compare(value,v,comp,numberFormat);
                    if ( result ){
                        return true;
                    }
                }
            }
		} catch ( Exception e ){
            logger.error(e.getMessage(), e);
		}
		return result;
	}

    //--------------------------------------------------------------------------
    /**
     *
     * @param value
     * @return boolean , the comparison result.
     */
    public boolean compare(String value){
        if ( !isValid || value == null ){
            return false;
        }

        boolean result = false;

        String[] vals = this.getValues();
        int size = vals.length;
        for ( int i=0; i<size; i++ ){
            String val = vals[i];
            if ( ContainerFilterBean.COMP_EQUAL.equals(this.getComp()) ){
            if ( value.equals(val) ){
                return true;
            }
            } else if ( ContainerFilterBean.COMP_NOT_EQUAL.equals(this.getComp())) {
                if ( !value.equals(val) ){
                    return true;
                }
            } else if ( ContainerFilterBean.COMP_ISNULL.equals(this.getComp())) {
                if ( value == null || "".equals(value.trim()) ){
                    return true;
                }
            } else if ( ContainerFilterBean.COMP_NOTNULL.equals(this.getComp())) {
                if ( value != null && !"".equals(value.trim()) ){
                    return true;
                }
            } else if ( ContainerFilterBean.COMP_STARTS_WITH.equals(this.getComp())) {
                if ( val.toLowerCase().startsWith(value.toLowerCase())){
                    return true;
                }                
            }
        }
        return result;
    }

	//--------------------------------------------------------------------------
    private boolean compareNullOrNotNull(String value)
    {
      if (!isValid)
      {
        return false;
      }
  
      if (ContainerFilterBean.COMP_ISNULL.equals(this.getComp()))
      {
        return value == null || value.trim().length() == 0;
      }
      else if (ContainerFilterBean.COMP_NOTNULL.equals(this.getComp()))
      {
        return value != null && value.trim().length() > 0;
      }
      
      return false;
    }
    
	/**
	 * Return true if the given long valueA (left) match the given valueB (right)
	 *
	 * @param valueA
	 * @param valueB
	 * @param comp the comparator
	 * @return boolean the comparison result.
	 */
	private boolean compare(String valueA, String valueB, String comp, String format){
		boolean result = false;
		int compResult = 0;
		if ( comp.equals(ContainerFilterBean.COMP_EQUAL) ){
			result = ( NumberFormats.compareNumber(valueA,valueB, format) == 0 );
		} else if ( comp.equals(ContainerFilterBean.COMP_SMALLER) ){
			result = ( NumberFormats.compareNumber(valueA,valueB, format) == -1 );
		} else if ( comp.equals(ContainerFilterBean.COMP_SMALLER_OR_EQUAL) ){
            compResult = NumberFormats.compareNumber(valueA,valueB, format);
			result = ( compResult == 0 || compResult == -1 );
		} else if ( comp.equals(ContainerFilterBean.COMP_BIGGER_OR_EQUAL) ){
            compResult = NumberFormats.compareNumber(valueA,valueB, format);
            result = ( compResult == 0 || compResult == 1 );
		} else if ( comp.equals(ContainerFilterBean.COMP_BIGGER) ){
            compResult = NumberFormats.compareNumber(valueA,valueB, format);
            result = ( compResult == 1 );
                } else if ( comp.equals(ContainerFilterBean.COMP_NOT_EQUAL) ){
            if ( valueA == null && valueB == null ){
                return true;
            } else {
                try {
			        result = valueA.equals(valueB) ;
                } catch ( Exception t ){
                }
            }
		}
        //JahiaConsole.println(CLASS_NAME+".compareNumber","valueA["+ valueA +"] " + comp + " valueB[" + valueB + "] result=" + result);

		return result;
	}
    
    /**
     * Return <code>true</code> if this clause is COMP_NOTNULL or COMP_ISNULL clause.
     * @return <code>true</code> if this clause is COMP_NOTNULL or COMP_ISNULL clause
     */
    public boolean isNullOrNotNullClause()
    {
      return ContainerFilterBean.COMP_ISNULL.equals(getComp())
        || ContainerFilterBean.COMP_NOTNULL.equals(getComp());
    }

    public static String getInvertedComp(String comp, boolean invert){
        if (!invert){
            return comp;
        }
        if (comp.equals(ContainerFilterBean.COMP_BIGGER)){
            return ContainerFilterBean.COMP_SMALLER_OR_EQUAL;
        } else if (comp.equals(ContainerFilterBean.COMP_BIGGER_OR_EQUAL)){
            return ContainerFilterBean.COMP_SMALLER;
        } else if (comp.equals(ContainerFilterBean.COMP_SMALLER)){
            return ContainerFilterBean.COMP_BIGGER_OR_EQUAL;
        } else if (comp.equals(ContainerFilterBean.COMP_SMALLER_OR_EQUAL)){
            return ContainerFilterBean.COMP_BIGGER;
        } else if (comp.equals(ContainerFilterBean.COMP_EQUAL)){
            return ContainerFilterBean.COMP_NOT_EQUAL;
        }
        return comp;
    }

}
