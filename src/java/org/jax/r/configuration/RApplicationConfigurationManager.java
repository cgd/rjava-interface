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

package org.jax.r.configuration;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.ZipInputStream;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;

import org.jax.r.jaxbgenerated.FileType;
import org.jax.r.jaxbgenerated.LaunchRUsingType;
import org.jax.r.jaxbgenerated.RApplicationConfiguration;
import org.jax.r.jaxbgenerated.RApplicationStateType;
import org.jax.r.jaxbgenerated.RInstallationType;
import org.jax.r.jaxbgenerated.RLaunchConfigurationType;
import org.jax.r.rintegration.RInstallation;
import org.jax.r.rintegration.RLaunchConfiguration;
import org.jax.r.rintegration.RLaunchConfiguration.LaunchUsingEnum;
import org.jax.util.ConfigurationUtilities;
import org.jax.util.io.FileUtilities;

/**
 * Base class for R application configuration managers.
 * @author <A HREF="mailto:keith.sheppard@jax.org">Keith Sheppard</A>
 */
public abstract class RApplicationConfigurationManager
{
    /**
     * Our logger
     */
    private static final Logger LOG = Logger.getLogger(
            RApplicationConfigurationManager.class.getName());
    
    private static final int MAX_RECENT_PROJECT_HISTORY_LENGTH = 10;
    
    /**
     * @see #getSaveOnExit()
     */
    private volatile boolean saveOnExit;
    
    /**
     * our configuration
     */
    private RApplicationConfiguration applicationConfiguration;
    
    /**
     * the file containing all of our configuration data
     */
    private File configurationFile;
    
    /**
     * the file containing all of our configuration data
     */
    private File applicationStateFile;
    
    /**
     * the application state
     */
    private RApplicationStateType applicationState;
    
    /**
     * our default config utilities
     */
    private ConfigurationUtilities configurationUtilities;

    /**
     * the JAXB context that we're using
     */
    private final JAXBContext jaxbContext;
    
    /**
     * Constructor
     * @param jaxbContext
     *          the JAXB context that we should use for marshalling
     */
    public RApplicationConfigurationManager(JAXBContext jaxbContext)
    {
        this.jaxbContext = jaxbContext;
        
        try
        {
            this.configurationUtilities = new ConfigurationUtilities();
            
            // add a shutdown hook so that we can save away any configuration
            // changes that are made when this app closes
            Runtime.getRuntime().addShutdownHook(
                new Thread()
                {
                    @Override
                    public void run()
                    {
                        try
                        {
                            if(RApplicationConfigurationManager.this.saveOnExit)
                            {
                                if(RApplicationConfigurationManager.this.saveApplicationConfiguration())
                                {
                                    if(LOG.isLoggable(Level.FINE))
                                    {
                                        LOG.fine(
                                                "completed saving application" +
                                                " configuration on exit");
                                    }
                                }
                                else
                                {
                                    LOG.severe(
                                            "attempt to save application " +
                                            "configuration on exit was " +
                                            "unsuccessfull");
                                }
                                
                                if(RApplicationConfigurationManager.this.saveApplicationState())
                                {
                                    if(LOG.isLoggable(Level.FINE))
                                    {
                                        LOG.fine(
                                                "completed saving application" +
                                                "state on exit");
                                    }
                                }
                                else
                                {
                                    LOG.severe(
                                            "attempt to save application " +
                                            "state on exit was " +
                                            "unsuccessfull");
                                }
                            }
                            else
                            {
                                if(LOG.isLoggable(Level.FINE))
                                {
                                    LOG.fine(
                                            "exiting without saving" +
                                            " application configuration");
                                }
                            }
                        }
                        catch(Exception ex)
                        {
                            LOG.log(Level.SEVERE,
                                    "failed to save application configuration" +
                                    " on exit",
                                    ex);
                        }
                    }
                });
            
    
            this.initializeApplicationConfiguration();
        }
        catch(Exception ex)
        {
            LOG.log(Level.SEVERE,
                    "Failed to initialize application",
                    ex);
        }
    }
    
    /**
     * Getter for the application state. This is meant to hold "session"
     * information that allows us to restore the application to the state
     * that it was in before the user last closed.
     * @return
     *          the application state
     */
    public RApplicationStateType getApplicationState()
    {
        return this.applicationState;
    }
    
    /**
     * Getter for the configuration file that should be used.
     * @return
     *          the configuration file's name
     */
    protected abstract String getConfigurationFileName();
    
    /**
     * Getter for the application state file that we should use
     * @return
     *          the application state file's name
     */
    protected abstract String getApplicationStateFileName();
    
