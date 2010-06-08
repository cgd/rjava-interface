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
import java.util.Arrays;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.Callable;
import java.util.concurrent.FutureTask;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.ZipInputStream;

import javax.swing.JDialog;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

import org.jax.r.configuration.RApplicationConfigurationManager;
import org.jax.r.jaxbgenerated.RApplicationConfiguration;
import org.jax.r.jaxbgenerated.RApplicationStateType;
import org.jax.r.jaxbgenerated.RConfigurationType;
import org.jax.r.jaxbgenerated.RInstallationType;
import org.jax.r.jaxbgenerated.RLaunchConfigurationType;
import org.jax.r.jaxbgenerated.SystemPropertyType;
import org.jax.r.rintegration.gui.RHomeSelectorFrame;
import org.jax.util.ConfigurationUtilities;
import org.jax.util.TextWrapper;
import org.jax.util.TypeSafeSystemProperties;
import org.jax.util.concurrent.SafeAWTInvoker;
import org.jax.util.io.FileExtensionFilter;
import org.jax.util.io.FileUtilities;
import org.jax.virtualmachine.CommandLineVirtualMachineLauncher;
import org.jax.virtualmachine.VirtualMachineException;
import org.jax.virtualmachine.VirtualMachineLauncher;
import org.jax.virtualmachine.VirtualMachineSettings;

/**
 * This class contains all of the major functionality needed to launch
 * a main application allowing the user to select an appropriate R
 * distribution.
 * @author <A HREF="mailto:keith.sheppard@jax.org">Keith Sheppard</A>
 */
// TODO add support for command line arguments to launcher
public abstract class RLauncher
{
    /**
     * our logger
     */
    private static final Logger LOG = Logger.getLogger(
            RLauncher.class.getName());
    
    /**
     * Get the application entry point. This class must have a main(...)
     * or the launch will fail.
     * @return
     *          the application's main
     */
    protected abstract Class<?> getApplicationMainClass();
    
    /**
     * This is the resource path for the classpath zip
     * @return
     *          the resource path
     */
    protected abstract String getClasspathZipFileResourcePath();
    
    /**
     * Getter for the application configuration manager
     * @return
     *          the application configuration manager
     */
    protected abstract RApplicationConfigurationManager getApplicationConfigurationManager();
    
    /**
     * Getter for the application name as it should be presented to the user
     * @return
     *          the application name
     */
    protected abstract String getReadableApplicationName();
    
    /**
     * System properties to add to the launch
     * @return
     *          getter for the launch system properties
     */
    private Properties getLaunchSystemProperties()
    {
        RApplicationConfiguration applicationConfiguration =
            this.getApplicationConfigurationManager().getApplicationConfiguration();
        Properties launchSystemProperties = new Properties();
        for(SystemPropertyType currProperty: applicationConfiguration.getLaunchSystemProperty())
        {
            launchSystemProperties.put(
                    currProperty.getKey(),
                    currProperty.getValue());
        }
        
        return launchSystemProperties;
    }
    
    private Long getJavaMemoryLimitMegabytes()
    {
        RApplicationConfiguration applicationConfiguration =
            this.getApplicationConfigurationManager().getApplicationConfiguration();
        return applicationConfiguration.getJavaMemoryLimitMegabytes();
    }
    
