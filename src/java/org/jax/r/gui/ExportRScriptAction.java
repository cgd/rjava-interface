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
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.AbstractAction;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JOptionPane;

import org.jax.r.configuration.RApplicationConfigurationManager;
import org.jax.r.jaxbgenerated.FileType;
import org.jax.r.jaxbgenerated.ObjectFactory;
import org.jax.r.jaxbgenerated.RApplicationStateType;
import org.jax.r.project.RCommandExporter;
import org.jax.r.project.RProject;
import org.jax.util.TextWrapper;
import org.jax.util.project.ProjectManager;

/**
 * Triggering this action causes an R script to get exported from this project
 * to a *.R file
 * @author <A HREF="mailto:keith.sheppard@jax.org">Keith Sheppard</A>
 */
public abstract class ExportRScriptAction extends AbstractAction
{
    /**
     * every {@link java.io.Serializable} is supposed to have one of these
     */
    private static final long serialVersionUID = -4184617493548704742L;
    
    /**
     * our logger
     */
    private static final Logger LOG = Logger.getLogger(
            ExportRScriptAction.class.getName());
    
    private final RCommandExporter rCommandExporter;
    
    /**
     * Constructor
     */
    public ExportRScriptAction()
    {
        super("Export R Commands to Script ...");
        
        this.rCommandExporter = new RCommandExporter();
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
        FileType rememberedJaxbRScriptDir =
            applicationState.getRecentRScriptExportDirectory();
        File rememberedScriptExportDir = null;
        if(rememberedJaxbRScriptDir != null && rememberedJaxbRScriptDir.getFileName() != null)
        {
            rememberedScriptExportDir = new File(rememberedJaxbRScriptDir.getFileName());
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
        fileChooser.setDialogTitle("Export R Commands to Script");
        fileChooser.setMultiSelectionEnabled(false);
        fileChooser.addChoosableFileFilter(
                RScriptFileFilter.getInstance());
        fileChooser.setFileFilter(
                RScriptFileFilter.getInstance());
        
        String activeProjectName = activeProject.getName();
        if(activeProjectName != null)
        {
            File startingFile = new File(
                    rememberedScriptExportDir,
                    activeProject.getName() + "." + RScriptFileFilter.R_SCRIPT_EXTENSION);
            fileChooser.setSelectedFile(startingFile);
        }
        
        int response = fileChooser.showSaveDialog(this.getParentFrame());
        if(response == JFileChooser.APPROVE_OPTION)
        {
            File selectedFile = fileChooser.getSelectedFile();
            
            // tack on the extension if there isn't one already
            if(!selectedFile.toString().toLowerCase().endsWith(
               RScriptFileFilter.getInstance().getExtensionWithDot().toLowerCase()))
            {
                String newFileName =
                    selectedFile.getName() + "." +
                    RScriptFileFilter.R_SCRIPT_EXTENSION;
                selectedFile =
                    new File(selectedFile.getParentFile(), newFileName);
            }
            
            boolean okToSaveFile = true;
            if(selectedFile.exists())
            {
                // ask the user if they're sure they want to overwrite
                String message =
                    "Exporting the R script to " +
                    selectedFile.getAbsolutePath() + " will overwrite an " +
                    " existing file. Would you like to continue anyway?";
                if(LOG.isLoggable(Level.FINE))
                {
                    LOG.fine(message);
                }
                
                int overwriteResponse = JOptionPane.showConfirmDialog(
                        this.getParentFrame(),
                        TextWrapper.wrapText(
                                message,
                                TextWrapper.DEFAULT_DIALOG_COLUMN_COUNT),
                        "Overwriting Existing File",
                        JOptionPane.OK_CANCEL_OPTION,
                        JOptionPane.QUESTION_MESSAGE);
                if(overwriteResponse != JOptionPane.OK_OPTION)
                {
                    if(LOG.isLoggable(Level.FINE))
                    {
                        LOG.fine("overwrite canceled");
                    }
                    okToSaveFile = false;
                }
            }
            
            if(okToSaveFile)
            {
                try
                {
                    selectedFile.createNewFile();
                    OutputStream os = new BufferedOutputStream(
                            new FileOutputStream(selectedFile));
                    this.rCommandExporter.exportCommandHistoryToStream(
                            activeProject,
                            os);
                    
                    File parentDir = selectedFile.getParentFile();
                    if(parentDir != null)
                    {
                        // update the "recent R script directory"
                        ObjectFactory objectFactory = new ObjectFactory();
                        FileType latestJaxbRScriptDir = objectFactory.createFileType();
                        latestJaxbRScriptDir.setFileName(
                                parentDir.getAbsolutePath());
                        applicationState.setRecentRScriptExportDirectory(
                                latestJaxbRScriptDir);
                    }
                }
                catch(Exception ex)
                {
                    // there was a problem... tell the user
                    String message =
                        "Failed to export command history to file: " +
                        selectedFile.getAbsolutePath();
                    LOG.info(message);
                    
                    JOptionPane.showMessageDialog(
                            this.getParentFrame(),
                            TextWrapper.wrapText(
                                    message,
                                    TextWrapper.DEFAULT_DIALOG_COLUMN_COUNT),
                                    "Error Exporting Command History",
                                    JOptionPane.ERROR_MESSAGE);
                }
            }
        }
    }
}
