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
import java.util.logging.Level;
import java.util.logging.Logger;

import org.jax.util.TypeSafeSystemProperties;
import org.jax.virtualmachine.VirtualMachineSettings;

/**
 * Implementation that knows where to check for the R Home on Mac
 * @author <A HREF="mailto:keith.sheppard@jax.org">Keith Sheppard</A>
 */
public class MacSpecificRFunctions extends AbstractPlatformSpecificRFunctions
{
    /**
     * our logger
     */
    private static final Logger LOG = Logger.getLogger(
            MacSpecificRFunctions.class.getName());
    
    /**
     * the mac environment variable for dynamically loaded library paths
     */
    private static final String LD_LIB_PATH_ENV_VAR_NAME =
        "DYLD_LIBRARY_PATH";
    
    // TODO these should be in a config or properties file
    /**
     * where we expect to find the R versions
     */
    private static final String DEFAULT_INSTALL_ROOT = "/Library/Frameworks/R.framework/Versions";

    /**
     * the default path to the R dynamically loadable library relative to the R_HOME
     */
    private static final String DEFAULT_R_LIBRARY_RELATIVE_PATH = "lib";
    
    /**
     * the R_HOME relative path
     */
    private static final String DEFAULT_R_HOME_RELATIVE_PATH = "Resources";
    
    /**
     * {@inheritDoc}
     */
    public File getExpectedInstallRoot()
    {
        return new File(DEFAULT_INSTALL_ROOT);
    }

    /**
     * {@inheritDoc}
     */
    public File rHomeToExpectedRLibrary(File home)
    {
        return new File(home, DEFAULT_R_LIBRARY_RELATIVE_PATH);
    }

    /**
     * {@inheritDoc}
     */
    public File versionRootToExpectedRHome(File versionRoot)
    {
        // the version root and R_HOME are the same thing in windows
        return new File(versionRoot, DEFAULT_R_HOME_RELATIVE_PATH);
    }

    /**
     * {@inheritDoc}
     */
    public boolean updateSettingsForRInstallation(
            VirtualMachineSettings settingsToUpdate,
            RInstallation installation)
    {
        settingsToUpdate.prependToEnvironmentVariable(
                LD_LIB_PATH_ENV_VAR_NAME,
                installation.getLibraryDirectory().getAbsolutePath());
        
        File matchingNativeDir = this.getMatchingJriNativeDir(
                installation);
        if(matchingNativeDir == null)
        {
            LOG.severe(
                    "could not find matching JRI natives directory " +
                    "for R version " + installation.getRVersion());
            return false;
        }
        else
        {
            if(LOG.isLoggable(Level.FINE))
            {
                LOG.fine(
                        "adding directory \"" +
                        matchingNativeDir.getAbsolutePath() +
                        "\" to " +
                        TypeSafeSystemProperties.JAVA_HOME_PROP_NAME);
                LOG.fine(
                        "setting environment var: " +
                        RInstallation.R_HOME_ENV_KEY + " to " +
                        installation.getRHomeDirectory().getAbsolutePath());
            }
            settingsToUpdate.prependToJavaLibraryPath(
                    matchingNativeDir.getAbsolutePath());
            settingsToUpdate.getEnvironment().put(
                    RInstallation.R_HOME_ENV_KEY,
                    installation.getRHomeDirectory().getAbsolutePath());
            
            return true;
        }
    }
}
