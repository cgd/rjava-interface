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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.jax.r.RCommand;
import org.jax.r.jaxbgenerated.ItemTypeType;
import org.jax.r.jaxbgenerated.RHistoryItemType;
import org.jax.r.jaxbgenerated.RProjectMetadataType;
import org.jax.r.jriutilities.RInterface;
import org.jax.r.jriutilities.RInterfaceListener;
import org.jax.r.jriutilities.SilentRCommand;
import org.jax.util.project.Project;
import org.rosuda.JRI.REXP;

/**
 * Base class for R projects
 * @author <A HREF="mailto:keith.sheppard@jax.org">Keith Sheppard</A>
 */
public abstract class RProject extends Project
{
    /**
     * our JAXB object factory
     */
    private final org.jax.r.jaxbgenerated.ObjectFactory objectFactory =
        new org.jax.r.jaxbgenerated.ObjectFactory();
    
    /**
     * the history for this project
     */
    private final List<RHistoryItemType> rHistory;
    
    /**
     * the R interface
     */
    private final RInterface rInterface;
    
    /**
     * for recording the R history when we save the project
     */
    private final RInterfaceListener historyRecorder = new RInterfaceListener()
    {
        /**
         * {@inheritDoc}
         */
        public void pendingCommandCountChanged(int updatedCommandCount)
        {
            // don't care
        }
        
        /**
         * {@inheritDoc}
         */
        public void completedCommandProcessing(
                RInterface eventSource,
                RCommand command,
                REXP result)
        {
            // don't care
        }

        /**
         * {@inheritDoc}
         */
        public void initiatedCommandProcessing(
                RInterface eventSource,
                RCommand command)
        {
            if(!(command instanceof SilentRCommand))
            {
                RHistoryItemType historyItem =
                    RProject.this.objectFactory.createRHistoryItemType();
                historyItem.setContent(command.getCommandText());
                historyItem.setItemType(ItemTypeType.COMMAND);
                RProject.this.rHistory.add(historyItem);
            }
        }

        /**
         * {@inheritDoc}
         */
        public void receivedComment(
                String comment)
        {
            RHistoryItemType historyItem =
                RProject.this.objectFactory.createRHistoryItemType();
            historyItem.setContent(comment);
            historyItem.setItemType(ItemTypeType.COMMENT);
            RProject.this.rHistory.add(historyItem);
        }

        /**
         * {@inheritDoc}
         */
        public void receivedMessageFromR(
                RInterface eventSource,
                String message,
                RCommand activeCommand)
        {
            // TODO deal with R messages
        }

        /**
         * {@inheritDoc}
         */
        public void receivedOutputFromR(
                RInterface eventSource,
                String output,
                RCommand activeCommand)
        {
            if(!(activeCommand instanceof SilentRCommand))
            {
                RHistoryItemType historyItem =
                    RProject.this.objectFactory.createRHistoryItemType();
                historyItem.setContent(output);
                historyItem.setItemType(ItemTypeType.R_OUTPUT);
                RProject.this.rHistory.add(historyItem);
            }
        }
    };

    /**
     * Constructor
     * @param rInterface
     *          the R Interface to use
     * @param projectMetadata
     *          the project metadata
     */
    public RProject(RInterface rInterface, RProjectMetadataType projectMetadata)
    {
        super(projectMetadata.getProjectName());
        
        this.rHistory = Collections.synchronizedList(
                new ArrayList<RHistoryItemType>(
                        projectMetadata.getRHistoryItem()));
        this.rInterface = rInterface;
        this.rInterface.addRInterfaceListener(this.historyRecorder);
    }

    /**
     * Constructor
     * @param rInterface
     *          the R Interface to use
     */
    public RProject(RInterface rInterface)
    {
        super(null);
        
        this.rHistory = Collections.synchronizedList(
                new ArrayList<RHistoryItemType>());
        this.rInterface = rInterface;
        this.rInterface.addRInterfaceListener(this.historyRecorder);
    }
    
    /**
     * detach the project from the R interface
     */
    public void detatchProject()
    {
        this.rInterface.removeRInterfaceListener(this.historyRecorder);
    }
    
    /**
     * Gets the R history which includes all commands that were evaluated,
     * and the output that was produced from the commands.
     * @return
     *          the rHistory (returns a copy for thread safety and
     *          data integrity)
     */
    public List<RHistoryItemType> getRHistory()
    {
        // make a copy for thread safety
        final List<RHistoryItemType> copyOfRHistory;
        synchronized(this.rHistory)
        {
            copyOfRHistory = new ArrayList<RHistoryItemType>(
                    this.rHistory);
        }
        
        return copyOfRHistory;
    }

    /**
     * Get the metadata for this project
     * @return
     *          the metadata
     */
    public abstract RProjectMetadataType getMetadata();
}
