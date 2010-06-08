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

import org.jax.r.RCommand;

/**
 * Interface used by components that edit R commands
 * @author <A HREF="mailto:keith.sheppard@jax.org">Keith Sheppard</A>
 */
public interface RCommandEditor
{
    /**
     * Get the R commands
     * @return
     *          the current R commands. these can be empty but should never be
     *          null
     */
    public RCommand[] getCommands();
    
    /**
     * Add the given listener to our listener list
     * @param editorListener
     *          the listener to add
     */
    public void addRCommandEditorListener(RCommandEditorListener editorListener);
    
    /**
     * Remove the given listener from our listener list
     * @param editorListener
     *          the listener to remove
     */
    public void removeRCommandEditorListener(RCommandEditorListener editorListener);
}