    /**
     * Get the classpath that should be used for this R launcher.
     * @return
     *          the classpath
     */
    private String getClasspath()
    {
        try
        {
            // expand the classpath zip
            File baseDir = new ConfigurationUtilities().getBaseDirectory();
            File classpathDir = new File(
                    baseDir,
                    TypeSafeSystemProperties.CLASS_PATH_PROP_NAME);
            
            classpathDir.mkdirs();
            ZipInputStream classpathZipStream = new ZipInputStream(
                    this.getClass().getResourceAsStream(
                            this.getClasspathZipFileResourcePath()));
            FileUtilities.unzipToDirectory(
                    classpathZipStream,
                    classpathDir);
            classpathZipStream.close();
            
            if(LOG.isLoggable(Level.FINE))
            {
                LOG.fine(
                        "successfully expanded jars to: " +
                        classpathDir.getAbsolutePath());
            }
            
            FileExtensionFilter jarFilter = new FileExtensionFilter("jar");
            File[] jarFiles = classpathDir.listFiles(jarFilter);
            StringBuffer classpath = new StringBuffer();
            for(int i = 0; i < jarFiles.length; i++)
            {
                classpath.append(jarFiles[i].getAbsolutePath());
                if(i < jarFiles.length - 1)
                {
                    // add a separator unless it's the last jar
                    classpath.append(File.pathSeparatorChar);
                }
            }
            
            if(LOG.isLoggable(Level.FINE))
            {
                LOG.fine("built classpath: " + classpath.toString());
            }
            
            return classpath.toString();
        }
        catch(IOException ex)
        {
            LOG.log(Level.SEVERE,
                    "failed to build classpath",
                    ex);
            return null;
        }
    }

    /**
     * Tell the user that we have detected a change in the R installation
     * directory structure and ask them if they want to select a new
     * installation.
     * @return
     *          true if they do
     */
    private boolean determineIfUserWantsToSelectRInstallation()
    {
        try
        {
            // TODO messages should be in a resource bundle
            String message =
                "A change in the R installation directory " +
                "structure has been detected. Would you like to " +
                "select a new R installation or keep using the " +
                "currently selected installation?";
            final JOptionPane optionPane = new JOptionPane(
                    TextWrapper.wrapText(
                            message,
                            TextWrapper.DEFAULT_DIALOG_COLUMN_COUNT));
            String[] options = new String[] {
                "Select an R Installation...",
                "Keep Current Selection"};
            optionPane.setOptions(options);
            optionPane.setMessageType(JOptionPane.QUESTION_MESSAGE);
            
            // jump through some swing thread-safety hoops
            FutureTask<Object> futureSelectedValue =
                new FutureTask<Object>(new Callable<Object>()
                {
                    public Object call() throws Exception
                    {
                        JDialog dialog = optionPane.createDialog(
                                null,
                                "R Installation Change Detected");
                        dialog.pack();
                        dialog.setVisible(true);
                        return optionPane.getValue();
                    }
                });
            SwingUtilities.invokeLater(futureSelectedValue);
            
            // figure out what the user just asked us to do
            Object selectedValue = futureSelectedValue.get();
            if(selectedValue == null)
            {
                if(LOG.isLoggable(Level.FINE))
                {
                    LOG.fine(
                            "user closed \"R Installation Change Detected\" " +
                            "dialog without making a selection");
                }
                
                return false;
            }
            else if(selectedValue == JOptionPane.UNINITIALIZED_VALUE)
            {
                LOG.severe(
                        "unexpected condition. user has not yet made " +
                        "a choice");
                return false;
            }
            else
            {
                if(selectedValue == options[0])
                {
                    if(LOG.isLoggable(Level.FINE))
                    {
                        LOG.fine("user wants to select an installation");
                    }
                    
                    return true;
                }
                else if(selectedValue == options[1])
                {
                    if(LOG.isLoggable(Level.FINE))
                    {
                        LOG.fine("user doesn't want to select an installation");
                    }
                    
                    return false;
                }
                else
                {
                    LOG.severe(
                            "received unexpected selection value: " +
                            selectedValue);
                    
                    return false;
                }
            }
        }
        catch(Exception ex)
        {
            LOG.log(Level.SEVERE,
                    "received exception while asking user about " +
                    "whether or not they wanted to select an R installation",
                    ex);
            return false;
        }
    }
    
