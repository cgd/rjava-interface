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

package org.jax.r.rintegration;

import java.io.File;

import org.jax.util.ObjectUtil;

/**
 * Holds info for an R install directory
 * @author <A HREF="mailto:keith.sheppard@jax.org">Keith Sheppard</A>
 */
public class RInstallation implements Comparable<RInstallation>
{
    /**
     * the name of the R home env variable
     */
    public static final String R_HOME_ENV_KEY = "R_HOME";

    /**
     * @see #getRHomeDirectory()
     */
    private final File rHomeDirectory;
    
    /**
     * @see #getRVersion()
     */
    private final String rVersion;

    /**
     * @see #getLibraryDirectory()
     */
    private final File libraryDirectory;
    
    /**
     * Constructor
     * @param rHomeDirectory
     *          see {@link #getRHomeDirectory()}
     * @param libraryDirectory
     *          see {@link #getLibraryDirectory()}
     * @param rVersion
     *          see {@link #getRVersion()}
     */
    public RInstallation(
            File rHomeDirectory,
            File libraryDirectory,
            String rVersion)
    {
        this.rHomeDirectory = rHomeDirectory;
        this.libraryDirectory = libraryDirectory;
        this.rVersion = rVersion;
    }

    /**
     * Get the directory containing the dynamic libraries that we need.
     * @return the lib dir
     */
    public File getLibraryDirectory()
    {
        return this.libraryDirectory;
    }

    /**
     * Getter for R_HOME
     * @return the rHomeDirectory
     */
    public File getRHomeDirectory()
    {
        return this.rHomeDirectory;
    }

    /**
     * Getter for the version that this home corresponds to
     * @return the rVersion
     */
    public String getRVersion()
    {
        return this.rVersion;
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(Object otherObject)
    {
        if(otherObject != null && otherObject instanceof RInstallation)
        {
            RInstallation otherRInst = (RInstallation)otherObject;
            return
                ObjectUtil.areEqual(this.libraryDirectory, otherRInst.libraryDirectory) &&
                ObjectUtil.areEqual(this.rHomeDirectory, otherRInst.rHomeDirectory) &&
                ObjectUtil.areEqual(this.rVersion, otherRInst.rVersion);
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
        return ObjectUtil.hashObject(this.rHomeDirectory);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString()
    {
        return
            "R Home Dir: \"" + this.rHomeDirectory.getName() +
            "\" R Home Lib Dir: \"" + this.libraryDirectory.getName() +
            "\" R Home version: \"" + this.rVersion + "\"";
    }

    /**
     * {@inheritDoc}
     */
    public int compareTo(RInstallation otherRInstallation)
    {
        // sort 1st on version
        if(!ObjectUtil.areEqual(this.rVersion, otherRInstallation.rVersion))
        {
            if(this.rVersion == null)
            {
                return -1;
            }
            else if(otherRInstallation.rVersion == null)
            {
                return 1;
            }
            else
            {
                return VersionStringComparator.getInstance().compare(
                        this.rVersion,
                        otherRInstallation.rVersion);
            }
        }
        else if(!ObjectUtil.areEqual(this.libraryDirectory, otherRInstallation.libraryDirectory))
        {
            if(this.libraryDirectory == null)
            {
                return -1;
            }
            else if(otherRInstallation.libraryDirectory == null)
            {
                return 1;
            }
            else
            {
                return this.libraryDirectory.compareTo(
                        otherRInstallation.libraryDirectory);
            }
        }
        else if(!ObjectUtil.areEqual(this.rHomeDirectory, otherRInstallation.rHomeDirectory))
        {
            if(this.rHomeDirectory == null)
            {
                return -1;
            }
            else if(otherRInstallation.rHomeDirectory == null)
            {
                return 1;
            }
            else
            {
                return this.rHomeDirectory.compareTo(
                        otherRInstallation.rHomeDirectory);
            }
        }
        else
        {
            // these are 100% equal
            return 0;
        }
    }
}
