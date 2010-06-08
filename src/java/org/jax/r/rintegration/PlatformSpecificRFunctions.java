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

import org.jax.virtualmachine.VirtualMachineSettings;

/**
 * This interface describes the expected directory structure.
 * @author <A HREF="mailto:keith.sheppard@jax.org">Keith Sheppard</A>
 */
public interface PlatformSpecificRFunctions
{
    /**
     * The default location where we expect the install root to be. Version
     * directories should be directly off of this directory.
     * @return
     *          the location where we expect R to be installed
     */
    public File getExpectedInstallRoot();

    /**
     * Given an R_HOME directory, this function returns the directory
     * that we expect to contain dynamic libraries
     * @param home
     *          the home dir
     * @return
     *          the dir containing the lib files
     */
    public File rHomeToExpectedRLibrary(File home);

    /**
     * Goes from a version root to the R_HOME.
     * @param versionRoot
     *          the version root that we're going to get an
     *          R_HOME from
     * @return
     *          the location where we expect the R_HOME to be
     *          given the version root
     */
    public File versionRootToExpectedRHome(File versionRoot);
    
    /**
     * Get the supported R super-versions. See
     * {@link VersionStringComparator#isSuperversionOf(String, String)} for a
     * definition.
     * @return
     *          supported super-version or null if we fail to determine
     *          supported super-versions
     */
    public String[] getSupportedRSuperVersions();
    
    /**
     * Update the given settings so that the VM launch will be able to use
     * the given {@link RInstallation} using a JRI interface
     * @param settingsToUpdate
     *          the settings we're updating
     * @param rInstallation
     *          the installation to add to the VM settings
     * @return
     *          true iff update is successfull
     */
    public boolean updateSettingsForRInstallation(
            VirtualMachineSettings settingsToUpdate,
            RInstallation rInstallation);
}