    /**
     * Launch with the given configuration
     * @param launchConfiguration
     *          the configuration to launch with
     */
    private void launchWithConfiguration(
            RLaunchConfiguration launchConfiguration)
    {
        switch(launchConfiguration.getLaunchUsing())
        {
            case LAUNCH_USING_ENVIRONMENT:
            {
                // the user wants us to use the current environment so invoke
                // main directly
                LOG.severe(
                        "direct launching not yet supported");
            }
            break;

            case LAUNCH_USING_SELECTED_INSTALLATION:
            {
                LOG.fine(
                        "the user wants to use selected installation, so " +
                        "we're going to use the application launcher");
                
                RInstallation selectedInstallation =
                    launchConfiguration.getSelectedInstallation();
                if(selectedInstallation == null)
                {
                    final String errorMessage =
                        "Error detected during application launch " +
                        "(no selected R installation). Exiting application.";
                    LOG.severe(errorMessage);
                    RLauncher.showLaunchErrorNotification(errorMessage);
                }
                else
                {
                    // take care of R_HOME
                    File rHomeFile =
                        selectedInstallation.getRHomeDirectory();
                    if(rHomeFile != null)
                    {
                        // take care of library path
                        File installationJavaLibFile =
                            selectedInstallation.getLibraryDirectory();
                        if(installationJavaLibFile != null)
                        {
                            // put it all together and launch the VM
                            VirtualMachineSettings vmSettings =
                                new VirtualMachineSettings();
                            vmSettings.getSystemProperties().putAll(
                                    this.getLaunchSystemProperties());
                            vmSettings.setMainClassName(
                                    this.getApplicationMainClass().getName());
                            vmSettings.setClasspath(
                                    this.getClasspath());
                            
                            Long javaMaxMemoryMegabytes = this.getJavaMemoryLimitMegabytes();
                            if(javaMaxMemoryMegabytes != null)
                            {
                                vmSettings.setUseDefaultMaxMemory(false);
                                vmSettings.setMaxMemoryMegabytes(
                                        javaMaxMemoryMegabytes.longValue());
                            }
                            PlatformSpecificRFunctionsFactory platformSpecificFactory =
                                PlatformSpecificRFunctionsFactory.getInstance();
                            PlatformSpecificRFunctions platformSpecific =
                                platformSpecificFactory.getPlatformSpecificRFunctions();
                            platformSpecific.updateSettingsForRInstallation(
                                    vmSettings,
                                    selectedInstallation);
                            VirtualMachineLauncher launcher =
                                new CommandLineVirtualMachineLauncher();
                            try
                            {
                                launcher.launchVirtualMachine(vmSettings);
                            }
                            catch(VirtualMachineException ex)
                            {
                                final String errorMessage =
                                    "Caught exception while attempting to " +
                                    "launch application.";
                                LOG.log(Level.SEVERE,
                                        errorMessage,
                                        ex);
                                RLauncher.showLaunchErrorNotification(errorMessage);
                            }
                        }
                        else
                        {
                            final String errorMessage =
                                "Error detected during application launch " +
                                "(null java.library.path in configuration). " +
                                "Exiting application.";
                            LOG.severe(errorMessage);
                            RLauncher.showLaunchErrorNotification(errorMessage);
                        }
                    }
                    else
                    {
                        final String errorMessage =
                            "Error detected during application launch " +
                            "(null R_HOME in configuration). Exiting application.";
                        LOG.severe(errorMessage);
                        RLauncher.showLaunchErrorNotification(errorMessage);
                    }
                }
            }
            break;

            default:
            {
                final String errorMessage =
                    "Error detected during application launch. " +
                    "Exiting application.";
                LOG.severe(errorMessage);
                RLauncher.showLaunchErrorNotification(errorMessage);
            }
            break;
        }
    }

    /**
     * Asks the user to select an R launch configuration
     * @param installDirStructure
     *          the R directory structure to use
     * @return
     *          the user selection or null if they cancel
     */
    private RLaunchConfiguration getLaunchConfigurationFromUser(
            PlatformSpecificRFunctions installDirStructure)
    {
        final RHomeSelectorFrame rHomeSelectorFrame = new RHomeSelectorFrame(
                installDirStructure);
        SwingUtilities.invokeLater(new Runnable()
        {
            public void run()
            {
                rHomeSelectorFrame.pack();
                rHomeSelectorFrame.setVisible(true);
            }
        });
        
        return rHomeSelectorFrame.getSelectedLaunchConfiguration();
    }

    /**
     * Convenience function for showing a launch error message dialog
     * @param errorMessage
     *          the message
     */
    protected static void showLaunchErrorNotification(
            final String errorMessage)
    {
        RLauncher.showErrorNotification(
                errorMessage,
                "Launch Error Detected");
    }
    
