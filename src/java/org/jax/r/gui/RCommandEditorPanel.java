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

import java.util.Iterator;
import java.util.concurrent.ConcurrentLinkedQueue;

import javax.swing.JPanel;

/**
 * A panel base class that contains common functionality for taking care
 * of the {@link RCommandEditor} stuff.
 * @author <A HREF="mailto:keith.sheppard@jax.org">Keith Sheppard</A>
 */
public abstract class RCommandEditorPanel extends JPanel implements RCommandEditor
{
    /**
     * every {@link java.io.Serializable} is supposed to have one of these
     */
    private static final long serialVersionUID = -8659207183899448121L;

    /**
     * our listener list
     */
    private ConcurrentLinkedQueue<RCommandEditorListener> commandListeners =
        new ConcurrentLinkedQueue<RCommandEditorListener>();

    /**
     * {@inheritDoc}
     */
    public void addRCommandEditorListener(RCommandEditorListener editorListener)
    {
        this.commandListeners.add(editorListener);
    }
    
    /**
     * Tell our listeners that the command has been modified
     */
    protected void fireCommandModified()
    {
        // use an iterator explicitly because we aren't guaranteed that a
        // for-each loop will use an iterator and we need one to be thread safe
        Iterator<RCommandEditorListener> listenerIter =
            this.commandListeners.iterator();
        while(listenerIter.hasNext())
        {
            listenerIter.next().commandModified(
                    this);
        }
    }

    /**
     * {@inheritDoc}
     */
    public void removeRCommandEditorListener(
            RCommandEditorListener editorListener)
    {
        this.commandListeners.remove(editorListener);
    }
}
