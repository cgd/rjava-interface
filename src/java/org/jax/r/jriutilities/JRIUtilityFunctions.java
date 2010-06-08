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

package org.jax.r.jriutilities;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.jax.r.RException;
import org.jax.r.RUtilities;
import org.rosuda.JRI.REXP;
import org.rosuda.JRI.RFactor;

/**
 * Some commonly used utility functions
 * @author <A HREF="mailto:keith.sheppard@jax.org">Keith Sheppard</A>
 */
// TODO do some meging/cleanup with RUtilities
public class JRIUtilityFunctions
{
    /**
     * The command for listing objects.
     */
    private static final String IDENTIFIERS_LIST_COMMAND = "ls()";
    
    /**
     * Determine if the given {@link RObject} counts as a top level object
     * @param rObject
     *          the object to test
     * @return
     *          true iff its a top level object in R
     */
    public static boolean isTopLevelObject(RObject rObject)
    {
        return JRIUtilityFunctions.isTopLevelObject(
                rObject.getAccessorExpressionString(),
                rObject.getRInterface());
    }
    
    /**
     * Similar to {@link #isTopLevelObject(RObject)}
     * @param accessorString
     *          the accessor string to test
     * @param rInterface
     *          the R interface to use
     * @return
     *          true if the object exists in the current environment
     */
    public static boolean isTopLevelObject(
            String accessorString,
            RInterface rInterface)
    {
        String existsExpressionString =
            "exists(\"" + accessorString + "\")";
        REXP existsExpression = rInterface.evaluateCommand(
                new SilentRCommand(existsExpressionString));
        return existsExpression.asBool().isTRUE();
    }
    
    /**
     * Get a list of all R identifiers in scope.
     * @param rInterface
     *          the r interface to use
     * @return
     *          the available identifiers
     * @throws RException
     *          the r exception
     */
    public static List<RObject> getTopLevelObjects(RInterface rInterface)
            throws RException
    {
        // get a listing of all identifiers
        REXP idsExpression = rInterface.evaluateCommand(new SilentRCommand(
                IDENTIFIERS_LIST_COMMAND));
        String[] ids = idsExpression.asStringArray();
        ArrayList<RObject> rObjects = new ArrayList<RObject>(ids.length);
        for(String currId: ids)
        {
            rObjects.add(new RObject(rInterface, currId));
        }
        
        return rObjects;
    }
    
    /**
     * Get all identifiers in scope with the given type
     * @param rInterface
     *          the R interface to use
     * @param type
     *          the type to look for
     * @return
     *          a list of matches
     * @throws RException
     *          if we run into problems with the R interface
     */
    public static List<RObject> getTopLevelObjectsOfType(
            RInterface rInterface,
            String type)
            throws RException
    {
        // 1st get a list of all the id's... then filter by type
        List<RObject> topLevelRObjects = JRIUtilityFunctions.getTopLevelObjects(
                rInterface);
        Iterator<RObject> objIter = topLevelRObjects.iterator();
        while(objIter.hasNext())
        {
            RObject currRObj = objIter.next();
            
            // filter this identifier out of the list if it isn't the right
            // type
            if(!JRIUtilityFunctions.inheritsRClass(currRObj, type))
            {
                objIter.remove();
            }
        }
        
        return topLevelRObjects;
    }
    
    /**
     * Return the result of "names(...)" on the given object.
     * @param rObject
     *          the object to run names on
     * @return
     *          the names
     */
    public static String[] getNames(RObject rObject)
    {
        String namesExpressionString =
            "names(" + rObject.getAccessorExpressionString() + ")";
        REXP namesResult = rObject.getRInterface().evaluateCommand(
                new SilentRCommand(namesExpressionString));
        
        if(namesResult == null)
        {
            return null;
        }
        else
        {
            return namesResult.asStringArray();
        }
    }
    
    /**
     * The R equivalent of instanceof.
     * @param rObject
     *          the R object
     * @param rClassName
     *          the R class
     * @return
     *          true iff the object inherits the given class type
     */
    public static boolean inheritsRClass(
            RObject rObject,
            String rClassName)
    {
        String inheritsClassExpressionString =
            "inherits(" + rObject.getAccessorExpressionString() + ", \"" +
            rClassName + "\")";
        REXP inheritsResult = rObject.getRInterface().evaluateCommand(
                new SilentRCommand(inheritsClassExpressionString));
        return inheritsResult.asBool().isTRUE();
    }

    /**
     * Get the column names for the given matrix object
     * @param rMatrix
     *          the matrix
     * @return
     *          the column names
     */
    public static String[] getColumnNames(
            RObject rMatrix)
    {
        String namesExpressionString =
            "colnames(" + rMatrix.getAccessorExpressionString() + ")";
        REXP namesResult = rMatrix.getRInterface().evaluateCommand(
                new SilentRCommand(namesExpressionString));
        
        if(namesResult == null)
        {
            return null;
        }
        else
        {
            return namesResult.asStringArray();
        }
    }
    