    /**
     * Convenience function for showing an error dialog
     * @param errorMessage
     *          the message
     * @param title
     *          the dialogs title
     */
    protected static void showErrorNotification(
            final String errorMessage,
            final String title)
    {
        SwingUtilities.invokeLater(new Runnable()
        {
            public void run()
            {
                JOptionPane.showMessageDialog(
                        null,
                        TextWrapper.wrapText(
                                errorMessage,
                                TextWrapper.DEFAULT_DIALOG_COLUMN_COUNT),
                        title,
                        JOptionPane.ERROR_MESSAGE);
            }
        });
    }
    
    /**
     * Launch the application, returning when the application completes.
     */
    public void launchApplication()
    {
        // get a hold of the application configuration
        RApplicationConfigurationManager configurationManager =
            this.getApplicationConfigurationManager();
        configurationManager.setSaveOnExit(false);
        RApplicationConfiguration jaxbRApplicationConfiguration =
            configurationManager.getApplicationConfiguration();
        RConfigurationType jaxbRConfiguration =
            jaxbRApplicationConfiguration.getRConfiguration();
        RLaunchConfigurationType jaxbRLaunchConfiguration =
            jaxbRConfiguration.getRLaunchConfiguration();
        RApplicationStateType applicationState =
            configurationManager.getApplicationState();
        
        PlatformSpecificRFunctions rInstallDirStructure =
            PlatformSpecificRFunctionsFactory.getInstance().getPlatformSpecificRFunctions();
        if(rInstallDirStructure == null)
        {
            final String errorMessage =
                "Your current operating system is not supported by " +
                this.getReadableApplicationName() + ". " +
                "Please see the " + this.getReadableApplicationName() +
                " home page for more information";
            LOG.severe(errorMessage);
            RLauncher.showErrorNotification(
                    errorMessage,
                    "Unsupported Operating System");
        }
        else
        {
            RInstallationScanner rScanner = new RInstallationScanner();
            RInstallation[] detectedInstallations =
                rScanner.scanForRInstallations(rInstallDirStructure);
            Arrays.sort(detectedInstallations);
            
            // see if the detected installations have changed since our
            // last startup
            RInstallation[] prevDetectedInstallations =
                RApplicationConfigurationManager.fromJaxbToNativeRInstallations(
                        applicationState.getDetectedRInstallation());
            boolean detectedRInstallsHaveChanged = !Arrays.equals(
                    detectedInstallations,
                    prevDetectedInstallations);
            if(detectedRInstallsHaveChanged)
            {
                // push the changes into the configuration file so it doesn't
                // show up as a change again when the user restarts
                List<RInstallationType> detectedInstallationsConfig =
                    applicationState.getDetectedRInstallation();
                List<RInstallationType> newDetectedInstallationsConfig =
                    RApplicationConfigurationManager.fromNativeToJaxbRInstallations(
                            detectedInstallations);
                detectedInstallationsConfig.clear();
                detectedInstallationsConfig.addAll(
                        newDetectedInstallationsConfig);
                configurationManager.saveApplicationConfiguration();
            }
            
            // scan for r directories
            if(jaxbRLaunchConfiguration != null)
            {
                RInstallationType selectedInstallation =
                    jaxbRLaunchConfiguration.getSelectedRInstallation();
                boolean selectedInstallationExists =
                    new File(selectedInstallation.getLibraryDirectory()).exists();
                
                if(!selectedInstallationExists)
                {
                    try
                    {
                        SafeAWTInvoker.safeInvokeAndWait(new Runnable()
                        {
                            public void run()
                            {
                                String message =
                                    "The selected R installation has been " +
                                    "removed. Please select a new installation " +
                                    "or cancel launch";
                                JOptionPane.showMessageDialog(
                                        null,
                                        TextWrapper.wrapText(
                                                message,
                                                TextWrapper.DEFAULT_DIALOG_COLUMN_COUNT),
                                        "Missing R Installation",
                                        JOptionPane.WARNING_MESSAGE);
                                LOG.fine("user warning: " + message);
                            }
                        });
                    }
                    catch(Exception ex)
                    {
                        LOG.log(Level.SEVERE,
                                "received exception trying to display a " +
                                "message to the user",
                                ex);
                    }
                    
                    RLaunchConfiguration launchConfiguration =
                        this.getLaunchConfigurationFromUser(rInstallDirStructure);
                    this.saveNewConfigurationAndLaunch(launchConfiguration);
                }
                else if(detectedRInstallsHaveChanged)
                {
                    boolean userWantsToSelectRInstallation =
                        this.determineIfUserWantsToSelectRInstallation();
                    if(userWantsToSelectRInstallation)
                    {
                        if(LOG.isLoggable(Level.FINE))
                        {
                            LOG.fine("user wants to select R installation");
                        }
                        RLaunchConfiguration launchConfiguration =
                            this.getLaunchConfigurationFromUser(
                                    rInstallDirStructure);
                        this.saveNewConfigurationAndLaunch(
                                launchConfiguration);
                    }
                    else
                    {
                        if(LOG.isLoggable(Level.FINE))
                        {
                            LOG.fine(
                                    "user doesn't want to select an R " +
                                    "installation even though the install " +
                                    "dir changed");
                        }
                        this.launchWithJaxbConfiguration(
                                jaxbRLaunchConfiguration);
                    }
                }
                else
                {
                    if(LOG.isLoggable(Level.FINE))
                    {
                        LOG.fine("R installations have not changed");
                    }
                    this.launchWithJaxbConfiguration(
                            jaxbRLaunchConfiguration);
                }
            }
            else
            {
                if(LOG.isLoggable(Level.FINE))
                {
                    LOG.fine(
                            "no current R launch configuration exists. " +
                            "prompting user");
                }
                RLaunchConfiguration launchConfiguration =
                    this.getLaunchConfigurationFromUser(rInstallDirStructure);
                this.saveNewConfigurationAndLaunch(launchConfiguration);
            }
        }
    }
    
