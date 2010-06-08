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

package org.jax.r;

/**
 * @author <A HREF="mailto:keith.sheppard@jax.org">Keith Sheppard</A>
 */
public class RAssignmentCommand implements RCommand
{
    private static final String R_ASSIGNEE_VALUE_EXPRESSION_SEPERATOR = " <- ";
    
    private final String commandText;
    
    /**
     * An assignment command
     * @param assigneeIdentifier
     *          the R identifier for the assignee
     * @param assignmentValueExpression
     *          the R expression whose value we're assigning to the
     *          assignee
     */
    public RAssignmentCommand(
            String assigneeIdentifier,
            String assignmentValueExpression)
    {
        this.commandText =
            assigneeIdentifier +
            R_ASSIGNEE_VALUE_EXPRESSION_SEPERATOR +
            assignmentValueExpression;
    }
    
    /**
     * {@inheritDoc}
     */
    public String getCommandText()
    {
        return this.commandText;
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public String toString()
    {
        return this.getCommandText();
    }
}