    /**
     * Get the row names for the given matrix object
     * @param rMatrix
     *          the matrix
     * @return
     *          the row names
     */
    public static String[] getRowNames(
            RObject rMatrix)
    {
        String namesExpressionString =
            "rownames(" + rMatrix.getAccessorExpressionString() + ")";
        REXP namesResult = rMatrix.getRInterface().evaluateCommand(
                new SilentRCommand(namesExpressionString));
        
        if(namesResult == null)
        {
            return null;
        }
        else
        {
            return namesResult.asStringArray();
        }
    }
    
    /**
     * Get the number of rows in the matrix
     * @param rMatrix
     *          the matrix
     * @return
     *          the number of rows
     */
    public static int getNumberOfRows(
            RObject rMatrix)
    {
        String numRowsExpression =
            "nrow(" + rMatrix.getAccessorExpressionString() + ")";
        REXP numRowsResult = rMatrix.getRInterface().evaluateCommand(
                new SilentRCommand(numRowsExpression));
        
        return numRowsResult.asInt();
    }
    
    /**
     * Get the number of columns in the matrix
     * @param rMatrix
     *          the matrix
     * @return
     *          the number of columns
     */
    public static int getNumberOfColumns(
            RObject rMatrix)
    {
        String numColsExpression =
            "ncol(" + rMatrix.getAccessorExpressionString() + ")";
        REXP numColsResult = rMatrix.getRInterface().evaluateCommand(
                new SilentRCommand(numColsExpression));
        
        return numColsResult.asInt();
    }
    
    /**
     * Get column double values from a matrix
     * @param rMatrix
     *          the matrix to get the values from
     * @param zeroBasedColumnIndex
     *          the zero based column index to use
     * @return
     *          the double values
     */
    public static double[] getColumnDoubles(
            RObject rMatrix,
            int zeroBasedColumnIndex)
    {
        String columnExpressionString = RUtilities.columnIndexExpression(
                rMatrix.getAccessorExpressionString(),
                zeroBasedColumnIndex);
        REXP columnRExpression = rMatrix.getRInterface().evaluateCommand(
                new SilentRCommand(columnExpressionString));
        
        if(columnRExpression == null)
        {
            return null;
        }
        else
        {
            return columnRExpression.asDoubleArray();
        }
    }
    
    /**
     * Get column string values from a matrix
     * @param rMatrix
     *          the matrix to get the values from
     * @param zeroBasedColumnIndex
     *          the zero based column index to use
     * @return
     *          the string values
     */
    public static String[] getColumnStrings(
            RObject rMatrix,
            int zeroBasedColumnIndex)
    {
        String columnExpressionString = RUtilities.columnIndexExpression(
                rMatrix.getAccessorExpressionString(),
                zeroBasedColumnIndex);
        REXP columnRExpression = rMatrix.getRInterface().evaluateCommand(
                new SilentRCommand(columnExpressionString));
        
        if(columnRExpression == null)
        {
            return null;
        }
        else
        {
            return columnRExpression.asStringArray();
        }
    }
    
    /**
     * Get row string values from a matrix
     * @param rMatrix
     *          the matrix to get the values from
     * @param zeroBasedRowIndex
     *          the zero based row index to use
     * @return
     *          the string values
     */
    public static String[] getRowStrings(
            RObject rMatrix,
            int zeroBasedRowIndex)
    {
        String rowExpressionString = RUtilities.rowIndexExpression(
                rMatrix.getAccessorExpressionString(),
                zeroBasedRowIndex);
        REXP rowRExpression = rMatrix.getRInterface().evaluateCommand(
                new SilentRCommand(rowExpressionString));
        
        if(rowRExpression == null)
        {
            return null;
        }
        else
        {
            return rowRExpression.asStringArray();
        }
    }
    
    /**
     * Get column factors as a string array from the given matrix
     * @param rMatrix
     *          the R matrix that we're getting the factor from
     * @param zeroBasedColumnIndex
     *          the column to get the factor from
     * @return
     *          the factors as strings
     */
    public static String[] getColumnFactors(
            RObject rMatrix,
            int zeroBasedColumnIndex)
    {
        String columnExpressionString = RUtilities.columnIndexExpression(
                rMatrix.getAccessorExpressionString(),
                zeroBasedColumnIndex);
        REXP columnRExpression = rMatrix.getRInterface().evaluateCommand(
                new SilentRCommand(columnExpressionString));
        
        if(columnRExpression == null)
        {
            return null;
        }
        else
        {
            return JRIUtilityFunctions.extractStringArrayFromFactor(
                    columnRExpression);
        }
    }
    
