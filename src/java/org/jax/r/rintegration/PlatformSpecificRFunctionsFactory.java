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

import java.util.logging.Level;
import java.util.logging.Logger;

import org.jax.util.TypeSafeSystemProperties;
import org.jax.util.TypeSafeSystemProperties.OsFamily;

/**
 * A factory which generates a {@link PlatformSpecificRFunctions} for the current
 * environment.
 * @author <A HREF="mailto:keith.sheppard@jax.org">Keith Sheppard</A>
 */
public final class PlatformSpecificRFunctionsFactory
{
    /**
     * our logger
     */
    private static final Logger LOG = Logger.getLogger(
            PlatformSpecificRFunctionsFactory.class.getName());
    
    /**
     * the instance accessible from {@link #getInstance()} 
     */
    private static final PlatformSpecificRFunctionsFactory instance =
        new PlatformSpecificRFunctionsFactory();
    
    /**
     * returns a reference to the singleton instance of
     * {@link PlatformSpecificRFunctionsFactory}
     * @return
     *          the singleton
     */
    public static PlatformSpecificRFunctionsFactory getInstance()
    {
        return PlatformSpecificRFunctionsFactory.instance;
    }
    
    /**
     * @see #getPlatformSpecificRFunctions()
     */
    private final PlatformSpecificRFunctions rInstallDirStructure;
    
    /**
     * private constructor. use {@link #getInstance()} to get a hold of
     * the singleton instance of this class
     */
    private PlatformSpecificRFunctionsFactory()
    {
        // create a temp variable so that we don't violate the java final rules
        PlatformSpecificRFunctions rInstallDirStructureTemp = null;
        
        try
        {
            OsFamily osFamily = TypeSafeSystemProperties.getOsFamily();
            if(osFamily == null)
            {
                LOG.warning("could not determine OS family");
            }
            else
            {
                switch(osFamily)
                {
                    // TODO add dir structure for linux family
                    case WINDOWS_OS_FAMILY:
                    {
                        rInstallDirStructureTemp =
                            new WindowsSpecificRFunctions();
                    }
                    break;
                    
                    case MAC_OS_FAMILY:
                    {
                        rInstallDirStructureTemp =
                            new MacSpecificRFunctions();
                    }
                    break;

                    default:
                    {
                        LOG.warning(
                                "I don't know what the R directory structure " +
                                "should look like for the detected operating " +
                                "system family: " + osFamily);
                    }
                    break;
                }
            }
        }
        catch(Exception ex)
        {
            LOG.log(Level.SEVERE,
                    "failed to detect the R install dir structure due to " +
                    "an unexpected exception",
                    ex);
        }
        finally
        {
            this.rInstallDirStructure = rInstallDirStructureTemp;
        }
    }
    
    /**
     * Getter for the {@link PlatformSpecificRFunctions} for the platform that
     * we're running in.
     * @return
     *          the instance
     */
    public PlatformSpecificRFunctions getPlatformSpecificRFunctions()
    {
        return this.rInstallDirStructure;
    }
}
