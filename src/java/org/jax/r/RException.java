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
 * Represents an R exception.
 * @author <A HREF="mailto:keith.sheppard@jax.org">Keith Sheppard</A>
 */
public class RException extends RuntimeException
{
    /**
     * java wants every {@link java.io.Serializable} to have one of these
     */
    private static final long serialVersionUID = 1849855935202727804L;

    /**
     * Construct a new R exception.
     */
    public RException()
    {
        super();
    }

    /**
     * Construct a new R exception.
     * @param message
     *          the message to the catcher
     */
    public RException(String message)
    {
        super(message);
    }

    /**
     * Construct a new R exception.
     * @param cause
     *          the root cause of this exception
     */
    public RException(Throwable cause)
    {
        super(cause);
    }

    /**
     * Construct a new R exception.
     * @param message
     *          the message to the catcher
     * @param cause
     *          the root cause of this exception
     */
    public RException(String message, Throwable cause)
    {
        super(message, cause);
    }
}