    /**
     * Convert the given R expression into an integer array
     * @param rExpression
     *          the expression to convert
     * @return
     *          the int array with NA's converted to nulls
     */
    public static Integer[] extractIntegerValues(REXP rExpression)
    {
        double[] expressionAsDoubles = rExpression.asDoubleArray();
        Integer[] expressionAsIntObjects = new Integer[expressionAsDoubles.length];
        for(int i = 0; i < expressionAsDoubles.length; i++)
        {
            if(Double.isNaN(expressionAsDoubles[i]))
            {
                expressionAsIntObjects[i] = null;
            }
            else
            {
                expressionAsIntObjects[i] = Integer.valueOf(
                        (int)expressionAsDoubles[i]);
            }
        }
        
        return expressionAsIntObjects;
    }
    
    /**
     * Convert the given R expression into an integer
     * @param rExpression
     *          the expression to convert
     * @return
     *          the integer (or null if it's an NA)
     */
    public static Integer extractIntegerValue(REXP rExpression)
    {
        double value = rExpression.asDouble();
        if(Double.isNaN(value))
        {
            return null;
        }
        else
        {
            return Integer.valueOf((int)value);
        }
    }
    
    /**
     * Convert the given R expression into a double array
     * @param rExpression
     *          the expression to convert
     * @return
     *          the double array with NA's converted to nulls
     */
    public static Double[] extractDoubleValues(REXP rExpression)
    {
        double[] expressionAsDoubles = rExpression.asDoubleArray();
        Double[] expressionAsDoubleObjects = new Double[expressionAsDoubles.length];
        for(int i = 0; i < expressionAsDoubles.length; i++)
        {
            if(Double.isNaN(expressionAsDoubles[i]))
            {
                expressionAsDoubleObjects[i] = null;
            }
            else
            {
                expressionAsDoubleObjects[i] = Double.valueOf(
                        expressionAsDoubles[i]);
            }
        }
        
        return expressionAsDoubleObjects;
    }
    
    /**
     * Extract a string array from the given factor expression
     * @param factorRExpression
     *          the factor expression
     * @return
     *          the string array
     * @throws IllegalArgumentException
     *          if the given R expression isn't a factor
     */
    public static String[] extractStringArrayFromFactor(REXP factorRExpression)
    throws
            IllegalArgumentException
    {
        RFactor factor = factorRExpression.asFactor();
        int factorSize = factor.size();
        String[] factorStrings = new String[factorSize];
        for(int i = 0; i < factorSize; i++)
        {
            factorStrings[i] = factor.at(i);
        }
        
        return factorStrings;
    }

    /**
     * Extract boolean values from the given r expression
     * @param rExpression
     *          the R expression that contains the boolean array
     * @return
     *          the boolean array
     */
    public static boolean[] extractBooleanValues(REXP rExpression)
    {
        int[] expressionAsInts = rExpression.asIntArray();
        boolean[] expressionAsBools = new boolean[expressionAsInts.length];
        for(int i = 0; i < expressionAsInts.length; i++)
        {
            expressionAsBools[i] = (expressionAsInts[i] == 1);
        }
        
        return expressionAsBools;
    }
    
    /**
     * Create a unique identifier that starts with the given string
     * @param rInterface
     *          the R Interface to use
     * @param startingIdentifier
     *          the starting string to use
     * @return
     *          the unique id that starts with the starting identifier
     */
    public static String createUniqueIdentifier(
            RInterface rInterface,
            String startingIdentifier)
    {
        startingIdentifier = startingIdentifier.trim();
        
        if(startingIdentifier.length() == 0)
        {
            startingIdentifier = "object";
        }
        
        String candidateIdentifier = startingIdentifier;
        RObject candidateRObject = new RObject(rInterface, candidateIdentifier);
        for(int i = 0;
            JRIUtilityFunctions.isTopLevelObject(candidateRObject);
            i++)
        {
            candidateIdentifier = startingIdentifier + i;
            candidateRObject = new RObject(rInterface, candidateIdentifier);
        }
        
        return candidateIdentifier;
    }
    
    /**
     * Find out if the R object referenced by the given ID is null
     * @param rObject
     *          the R object we're checking for null
     * @return
     *          true iff the given identifier is null
     */
    public static boolean isNull(RObject rObject)
    {
        return isNull(
                rObject.getRInterface(),
                rObject.getAccessorExpressionString());
    }
    
    /**
     * Find out if the R object referenced by the given ID is null
     * @param rInterface
     *          the R interface to use
     * @param identifier
     *          the identifier to check
     * @return
     *          true iff the given identifier is null
     */
    public static boolean isNull(
            RInterface rInterface,
            String identifier)
    {
        // we can use a null test to find out
        REXP result = rInterface.evaluateCommand(new SilentRCommand(
                "is.null(" + identifier + ')'));
        return result.asBool().isTRUE();
    }
}
