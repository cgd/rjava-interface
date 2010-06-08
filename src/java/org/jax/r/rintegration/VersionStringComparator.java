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

package org.jax.r.rintegration;

import java.util.Comparator;
import java.util.StringTokenizer;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Determines relative order of version strings where earlier releases are
 * considered "less than" newer releases.
 * @author <A HREF="mailto:keith.sheppard@jax.org">Keith Sheppard</A>
 */
// TODO unit test me
public class VersionStringComparator implements Comparator<String>
{
    /**
     * our logger
     */
    private static final Logger LOG = Logger.getLogger(
            VersionStringComparator.class.getName());
    
    /**
     * our singleton instance
     * @see #getInstance()
     */
    private static final VersionStringComparator INSTANCE =
            new VersionStringComparator();
    
    /**
     * String containing the possible delimiter characters for a version
     * string.
     */ 
    // TODO this should be programmatically hooked up to RHomeScanner's regex
    private static final String VERSION_DELIMITERS = ".-_";

    /**
     * compare the two given version strings
     * @param versionString1
     *          the 1st string
     * @param versionString2
     *          the 2nd string
     * @return
     *          see {@link java.util.Comparator#compare(Object, Object)}
     *          for the rules on this
     */
    public int compare(String versionString1, String versionString2)
    {
        StringTokenizer tok1 = new StringTokenizer(
                versionString1,
                VERSION_DELIMITERS);
        StringTokenizer tok2 = new StringTokenizer(
                versionString2,
                VERSION_DELIMITERS);
        
        // iterate through the version string's tokens until you see a
        // difference, run out of tokens or hit an "invalid" string
        while(tok1.hasMoreTokens() || tok2.hasMoreElements())
        {
            // at least one has more tokens
            if(!tok1.hasMoreTokens() || !tok2.hasMoreTokens())
            {
                // only one ran out of tokens... the other is "greater"
                return tok1.countTokens() - tok2.countTokens();
            }
            else
            {
                // both have more tokens
                String currTokenString1 = tok1.nextToken();
                String currTokenString2 = tok2.nextToken();
                
                // convert tokens into ints checking for validity
                boolean currTok1Valid = true;
                boolean currTok2Valid = true;
                
                int currTok1IntValue = 0;
                int currTok2IntValue = 0;
                
                try
                {
                    currTok1IntValue = Integer.parseInt(currTokenString1);
                }
                catch(NumberFormatException ex)
                {
                    // invalid number
                    currTok1Valid = false;
                    if(LOG.isLoggable(Level.FINE))
                    {
                        LOG.log(Level.FINE,
                                "version token is not a number",
                                ex);
                    }
                }
                
                try
                {
                    currTok2IntValue = Integer.parseInt(currTokenString2);
                }
                catch(NumberFormatException ex)
                {
                    // invalid number
                    currTok2Valid = false;
                    if(LOG.isLoggable(Level.FINE))
                    {
                        LOG.log(Level.FINE,
                                "version token is not a number",
                                ex);
                    }
                }
                
                // if they're both valid, compare int values
                if(currTok1Valid && currTok2Valid)
                {
                    // look for a value difference. keep looping if none is
                    // found
                    int valueDifference = currTok1IntValue - currTok2IntValue;
                    if(valueDifference != 0)
                    {
                        // we found a version difference... return it!
                        return valueDifference;
                    }
                }
                else
                {
                    // at least one token is not a valid number. if the other
                    // is valid, it wins as "greater" if the other is also
                    // invalid, then we break out of the loop and fall back
                    // on plain string comparison
                    if(currTok1Valid)
                    {
                        return 1;
                    }
                    else if(currTok2Valid)
                    {
                        return -1;
                    }
                    else
                    {
                        break;
                    }
                }
            }
        }
        
        // our fall-back position is to just compare the two as normal strings
        // which is not really what we want to do (this is a final option)
        return versionString1.compareTo(versionString2);
    }
    
    /**
     * Determines whether or not the potential subversion is a subversion of
     * the given super-version or not. Eg: 3.2.1.15 is a subversion of
     * 3.2 and 3.2.1 but is not a subversion of 3.2.2
     * @param superVersion
     *          the super version
     * @param potentialSubversion
     *          the version that we're testing
     * @return
     *          true iff it is a subversion
     */
    public boolean isSuperversionOf(
            String superVersion,
            String potentialSubversion)
    {
        StringTokenizer superVersionTok = new StringTokenizer(
                superVersion,
                VERSION_DELIMITERS);
        StringTokenizer potentialSubversionTok = new StringTokenizer(
                potentialSubversion,
                VERSION_DELIMITERS);
        
        while(superVersionTok.hasMoreTokens())
        {
            if(potentialSubversionTok.hasMoreTokens())
            {
                String currSuperTok = superVersionTok.nextToken();
                String currSubTok = potentialSubversionTok.nextToken();
                if(!currSuperTok.equals(currSubTok))
                {
                    // found a miss match, so it isn't a subversion
                    return false;
                }
            }
            else
            {
                // the subversion can't be shorter
                return false;
            }
        }
        
        // it's a subversion
        return true;
    }
    
    /**
     * Determine if any of the given candidate super-versions are
     * super-versions of the the given sub-version as defined by
     * {@link #isSuperversionOf(String, String)}.
     * @param candidateSuperVersions
     *          the candidate super-versions
     * @param potentialSubVersion
     *          the potential sub-version that we're testing the super
     *          versions against
     * @return
     *          true if any are super-versions of the sub-version
     */
    public boolean areAnySuperVersionOf(
            String[] candidateSuperVersions,
            String potentialSubVersion)
    {
        for(String currCandidateSuperVersion: candidateSuperVersions)
        {
            if(this.isSuperversionOf(
                    currCandidateSuperVersion,
                    potentialSubVersion))
            {
                return true;
            }
        }
        
        return false;
    }

    /**
     * Singleton instance getter
     * @return
     *          the instance
     */
    public static VersionStringComparator getInstance()
    {
        return VersionStringComparator.INSTANCE;
    }
}
