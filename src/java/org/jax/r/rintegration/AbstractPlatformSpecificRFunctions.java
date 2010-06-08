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
import java.io.IOException;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.ZipInputStream;

import org.jax.util.ConfigurationUtilities;
import org.jax.util.TypeSafeSystemProperties;
import org.jax.util.io.FileUtilities;

/**
 * Contains basic functionality common to most platforms.
 * @author <A HREF="mailto:keith.sheppard@jax.org">Keith Sheppard</A>
 */
public abstract class AbstractPlatformSpecificRFunctions implements
        PlatformSpecificRFunctions
{
    /**
     * the jri natives directory
     */
    private static final String JRI_NATIVES_BASE_DIR_NAME = "jri-natives";
    
    /**
     * the prefix for the JRI R dependency directory
     */
    private static final String JRI_R_DEPENDENCY_VERSION_PREFIX = "r-";
    
    /**
     * our logger...
     */
    private static final Logger LOG = Logger.getLogger(
            AbstractPlatformSpecificRFunctions.class.getName());
    
//    /**
//     * {@inheritDoc}
//     */
//    public boolean updateSettingsForRInstallation(
//            VirtualMachineSettings settingsToUpdate,
//            RInstallation installation)
//    {
//        File matchingNativeDir = this.getMatchingJriNativeDir(
//                    installation);
//        if(matchingNativeDir == null)
//        {
//            LOG.severe(
//                    "could not find matching JRI natives directory " +
//                    "for R version " + installation.getRVersion());
//            return false;
//        }
//        else
//        {
//            if(LOG.isLoggable(Level.FINE))
//            {
//                LOG.fine(
//                        "adding directory \"" +
//                        matchingNativeDir.getAbsolutePath() +
//                        "\" to " +
//                        TypeSafeSystemProperties.JAVA_HOME_PROP_NAME);
//                LOG.fine(
//                        "setting environment var: " +
//                        RInstallation.R_HOME_ENV_KEY + " to " +
//                        installation.getRHomeDirectory().getAbsolutePath());
//            }
//            settingsToUpdate.prependToJavaLibraryPath(
//                    matchingNativeDir.getAbsolutePath());
//            settingsToUpdate.getEnvironment().put(
//                    RInstallation.R_HOME_ENV_KEY,
//                    installation.getRHomeDirectory().getAbsolutePath());
//            
//            return true;
//        }
//    }
    
    /**
     * {@inheritDoc}
     */
    public String[] getSupportedRSuperVersions()
    {
        File[] jriNativeDirs = this.getJriNativeDirs();
        if(jriNativeDirs == null)
        {
            return null;
        }
        else
        {
            ArrayList<String> supportedRSuperVersions =
                new ArrayList<String>(jriNativeDirs.length);
            
            for(File currDepDir: jriNativeDirs)
            {
                String currDepDirString = currDepDir.getName();
                if(currDepDirString.startsWith(JRI_R_DEPENDENCY_VERSION_PREFIX))
                {
                    String versionSubstring = currDepDirString.substring(
                            JRI_R_DEPENDENCY_VERSION_PREFIX.length());
                    supportedRSuperVersions.add(versionSubstring);
                }
            }
            
            return supportedRSuperVersions.toArray(
                    new String[supportedRSuperVersions.size()]);
        }
    }

    private File[] getJriNativeDirs()
    {
        try
        {
            ConfigurationUtilities configUtilities =
                new ConfigurationUtilities();
            File baseDir = configUtilities.getBaseDirectory();
            File jriNativesBaseDir = new File(
                    baseDir,
                    JRI_NATIVES_BASE_DIR_NAME);
            
            if(!jriNativesBaseDir.exists())
            {
                LOG.fine(
                        "restoring JRI natives to: " +
                        jriNativesBaseDir.getAbsolutePath());
                this.restoreJriNatives(configUtilities);
            }
            
            // confirm that the directory now exists
            if(!jriNativesBaseDir.isDirectory())
            {
                LOG.severe(
                        "expected " + jriNativesBaseDir.getAbsolutePath() +
                        " to be a directory. " + jriNativesBaseDir.getAbsolutePath() +
                        " either does not exist or is a non-directory file.");
                return null;
            }
            else
            {
                File platformSpecificJRINativesDir = new File(
                        jriNativesBaseDir,
                        TypeSafeSystemProperties.getOsFamily().getUniqueFamilyNamePrefix());
                if(!platformSpecificJRINativesDir.isDirectory())
                {
                    LOG.severe(
                            "directory doesn't exist " +
                            platformSpecificJRINativesDir.getAbsolutePath());
                    return null;
                }
                else
                {
                    return platformSpecificJRINativesDir.listFiles();
                }
            }
        }
        catch(IOException ex)
        {
            LOG.log(Level.SEVERE,
                    "caught exception initializing configuration utilities",
                    ex);
            return null;
        }
    }
    
    /**
     * Get the JRI native directory that matches the given installation. This
     * file can be used as a part of the java.library.path
     * @param installation
     *          the R installation that we're looking up a JRI directory for
     * @return
     *          the matching JRI directory or null if we can't find any
     *          JRI natives to match the given R installation (eg. if
     *          none of the versions are compatible)
     */
    protected File getMatchingJriNativeDir(
            RInstallation installation)
    {
        VersionStringComparator versionComp =
            VersionStringComparator.getInstance();
        for(File currNativeDir: this.getJriNativeDirs())
        {
            if(currNativeDir.getName().startsWith(JRI_R_DEPENDENCY_VERSION_PREFIX))
            {
                String versionSubstring = currNativeDir.getName().substring(
                        JRI_R_DEPENDENCY_VERSION_PREFIX.length());
                if(versionComp.isSuperversionOf(
                        versionSubstring,
                        installation.getRVersion()))
                {
                    // we found the directory!
                    return currNativeDir;
                }
            }
        }
        
        // could not match the version in the given installation
        return null;
    }
    
    /**
     * @param configUtilities
     */
    private void restoreJriNatives(ConfigurationUtilities configUtilities)
    {
        // now expand the jri natives
        ZipInputStream jriNativesZipIn = new ZipInputStream(
                this.getClass().getResourceAsStream(
                        "/JRI-" +
                        TypeSafeSystemProperties.getOsFamily().getUniqueFamilyNamePrefix() +
                        "-natives.zip"));
        try
        {
            configUtilities.getBaseDirectory().mkdirs();
            FileUtilities.unzipToDirectory(
                    jriNativesZipIn,
                    configUtilities.getBaseDirectory());
            jriNativesZipIn.close();
        }
        catch(IOException ex)
        {
            LOG.log(Level.SEVERE,
                    "failed to restore JRI natives",
                    ex);
        }
    }
}
