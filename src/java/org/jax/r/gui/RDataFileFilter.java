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

package org.jax.r.gui;

import org.jax.util.io.FileChooserExtensionFilter;


/**
 * File filter for *.R data objects
 * @author <A HREF="mailto:keith.sheppard@jax.org">Keith Sheppard</A>
 */
public class RDataFileFilter extends FileChooserExtensionFilter
{
    /**
     * the extension string
     */
    public static final String R_DATA_EXTENSION = "RData";
    
    /**
     * the instance
     */
    private static final RDataFileFilter instance = new RDataFileFilter();
    
    /**
     * Constructor
     */
    private RDataFileFilter()
    {
        super(R_DATA_EXTENSION, "R Data (*.RData)");
    }
    
    /**
     * Getter for the singleton instance
     * @return
     *          the singleton filter instance
     */
    public static RDataFileFilter getInstance()
    {
        return RDataFileFilter.instance;
    }
}
