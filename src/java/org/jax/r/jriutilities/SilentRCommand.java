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

import org.jax.r.RCommand;
import org.jax.r.SimpleRCommand;

/**
 * Used for issuing commands that we don't want to make visible
 * to the application user.
 * @author <A HREF="mailto:keith.sheppard@jax.org">Keith Sheppard</A>
 */
public class SilentRCommand implements RCommand
{
    /**
     * the encapsulated command
     */
    private final RCommand encapsulatedCommand;
    
    /**
     * Constructor. This constructor automatically appends a newline to the
     * given command text.
     * @param commandText
     *          the command text
     */
    public SilentRCommand(String commandText)
    {
        this.encapsulatedCommand = new SimpleRCommand(commandText);
    }
    
    /**
     * Constructor
     * @param encapsulatedCommand 
     *          the command that this silent command is encapsulating
     */
    public SilentRCommand(RCommand encapsulatedCommand)
    {
        this.encapsulatedCommand = encapsulatedCommand;
    }

    /**
     * {@inheritDoc}
     */
    public String getCommandText()
    {
        return this.encapsulatedCommand.getCommandText();
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public String toString()
    {
        return "Silent command: \"" + this.getCommandText() + "\"";
    }
}
