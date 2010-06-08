/*
 * Copyright (c) 2009 The Jackson Laboratory
 * 
 * This software was developed by Gary Churchill's Lab at The Jackson
 * Laboratory (see http://research.jax.org/faculty/churchill).
 *
 * This is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this software.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.jax.r;

import java.util.List;

/**
 * A collection of static R utility functions
 * @author <A HREF="mailto:keith.sheppard@jax.org">Keith Sheppard</A>
 */
public class RUtilities
{
    private static final String R_VECTOR_START = "c(";
    private static final String R_VECTOR_END = ")";
    private static final String R_VECTOR_ELEMENT_SEPERATOR = ", ";
    private static final String R_STRING_QUOTES = "\"";
    private static final String R_TRUE = "TRUE";
    private static final String R_FALSE = "FALSE";
    
    private static final String[] RESERVED_R_IDENTIFIERS = new String [] {
        "if",
        "else",
        "if",
        "else",
        "repeat",
        "while",
        "function",
        "for",
        "in",
        "next",
        "break",
        "TRUE",
        "FALSE",
        "NULL",
        "Inf",
        "NaN",
        "NA",
        "NA_integer_",
        "NA_real_",
        "NA_complex_",
        "NA_character_"};

    private static final char BACKTICK = '`';
    
    /**
     * private constructor (all our functions are static)
     */
    private RUtilities()
    {
    }
    
    /**
     * Get the names expression for the given inner expression
     * @param rInnerExpression
     *          the inner expression that we're getting names for
     * @return
     *          the names
     */
    public static String rNamesExpression(String rInnerExpression)
    {
        return "names(" + rInnerExpression + ")";
    }
    
    /**
     * Create an index expression given the R expression and index
     * @param rExpressionToIndex
     *          the R expression
     * @param zeroBasedIndex
     *          the index
     * @return
     *          the index expression
     */
    public static String indexExpression(
            String rExpressionToIndex,
            int zeroBasedIndex)
    {
        // R wants 1 based indexes
        return rExpressionToIndex + "[" + (zeroBasedIndex + 1) + "]";
    }
    
    /**
     * Create an index expression given the R expression and index
     * @param rExpressionToIndex
     *          the R expression
     * @param zeroBasedColumnIndex
     *          the index
     * @return
     *          the index expression
     */
    public static String columnIndexExpression(
            String rExpressionToIndex,
            int zeroBasedColumnIndex)
    {
        // R wants 1 based indexes
        return rExpressionToIndex + "[," + (zeroBasedColumnIndex + 1) + "]";
    }
    
    /**
     * Create an index expression given the R expression and index
     * @param rExpressionToIndex
     *          the R expression
     * @param zeroBasedRowIndex
     *          the index
     * @return
     *          the index expression
     */
    public static String rowIndexExpression(
            String rExpressionToIndex,
            int zeroBasedRowIndex)
    {
        // R wants 1 based indexes
        return rExpressionToIndex + "[" + (zeroBasedRowIndex + 1) + ",]";
    }
    
    /**
     * Convert the given double array into an R vector string
     * @param doubleArray
     *          the double array to convert
     * @return
     *          the R value string
     */
    public static String doubleArrayToRVector(double[] doubleArray)
    {
        String[] rValueArray = new String[doubleArray.length];
        
        for(int i = 0; i < doubleArray.length; i++)
        {
            rValueArray[i] = RUtilities.javaDoubleToRDouble(doubleArray[i]);
        }
        
        return RUtilities.objectArrayToRVector(rValueArray);
    }
    
    /**
     * Convert the given java double to an R double string
     * @param javaDouble
     *          the java double to convert
     * @return
     *          the R string value
     */
    public static String javaDoubleToRDouble(double javaDouble)
    {
        if(javaDouble == Double.POSITIVE_INFINITY)
        {
            return "Inf";
        }
        else
        {
            return Double.toString(javaDouble);
        }
    }

    /**
     * Convert the given integer array into an R vector string
     * @param intArray
     *          the int array to convert
     * @return
     *          the R value string
     */
    public static String intArrayToRVector(int[] intArray)
    {
        String[] rValueArray = new String[intArray.length];
        
        for(int i = 0; i < intArray.length; i++)
        {
            rValueArray[i] =
                RUtilities.javaIntToRInt(intArray[i]);
        }
        
        return RUtilities.objectArrayToRVector(rValueArray);
    }

    /**
     * Convert the given java integer into an R value string
     * @param javaInt
     *          the java integer
     * @return
     *          the R value string for the given java integer
     */
    public static String javaIntToRInt(int javaInt)
    {
        return Integer.toString(javaInt);
    }
    
    /**
     * Convert a java string list to it's R string vector representation
     * @param stringList
     *          the string list
     * @return
     *          the vector
     */
    public static String stringListToRVector(List<String> stringList)
    {
        return stringArrayToRVector(
                stringList.toArray(new String[stringList.size()]));
    }

