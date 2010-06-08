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

import java.util.concurrent.Future;

import org.jax.r.RCommand;
import org.jax.r.RException;
import org.jax.util.concurrent.LongRunningTask;
import org.rosuda.JRI.REXP;

/**
 * This interface provides simplified access to JRI. Any implementation
 * of these methods should be thread safe, and guarantee
 * that all commands/comments are executed in the order that they're requested.
 * If a caller wants to guarantee that no thread can insert any input
 * (commands or comments) between a block of calls, that caller can
 * synchronize on the R Interface that they are sending input to.
 * @author <A HREF="mailto:keith.sheppard@jax.org">Keith Sheppard</A>
 */
public interface RInterface extends LongRunningTask
{
    /**
     * Flush any pending commands from the command queue
     */
    public void flushCommands();
    
    /**
     * Evaluate the command string (with a newline appended) and return the
     * result.
     * @param command
     *          the command to evaluate
     * @return
     *          the result of the command
     * @throws RException
     *          if anything goes wrong
     */
    public REXP evaluateCommand(String command) throws RException;

    /**
     * Evaluate the command and return the result.
     * @param command
     *          the command to evaluate
     * @return
     *          the result of the command
     * @throws RException
     *          if anything goes wrong
     */
    public REXP evaluateCommand(RCommand command) throws RException;

    /**
     * Evaluate the command string (with a newline appended) and return the
     * future result.
     * @param command
     *          the command to evaluate
     * @return
     *          the future result
     * @throws RException
     *          if anything goes wrong
     */
    public Future<REXP> evaluateCommandAsynchronous(String command) throws RException;
    
    /**
     * Evaluate the command and return the future result.
     * @param command
     *          the command to evaluate
     * @return
     *          the future result
     * @throws RException
     *          if anything goes wrong
     */
    public Future<REXP> evaluateCommandAsynchronous(RCommand command) throws RException;
    
    /**
     * Evaluate the given command string (with a newline appended), but don't
     * bother returning the result of the evaluation.
     * @param command
     *          the command to evaluate
     * @throws RException
     *          if anything goes wrong
     */
    public void evaluateCommandNoReturn(String command) throws RException;
    
    /**
     * Evaluate the given command, but don't bother returning the result
     * of the evaluation.
     * @param command
     *          the command to evaluate
     * @throws RException
     *          if anything goes wrong
     */
    public void evaluateCommandNoReturn(RCommand command) throws RException;
    
    /**
     * Insert a comment line. A '#' is prepended to this comment and a
     * newline is appended to the end.
     * @param comment
     *          the comment
     * @throws RException
     *          if anything goes wrong
     */
    public void insertComment(String comment) throws RException;
    
    /**
     * Insert a comment with the assumption that it's already escaped (No
     * '#' is prepended and no newline is appended)
     * @param verbatimComment
     *          the comment
     * @throws RException
     *          if anything goes wrong
     */
    public void insertCommentVerbatim(String verbatimComment) throws RException;
    
    /**
     * For determining if this interface has work to do.
     * @return
     *          true if there are any commands pending
     */
    public boolean isAnyCommandPending();
    
    /**
     * Add the given listener to our listener list
     * @param listenerToAdd
     *          the listener that we're adding
     */
    public void addRInterfaceListener(RInterfaceListener listenerToAdd);
    
    /**
     * Remove the given listener from our listener list
     * @param listenerToRemove
     *          the listener that we're removing
     */
    public void removeRInterfaceListener(RInterfaceListener listenerToRemove);
}
