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

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.text.BadLocationException;
import javax.swing.text.StyledDocument;

import org.jax.r.gui.RDocumentStyle;
import org.jax.r.jaxbgenerated.RHistoryItemType;
import org.jax.r.jriutilities.DocumentWritingRInterfaceListener;
import org.jax.util.project.ProjectManager;

/**
 * This is a {@link DocumentWritingRInterfaceListener} that listens for new
 * projects being loaded so that it can restore the new project's console
 * output.
 * @author <A HREF="mailto:keith.sheppard@jax.org">Keith Sheppard</A>
 */
public class ProjectAwareDocumentWritingRInterfaceListener
extends
        DocumentWritingRInterfaceListener
{
    /**
     * our logger
     */
    private static final Logger LOG = Logger.getLogger(
            ProjectAwareDocumentWritingRInterfaceListener.class.getName());
    
    /**
     * The listener that pays attention to the active project
     */
    private final PropertyChangeListener activeProjectPropertyListener =
        new PropertyChangeListener()
        {
            /**
             * {@inheritDoc}
             */
            public void propertyChange(PropertyChangeEvent evt)
            {
                if(evt.getPropertyName().equals(
                        ProjectManager.ACTIVE_PROJECT_PROPERTY_NAME))
                {
                    ProjectAwareDocumentWritingRInterfaceListener.this.activeProjectChanged(
                            (RProject)evt.getNewValue());
                }
            }
        };
    
    /**
     * Constructor
     * @param attachedDocument
     *          the document to write to
     * @param projectManager
     *          the project manager
     */
    public ProjectAwareDocumentWritingRInterfaceListener(
            StyledDocument attachedDocument,
            ProjectManager projectManager)
    {
        super(attachedDocument);
        
        // register with the project manager since we want to be updated if
        // the active project changes
        projectManager.addPropertyChangeListener(
                ProjectManager.ACTIVE_PROJECT_PROPERTY_NAME,
                this.activeProjectPropertyListener);
    }
    
    /**
     * Respond to a change in the active project
     * @param newActiveProject
     *          the new active project
     */
    private void activeProjectChanged(RProject newActiveProject)
    {
        // 1st clear out the document contents
        this.clearHistory();
        
        // now play back the command history
        List<RHistoryItemType> commandHistory = newActiveProject.getRHistory();
        synchronized(commandHistory)
        {
            for(RHistoryItemType currHistoryItem: commandHistory)
            {
                switch(currHistoryItem.getItemType())
                {
                    case COMMAND:
                    {
                        this.appendPromptToDocument();
                        this.appendToAttachedDocument(
                                currHistoryItem.getContent() + "\n",
                                RDocumentStyle.CALLER_COMMAND_STYLE);
                    }
                    break;

                    case COMMENT:
                    {
                        this.appendPromptToDocument();
                        this.appendToAttachedDocument(
                                currHistoryItem.getContent(),
                                RDocumentStyle.CALLER_COMMENT_STYLE);
                    }
                    break;

                    case R_OUTPUT:
                    {
                        this.appendToAttachedDocument(
                                currHistoryItem.getContent(),
                                RDocumentStyle.R_OUTPUT_STYLE);
                    }
                    break;

                    default:
                    {
                        LOG.warning(
                                "don't know how to deal with an R history " +
                                "item of type: " +
                                currHistoryItem.getItemType().name());
                    }
                    break;
                }
            }
            this.appendPromptToDocument();
        }
    }

    /**
     * Get rid of all of the old text
     */
    private void clearHistory()
    {
        try
        {
            this.attachedDocument.remove(
                    0,
                    this.attachedDocument.getLength());
        }
        catch(BadLocationException ex)
        {
            LOG.log(Level.SEVERE,
                    "caught exception clearing history",
                    ex);
        }
    }
}
