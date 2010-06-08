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

package org.jax.r.jriutilities;

import java.util.EventListener;

import org.jax.r.RCommand;
import org.rosuda.JRI.REXP;

/**
 * This should be implemented by classes that want to hear
 * events from an R interface.
 * @author <A HREF="mailto:keith.sheppard@jax.org">Keith Sheppard</A>
 */
public interface RInterfaceListener extends EventListener
{
    /**
     * Receive a message indicating a problem from R
     * @param eventSource
     *          the source that triggered this event
     * @param message
     *          the message from R
     * @param activeCommand
     *          the command that was executing when we received the message
     *          (can be null)
     */
    public abstract void receivedMessageFromR(
            RInterface eventSource,
            String message,
            RCommand activeCommand);
    
    /**
     * Receive output from R. This is text that would normally show
     * up on the command line.
     * @param eventSource
     *          the source that triggered this event
     * @param output
     *          the output from R
     * @param activeCommand
     *          the command that was executing when we received the output
     *          (can be null)
     */
    public abstract void receivedOutputFromR(
            RInterface eventSource,
            String output,
            RCommand activeCommand);
    
    /**
     * Receive notification from R interface when it begins processing
     * a command.
     * @param eventSource
     *          the source that triggered this event
     * @param command
     *          the command that R interface has started to process
     */
    public abstract void initiatedCommandProcessing(
            RInterface eventSource,
            RCommand command);
    
    /**
     * Indicates that processing has completed on the given command.
     * @param eventSource
     *          the source that triggered this event
     * @param command
     *          the command that R interface has just finished with
     * @param result
     *          the result... can be null if either no return value
     *          was requested, or if something went wrong in processing
     */
    public abstract void completedCommandProcessing(
            RInterface eventSource,
            RCommand command,
            REXP result);

    /**
     * Indicates that a comment has been received
     * @param comment
     *          the comment
     */
    public abstract void receivedComment(String comment);
    
    /**
     * This function is called when the number of pending commands has changed
     * @param updatedCommandCount
     *          the updated count
     */
    public abstract void pendingCommandCountChanged(int updatedCommandCount);
}
