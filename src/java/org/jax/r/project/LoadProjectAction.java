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

package org.jax.r.project;

import java.awt.event.ActionEvent;
import java.io.File;

import javax.swing.AbstractAction;
import javax.swing.ImageIcon;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JOptionPane;

import org.jax.r.configuration.RApplicationConfigurationManager;
import org.jax.r.jaxbgenerated.FileType;
import org.jax.r.jaxbgenerated.RApplicationStateType;
import org.jax.util.TextWrapper;
import org.jax.util.concurrent.MultiTaskProgressPanel;
import org.jax.util.project.ProjectManager;

/**
 * Action for loading a project
 * @author <A HREF="mailto:keith.sheppard@jax.org">Keith Sheppard</A>
 */
public abstract class LoadProjectAction extends AbstractAction
{
    /**
     * every {@link java.io.Serializable} is supposed to have one of these
     */
    private static final long serialVersionUID = 1435828601585521887L;

    /**
     * the name the user sees
     */
    private static final String ACTION_NAME = "Open Project...";
    
    /**
     * the icon resource location
     */
    private static final String ICON_RESOURCE_LOCATION =
        "/images/action/open-project-16x16.png";
    
    /**
     * 
     */
    public LoadProjectAction()
    {
        super(ACTION_NAME,
              new ImageIcon(LoadProjectAction.class.getResource(
                          ICON_RESOURCE_LOCATION)));
    }
    
    /**
     * {@inheritDoc}
     */
    public void actionPerformed(ActionEvent e)
    {
        // prompt the user if they're about to lose unsaved changes
        final ProjectManager projectManager = this.getProjectManager();
        if(projectManager.isActiveProjectModified())
        {
            String message =
                "The current project contains unsaved modifications. Loading " +
                "a new project will cause these modifications to be lost. " +
                "Would you like to continue without saving?";
            int response = JOptionPane.showConfirmDialog(
                    this.getParentFrame(),
                    TextWrapper.wrapText(
                            message,
                            TextWrapper.DEFAULT_DIALOG_COLUMN_COUNT),
                    "Unsaved Project Modifications",
                    JOptionPane.OK_CANCEL_OPTION,
                    JOptionPane.QUESTION_MESSAGE);
            if(response == JOptionPane.CLOSED_OPTION || response == JOptionPane.CANCEL_OPTION)
            {
                return;
            }
        }
        
        // try to be smart about the file dialogs starting dir
        File startingDir = null;
        {
            RApplicationConfigurationManager configurationManager =
                this.getConfigurationManager();
            RApplicationStateType applicationState =
                configurationManager.getApplicationState();
            
            FileType[] recentProjects =
                    applicationState.getRecentProjectFile().toArray(
                            new FileType[0]);
            if(recentProjects.length > 0)
            {
                startingDir = new File(recentProjects[0].getFileName()).getParentFile();
            }
        }
        
        // slect the project file to load
        JFileChooser fileChooser = new JFileChooser(startingDir);
        fileChooser.addChoosableFileFilter(
                projectManager.getProjectFileFilter());
        fileChooser.setFileFilter(
                projectManager.getProjectFileFilter());
        fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
        fileChooser.setApproveButtonText(
                "Open " + this.getProjectTypeName() + " Project");
        fileChooser.setDialogTitle(
                "Open " + this.getProjectTypeName() + " Project");
        fileChooser.setMultiSelectionEnabled(false);
        int response = fileChooser.showOpenDialog(this.getParentFrame());
        if(response == JFileChooser.APPROVE_OPTION)
        {
            LoadProjectFileTask loadTask = new LoadProjectFileTask(
                    fileChooser.getSelectedFile(),
                    projectManager,
                    this.getParentFrame(),
                    this.getProjectTypeName());
            this.getTaskProgressPanel().addTaskToTrack(
                    loadTask,
                    true);
            Thread loadThread = new Thread(loadTask);
            loadThread.start();
        }
    }
    
    /**
     * Get a string representation of the type of project that we're talking
     * about. Eg. "J/qtl"
     * @return
     *          the project type name
     */
    protected abstract String getProjectTypeName();
    
    /**
     * Get the parent frame to use for any dialogs we show
     * @return
     *          the parent frame
     */
    protected abstract JFrame getParentFrame();
    
    /**
     * Get the project manager to use
     * @return
     *          the project manager
     */
    protected abstract ProjectManager getProjectManager();
    
    /**
     * Get the config manager to use
     * @return
     *          the config manager
     */
    protected abstract RApplicationConfigurationManager getConfigurationManager();
    
    /**
     * Get the progress panel to use
     * @return
     *          the progress panel
     */
    protected abstract MultiTaskProgressPanel getTaskProgressPanel();
}
