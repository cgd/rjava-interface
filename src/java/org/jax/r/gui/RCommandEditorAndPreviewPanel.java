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

import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.text.BadLocationException;
import javax.swing.text.StyledDocument;

import org.jax.r.RCommand;

/**
 * An R command editor that includes a preview panel.
 * @author <A HREF="mailto:keith.sheppard@jax.org">Keith Sheppard</A>
 */
public class RCommandEditorAndPreviewPanel
        extends javax.swing.JPanel
        implements RCommandEditor
{
    /**
     * Our logger
     */
    private static final Logger LOG = Logger.getLogger(
            RCommandEditorAndPreviewPanel.class.getName());
    
    /**
     * every {@link java.io.Serializable} is supposed to have one of these
     */
    private static final long serialVersionUID = -3304190485844223296L;
    
    /**
     * @see #getInternalEditorPanel()
     */
    private final RCommandEditorPanel internalEditorPanel;
    
    /**
     * listens for edits to the R command and updates the preview panel
     */
    private RCommandEditorListener commandEditorListener = new RCommandEditorListener()
    {
        public void commandModified(
                RCommandEditor editor)
        {
            RCommandEditorAndPreviewPanel.this.commandModified(
                    editor);
        }
    };
    
    /**
     * Constructor
     * @param internalEditorPanel
     *          see {@link #getInternalEditorPanel()}
     */
    public RCommandEditorAndPreviewPanel(
            RCommandEditorPanel internalEditorPanel)
    {
        this.internalEditorPanel = internalEditorPanel;
        this.initComponents();
        RDocumentStyle.initializeDocumentStyles(
                this.commandPreviewTextPane.getStyledDocument());
        this.internalEditorPanel.addRCommandEditorListener(
                this.commandEditorListener);
        this.commandModified(internalEditorPanel);
    }

    /**
     * This method is called when our internal panel modifies its command
     * @param editor
     *          the root cause of the modification
     */
    private void commandModified(RCommandEditor editor)
    {
        this.updateCommandPreview(editor.getCommands());
    }

    /**
     * Update the command preview so that it displays the given commands.
     * @param commands
     *          the commands to display
     */
    private void updateCommandPreview(RCommand[] commands)
    {
        StyledDocument previewDocument =
            this.commandPreviewTextPane.getStyledDocument();
        try
        {
            // clear out the current contents
            previewDocument.remove(0, previewDocument.getLength());
            
            // iterate through the commands adding them to the preview
            // one at a time.
            for(int i = 0; i < commands.length; i++)
            {
                // insert a "> " at the beginning of each command
                previewDocument.insertString(
                        previewDocument.getLength(),
                        "> ",
                        previewDocument.getStyle(
                                RDocumentStyle.R_OUTPUT_STYLE.name()));

                RCommand currCommand = commands[i];
                if(i < commands.length - 1)
                {
                    previewDocument.insertString(
                            previewDocument.getLength(),
                            currCommand.getCommandText() + "\n",
                            previewDocument.getStyle(
                                    RDocumentStyle.CALLER_COMMAND_STYLE.name()));
                }
                else
                {
                    // this is the last command... don't append a newline
                    previewDocument.insertString(
                            previewDocument.getLength(),
                            currCommand.getCommandText(),
                            previewDocument.getStyle(
                                    RDocumentStyle.CALLER_COMMAND_STYLE.name()));
                }
            }
        }
        catch(BadLocationException ex)
        {
            LOG.log(Level.SEVERE,
                    "failed to update R preview",
                    ex);
            
            LOG.severe("failed command preview count: " + commands.length);
            for(RCommand command: commands)
            {
                LOG.severe("Command: " + command);
            }
        }
    }
    
    /**
     * This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("all")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        javax.swing.JSplitPane commandEditorSplitPane = new javax.swing.JSplitPane();
        javax.swing.JPanel commandEditorPanelDownCast = this.internalEditorPanel;
        javax.swing.JPanel commandEditorPreviewContainerPanel = new javax.swing.JPanel();
        javax.swing.JLabel commandPreviewLabel = new javax.swing.JLabel();
        javax.swing.JScrollPane commandPreviewScrollPane = new javax.swing.JScrollPane();
        commandPreviewTextPane = new javax.swing.JTextPane();

        commandEditorSplitPane.setOrientation(javax.swing.JSplitPane.VERTICAL_SPLIT);
        commandEditorSplitPane.setResizeWeight(0.8);
        commandEditorSplitPane.setOneTouchExpandable(true);
        commandEditorSplitPane.setLeftComponent(commandEditorPanelDownCast);

        commandPreviewLabel.setText("Command Preview:");

        commandPreviewScrollPane.setMinimumSize(new java.awt.Dimension(23, 50));

        commandPreviewTextPane.setEditable(false);
        commandPreviewScrollPane.setViewportView(commandPreviewTextPane);

        org.jdesktop.layout.GroupLayout commandEditorPreviewContainerPanelLayout = new org.jdesktop.layout.GroupLayout(commandEditorPreviewContainerPanel);
        commandEditorPreviewContainerPanel.setLayout(commandEditorPreviewContainerPanelLayout);
        commandEditorPreviewContainerPanelLayout.setHorizontalGroup(
            commandEditorPreviewContainerPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(commandEditorPreviewContainerPanelLayout.createSequentialGroup()
                .addContainerGap()
                .add(commandEditorPreviewContainerPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(commandPreviewScrollPane, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 541, Short.MAX_VALUE)
                    .add(commandPreviewLabel))
                .addContainerGap())
        );
        commandEditorPreviewContainerPanelLayout.setVerticalGroup(
            commandEditorPreviewContainerPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(commandEditorPreviewContainerPanelLayout.createSequentialGroup()
                .addContainerGap()
                .add(commandPreviewLabel)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(commandPreviewScrollPane, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 106, Short.MAX_VALUE)
                .addContainerGap())
        );

        commandEditorSplitPane.setRightComponent(commandEditorPreviewContainerPanel);

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(0, 625, Short.MAX_VALUE)
            .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                .add(layout.createSequentialGroup()
                    .addContainerGap()
                    .add(commandEditorSplitPane, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 585, Short.MAX_VALUE)
                    .addContainerGap()))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(0, 300, Short.MAX_VALUE)
            .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                .add(layout.createSequentialGroup()
                    .addContainerGap()
                    .add(commandEditorSplitPane, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 260, Short.MAX_VALUE)
                    .addContainerGap()))
        );
    }// </editor-fold>//GEN-END:initComponents

    /**
     * {@inheritDoc}
     */
    public void addRCommandEditorListener(RCommandEditorListener editorListener)
    {
        this.internalEditorPanel.addRCommandEditorListener(
                editorListener);
    }

    /**
     * {@inheritDoc}
     */
    public RCommand[] getCommands()
    {
        return this.internalEditorPanel.getCommands();
    }
    
    /**
     * {@inheritDoc}
     */
    public void removeRCommandEditorListener(
            RCommandEditorListener editorListener)
    {
        this.internalEditorPanel.removeRCommandEditorListener(
                editorListener);
    }

    /**
     * The internal editor pane that got passed in through our constructor.
     * @return
     *          the internalEditorPanel
     */
    public RCommandEditorPanel getInternalEditorPanel()
    {
        return this.internalEditorPanel;
    }
    
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JTextPane commandPreviewTextPane;
    // End of variables declaration//GEN-END:variables
    
}
