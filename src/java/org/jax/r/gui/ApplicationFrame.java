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

import java.awt.Rectangle;
import java.util.logging.Logger;

import javax.swing.JDesktopPane;
import javax.swing.JFrame;
import javax.swing.JMenuBar;
import javax.swing.JOptionPane;
import javax.swing.JTree;
import javax.swing.SwingUtilities;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.StyledDocument;

import org.jax.r.jriutilities.RInterface;
import org.jax.r.project.ProjectAwareDocumentWritingRInterfaceListener;
import org.jax.util.TextWrapper;
import org.jax.util.concurrent.MultiTaskProgressPanel;
import org.jax.util.project.ProjectManager;

/**
 * The main frame class for the J/qtl application.
 * @author <A HREF="mailto:keith.sheppard@jax.org">Keith Sheppard</A>
 */
public class ApplicationFrame extends javax.swing.JFrame
{
    /**
     * every {@link java.io.Serializable} is supposed to have one of these
     */
    private static final long serialVersionUID = -4657115227288438478L;
    
    private final RInterface rInterface;
    
    private final JMenuBar menuBar;
    
    private final JDesktopPane desktop;
    
    private final JTree projectTree;
    
    private static final Logger LOG = Logger.getLogger(
            ApplicationFrame.class.getName());
    
    private MultiTaskProgressPanel taskProgressPanel = new MultiTaskProgressPanel();
    
    private final ProjectManager projectManager;
    
    /**
     * Constructor
     * @param title
     *          the application title
     * @param rInterface
     *          the R interface to attach to
     * @param menuBar
     *          the menu bar to use
     * @param desktop
     *          the main desktop pane
     * @param projectTree
     *          the project tree
     * @param projectManager
     *          the project manager
     */
    public ApplicationFrame(
            String title,
            RInterface rInterface,
            JMenuBar menuBar,
            JDesktopPane desktop,
            JTree projectTree,
            ProjectManager projectManager)
    {
        super(title);
        
        this.rInterface = rInterface;
        this.menuBar = menuBar;
        this.desktop = desktop;
        this.projectTree = projectTree;
        this.projectManager = projectManager;
        
        this.initComponents();
        this.postGuiInit();
    }
    
    /**
     * Getter for the task progress panel
     * @return the task progress panel
     */
    public MultiTaskProgressPanel getTaskProgressPanel()
    {
        return this.taskProgressPanel;
    }
    
    /**
     * take care of the initialization that isn't handled by the GUI builder
     */
    private void postGuiInit()
    {
        StyledDocument terminalDocument =
            this.terminalTextPane.getStyledDocument();
        terminalDocument.addDocumentListener(new DocumentListener()
        {
            /**
             * {@inheritDoc}
             */
            public void changedUpdate(DocumentEvent e)
            {
                // Don't care
            }
            
            /**
             * {@inheritDoc}
             */
            public void insertUpdate(DocumentEvent e)
            {
                SwingUtilities.invokeLater(new Runnable()
                {
                    /**
                     * {@inheritDoc}
                     */
                    public void run()
                    {
                        ApplicationFrame.this.terminalTextPane.scrollRectToVisible(new Rectangle(
                                0,
                                ApplicationFrame.this.terminalTextPane.getHeight(),
                                0,
                                0));
                    }
                });
            }
            
            /**
             * {@inheritDoc}
             */
            public void removeUpdate(DocumentEvent e)
            {
                // Don't care
            }
        });
        
        this.dataTreeScrollPane.setViewportView(this.projectTree);
        
        this.rInterface.addRInterfaceListener(
                new ProjectAwareDocumentWritingRInterfaceListener(
                        terminalDocument,
                        this.projectManager));
        this.setExtendedState(JFrame.MAXIMIZED_BOTH);
        this.taskProgressPanel.addTaskToTrack(this.rInterface);
    }
    
    /**
     * Ask the user to enter a command
     */
    private void insertUserCommand()
    {
        String commandString = (String)JOptionPane.showInputDialog(
                this,
                TextWrapper.wrapText(
                        "Text will be interpreted as a comment if it starts " +
                        "with a '#' character. Otherwise it is executed as an " +
                        "R command. Please use care when entering a command since no " +
                        "effort is made to ensure that the command is valid " +
                        "or that the project data structures will be unaffected " +
                        "before the command is executed.",
                        TextWrapper.DEFAULT_DIALOG_COLUMN_COUNT),
                "User Defined R Comment or Command",
                JOptionPane.PLAIN_MESSAGE,
                null,
                null,
                "# ");

        if((commandString != null) && (commandString.trim().length() > 0))
        {
            String trimmedCommandString = commandString.trim();
            if(trimmedCommandString.startsWith("#"))
            {
                LOG.fine("inserting user defined comment");
                this.rInterface.insertCommentVerbatim(commandString + "\n");
            }
            else
            {
                LOG.info("evaluating user entered command: " + commandString);
                this.rInterface.insertComment("evaluating user entered command");
                this.rInterface.evaluateCommandNoReturn(commandString.trim());
                
                LOG.fine(
                        "notifying active project of a potential change " +
                        "to the data structures");
                
                this.projectManager.refreshProjectDataStructures();
                this.projectManager.notifyActiveProjectModified();
            }
        }
        else
        {
            LOG.info("User entered command canceled");
        }
    }

