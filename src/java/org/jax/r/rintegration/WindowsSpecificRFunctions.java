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
 * Implementation that knows where to check for the R Home on Windows
 * @author <A HREF="mailto:keith.sheppard@jax.org">Keith Sheppard</A>
 */
public class WindowsSpecificRFunctions extends AbstractPlatformSpecificRFunctions
{
    /**
     * our logger
     */
    private static final Logger LOG = Logger.getLogger(
            WindowsSpecificRFunctions.class.getName());
    
    // TODO these should be in a config or properties file
    /**
     * where we expect to find the R versions
     */
    private static final String DEFAULT_INSTALL_ROOT = "C:\\Program Files\\R";

    /**
     * the env variable windows uses to look up DLL's
     */
    private static final String LD_LIB_PATH_ENV_VAR_NAME = "PATH";
    
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
        return new File(new File(home, "bin"), "i386");
    }

    /**
     * {@inheritDoc}
     */
    public File versionRootToExpectedRHome(File versionRoot)
    {
        // the version root and R_HOME are the same thing in windows
        return versionRoot;
    }

    /**
     * {@inheritDoc}
     */
    public boolean updateSettingsForRInstallation(
            VirtualMachineSettings settingsToUpdate, RInstallation installation)
    {
        settingsToUpdate.prependToEnvironmentVariableCaseInsensitive(
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
            
            // TODO we should eventually be using java.library.path for
            //      this instead of %PATH%. The VM launcher should be the one
            //      that knows not to but it in the java.library.path
            //      for windows.
            //      The whole reason that we're doing this is that
            //      the CommandLineVirtualMachine launcher chokes on the
            //      java.library.path that comes out of web-start
            settingsToUpdate.setJavaLibraryPath(null);
            settingsToUpdate.prependToEnvironmentVariableCaseInsensitive(
                    LD_LIB_PATH_ENV_VAR_NAME,
                    matchingNativeDir.getAbsolutePath());
            settingsToUpdate.getEnvironment().put(
                    RInstallation.R_HOME_ENV_KEY,
                    installation.getRHomeDirectory().getAbsolutePath());
            
            return true;
        }

    }
}