    /**
     * Convert the given string array into an r vector string
     * @param stringArray
     *          the java string
     * @return
     *          the string for an R vector of strings
     */
    public static String stringArrayToRVector(String[] stringArray)
    {
        String[] rStringArray = new String[stringArray.length];
        
        for(int i = 0; i < stringArray.length; i++)
        {
            rStringArray[i] =
                RUtilities.javaStringToRString(stringArray[i]);
        }
        
        return RUtilities.objectArrayToRVector(rStringArray);
    }

    /**
     * Convert the given raw java string into an R string by adding quotes
     * @param javaString
     *          the raw java string
     * @return
     *          the R quoted string
     */
    public static String javaStringToRString(String javaString)
    {
        // we need to quote the string for r
        return R_STRING_QUOTES +
               javaString.replace("\\", "\\\\") +
               R_STRING_QUOTES;
    }

    /**
     * Convert the given boolean array to an R vector string
     * @param booleanArray
     *          the boolean array to convert
     * @return
     *          the R vector string
     */
    public static String booleanArrayToRVector(boolean[] booleanArray)
    {
        String[] rValueArray = new String[booleanArray.length];
        
        for(int i = 0; i < booleanArray.length; i++)
        {
            rValueArray[i] =
                RUtilities.javaBooleanToRBoolean(booleanArray[i]);
        }
        
        return RUtilities.objectArrayToRVector(rValueArray);
    }

    /**
     * Convert the given java boolean type into an R value string
     * @param javaBoolean
     *          the java boolean to convert
     * @return
     *          the R value string
     */
    public static String javaBooleanToRBoolean(boolean javaBoolean)
    {
        return javaBoolean ? R_TRUE : R_FALSE;
    }

    /**
     * Convert the given values to an R vector. Note that the given values
     * are not quoted. It is assumed that if they are already in "R" form and
     * so the {@link Object#toString()} value is used directly
     * @param rValues
     *          the values whose {@link Object#toString()} values
     *          we will use to form the list
     * @return
     *          the string that can be used as an R vector
     */
    public static String objectArrayToRVector(Object[] rValues)
    {
        StringBuffer sb = new StringBuffer(R_VECTOR_START);
        
        if(rValues.length > 0)
        {
            // treat the 1st one as a special case (no comma)
            sb.append(rValues[0].toString());
            
            // all the rest have commas
            for(int i = 1; i < rValues.length; i++)
            {
                sb.append(R_VECTOR_ELEMENT_SEPERATOR);
                sb.append(rValues[i].toString());
            }
        }
        
        sb.append(R_VECTOR_END);
        
        return sb.toString();
    }
    
    /**
     * Convert from an R identifier to a "readable string". All we're doing
     * in this implementation is replacing underscores with spaces.
     * @see #fromReadableNameToRIdentifier(String)
     * @param rIdentifier
     *          the identifier that we're getting a "readable" name for
     * @return
     *          the "readable" name
     */
    public static String fromRIdentifierToReadableName(String rIdentifier)
    {
        return rIdentifier.replace('_', ' ');
    }
    
    /**
     * Get an error message that results from converting the given
     * readable name to an R identifier.
     * @param readableName
     *          the name to check
     * @return
     *          an error message suitable for presenting to the user or null
     *          if there is nothing wrong with the given readableName
     */
    public static String getErrorMessageForReadableName(String readableName)
    {
        // use the message in the exception (if we get one)
        try
        {
            fromReadableNameToRIdentifier(readableName);
            return null;
        }
        catch(RSyntaxException ex)
        {
            return ex.getMessage();
        }
    }
    
    /**
     * Convert the given name from a readable name to an R identifier. The
     * only conversion that this method currently does is to go from spaces
     * to underscores. Other than that the given name must be a valid R
     * identifier in every other way or an {@link RSyntaxException} is
     * thrown.
     * @param readableName
     *          the readable name
     * @return
     *          the R identifier for the readable name
     * @throws RSyntaxException
     *          if we can't convert the given readable name to an identifier
     */
    public static String fromReadableNameToRIdentifier(String readableName)
    throws RSyntaxException
    {
        StringBuffer rIdentifierBuffer = new StringBuffer(readableName.length());
        
        if(readableName.length() > 0)
        {
            rIdentifierBuffer.append(fromReadableHeadCharToRIdentifier(
                    readableName.charAt(0)));
            for(int i = 1; i < readableName.length(); i++)
            {
                rIdentifierBuffer.append(fromReadableTailCharToRIdentifier(
                        readableName.charAt(i)));
            }
        }
        
        String rIdentifier = rIdentifierBuffer.toString();
        
        if(isAReservedRIdentifier(rIdentifier))
        {
            throw new RSyntaxException(
                    "The name \"" + readableName + "\" clashes with a reserved " +
                    "R identifier. Please change it.");
        }
        else
        {
            return rIdentifier;
        }
    }
    