    /**
     * Getter for the configuration zip resource that we should use
     * @return
     *          the resource path for the zip file
     */
    protected abstract String getConfigurationZipResourceName();
    
    /**
     * Create a new application state instance
     * @return
     *          the new application state instance
     */
    protected abstract RApplicationStateType createNewApplicationState();
    
    /**
     * load the application configuration or create it if it doesn't exist
     * @throws JAXBException
     *          if we catch a jaxb exception while unmarshalling
     * @throws URISyntaxException 
     */
    private void initializeApplicationConfiguration() throws JAXBException, URISyntaxException
    {
        // create the config dir if it doesn't yet exist
        this.configurationFile = new File(
                this.configurationUtilities.getBaseDirectory(),
                this.getConfigurationFileName());
        this.applicationStateFile = new File(
                this.configurationUtilities.getBaseDirectory(),
                this.getApplicationStateFileName());
        if(!this.configurationFile.exists())
        {
            ZipInputStream configZipIn = new ZipInputStream(
                    this.getClass().getResourceAsStream(
                            this.getConfigurationZipResourceName()));
            try
            {
                this.configurationUtilities.getBaseDirectory().mkdirs();
                FileUtilities.unzipToDirectory(
                        configZipIn,
                        this.configurationUtilities.getBaseDirectory());
                configZipIn.close();
            }
            catch(IOException ex)
            {
                LOG.log(Level.SEVERE,
                        "failed to restore application configuration",
                        ex);
            }
        }
        
        Unmarshaller jaxbUnmarshaller =
            this.jaxbContext.createUnmarshaller();
        
        // read in the configuration file if it exists
        if(this.configurationFile.isFile())
        {
            if(LOG.isLoggable(Level.FINE))
            {
                LOG.fine(
                        "loading configuration file: " +
                        this.configurationFile.getAbsolutePath());
            }
            
            this.applicationConfiguration =
                (RApplicationConfiguration)jaxbUnmarshaller.unmarshal(
                        this.configurationFile);
        }
        else
        {
            LOG.severe(
                    "the configuration file is missing (or not the " +
                    "correct file type).");
        }
        
        // read in the application state if it exists
        if(this.applicationStateFile.isFile())
        {
            if(LOG.isLoggable(Level.FINE))
            {
                LOG.fine(
                        "loading application state file: " +
                        this.applicationStateFile.getAbsolutePath());
            }
            
            this.applicationState =
                (RApplicationStateType)jaxbUnmarshaller.unmarshal(
                        this.applicationStateFile);
        }
        else
        {
            if(LOG.isLoggable(Level.FINE))
            {
                LOG.fine(
                        "the application state file is missing, so we're " +
                        "just going to create a new one from scratch");
            }
            this.applicationState = this.createNewApplicationState();
        }
    }

    /**
     * Determines whether or not we attempt to save data on
     * {@linkplain Runtime#addShutdownHook(Thread) virtual machine shutdown}.
     * @return
     *      true iff we will try to save on exit
     */
    public boolean getSaveOnExit()
    {
        return this.saveOnExit;
    }

    /**
     * Setter for {@link #saveOnExit}.
     * @param saveOnExit
     *          the new value for {@link #saveOnExit}
     * @see #getSaveOnExit()
     */
    public void setSaveOnExit(boolean saveOnExit)
    {
        this.saveOnExit = saveOnExit;
    }

    /**
     * Getter for the R application configuration. This shared instance
     * allows updates to be visible in all parts of the application
     * using this configuration.
     * @return
     *      the application configuration
     */
    public RApplicationConfiguration getApplicationConfiguration()
    {
        return this.applicationConfiguration;
    }
    
    /**
     * Convert the given jaxb R installation type to a native java type.
     * @param jaxbRInstallation
     *          the jaxb type
     * @return
     *          the native type
     */
    private static RInstallation fromJaxbToNativeRInstallation(
            RInstallationType jaxbRInstallation)
    {
        if(jaxbRInstallation == null)
        {
            return null;
        }
        else
        {
            String rHome =
                jaxbRInstallation.getRHomeDirectory();
            String libDir =
                jaxbRInstallation.getLibraryDirectory();
            String version =
                jaxbRInstallation.getVersion();
            RInstallation rInstallation = new RInstallation(
                    new File(rHome),
                    new File(libDir),
                    version);
            
            return rInstallation;
        }
    }

