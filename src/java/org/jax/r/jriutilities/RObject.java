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

import java.util.Iterator;
import java.util.List;

import org.jax.r.RUtilities;
import org.jax.util.ObjectUtil;


/**
 * This type provides some basic support for java objects that
 * are backed by R objects.
 * @author <A HREF="mailto:keith.sheppard@jax.org">Keith Sheppard</A>
 */
public class RObject
{
    /**
     * @see #getRInterface()
     */
    private final RInterface rInterface;
    
    /**
     * @see #getAccessorExpressionString()
     */
    private final String accessorExpressionString;

    /**
     * Constructor
     * @param rInterface
     *          see {@link #getRInterface()}
     * @param accessorExpressionString
     *          see {@link #getAccessorExpressionString()}
     */
    public RObject(RInterface rInterface, String accessorExpressionString)
    {
        this.rInterface = rInterface;
        this.accessorExpressionString = accessorExpressionString;
    }
    
    /**
     * This is the expression that you'd type in the R terminal in order
     * to get a handle on the backing R object
     * @return
     *          the accessor string
     */
    public String getAccessorExpressionString()
    {
        return this.accessorExpressionString;
    }
    
    /**
     * The R interface that holds this object.
     * @return
     *          the R interface
     */
    public RInterface getRInterface()
    {
        return this.rInterface;
    }
    
    /**
     * Filter out any objects from the given list that aren't owned by this
     * object.
     * @param rObjects
     *          the list that we're going to filter
     */
    protected void removeObjectsNotOwnedByThis(List<RObject> rObjects)
    {
        String ownershipPrefix =
            this.getAccessorExpressionString() + ".";
        Iterator<RObject> rObjectsIter = rObjects.iterator();
        while(rObjectsIter.hasNext())
        {
            RObject currRObject = rObjectsIter.next();
            if(!currRObject.getAccessorExpressionString().startsWith(ownershipPrefix))
            {
                rObjectsIter.remove();
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(Object obj)
    {
        if(obj instanceof RObject)
        {
            RObject otherRObject = (RObject)obj;
            return ObjectUtil.areEqual(
                    this.accessorExpressionString,
                    otherRObject.accessorExpressionString);
        }
        else
        {
            return false;
        }
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode()
    {
        return ObjectUtil.hashObject(
                this.accessorExpressionString);
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public String toString()
    {
        // get to the last 'dot'
        String accessorExpressionString = this.accessorExpressionString;
        int lastDotIndex = accessorExpressionString.lastIndexOf('.');
        if(lastDotIndex == -1)
        {
            return RUtilities.fromRIdentifierToReadableName(
                    accessorExpressionString);
        }
        else
        {
            return RUtilities.fromRIdentifierToReadableName(
                    accessorExpressionString.substring(lastDotIndex + 1));
        }
    }
}
