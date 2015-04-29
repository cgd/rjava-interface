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
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * This class finds potential {@link RInstallation}s
 * @author <A HREF="mailto:keith.sheppard@jax.org">Keith Sheppard</A>
 */
public class RInstallationScanner
{
    /**
     * our logger
     */
    private static final Logger LOG = Logger.getLogger(RInstallationScanner.class.getName());
    
    /**
     * the regular expression i'm using to dig the version out from the dir name.
     * this pattern accepts digits anywhere, and accepts '.', '-', and '_' on
     * the internal part of the version string, but not on the border.
     */
    private static final Pattern R_DIR_NAME_TO_VERSION_MATCHER =
        Pattern.compile("([0-9]+[0-9_\\-\\.]*[0-9]+|[0-9])");
    
    /**
     * Converts an R_HOME directory into an {@link RInstallation}.
     * @param rInstallDirStructure
     *          holds info about the directory structure of R installs
     * @param rHomeDirectory
     *          the home directory (R_HOME)
     * @return
     *          the {@link RInstallation} for the directory or null
     *          if our validity checks fail for the R_HOME
     *          that's passed in
     */
    public RInstallation rHomeDirectoryToRInstallation(
            PlatformSpecificRFunctions rInstallDirStructure,
            File rHomeDirectory)
    {
        // start off as null...
        RInstallation rInstallation = null;
        
        if(rHomeDirectory.isDirectory())
        {
            // ok, see if we can see the R lib dir where we expect it to be
            File rLibraryDirectory =
                rInstallDirStructure.rHomeToExpectedRLibrary(rHomeDirectory);
            if(rLibraryDirectory.isDirectory())
            {
                try
                {
                    // the expected R library exists... this looks like a valid R home
                    // 1st pull out the version string
                    Matcher versionMatcher =
                        R_DIR_NAME_TO_VERSION_MATCHER.matcher(
                                rHomeDirectory.getCanonicalPath());
                    String versionString = null;
                    
                    // only hold on to the last match (assuming the last match is
                    // most likely to be a version string)
                    while(versionMatcher.find())
                    {
                        versionString = versionMatcher.group();
                    }
                    
                    if(versionString == null)
                    {
                        if(LOG.isLoggable(Level.FINE))
                        {
                            LOG.fine(
                                    "could not detect what version of R lives in \"" +
                                    rHomeDirectory.getCanonicalPath() + "\"");
                        }
                    }
                    
                    // we have everything we need to make a home! :-)
                    // it's ok if versionString is null
                    rInstallation = new RInstallation(
                            rHomeDirectory,
                            rLibraryDirectory,
                            versionString);
                }
                catch(IOException ex)
                {
                    LOG.log(Level.SEVERE,
                            "caught exception while trying to convert" +
                            " an R_HOME directory into an RInstallation object",
                            ex);
                }
            }
        }
        else if(LOG.isLoggable(Level.FINE))
        {
            // we already know it's not a directory... see if it even exists
            if(rHomeDirectory.exists())
            {
                LOG.fine(
                        "expected R home \"" + rHomeDirectory.getPath() +
                        "\" exists but is not a directory");
            }
            else
            {
                LOG.fine(
                        "expected R home \"" + rHomeDirectory.getPath() +
                        "\" does not exist");
            }
        }
        
        return rInstallation;
    }
    
    /**
     * Scan for {@link RInstallation}s using the install structure passed in
     * @param rInstallDirStructure
     *          the install directory structure to use for this scan
     * @return
     *          the potential {@link RInstallation}s that we find
     */
    public RInstallation[] scanForRInstallations(PlatformSpecificRFunctions rInstallDirStructure)
    {
        return this.scanForRInstallations(
                rInstallDirStructure,
                null);
    }
    
    /**
     * Scan for {@link RInstallation}s using the install structure passed in
     * @param rInstallDirStructure
     *          the install directory structure to use for this scan
     * @param minimumVersionString
     *          the minimum version string of the {@link RInstallation}s that we can
     *          return. this uses {@link VersionStringComparator} to determine
     *          version ordering. if this string is null then we pass
     *          every {@link RInstallation} through wether or not we can even detect
     *          a version (ie. the version can be null)
     * @return
     *          a sorted list of potential {@link RInstallation}s that we found
     */
    public RInstallation[] scanForRInstallations(
            PlatformSpecificRFunctions rInstallDirStructure,
            String minimumVersionString)
    {
        List<RInstallation> discoveredRInstallations = new ArrayList<RInstallation>();
        
        for(File defaultInstallRootDir : rInstallDirStructure.getExpectedInstallRoots()) {
            if(!defaultInstallRootDir.exists())
            {
                LOG.info(
                        "default R install dir \"" +
                        defaultInstallRootDir +
                        "\" doesn't exist");
            }
            else if(!defaultInstallRootDir.isDirectory())
            {
                LOG.info(
                        "default R install dir \"" +
                        defaultInstallRootDir +
                        "\" exists but is not a directory");
            }
            else
            {
                // we're ready to dive into the install root
                File[] versionRootsArray = defaultInstallRootDir.listFiles();
                
                if(versionRootsArray == null || versionRootsArray.length == 0)
                {
                    LOG.info(
                            "didn't find any R versions in the default R install directory \"" +
                            defaultInstallRootDir + "\"");
                }
                else
                {
                    Map<File, File> canonicalToAbsoluteVersionRootMap = new HashMap<File, File>();
                    for(File versionRoot: versionRootsArray)
                    {
                        try
                        {
                            File canonicalPath = versionRoot.getCanonicalFile();
                            File absolutePath = versionRoot.getAbsoluteFile();
                            
                            if(!canonicalToAbsoluteVersionRootMap.containsKey(canonicalPath) ||
                               absolutePath.equals(canonicalPath))
                            {
                                canonicalToAbsoluteVersionRootMap.put(
                                        canonicalPath,
                                        absolutePath);
                            }
                        }
                        catch(IOException ex)
                        {
                            // Fall back on the absolute path
                            File absolutePath = versionRoot.getAbsoluteFile();
                            canonicalToAbsoluteVersionRootMap.put(
                                    absolutePath,
                                    absolutePath);
                        }
                    }
                    List<File> versionRoots = new ArrayList<File>(
                            canonicalToAbsoluteVersionRootMap.values());
                    Collections.sort(versionRoots);
                    
                    for(File currVersionRoot: versionRoots)
                    {
                        // check for the R_HOME
                        File currRHomeDirectory =
                            rInstallDirStructure.versionRootToExpectedRHome(
                                    currVersionRoot);
                        RInstallation currRInstallation =
                            this.rHomeDirectoryToRInstallation(
                                    rInstallDirStructure,
                                    currRHomeDirectory);
                        
                        if(currRInstallation != null)
                        {
                            if(minimumVersionString == null)
                            {
                                // if the minimum version string is null, then we
                                // don't need to go through any more checks. just
                                // pass the R Home through
                                discoveredRInstallations.add(currRInstallation);
                            }
                            else if(currRInstallation.getRVersion() != null &&
                                    VersionStringComparator.getInstance().compare(
                                            currRInstallation.getRVersion(),
                                            minimumVersionString) >= 0)
                            {
                                // the versioning test passed, we can put this
                                // R Home through
                                discoveredRInstallations.add(currRInstallation);
                            }
                        }
                    }
                }
            }
        }
        
        // return any R_HOME's that we've found
        RInstallation[] returnVal =
            discoveredRInstallations.toArray(
                    new RInstallation[discoveredRInstallations.size()]);
        Arrays.sort(returnVal);
        return returnVal;
    }
}
