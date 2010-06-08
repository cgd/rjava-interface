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

import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.text.BadLocationException;
import javax.swing.text.StyledDocument;

import org.jax.r.RCommand;
import org.jax.r.gui.RDocumentStyle;
import org.rosuda.JRI.REXP;

/**
 * This class writes what it hears from the R interface to a
 * styled document. The output is formatted to look like R terminal
 * output.
 * @author <A HREF="mailto:keith.sheppard@jax.org">Keith Sheppard</A>
 */
public class DocumentWritingRInterfaceListener implements RInterfaceListener
{
    /**
     * our logger
     */
    private static final Logger LOG =
        Logger.getLogger(DocumentWritingRInterfaceListener.class.getName());
    
    /**
     * the document that we're going to write to
     */
    protected final StyledDocument attachedDocument;
    
    /**
     * Constructor
     * @param attachedDocument
     *          the document that we're going to write to
     */
    public DocumentWritingRInterfaceListener(StyledDocument attachedDocument)
    {
        this.attachedDocument = attachedDocument;
        RDocumentStyle.initializeDocumentStyles(attachedDocument);
    }
    
    /**
     * {@inheritDoc}
     */
    public void completedCommandProcessing(
            RInterface eventSource,
            RCommand command,
            REXP result)
    {
        if(!(command instanceof SilentRCommand))
        {
            this.appendPromptToDocument();
        }
    }
    
    /**
     * A little wrapper function for appending text to the document
     * @param text
     *          the text to append
     * @param docStyle
     *          the style to use
     */
    protected void appendToAttachedDocument(String text, RDocumentStyle docStyle)
    {
        try
        {
            this.attachedDocument.insertString(
                    this.attachedDocument.getLength(),
                    text,
                    this.attachedDocument.getStyle(docStyle.name()));
        }
        catch(BadLocationException ex)
        {
            LOG.log(Level.SEVERE,
                    "caught exception updating attached document",
                    ex);
        }
    }
    
    /**
     * Add prompt to the document
     */
    protected void appendPromptToDocument()
    {
        this.appendToAttachedDocument("> ", RDocumentStyle.R_OUTPUT_STYLE);
    }

    /**
     * {@inheritDoc}
     */
    public void initiatedCommandProcessing(
            RInterface eventSource,
            RCommand activeCommand)
    {
        if(!(activeCommand instanceof SilentRCommand))
        {
            if(LOG.isLoggable(Level.FINEST))
            {
                LOG.finest("appending command to document: " + activeCommand);
            }
            this.appendToAttachedDocument(
                    activeCommand.getCommandText() + "\n",
                    RDocumentStyle.CALLER_COMMAND_STYLE);
        }
        else
        {
            if(LOG.isLoggable(Level.FINEST))
            {
                LOG.finest("not appending silent command to document: " + activeCommand);
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    public void receivedComment(String comment)
    {
        if(LOG.isLoggable(Level.FINEST))
        {
            LOG.finest("appending comment to document: " + comment);
        }
        this.appendToAttachedDocument(comment, RDocumentStyle.CALLER_COMMENT_STYLE);
        this.appendPromptToDocument();
    }

    /**
     * {@inheritDoc}
     */
    public void receivedMessageFromR(
            RInterface eventSource,
            String message,
            RCommand activeCommand)
    {
        if(!(activeCommand instanceof SilentRCommand))
        {
            if(LOG.isLoggable(Level.FINEST))
            {
                LOG.finest("appending message to document: " + message);
            }
            this.appendToAttachedDocument(message, RDocumentStyle.R_MESSAGE_STYLE);
        }
        else
        {
            if(LOG.isLoggable(Level.FINEST))
            {
                LOG.finest("not appending silent message to document: " + message);
            }
        }
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
            if(LOG.isLoggable(Level.FINEST))
            {
                LOG.finest("appending output to document: " + output);
            }
            this.appendToAttachedDocument(output, RDocumentStyle.R_OUTPUT_STYLE);
        }
        else
        {
            if(LOG.isLoggable(Level.FINEST))
            {
                LOG.finest("not appending silent output to document: " + output);
            }
        }
    }
    
    /**
     * {@inheritDoc}
     */
    public void pendingCommandCountChanged(int updatedCommandCount)
    {
        // no-op
    }

    /**
     * Getter for the document that this listener writes to.
     * @return
     *          the document
     */
    public StyledDocument getAttachedDocument()
    {
        return this.attachedDocument;
    }
}
