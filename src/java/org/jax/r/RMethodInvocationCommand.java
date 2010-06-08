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

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

/**
 * An R command type for invoking a method
 * @author <A HREF="mailto:keith.sheppard@jax.org">Keith Sheppard</A>
 */
public class RMethodInvocationCommand implements RCommand
{
    private static final String R_PARAMETER_START = "(";
    private static final String R_PARAMETER_END = ")";
    private static final String R_PARAMETER_SEPERATOR = ", ";
    
    private final String methodName;
    
    private final List<RCommandParameter> parameters;
    
    private final String commandText;

    /**
     * Constructor
     * @param methodName
     *          the method name
     * @param parameters
     *          the parameters
     */
    public RMethodInvocationCommand(
            String methodName,
            RCommandParameter... parameters)
    {
        this(methodName, Arrays.asList(parameters));
    }
    
    /**
     * Constructor
     * @param methodName
     *          the name of this method (must be non-null)
     * @param parameters
     *          the parameters to input for this method (must be non-null)
     */
    public RMethodInvocationCommand(
            String methodName,
            List<RCommandParameter> parameters)
    {
        if(methodName == null || parameters == null)
        {
            throw new NullPointerException(
                    "method name and parameters must " +
                    "be non-null");
        }
        
        this.methodName = methodName;
        this.parameters = parameters;
        
        StringBuffer commandTextBuffer = new StringBuffer(methodName);
        commandTextBuffer.append(R_PARAMETER_START);
        Iterator<RCommandParameter> parameterIter = parameters.iterator();
        if(parameterIter.hasNext())
        {
            // The 1st parameter gets special treatment
            RCommandParameter currParam = parameterIter.next();
            commandTextBuffer.append(currParam.getParameterString());
            
            // all of the rest of the parameters need the seperator
            while(parameterIter.hasNext())
            {
                currParam = parameterIter.next();
                commandTextBuffer.append(R_PARAMETER_SEPERATOR);
                commandTextBuffer.append(currParam.getParameterString());
            }
        }
        commandTextBuffer.append(R_PARAMETER_END);
        
        this.commandText = commandTextBuffer.toString();
    }
    
    /**
     * Getter for the name of the method that we're invoking
     * @return the methodName
     */
    public String getMethodName()
    {
        return this.methodName;
    }

    /**
     * Getter for the parameter list for the method that we're invoking
     * @return the parameters
     */
    public List<RCommandParameter> getParameters()
    {
        return this.parameters;
    }

    /**
     * {@inheritDoc}
     */
    public String getCommandText()
    {
        return this.commandText;
    }
}
