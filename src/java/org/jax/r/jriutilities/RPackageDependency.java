/*
 * Copyright (c) 2010 The Jackson Laboratory
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

import org.jax.r.RCommand;
import org.jax.r.RCommandParameter;
import org.jax.r.RMethodInvocationCommand;
import org.jax.r.RUtilities;
import org.jax.r.rintegration.VersionStringComparator;
import org.rosuda.JRI.REXP;

/**
 * Class representing an r package dependency
 * @author <A HREF="mailto:keith.sheppard@jax.org">Keith Sheppard</A>
 */
public abstract class RPackageDependency
{
    /**
     * enum for describing the installation status of the package
     */
    public enum PackageStatus
    {
        /**
         * for when no version of the package is installed
         */
        PACKAGE_MISSING,
        
        /**
         * if the installed version is too old
         */
        PACKAGE_TOO_OLD,
        
        /**
         * if a good version is installed
         */
        PACKAGE_OK
    }
    
    private final RInterface rInterface;
    private final String packageName;
    private final String minimumVersion;
    
    private static final String PACKAGE_DESC = "packageDescription";
    private static final String PACKAGE_FIELD = "Package";
    private static final String VERSION_FIELD = "Version";
    private static final String VERSION_INFO_FIELDS =
        RUtilities.stringArrayToRVector(new String[] {PACKAGE_FIELD, VERSION_FIELD});

    /**
     * Constructor
     * @param rInterface        the R interface
     * @param packageName       the package name
     * @param minimumVersion    the package version
     */
    public RPackageDependency(
            RInterface rInterface,
            String packageName,
            String minimumVersion)
    {
        this.rInterface = rInterface;
        this.packageName = packageName;
        this.minimumVersion = minimumVersion;
    }
    
    /**
     * Getter for the R interface
     * @return the R interface
     */
    public RInterface getRInterface()
    {
        return this.rInterface;
    }
    
    /**
     * Getter for the package name
     * @return the package name
     */
    public String getPackageName()
    {
        return this.packageName;
    }
    
    /**
     * Getter for the minimum allowed package version
     * @return the minimum package version
     */
    public String getMinimumVersion()
    {
        return this.minimumVersion;
    }
    
    /**
     * Get the version string for the currently installed version
     * @return  the version string or null if it isn't installed
     */
    public String getInstalledVersion()
    {
        RCommand getVersionCommand = new SilentRCommand(new RMethodInvocationCommand(
                PACKAGE_DESC,
                new RCommandParameter(
                        RUtilities.javaStringToRString(this.getPackageName())),
                new RCommandParameter(
                        "fields",
                        RUtilities.javaStringToRString(VERSION_FIELD))));
        REXP versionExpr = this.rInterface.evaluateCommand(getVersionCommand);
        return versionExpr.asString();
    }
    
    /**
     * Print out the version info for this package
     */
    public void showVersionInfo()
    {
        RMethodInvocationCommand showVersionCommand =
            new RMethodInvocationCommand(
                    PACKAGE_DESC,
                    new RCommandParameter(
                            RUtilities.javaStringToRString(this.getPackageName())),
                    new RCommandParameter("fields", VERSION_INFO_FIELDS));
        this.rInterface.evaluateCommandNoReturn(showVersionCommand);
    }
    
    /**
     * Getter for the package status
     * @return  the package status
     */
    public PackageStatus getPackageStatus()
    {
        String installedVersion = this.getInstalledVersion();
        if(installedVersion == null)
        {
            return PackageStatus.PACKAGE_MISSING;
        }
        else
        {
            VersionStringComparator versionCmp = VersionStringComparator.getInstance();
            if(versionCmp.compare(installedVersion, this.minimumVersion) >= 0)
            {
                return PackageStatus.PACKAGE_OK;
            }
            else
            {
                return PackageStatus.PACKAGE_TOO_OLD;
            }
        }
    }
    
    /**
     * load this package
     */
    public void loadPackage()
    {
        RCommand loadMethod = new RMethodInvocationCommand(
                "library",
                new RCommandParameter(RUtilities.javaStringToRString(this.packageName)));
        this.rInterface.evaluateCommand(loadMethod);
    }
    
    /**
     * Install this dependency
     */
    public abstract void installPackage();
}
