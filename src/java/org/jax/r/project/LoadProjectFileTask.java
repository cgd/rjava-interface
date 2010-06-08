/*
 * Copyright (c) 2008 The Jackson Laboratory
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

import java.awt.Frame;
import java.io.File;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

import org.jax.util.TextWrapper;
import org.jax.util.concurrent.SimpleLongRunningTask;
import org.jax.util.project.ProjectManager;

/**
 * @author <A HREF="mailto:keith.sheppard@jax.org">Keith Sheppard</A>
 */
public class LoadProjectFileTask
extends SimpleLongRunningTask
implements Runnable
{
    /**
     * our logger
     */
    private static final Logger LOG = Logger.getLogger(
            LoadProjectFileTask.class.getName());
    
    private final File file;
    private final ProjectManager projectManager;
    private final Frame parentFrame;
    private final String projectTypeName;
    
    /**
     * create a long running task for loading a project file
     * @param file
     *          the project file
     * @param projectManager
     *          the project manager
     * @param parentFrame
     *          the parent frame to use for any needed popup dialogs
     * @param projectTypeName
     *          the name for the type of project (eg. "J/maanova")
     */
    public LoadProjectFileTask(
            File file,
            ProjectManager projectManager,
            Frame parentFrame,
            String projectTypeName)
    {
        this.file = file;
        this.projectManager = projectManager;
        this.parentFrame = parentFrame;
        this.projectTypeName = projectTypeName;
    }

    /**
     * {@inheritDoc}
     */
    public void run()
    {
        try
        {
            if(!this.projectManager.loadActiveProject(this.file))
            {
                SwingUtilities.invokeLater(new Runnable()
                {
                    /**
                     * {@inheritDoc}
                     */
                    public void run()
                    {
                        try
                        {
                            LoadProjectFileTask.this.notifyError();
                        }
                        catch(Exception ex)
                        {
                            LOG.log(Level.SEVERE,
                                    "Error loading project",
                                    ex);
                        }
                    }
                });
            }
            
            this.setWorkUnitsCompleted(1);
        }
        catch(Exception ex)
        {
            LOG.log(Level.SEVERE,
                    "Error loading project",
                    ex);
        }
    }

    private void notifyError()
    {
        // there was a problem... tell the user
        String message =
            "Failed to load selected " +
            LoadProjectFileTask.this.projectTypeName +
            " project file: " +
            LoadProjectFileTask.this.file.getAbsolutePath();
        LOG.info(message);
        
        JOptionPane.showMessageDialog(
                LoadProjectFileTask.this.parentFrame,
                TextWrapper.wrapText(
                        message,
                        TextWrapper.DEFAULT_DIALOG_COLUMN_COUNT),
                "Error Loading Project",
                JOptionPane.ERROR_MESSAGE);
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public String getTaskName()
    {
        return "Loading Project File";
    }
}
