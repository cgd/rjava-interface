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

/**
 * An R command parameter.
 * @see RMethodInvocationCommand
 * @author <A HREF="mailto:keith.sheppard@jax.org">Keith Sheppard</A>
 */
public class RCommandParameter
{
    private static final String R_PARAMETER_VALUE_SEPERATOR = "=";
    
    /**
     * @see #getName()
     */
    private final String name;
    
    /**
     * @see #getValue()
     */
    private final String value;
    
    /**
     * Construct a parameter without a name
     * @param value
     *          the value of the parameter
     */
    public RCommandParameter(
            String value)
    {
        this(null, value);
    }
    
    /**
     * Construct a parameter with a name and value.
     * @param name
     *          the name of the parameter
     * @param value
     *          the value of the parameter
     */
    public RCommandParameter(
            String name,
            String value)
    {
        this.name = name;
        this.value = value;
    }
    
    /**
     * Get the name of this parameter
     * @return
     *          the name (null if the parameter is unnamed)
     */
    public String getName()
    {
        return this.name;
    }
    
    /**
     * Convert this parameter into the parameter string as it should be
     * formatted for input to R
     * @return
     *          the string
     */
    public String getParameterString()
    {
        if(this.name == null)
        {
            return this.value;
        }
        else
        {
            return
            this.name +
            R_PARAMETER_VALUE_SEPERATOR +
            this.value;
        }
    }
    
    /**
     * Get the value of this parameter
     * @return
     *          the value (should not be null)
     */
    public String getValue()
    {
        return this.value;
    }
}