    /**
     * save the given configuration and launch with it
     * @param rLaunchConfiguration
     *          the launch configuration
     */
    private void saveNewConfigurationAndLaunch(
            RLaunchConfiguration rLaunchConfiguration)
    {
        if(rLaunchConfiguration != null)
        {
            // save away launch configuration to configuration
            RApplicationConfigurationManager applicationConfigurationManager =
                this.getApplicationConfigurationManager();
            RApplicationConfiguration jaxbApplicationConfiguration =
                this.getApplicationConfigurationManager().getApplicationConfiguration();
            RLaunchConfigurationType jaxbRLaunchConfiguration =
                RApplicationConfigurationManager.fromNativeToJaxbRLaunchConfiguration(
                        rLaunchConfiguration);
            jaxbApplicationConfiguration.getRConfiguration().setRLaunchConfiguration(
                    jaxbRLaunchConfiguration);
            applicationConfigurationManager.saveApplicationConfiguration();
            applicationConfigurationManager.saveApplicationState();
            
            // launch
            this.launchWithConfiguration(rLaunchConfiguration);
        }
        else
        {
            if(LOG.isLoggable(Level.FINE))
            {
                LOG.fine(
                        "user canceled launch configuration. " +
                        "exiting...");
            }
        }
    }
    
    /**
     * Launch using the given jaxb launch configuration.
     * @param jaxbLaunchConfiguration
     *          the jaxb launch configuration
     */
    private void launchWithJaxbConfiguration(RLaunchConfigurationType jaxbLaunchConfiguration)
    {
        RLaunchConfiguration launchConfiguration =
            RApplicationConfigurationManager.fromJaxbToNativeRLaunchConfiguration(
                    jaxbLaunchConfiguration);
        
        if(launchConfiguration != null)
        {
            this.launchWithConfiguration(launchConfiguration);
        }
        else
        {
            final String errorMessage =
                "Fatal error detected in " + this.getReadableApplicationName() +
                " launch configuration";
            LOG.severe(errorMessage);
            RLauncher.showErrorNotification(
                    errorMessage,
                    "Fatal Configuration Error");
        }
    }
}