    /**
     * Convert the given list of jaxb R installations into an array of
     * native R installations
     * @param jaxbRInstallations
     *          the jaxb types to convert
     * @return
     *          the native types
     */
    public static RInstallation[] fromJaxbToNativeRInstallations(
            List<RInstallationType> jaxbRInstallations)
    {
        if(jaxbRInstallations == null)
        {
            return new RInstallation[0];
        }
        else
        {
            RInstallation[] rInstallations =
                new RInstallation[jaxbRInstallations.size()];
            Iterator<RInstallationType> jaxbInstalIter = jaxbRInstallations.iterator();
            for(int i = 0; i < rInstallations.length; i++)
            {
                rInstallations[i] = fromJaxbToNativeRInstallation(
                        jaxbInstalIter.next());
            }
            
            return rInstallations;
        }
    }
    
    /**
     * Convert from a jaxb R launch config type to a native java type
     * @param jaxbRLaunchConfiguration
     *          the JAXB type to convert
     * @return
     *          the native java type
     */
    public static RLaunchConfiguration fromJaxbToNativeRLaunchConfiguration(
            RLaunchConfigurationType jaxbRLaunchConfiguration)
    {
        if(jaxbRLaunchConfiguration == null)
        {
            return null;
        }
        else
        {
            LaunchUsingEnum nativeLaunchRUsingType;
            
            switch(jaxbRLaunchConfiguration.getLaunchRUsing())
            {
                case ENVIRONMENT_VARIABLES:
                    nativeLaunchRUsingType =
                        LaunchUsingEnum.LAUNCH_USING_ENVIRONMENT;
                    break;
                case SELECTED_R_INSTALLATION:
                    nativeLaunchRUsingType =
                        LaunchUsingEnum.LAUNCH_USING_SELECTED_INSTALLATION;
                    break;
                default:
                    LOG.severe(
                            "missed a launch R using enum type: " +
                            jaxbRLaunchConfiguration.getLaunchRUsing());
                    nativeLaunchRUsingType = null;
                    break;
            }
            
            RInstallation nativeRInstallation =
                fromJaxbToNativeRInstallation(
                        jaxbRLaunchConfiguration.getSelectedRInstallation());
            RLaunchConfiguration nativeRLaunchConfiguration =
                new RLaunchConfiguration(
                        nativeLaunchRUsingType,
                        nativeRInstallation);
            
            return nativeRLaunchConfiguration;
        }
    }

    /**
     * Convert from a native java R installation type to a JAXB R
     * installation type.
     * @param nativeRInstallation
     *          the native instance
     * @return
     *          a JAXB instance
     */
    private static RInstallationType fromNativeToJaxbRInstallation(
            RInstallation nativeRInstallation)
    {
        if(nativeRInstallation == null)
        {
            return null;
        }
        else
        {
            org.jax.r.jaxbgenerated.ObjectFactory rObjectFactory =
                new org.jax.r.jaxbgenerated.ObjectFactory();
            RInstallationType jaxbRInstallation =
                rObjectFactory.createRInstallationType();
            jaxbRInstallation.setLibraryDirectory(
                    nativeRInstallation.getLibraryDirectory().getAbsolutePath());
            jaxbRInstallation.setRHomeDirectory(
                    nativeRInstallation.getRHomeDirectory().getAbsolutePath());
            jaxbRInstallation.setVersion(
                    nativeRInstallation.getRVersion());
            
            return jaxbRInstallation;
        }
    }

    /**
     * Convert from a native java R installation array to a JAXB R
     * installation list
     * @param nativeRInstallations
     *          the native java type
     * @return
     *          the JAXB type
     */
    public static List<RInstallationType> fromNativeToJaxbRInstallations(
            RInstallation[] nativeRInstallations)
    {
        if(nativeRInstallations == null)
        {
            return Collections.emptyList();
        }
        else
        {
            // convert to and fill in the jaxb installations
            List<RInstallationType> jaxbDetectedRInstallations =
                new ArrayList<RInstallationType>(
                        nativeRInstallations.length);
            for(RInstallation currNativeInstallation: nativeRInstallations)
            {
                RInstallationType currJaxbRInstallation =
                    fromNativeToJaxbRInstallation(
                            currNativeInstallation);
                jaxbDetectedRInstallations.add(currJaxbRInstallation);
            }
            
            return jaxbDetectedRInstallations;
        }
    }
    
