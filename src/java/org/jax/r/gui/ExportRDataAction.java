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

package org.jax.r.gui;

import java.awt.event.ActionEvent;
import java.io.File;
import java.util.logging.Logger;

import javax.swing.AbstractAction;
import javax.swing.JFileChooser;
import javax.swing.JFrame;

import org.jax.r.RCommandParameter;
import org.jax.r.RMethodInvocationCommand;
import org.jax.r.RUtilities;
import org.jax.r.configuration.RApplicationConfigurationManager;
import org.jax.r.jaxbgenerated.FileType;
import org.jax.r.jaxbgenerated.ObjectFactory;
import org.jax.r.jaxbgenerated.RApplicationStateType;
import org.jax.r.jriutilities.RInterface;
import org.jax.r.jriutilities.RInterfaceFactory;
import org.jax.r.jriutilities.SilentRCommand;
import org.jax.r.project.RProject;
import org.jax.util.gui.MessageDialogUtilities;
import org.jax.util.project.ProjectManager;

/**
 * An action for exporting R Data
 * @author <A HREF="mailto:keith.sheppard@jax.org">Keith Sheppard</A>
 */
public abstract class ExportRDataAction extends AbstractAction
{
    /**
     * every {@link java.io.Serializable} is supposed to have one of these
     */
    private static final long serialVersionUID = 2910369120340154567L;
    
    /**
     * our logger
     */
    private static final Logger LOG = Logger.getLogger(
            ExportRDataAction.class.getName());
    
    /**
     * Constructor
     */
    public ExportRDataAction()
    {
        super("Export R Data ...");
    }
    
    /**
     * Getter for the project manager
     * @return the project manager
     */
    protected abstract ProjectManager getProjectManager();
    
    /**
     * Getter for the application manager
     * @return the application manager
     */
    protected abstract RApplicationConfigurationManager getApplicationConfigurationManager();
    
    /**
     * Getter for the parent frame to use for creating dialogs etc
     * @return the parent frame
     */
    protected abstract JFrame getParentFrame();
    
    /**
     * {@inheritDoc}
     */
    public void actionPerformed(ActionEvent e)
    {
        // use the remembered starting dir
        RApplicationStateType applicationState =
            this.getApplicationConfigurationManager().getApplicationState();
        FileType rememberedJaxbRExportDir =
            applicationState.getRecentRScriptExportDirectory();
        File rememberedExportDir = null;
        if(rememberedJaxbRExportDir != null && rememberedJaxbRExportDir.getFileName() != null)
        {
            rememberedExportDir = new File(rememberedJaxbRExportDir.getFileName());
        }
        
        // TODO pick a starting file based on the project name
        // TODO need some kind of validation here
        RProject activeProject =
            (RProject)this.getProjectManager().getActiveProject();
        
        // select the R file to save
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogType(JFileChooser.SAVE_DIALOG);
        fileChooser.setApproveButtonText("Export");
        fileChooser.setApproveButtonMnemonic('e');
        fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
        fileChooser.setDialogTitle("Export R Data");
        fileChooser.setMultiSelectionEnabled(false);
        fileChooser.addChoosableFileFilter(
                RDataFileFilter.getInstance());
        fileChooser.setFileFilter(
                RDataFileFilter.getInstance());
        
        String activeProjectName = activeProject.getName();
        if(activeProjectName != null)
        {
            File startingFile = new File(
                    rememberedExportDir,
                    activeProject.getName() + "." + RDataFileFilter.R_DATA_EXTENSION);
            fileChooser.setSelectedFile(startingFile);
        }
        
        int response = fileChooser.showSaveDialog(this.getParentFrame());
        if(response == JFileChooser.APPROVE_OPTION)
        {
            File selectedFile = fileChooser.getSelectedFile();
            
            // tack on the extension if there isn't one already
            if(!selectedFile.toString().toLowerCase().endsWith(
               RDataFileFilter.getInstance().getExtensionWithDot().toLowerCase()))
            {
                String newFileName =
                    selectedFile.getName() + "." +
                    RDataFileFilter.R_DATA_EXTENSION;
                selectedFile =
                    new File(selectedFile.getParentFile(), newFileName);
            }
            
            boolean okToSaveFile = true;
            if(selectedFile.exists())
            {
                // ask the user if they're sure they want to overwrite
                okToSaveFile = MessageDialogUtilities.confirmOverwrite(
                        this.getParentFrame(),
                        selectedFile);
            }
            
            if(okToSaveFile)
            {
                try
                {
                    this.exportRDataToFile(selectedFile);
                    
                    File parentDir = selectedFile.getParentFile();
                    if(parentDir != null)
                    {
                        // update the "recent R export directory"
                        ObjectFactory objectFactory = new ObjectFactory();
                        FileType latestJaxbRExportDir = objectFactory.createFileType();
                        latestJaxbRExportDir.setFileName(
                                parentDir.getAbsolutePath());
                        applicationState.setRecentRScriptExportDirectory(
                                latestJaxbRExportDir);
                    }
                }
                catch(Exception ex)
                {
                    // there was a problem... tell the user
                    String message =
                        "Failed to export R Data to file: " +
                        selectedFile.getAbsolutePath();
                    LOG.info(message);
                    MessageDialogUtilities.error(
                            this.getParentFrame(),
                            message,
                            "Error Exporting R Data");
                }
            }
        }
    }
    
    /**
     * Execute the export command
     * @param file
     *          the file that we should export to
     */
    private void exportRDataToFile(File file)
    {
        RMethodInvocationCommand saveImageMethod = new RMethodInvocationCommand(
                "save.image",
                new RCommandParameter(
                        "file",
                        RUtilities.javaStringToRString(file.getAbsolutePath())));
        RInterface rInterface = RInterfaceFactory.getRInterfaceInstance();
        rInterface.evaluateCommandNoReturn(new SilentRCommand(saveImageMethod));
    }
}
