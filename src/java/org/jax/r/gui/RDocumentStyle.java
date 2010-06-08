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

import java.awt.Color;

import javax.swing.text.Style;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;

/**
 * Enum values for displaying R input and output.
 */
// TODO these styles should really come from a properties file
public enum RDocumentStyle
{
    /**
     * style enum for comments
     */
    CALLER_COMMENT_STYLE
    {
        /**
         * {@inheritDoc}
         */
        @Override
        public void updateStyle(Style styleToUpdate)
        {
            StyleConstants.setForeground(styleToUpdate, Color.GRAY);
            StyleConstants.setItalic(styleToUpdate, true);
        }
    },
    
    /**
     * style enum for commands
     */
    CALLER_COMMAND_STYLE
    {
        /**
         * {@inheritDoc}
         */
        @Override
        public void updateStyle(Style styleToUpdate)
        {
            StyleConstants.setForeground(styleToUpdate, Color.BLUE);
        }
    },
    
    /**
     * style enum for R output
     */
    R_OUTPUT_STYLE
    {
        /**
         * {@inheritDoc}
         */
        @Override
        public void updateStyle(Style styleToUpdate)
        {
            StyleConstants.setForeground(styleToUpdate, Color.BLACK);
        }
    },
    
    /**
     * style enum for R messages
     */
    R_MESSAGE_STYLE
    {
        /**
         * {@inheritDoc}
         */
        @Override
        public void updateStyle(Style styleToUpdate)
        {
            StyleConstants.setForeground(styleToUpdate, Color.RED);
        }
    };
    
    /**
     * Modifies the given style to make it be whatever
     * this enum wants it to be. This function should
     * be applied to a style whose parent style has
     * already gone through the {@link #updateBaseStyle(Style)}
     * 
     * @param styleToUpdate
     *          the style to modify
     */
    public abstract void updateStyle(Style styleToUpdate);
    
    /**
     * This method will do all of the style updates that are
     * common to our different styles. The result should be
     * used as a parent style for all of the styles.
     * @param baseStyleToUpdate
     *          the style to update with all of the base attributes
     *          that we care about
     */
    public static void updateBaseStyle(Style baseStyleToUpdate)
    {
        StyleConstants.setFontFamily(baseStyleToUpdate, "Monospaced");
        StyleConstants.setFontSize(baseStyleToUpdate, 12);
    }
    
    /**
     * Initializes styles that we want to use in the styled document.
     * @param document
     *          the document to initialize with R styles
     */
    public static void initializeDocumentStyles(StyledDocument document)
    {
        // 1st create a parent font style
        Style parentStyle = document.addStyle(null, null);
        RDocumentStyle.updateBaseStyle(parentStyle);
        
        // iterate through the styles, adding them to the
        // styled document
        for(RDocumentStyle currentStyleEnum: RDocumentStyle.values())
        {
            // have the document build a new style for us and we
            // can then update it
            Style newStyle = document.addStyle(
                    currentStyleEnum.name(),
                    parentStyle);
            currentStyleEnum.updateStyle(
                    newStyle);
        }
    }
}