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

import org.jax.util.ObjectUtil;

/**
 * A simple class with properties describing how R should be launched.
 * @author <A HREF="mailto:keith.sheppard@jax.org">Keith Sheppard</A>
 */
public class RLaunchConfiguration
{
    /**
     * Tells us wether we should launch using the environment variables,
     * or if we should use the selected installation
     */
    public static enum LaunchUsingEnum
    {
        /**
         * launch from {@link RLaunchConfiguration#getSelectedInstallation()}
         */
        LAUNCH_USING_SELECTED_INSTALLATION,
        
        /**
         * Launch using environment variables
         */
        LAUNCH_USING_ENVIRONMENT
    }
    
    /**
     * @see #getLaunchUsing()
     */
    private final LaunchUsingEnum launchUsing;
    
    /**
     * @see #getSelectedInstallation()
     */
    private final RInstallation selectedInstallation;

    /**
     * Constructor
     * @param launchUsing
     *          see {@link #getLaunchUsing()}
     * @param rInstallation 
     *          see {@link #getSelectedInstallation()}
     */
    public RLaunchConfiguration(
            LaunchUsingEnum launchUsing,
            RInstallation rInstallation)
    {
        this.launchUsing = launchUsing;
        this.selectedInstallation = rInstallation;
    }
    
    /**
     * @return the launchUsing
     */
    public LaunchUsingEnum getLaunchUsing()
    {
        return this.launchUsing;
    }

    /**
     * Get the selected R installation. Can be null if
     * {@link #getLaunchUsing()} == {@link LaunchUsingEnum#LAUNCH_USING_ENVIRONMENT}
     * @return the selected R installation
     */
    public RInstallation getSelectedInstallation()
    {
        return this.selectedInstallation;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(Object otherRLaunchConfigurationObj)
    {
        if(otherRLaunchConfigurationObj == null)
        {
            return false;
        }
        else
        {
            if(otherRLaunchConfigurationObj instanceof RLaunchConfiguration)
            {
                RLaunchConfiguration otherRLaunchConfiguration =
                    (RLaunchConfiguration)otherRLaunchConfigurationObj;
                return
                    ObjectUtil.areEqual(this.launchUsing, otherRLaunchConfiguration.launchUsing) &&
                    ObjectUtil.areEqual(this.selectedInstallation, otherRLaunchConfiguration.selectedInstallation);
            }
            else
            {
                return false;
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode()
    {
        return
            ObjectUtil.hashObject(this.launchUsing) +
            ObjectUtil.hashObject(this.selectedInstallation);
    }
}
