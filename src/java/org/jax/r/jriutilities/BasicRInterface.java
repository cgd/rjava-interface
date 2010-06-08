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

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.jax.r.RCommand;
import org.jax.r.RException;
import org.jax.r.SimpleRCommand;
import org.jax.util.concurrent.AbstractLongRunningTask;
import org.jax.util.concurrent.SettableFuture;
import org.rosuda.JRI.REXP;
import org.rosuda.JRI.RMainLoopCallbacks;
import org.rosuda.JRI.Rengine;

/**
 * A basic implementation of the R interface functionality that
 * we want. See {@link RInterface} for a more detailed description
 * of the requirements that this class fulfills.
 * @author <A HREF="mailto:keith.sheppard@jax.org">Keith Sheppard</A>
 */
public class BasicRInterface
extends AbstractLongRunningTask
implements RInterface
{
    /**
     * our logger
     */
    private static final Logger LOG = Logger.getLogger(BasicRInterface.class.getName());
    
    /**
     * a dummy command to flush the queue
     */
    private static final RCommand FLUSH_COMMAND = new SilentRCommand(
            "\"finished flushing R commands\"");
    
    /**
     * arguments that we should pass to the R enging
     */
    // TODO add to properties/config file when we have one
    private static final String[] R_ENGINE_ARGS = new String[] {"--save"};
    
    /**
     * the prefix that we append to comment strings
     */
    private static final String R_COMMENT_PREFIX = "# ";
    
    /**
     * the list of listeners
     */
    private final ConcurrentLinkedQueue<RInterfaceListener> listenerList;
    
    /**
     * Our R loop.
     */
    private final RMainLoopCallbacks rCallBacks;
    
    /**
     * the thread safe R command queue
     */
    private final BlockingQueue<AnyRInput> commandQueue;
    
    /**
     * holds the pending command count
     */
    private final AtomicInteger pendingCommandCounter;
    
    /**
     * Our interface to R.
     */
    @SuppressWarnings("unused")
    private Rengine rEngine;

    /**
     * flag that determines whether or not the R engine has been started yet
     */
    private volatile boolean rHasBeenStarted;
    
    /**
     * Constructor. I made this package protected because we need to get to this
     * from the factory method. We can't allow more than one instance to be
     * created... see {@link org.rosuda.JRI.Rengine} for explanation of why
     * we can't have 2 R engines.
     */
    /*package-protected*/ BasicRInterface()
    {
        this.rHasBeenStarted = false;
        this.commandQueue = new LinkedBlockingQueue<AnyRInput>();
        this.rCallBacks = new RMainLoopCallBacksImpl();
        this.listenerList = new ConcurrentLinkedQueue<RInterfaceListener>();
        this.pendingCommandCounter = new AtomicInteger(0);
    }
    
    /**
     * just delegate to the R engine. the whole reason
     * for this method is that it's not good practice to register
     * for callbacks in the constructor if there is a chance
     * they will get called before initialization completes.
     */
    private void startR()
    {
        this.rEngine = new Rengine(
                R_ENGINE_ARGS,
                true,
                this.rCallBacks);
    }
    
    /**
     * {@inheritDoc}
     */
    public void flushCommands()
    {
        this.evaluateCommand(FLUSH_COMMAND);
    }
    
    /**
     * {@inheritDoc}
     */
    public REXP evaluateCommand(String command) throws RException
    {
        return this.evaluateCommand(new SimpleRCommand(command));
    }

    /**
     * {@inheritDoc}
     */
    public Future<REXP> evaluateCommandAsynchronous(String command)
            throws RException
    {
        return this.evaluateCommandAsynchronous(new SimpleRCommand(command));
    }

    /**
     * {@inheritDoc}
     */
    public void evaluateCommandNoReturn(String command) throws RException
    {
        this.evaluateCommandNoReturn(new SimpleRCommand(command));
    }

    /**
     * {@inheritDoc}
     */
    public REXP evaluateCommand(RCommand command) throws RException
    {
        try
        {
            // do the lazy thing and delegate to the asynchronous method
            return this.evaluateCommandAsynchronous(command).get();
        }
        catch(Exception ex)
        {
            // pass the buck on this exception
            throw new RException(ex);
        }
    }

    /**
     * {@inheritDoc}
     */
    public synchronized Future<REXP> evaluateCommandAsynchronous(RCommand command) throws RException
    {
        if(!this.rHasBeenStarted)
        {
            this.rHasBeenStarted = true;
            this.startR();
        }
        
        // increment the pending command count
        this.pendingCommandCounter.incrementAndGet();
        this.fireChangeEvent();
        
        AnyRInput input = new AnyRInput(command, AnyRInput.InputType.COMMAND_NEEDS_RETURN);
        this.commandQueue.add(input);
        return input.getAssociatedResult();
    }

    /**
     * {@inheritDoc}
     */
    public synchronized void evaluateCommandNoReturn(RCommand command)
    {
        if(!this.rHasBeenStarted)
        {
            this.rHasBeenStarted = true;
            this.startR();
        }
        
        // increment the pending command count
        this.pendingCommandCounter.incrementAndGet();
        this.fireChangeEvent();
        
        AnyRInput input = new AnyRInput(command, AnyRInput.InputType.COMMAND_NO_RETURN);
        this.commandQueue.add(input);
    }

    /**
     * {@inheritDoc}
     */
    public void insertComment(String comment)
    {
        this.insertCommentVerbatim(R_COMMENT_PREFIX + comment + '\n');
    }

    /**
     * {@inheritDoc}
     */
    public synchronized void insertCommentVerbatim(String verbatimComment)
    {
        if(!this.rHasBeenStarted)
        {
            this.rHasBeenStarted = true;
            this.startR();
        }
        
        AnyRInput input = new AnyRInput(verbatimComment, AnyRInput.InputType.COMMENT);
        this.commandQueue.add(input);
    }

    /**
     * {@inheritDoc}
     */
    public boolean isAnyCommandPending()
    {
        // check the counter for any pending commands
        return this.pendingCommandCounter.get() > 0;
    }

    /**
     * {@inheritDoc}
     */
    public void addRInterfaceListener(RInterfaceListener listenerToAdd)
    {
        if(listenerToAdd == null)
        {
            throw new NullPointerException(
                    "can't have a null listener");
        }
        
        this.listenerList.add(listenerToAdd);
    }

    /**
     * {@inheritDoc}
     */
    public void removeRInterfaceListener(RInterfaceListener listenerToRemove)
    {
        this.listenerList.remove(listenerToRemove);
    }
    
    /**
     * Notify listeners that the pending command count has changed
     * @param updatedCommandCount
     *          the updated count
     */
    private void firePendingCommandCountChanged(int updatedCommandCount)
    {
        for(RInterfaceListener currListener: this.listenerList)
        {
            currListener.pendingCommandCountChanged(updatedCommandCount);
        }
    }
    
    /**
     * tell all the listeners that command processing completed
     * @see RInterfaceListener#completedCommandProcessing(RInterface, RCommand, REXP)
     */
    private void fireCompletedCommandProcessing(RCommand command, REXP result)
    {
        for(RInterfaceListener currListener: this.listenerList)
        {
            currListener.completedCommandProcessing(this, command, result);
        }
    }
    
    /**
     * tell all listeners that we've received a comment
     * @param comment
     *          the comment that we've received
     */
    private void fireReceivedComment(String comment)
    {
        for(RInterfaceListener currListener: this.listenerList)
        {
            currListener.receivedComment(comment);
        }
    }
    
    /**
     * tell all the listeners that command processing initiated
     * @see RInterfaceListener#initiatedCommandProcessing(RInterface, RCommand)
     */
    private void fireInitiatedCommandProcessing(RCommand command)
    {
        for(RInterfaceListener currListener: this.listenerList)
        {
            currListener.initiatedCommandProcessing(this, command);
        }
    }
    
    /**
     * tell all the listeners that we received a message from R
     * @see RInterfaceListener#receivedMessageFromR(RInterface, String, RCommand)
     */
    private void fireReceivedMessageFromR(String message, RCommand activeCommand)
    {
        for(RInterfaceListener currListener: this.listenerList)
        {
            currListener.receivedMessageFromR(this, message, activeCommand);
        }
    }
    
    /**
     * tell all the listeners that we received output from R
     * @see RInterfaceListener#receivedOutputFromR(RInterface, String, RCommand)
     */
    private void fireReceivedOutputFromR(String output, RCommand activeCommand)
    {
        for(RInterfaceListener currListener: this.listenerList)
        {
            currListener.receivedOutputFromR(this, output, activeCommand);
        }
    }
    
    /**
     * Holds all of our input types so that we can send them down the
     * same Queue.
     */
    private static class AnyRInput
    {
        /**
         * for differentiating between different input types
         */
        public static enum InputType {
            /**
             * a command that needs a return value
             */
            COMMAND_NEEDS_RETURN,
            
            /**
             * a command that doesn't need a return value
             */
            COMMAND_NO_RETURN,
            
            /**
             * just a comment
             */
            COMMENT
        }
        
        /**
         * @see #getInput()
         */
        private final Object input;
        
        /**
         * @see #getInputType()
         */
        private final InputType inputType;
        
        /**
         * @see #getAssociatedResult()
         */
        private final SettableFuture<REXP> associatedResult;
        
        /**
         * Constructor. you give us the R command, we make the future
         * result
         * @param input
         *          the input
         * @param inputType
         *          the input type
         */
        public AnyRInput(Object input, InputType inputType)
        {
            this.input = input;
            this.inputType = inputType;
            
            if(inputType == InputType.COMMAND_NEEDS_RETURN)
            {
                this.associatedResult = new SettableFuture<REXP>();
            }
            else
            {
                this.associatedResult = null;
            }
        }
        
        /**
         * Getter for the result... only valid if we
         * have a command type that's looking for a
         * result
         * @return the associatedResult
         */
        public SettableFuture<REXP> getAssociatedResult()
        {
            return this.associatedResult;
        }

        /**
         * @return the input
         */
        public Object getInput()
        {
            return this.input;
        }

        /**
         * @return the inputType
         */
        public InputType getInputType()
        {
            return this.inputType;
        }
    }
    
    /**
     * Our private R callback class...
     */
    private class RMainLoopCallBacksImpl implements RMainLoopCallbacks
    {
        /**
         * Holds our pending command
         */
        private RCommand pendingCommand = null;
        
        /**
         * Called when R transitions to and from "working" mode
         * @param rEngine
         *          the R engine that called us
         * @param which
         *          whether we've entered or exited "busy" state
         */
        public void rBusy(Rengine rEngine, int which)
        {
            if(LOG.isLoggable(Level.FINE))
            {
                LOG.fine("R busy status changed to " + which);
            }
        }

        /**
         * Called when R wants us to choose a file (not implemented).
         * @param rEngine
         *          the R engine that called us
         * @param newFile
         *          indicates if we should select a new or existing file
         * @return
         *          the file that we chose
         */
        public String rChooseFile(Rengine rEngine, int newFile)
        {
            LOG.warning("R is asking us to choose a file, but we don't do that");
            return null;
        }

        /**
         * R wants us to flush any buffered output (not implemented).
         * @param rEngine
         *          the R engine calling us
         */
        public void rFlushConsole(Rengine rEngine)
        {
            if(LOG.isLoggable(Level.FINE))
            {
                LOG.fine("R is asking us to flush console output");
            }
        }

        /**
         * R wants us to load history from the given file
         * @param rEngine
         *          the R engine calling us
         * @param filename
         *          the filename that we're supposed to load from
         */
        public void rLoadHistory(Rengine rEngine, String filename)
        {
            LOG.warning(
                    "R is asking us to load history from \"" + filename +
                    "\", but we don't do that");
        }

        /**
         * This callback allows us to send input to R.
         * @param rEngine
         *          the R engine
         * @param prompt
         *          the prompt string that we should use
         * @param addToHistory
         *          tells us whether we should be keeping history or not
         * @return
         *          the command string for R
         */
        public String rReadConsole(Rengine rEngine, String prompt, int addToHistory)
        {
            // see if we just finished a pending command
            if(this.pendingCommand != null)
            {
                // notify listeners that we're done
                BasicRInterface.this.pendingCommandCounter.decrementAndGet();
                BasicRInterface.this.fireChangeEvent();
                BasicRInterface.this.fireCompletedCommandProcessing(
                        this.pendingCommand,
                        null);
                
                // set the pending command to null
                this.pendingCommand = null;
            }
            
            try
            {
                while(this.pendingCommand == null)
                {
                    BasicRInterface.this.firePendingCommandCountChanged(
                            BasicRInterface.this.commandQueue.size());
                    
                    // read the next input item off the queue
                    AnyRInput input = BasicRInterface.this.commandQueue.take();
                    BasicRInterface.this.firePendingCommandCountChanged(
                            BasicRInterface.this.commandQueue.size() + 1);
                    
                    if(input.inputType == AnyRInput.InputType.COMMENT)
                    {
                        // notify listeners
                        BasicRInterface.this.fireReceivedComment(
                                (String)input.getInput());
                        if(LOG.isLoggable(Level.FINE))
                        {
                            LOG.fine("R Comment: " + input.getInput());
                        }
                    }
                    else if(input.inputType == AnyRInput.InputType.COMMAND_NEEDS_RETURN)
                    {
                        RCommand rCommand = (RCommand)input.getInput();
                        String command = rCommand.getCommandText() + "\n";
                        
                        // notify listeners that we've started
                        BasicRInterface.this.fireInitiatedCommandProcessing(
                                rCommand);
                        if(LOG.isLoggable(Level.FINE))
                        {
                            LOG.fine("R Command (requires return): " + command);
                        }
                        
                        // have R do its thing
                        SettableFuture<REXP> associatedResult = input.getAssociatedResult();
                        REXP result = rEngine.eval(command, true);
                        associatedResult.set(result);
                        
                        // notify listeners that we're done
                        BasicRInterface.this.pendingCommandCounter.decrementAndGet();
                        BasicRInterface.this.fireChangeEvent();
                        BasicRInterface.this.fireCompletedCommandProcessing(
                                rCommand,
                                result);
                    }
                    else if(input.inputType == AnyRInput.InputType.COMMAND_NO_RETURN)
                    {
                        RCommand rCommand = (RCommand)input.getInput();
                        
                        // notify listeners that we've started
                        BasicRInterface.this.fireInitiatedCommandProcessing(rCommand);
                        if(LOG.isLoggable(Level.FINE))
                        {
                            LOG.fine("R Command (no return): " + rCommand);
                        }
                        
                        // use the R loop to process this command since we don't need a
                        // return value. the advantage of doing it this way is that we
                        // get R output text if there is any (rEngine.eval won't do
                        // that for us
                        this.pendingCommand = rCommand;
                    }
                }
            }
            catch(Exception ex)
            {
                LOG.log(Level.SEVERE,
                        "received an exception while trying to evaluate R input",
                        ex);
            }
            
            return this.pendingCommand.getCommandText() + "\n";
        }

        /**
         * R wants us to "save history" to the given file. We ignore
         * this one.
         * @param rEngine
         *          the R engine
         * @param filename
         *          the file that we're supposed to save history to
         */
        public void rSaveHistory(Rengine rEngine, String filename)
        {
            LOG.warning(
                    "we're being asked by R to save our history to \"" + filename +
                    "\", but we don't do that");
        }

        /**
         * Show the given warning message from R
         * @param rEngine
         *          the R engine that generated the warning message
         * @param message
         *          the message
         */
        public void rShowMessage(Rengine rEngine, String message)
        {
            LOG.warning(
                    "received the following R message \"" + message + "\"");
            
            BasicRInterface.this.fireReceivedMessageFromR(
                    message,
                    this.pendingCommand);
        }

        /**
         * For receiving R's output text.
         * @param rEngine
         *          the R engine calling us
         * @param text
         *          the text output to write
         * @param type
         *          the type
         */
        public void rWriteConsole(Rengine rEngine, String text, int type)
        {
            if(LOG.isLoggable(Level.FINE))
            {
                LOG.fine("R output: \"" + text + "\"");
            }
            
            BasicRInterface.this.fireReceivedOutputFromR(
                    text,
                    this.pendingCommand);
        }
    }
    
    /*
     * long-running task functions
     */
    
    /**
     * {@inheritDoc}
     */
    public String getTaskName()
    {
        return "Evaluating R Command";
    }
    
    /**
     * {@inheritDoc}
     */
    public int getTotalWorkUnits()
    {
        return this.pendingCommandCounter.get();
    }
    
    /**
     * {@inheritDoc}
     */
    public int getWorkUnitsCompleted()
    {
        return 0;
    }
}