    /**
     * Determine if the given string is a reserved R identifier
     * @param identifierToCheck
     *          the identifier we're checking
     * @return
     *          true iff its reserved
     */
    private static boolean isAReservedRIdentifier(String identifierToCheck)
    {
        for(String currReservedIdentifier: RESERVED_R_IDENTIFIERS)
        {
            if(identifierToCheck.equals(currReservedIdentifier))
            {
                return true;
            }
        }
        
        return false;
    }

    /**
     * Convert a readable head char into an R identifier
     * @param readableHeadChar
     *          the readable head character
     * @return
     *          the legal identifier char
     * @throws RSyntaxException
     *          if we can't make the conversion to an R character
     */
    private static char fromReadableHeadCharToRIdentifier(char readableHeadChar)
    throws RSyntaxException
    {
        char headIdentifierChar =
            fromReadableTailCharToRIdentifier(readableHeadChar);
        if(headIdentifierChar == '_' ||
           (headIdentifierChar >= '0' && headIdentifierChar <= '9'))
        {
            throw new RSyntaxException(
                    "Name cannot start with \'" + readableHeadChar + "\'. " +
                    "Legal starting characters are \'a\'-\'z\' and \'A\'-\'Z\'");
        }
        else
        {
            return headIdentifierChar;
        }
    }

    /**
     * Convert from an identifiers tail character (anything but the starting
     * character) to its "readable" counterpart
     * @param readableTailChar
     *          the readable character
     * @return
     *          the readable character
     * @throws RSyntaxException
     *          if we cannot make the conversion for the given character
     */
    private static char fromReadableTailCharToRIdentifier(char readableTailChar)
    throws RSyntaxException
    {
        if(readableTailChar == '_')
        {
            return readableTailChar;
        }
        else
        {
            if(readableTailChar == ' ')
            {
                return '_';
            }
            else
            {
                if(readableTailChar >= '0' && readableTailChar <= '9')
                {
                    return readableTailChar;
                }
                else
                {
                    char upperReadableTailChar = Character.toUpperCase(readableTailChar);
                    if(upperReadableTailChar >= 'A' && upperReadableTailChar <= 'Z')
                    {
                        return readableTailChar;
                    }
                    else
                    {
                        throw new RSyntaxException(
                                "Name cannot contain \'" + readableTailChar +
                                "\'. Legal values are \'_\', \'a\'-\'z\', " +
                                "\'A\'-\'Z\', \'0\'-\'9\' and spaces.");
                    }
                }
            }
        }
    }
    
    /**
     * Quote the given identifier if it is needed
     * @param identifier
     *          the identifier that we might quote
     * @return
     *          the R identifier for the readable name
     * @throws RSyntaxException
     *          if we can't convert the given readable name to an identifier
     */
    public static String quoteIdentifierIfRequired(String identifier)
    throws RSyntaxException
    {
        int nameLen = identifier.length();
        if(nameLen == 0)
        {
            throw new RSyntaxException(
                    "identifier name cannot be empty");
        }
        else
        {
            boolean quotingRequired = false;
            backtickCheck(identifier.charAt(0));
            if(!isLeagalIdentifierStartChar(identifier.charAt(0)))
            {
                quotingRequired = true;
            }
            else
            {
                for(int i = 1; i < nameLen; i++)
                {
                    char currChar = identifier.charAt(i);
                    backtickCheck(currChar);
                    if(!isLeagalIdentifierChar(currChar))
                    {
                        quotingRequired = true;
                        break;
                    }
                }
            }
            
            if(quotingRequired || isAReservedRIdentifier(identifier))
            {
                // backticks can make almost any invalid identifier a valid
                // identifier
                return BACKTICK + identifier + BACKTICK;
            }
            else
            {
                return identifier;
            }
        }
    }
    
    /**
     * Throws an exception if the given character is a backtick
     * @param character
     *          the character
     * @throws RSyntaxException
     *          the exception (if its a backtick)
     */
    private static void backtickCheck(char character) throws RSyntaxException
    {
        if(character == BACKTICK)
        {
            throw new RSyntaxException(
                    "Backticks '`' are not permitted in identifier names");
        }
    }
    
    /**
     * return true iff the given character is legal in R even when unquoted
     * @param startChar
     *          the character to test
     * @return
     *          true if it's legal when unquoted
     */
    private static boolean isLeagalIdentifierStartChar(char startChar)
    {
        return (startChar >= 'a' && startChar <= 'z') ||
               (startChar >= 'A' && startChar <= 'Z');
    }
    
    /**
     * Like {@link #isLeagalIdentifierStartChar(char)} but for the internal
     * characters
     * @param character
     *          the characters
     * @return
     *          true or false
     */
    private static boolean isLeagalIdentifierChar(char character)
    {
        return isLeagalIdentifierStartChar(character) ||
               character == '_' ||
               character == '.' ||
               (character >= '0' && character <= '9');
    }
}