    /**
     * Close the application after checking if there are any unsaved project
     * changes.
     */
    public void closeApplication()
    {
        if(this.projectManager.isActiveProjectModified())
        {
            String message =
                "The current project contains unsaved modifications. Exiting the " +
                "application before saving will cause these modifications to be lost. " +
                "Would you like to exit without saving?";
            int response = JOptionPane.showConfirmDialog(
                    this,
                    TextWrapper.wrapText(
                            message,
                            TextWrapper.DEFAULT_DIALOG_COLUMN_COUNT),
                    "Unsaved Project Modifications",
                    JOptionPane.OK_CANCEL_OPTION,
                    JOptionPane.QUESTION_MESSAGE);
            if(response == JOptionPane.CLOSED_OPTION || response == JOptionPane.CANCEL_OPTION)
            {
                return;
            }
        }
        
        // we're OK to quit
        System.exit(0);
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("all")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        javax.swing.JSplitPane outerSplitPane = new javax.swing.JSplitPane();
        javax.swing.JSplitPane innerSplitPane = new javax.swing.JSplitPane();
        dataTreeScrollPane = new javax.swing.JScrollPane();
        javax.swing.JDesktopPane desktopPane = desktop;
        javax.swing.JPanel terminalPanel = new javax.swing.JPanel();
        javax.swing.JPanel terminalHeaderPanel = new javax.swing.JPanel();
        terminalToolBar = new javax.swing.JToolBar();
        javax.swing.JButton insertCommandButton = new javax.swing.JButton();
        javax.swing.JPanel taskTrackerPanelDowncast = taskProgressPanel;
        javax.swing.JScrollPane terminalScrollPane = new javax.swing.JScrollPane();
        terminalTextPane = new javax.swing.JTextPane();
        javax.swing.JMenuBar mainMenu = menuBar;

        setDefaultCloseOperation(javax.swing.WindowConstants.DO_NOTHING_ON_CLOSE);
        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosing(java.awt.event.WindowEvent evt) {
                formWindowClosing(evt);
            }
        });

        outerSplitPane.setOrientation(javax.swing.JSplitPane.VERTICAL_SPLIT);
        outerSplitPane.setResizeWeight(0.7);
        outerSplitPane.setOneTouchExpandable(true);

        innerSplitPane.setResizeWeight(0.2);
        innerSplitPane.setOneTouchExpandable(true);
        innerSplitPane.setLeftComponent(dataTreeScrollPane);
        innerSplitPane.setRightComponent(desktopPane);

        outerSplitPane.setTopComponent(innerSplitPane);

        terminalPanel.setLayout(new java.awt.BorderLayout());

        terminalHeaderPanel.setLayout(new java.awt.GridBagLayout());

        terminalToolBar.setFloatable(false);
        terminalToolBar.setRollover(true);

        insertCommandButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/terminal-16x16.png"))); // NOI18N
        insertCommandButton.setText("Insert Comment or Command ...");
        insertCommandButton.setFocusable(false);
        insertCommandButton.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        insertCommandButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                insertCommandButtonActionPerformed(evt);
            }
        });
        terminalToolBar.add(insertCommandButton);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        terminalHeaderPanel.add(terminalToolBar, gridBagConstraints);
        terminalHeaderPanel.add(taskTrackerPanelDowncast, new java.awt.GridBagConstraints());

        terminalPanel.add(terminalHeaderPanel, java.awt.BorderLayout.PAGE_START);

        terminalTextPane.setEditable(false);
        terminalTextPane.setDragEnabled(false);
        terminalScrollPane.setViewportView(terminalTextPane);

        terminalPanel.add(terminalScrollPane, java.awt.BorderLayout.CENTER);

        outerSplitPane.setRightComponent(terminalPanel);

        getContentPane().add(outerSplitPane, java.awt.BorderLayout.CENTER);
        setJMenuBar(mainMenu);

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void insertCommandButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_insertCommandButtonActionPerformed
        this.insertUserCommand();
    }//GEN-LAST:event_insertCommandButtonActionPerformed

    private void formWindowClosing(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_formWindowClosing
        this.closeApplication();
    }//GEN-LAST:event_formWindowClosing
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JScrollPane dataTreeScrollPane;
    private javax.swing.JTextPane terminalTextPane;
    private javax.swing.JToolBar terminalToolBar;
    // End of variables declaration//GEN-END:variables
    
}