    /**
     * Convert from a native java R launch config type to a  JAXB type
     * @param nativeRLaunchConfiguration
     *          the native java type
     * @return
     *          the JAXB type
     */
    public static RLaunchConfigurationType fromNativeToJaxbRLaunchConfiguration(
            RLaunchConfiguration nativeRLaunchConfiguration)
    {
        if(nativeRLaunchConfiguration == null)
        {
            return null;
        }
        else
        {
            org.jax.r.jaxbgenerated.ObjectFactory rObjectFactory =
                new org.jax.r.jaxbgenerated.ObjectFactory();
            RLaunchConfigurationType jaxbRLaunchConfiguration =
                rObjectFactory.createRLaunchConfigurationType();
            switch(nativeRLaunchConfiguration.getLaunchUsing())
            {
                case LAUNCH_USING_ENVIRONMENT:
                    jaxbRLaunchConfiguration.setLaunchRUsing(
                         LaunchRUsingType.ENVIRONMENT_VARIABLES);
                    break;
                case LAUNCH_USING_SELECTED_INSTALLATION:
                    jaxbRLaunchConfiguration.setLaunchRUsing(
                         LaunchRUsingType.SELECTED_R_INSTALLATION);
                    break;
                default:
                    LOG.severe(
                            "missed an launch using enum type: " +
                            nativeRLaunchConfiguration.getLaunchUsing());
                    break;
            }
            
            jaxbRLaunchConfiguration.setSelectedRInstallation(
                    fromNativeToJaxbRInstallation(
                            nativeRLaunchConfiguration.getSelectedInstallation()));
            
            return jaxbRLaunchConfiguration;
        }
    }
    
    /**
     * Save any configuration changes to the application configuration file.
     * @see #getApplicationConfiguration()
     * @return
     *          true iff the configuration is successfully saved
     */
    public boolean saveApplicationConfiguration()
    {
        try
        {
            // save to file
            FileOutputStream configFileOut = new FileOutputStream(
                    this.configurationFile);
            Marshaller marshaller = this.jaxbContext.createMarshaller();
            marshaller.setProperty(
                    Marshaller.JAXB_FORMATTED_OUTPUT,
                    Boolean.TRUE);
            marshaller.marshal(
                    this.applicationConfiguration,
                    configFileOut);
            configFileOut.close();
            
            return true;
        }
        catch(Exception ex)
        {
            LOG.log(Level.SEVERE,
                    "failed to save application configuration due to exception",
                    ex);
            
            return false;
        }
    }
    
    /**
     * Save any configuration changes to the application state.
     * @see #getApplicationState()
     * @return
     *          true iff the state is successfully saved
     */
    public boolean saveApplicationState()
    {
        try
        {
            // save to file
            FileOutputStream applicationStateFileOut = new FileOutputStream(
                    this.applicationStateFile);
            Marshaller marshaller = this.jaxbContext.createMarshaller();
            marshaller.setProperty(
                    Marshaller.JAXB_FORMATTED_OUTPUT,
                    Boolean.TRUE);
            marshaller.marshal(
                    this.getApplicationState(),
                    applicationStateFileOut);
            applicationStateFileOut.close();
            
            return true;
        }
        catch(Exception ex)
        {
            LOG.log(Level.SEVERE,
                    "failed to save application configuration due to exception",
                    ex);
            
            return false;
        }
    }
    
    /**
     * Tell the confguration that the active project file changed so that we
     * can add the new file to the recent files list
     * @param activeProjectFile
     *          the new active project file
     */
    public void notifyActiveProjectFileChanged(File activeProjectFile)
    {
        if(activeProjectFile != null)
        {
            // update the "recent project files"
            String absolutePath = activeProjectFile.getAbsolutePath();

            List<FileType> recentProjects = this.applicationState.getRecentProjectFile();

            Iterator<FileType> recentProjectsIter = recentProjects.iterator();
            while(recentProjectsIter.hasNext())
            {
                if(recentProjectsIter.next().getFileName().equals(absolutePath))
                {
                    // remove any duplicates 1st
                    recentProjectsIter.remove();
                }
            }
            
            org.jax.r.jaxbgenerated.ObjectFactory objectFactory =
                new org.jax.r.jaxbgenerated.ObjectFactory();
            FileType newJaxbProjFile = objectFactory.createFileType();
            newJaxbProjFile.setFileName(absolutePath);
            recentProjects.add(0, newJaxbProjFile);

            // make sure the list doesn't grow beyond the max
            int recentProjectsSize = recentProjects.size();
            if(recentProjectsSize > MAX_RECENT_PROJECT_HISTORY_LENGTH)
            {
                for(int i = recentProjectsSize - 1; i >= MAX_RECENT_PROJECT_HISTORY_LENGTH; i--)
                {
                    recentProjects.remove(i);
                }
            }
        }
    }
}
