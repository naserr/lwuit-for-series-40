/*
 * Copyright (c) 2008, 2010, Oracle and/or its affiliates. All rights reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the LICENSE file that accompanied this code.
 *
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 * version 2 for more details (a copy is included in the LICENSE file that
 * accompanied this code).
 *
 * You should have received a copy of the GNU General Public License version
 * 2 along with this work; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * Please contact Oracle, 500 Oracle Parkway, Redwood Shores
 * CA 94065 USA or visit www.oracle.com if you need additional information or
 * have any questions.
 */
package com.sun.lwuit;

import com.sun.lwuit.animations.CommonTransitions;
import com.sun.lwuit.animations.Transition;
import com.sun.lwuit.events.ActionEvent;
import com.sun.lwuit.events.ActionListener;
import com.sun.lwuit.geom.Dimension;
import com.sun.lwuit.impl.LWUITImplementation;
import com.sun.lwuit.impl.s40.S40Implementation;
import com.sun.lwuit.layouts.BorderLayout;
import com.sun.lwuit.layouts.BoxLayout;
import com.sun.lwuit.layouts.FlowLayout;
import com.sun.lwuit.layouts.GridLayout;
import com.sun.lwuit.list.ListCellRenderer;
import com.sun.lwuit.plaf.LookAndFeel;
import com.sun.lwuit.plaf.Style;
import com.sun.lwuit.plaf.UIManager;
import java.util.Stack;
import java.util.Vector;

/**
 * This class represents the Form MenuBar.
 * This class is responsible to show the Form Commands and to handle device soft
 * keys, back key, clear key, etc...
 * This class can be overridden and replaced in the LookAndFeel
 * @see LookAndFeel#setMenuBarClass(java.lang.Class) 
     
 * @author Chen Fishbein
 */
public class MenuBar extends Container implements ActionListener {

    private Command selectCommand;
    private Command defaultCommand;
    /**
     * Indicates the command that is defined as the back command out of this form.
     * A back command can be used both to map to a hardware button (e.g. on the Sony Ericsson devices)
     * and by elements such as transitions etc. to change the behavior based on 
     * direction (e.g. slide to the left to enter screen and slide to the right to exit with back).
     */
    private Command backCommand;
    /**
     * Indicates the command that is defined as the clear command out of this form similar
     * in spirit to the back command
     */
    private Command clearCommand;
    /**
     * This member holds the left soft key value
     */
    static int leftSK;
    /**
     * This member holds the right soft key value
     */
    static int rightSK;
    /**
     * This member holds the 2nd right soft key value
     * this is used for different BB devices
     */
    static int rightSK2;
    /**
     * This member holds the back command key value
     */
    static int backSK;
    /**
     * This member holds the clear command key value
     */
    static int clearSK;
    static int backspaceSK;
    

    static {
        // RIM and potentially other devices reinitialize the static initializer thus overriding
        // the new static values set by the initialized display https://lwuit.dev.java.net/issues/show_bug.cgi?id=232
        if (Display.getInstance() == null || Display.getInstance().getImplementation() == null) {
            leftSK = -6;
            rightSK = -7;
            rightSK2 = -7;
            backSK = -11;
            clearSK = -8;
            backspaceSK = -8;
        }
    }
    private Command menuCommand;
    private Vector commands = new Vector();
    private Button[] soft;
    private Command[] softCommand;
    private Button left;
    private Button right;
    private Button main;
    private ListCellRenderer menuCellRenderer;
    private Transition transitionIn;
    private Transition transitionOut;
    private Component commandList;
    private Style menuStyle;
    private Command selectMenuItem;
    private Command cancelMenuItem;
    private Form parent;
    private int softkeyCount;
    private boolean thirdSoftButton;
    
    private Stack backStack = new Stack();
    /**
     * Empty Constructor
     */
    public MenuBar() {
    }
    
    /**
     * Initialize the MenuBar
     * 
     * @param parent the associated Form
     */
    protected void initMenuBar(Form parent) {
        this.parent = parent;
        selectMenuItem = createMenuSelectCommand();
        cancelMenuItem = createMenuCancelCommand();
        LookAndFeel lf = UIManager.getInstance().getLookAndFeel();
        menuStyle = UIManager.getInstance().getComponentStyle("Menu");
        setUIID("SoftButton");
        menuCommand = new Command(UIManager.getInstance().localize("menu", "Options"), lf.getMenuIcons()[2]);
        // use the slide transition by default
        if (lf.getDefaultMenuTransitionIn() != null || lf.getDefaultMenuTransitionOut() != null) {
            transitionIn = lf.getDefaultMenuTransitionIn();
            transitionOut = lf.getDefaultMenuTransitionOut();
        } else {
            transitionIn = CommonTransitions.createSlide(CommonTransitions.SLIDE_VERTICAL, true, 300, true);
            transitionOut = CommonTransitions.createSlide(CommonTransitions.SLIDE_VERTICAL, false, 300, true);
        }
        menuCellRenderer = lf.getMenuRenderer();
        softkeyCount = 3; //Display.getInstance().getImplementation().getSoftkeyCount();
        thirdSoftButton = true; //Display.getInstance().isThirdSoftButton();

        int commandBehavior = getCommandBehavior();
        if (softkeyCount > 1 && commandBehavior < Display.COMMAND_BEHAVIOR_BUTTON_BAR) {
            if (thirdSoftButton) {
                setLayout(new GridLayout(1, 3));
                soft = new Button[]{createSoftButton("SoftButtonCenter"), createSoftButton("SoftButtonLeft"), createSoftButton("SoftButtonRight")};
                main = soft[0];
                left = soft[1];
                right = soft[2];
                if (parent.isRTL()) {
                    right.setUIID("SoftButtonLeft");
                    left.setUIID("SoftButtonRight");
                    addComponent(right);
                    addComponent(main);
                    addComponent(left);
                } else {
                    addComponent(left);
                    addComponent(main);
                    addComponent(right);
                }
                if (isReverseSoftButtons()) {
                    Button b = soft[1];
                    soft[1] = soft[2];
                    soft[2] = b;
                }
            } else {
                setLayout(new GridLayout(1, 2));
                soft = new Button[]{createSoftButton("SoftButtonLeft"), createSoftButton("SoftButtonRight")};
                main = soft[0];
                left = soft[0];
                right = soft[1];
                if (parent.isRTL()) {
                    right.setUIID("SoftButtonLeft");
                    left.setUIID("SoftButtonRight");
                    addComponent(right);
                    addComponent(left);
                } else {
                    addComponent(left);
                    addComponent(right);
                }
                if (isReverseSoftButtons()) {
                    Button b = soft[0];
                    soft[0] = soft[1];
                    soft[1] = b;
                }
            }
            // It doesn't make sense for softbuttons to have ... at the end
            for (int iter = 0; iter < soft.length; iter++) {
                soft[iter].setEndsWith3Points(false);
            }
        } else {
            // special case for touch screens we still want the 3 softbutton areas...
            if (thirdSoftButton) {
                setLayout(new GridLayout(1, 3));
                soft = new Button[]{createSoftButton("SoftButtonCenter"), createSoftButton("SoftButtonLeft"), createSoftButton("SoftButtonRight")};
                main = soft[0];
                left = soft[1];
                right = soft[2];
                addComponent(left);
                addComponent(main);
                addComponent(right);
                if (isReverseSoftButtons()) {
                    Button b = soft[1];
                    soft[1] = soft[2];
                    soft[2] = b;
                }
            } else {
                soft = new Button[]{createSoftButton("SoftButtonCenter")};
            }
        }

        softCommand = new Command[soft.length];
    }

    private int getCommandBehavior() {
        int i = Display.getInstance().getCommandBehavior();
        if(Display.getInstance().getImplementation().getSoftkeyCount() == 0) {
            if(i != Display.COMMAND_BEHAVIOR_BUTTON_BAR && i != Display.COMMAND_BEHAVIOR_BUTTON_BAR_TITLE_BACK &&
                    i != Display.COMMAND_BEHAVIOR_BUTTON_BAR_TITLE_RIGHT) {
                return Display.COMMAND_BEHAVIOR_BUTTON_BAR_TITLE_BACK;
            }
            return i;
        }
        if(i == Display.COMMAND_BEHAVIOR_DEFAULT) {
            if(Display.getInstance().isTouchScreenDevice()) {
                return Display.COMMAND_BEHAVIOR_TOUCH_MENU;
            }
            return Display.COMMAND_BEHAVIOR_SOFTKEY;
        }
        return i;
    }

    /**
     * Default command is invoked when a user presses fire, this functionality works
     * well in some situations but might collide with elements such as navigation
     * and combo boxes. Use with caution.
     * 
     * @param defaultCommand the command to treat as default
     */
    public void setDefaultCommand(Command defaultCommand) {
        if(this.defaultCommand == defaultCommand) {
            return;
        }
        Command olddef = this.defaultCommand;
        this.defaultCommand = defaultCommand;
        if(!commands.contains(defaultCommand)) {
            commands.addElement(defaultCommand);
        }
        if (olddef != null) {
            removeCommand(olddef);
            addCommand(olddef);
        }
        
        if(isNativeCommandBehavior()) {
            S40Implementation impl = (S40Implementation)Display.getInstance().getImplementation();
            if(Display.getInstance().getCurrent() == parent) {
                impl.setPrimaryNativeCommand(defaultCommand);
            }
        }else {
            if(!isMenuBarInstalled()) {
                installMenuBar();
            }
            updateCommands();
        }
    }

    /**
     * Default command is invoked when a user presses fire, this functionality works
     * well in some situations but might collide with elements such as navigation
     * and combo boxes. Use with caution.
     * 
     * @return the command to treat as default
     */
    public Command getDefaultCommand() {
        if (selectCommand != null) {
            return selectCommand;
        }
        return defaultCommand;
    }

    /**
     * Indicates the command that is defined as the clear command in this form.
     * A clear command can be used both to map to a "clear" hardware button 
     * if such a button exists.
     * 
     * @param clearCommand the command to treat as the clear Command
     */
    public void setClearCommand(Command clearCommand) {
        if(clearCommand == null) {
            return;
        }
        this.clearCommand = clearCommand;
        if(!commands.contains(clearCommand)) {
            commands.addElement(clearCommand);
        }
        updateCommands();
        if (!isMenuBarInstalled()) {
            installMenuBar();
        }
    }

    /**
     * Indicates the command that is defined as the clear command in this form.
     * A clear command can be used both to map to a "clear" hardware button 
     * if such a button exists.
     * 
     * @return the command to treat as the clear Command
     */
    public Command getClearCommand() {
        return clearCommand;
    }
    
    private Button findCommandComponent(Command c) {
        Button b = findCommandComponent(c, this);
        if(b == null) {
            return findCommandComponent(c, parent.getTitleArea());
        }
        return b;
    }
    
    private Button findCommandComponent(Command c, Container cnt) {
        int count = cnt.getComponentCount();
        for(int iter = 0 ; iter < count ; iter++) {
            Component current = cnt.getComponentAt(iter);
            if(current instanceof Button) {
                Button b = (Button)current;
                if(b.getCommand() == c) {
                    return b;
                }
            } else {
                if(current instanceof Container) {
                    findCommandComponent(c, (Container)current);
                }
            }
        }
        return null;
    }

    private void moveCommandToTitle(Container title, Command c) {
        Button b = findCommandComponent(c);
        if(b != null) {
            b.getParent().removeComponent(b);
        } else {
            b = new Button(c);
        }
        b.setUIID("TitleCommand");
        title.addComponent(b);
    }

    private void adaptTitleLayoutBackCommandStructure() {
        Container t = parent.getTitleArea();
        if(t.getComponentCount() == 3) {
            return;
        }
        BorderLayout titleLayout = (BorderLayout)t.getLayout();
        titleLayout.setCenterBehavior(BorderLayout.CENTER_BEHAVIOR_CENTER_ABSOLUTE);
        t.removeAll();
        t.addComponent(BorderLayout.CENTER, parent.getTitleComponent());
        Container leftContainer = new Container(new BoxLayout(BoxLayout.X_AXIS));
        Container rightContainer = new Container(new BoxLayout(BoxLayout.X_AXIS));
        t.addComponent(BorderLayout.EAST, rightContainer);
        t.addComponent(BorderLayout.WEST, leftContainer);
    }

    private Container findLeftTitleContainer() {
        return (Container)((BorderLayout)parent.getTitleArea().getLayout()).getWest();
    }

    private Container findRightTitleContainer() {
        return (Container)((BorderLayout)parent.getTitleArea().getLayout()).getEast();
    }

    private void updateTitleCommandPlacement() {
        int commandBehavior = getCommandBehavior();
        Container t = parent.getTitleArea();
        BorderLayout titleLayout = (BorderLayout)t.getLayout();
        if(getParent() == null) {
            installMenuBar();
        } else {
            if(getParent() == parent.getTitleArea() && commandBehavior != Display.COMMAND_BEHAVIOR_BUTTON_BAR_TITLE_RIGHT) {
                getParent().removeComponent(this);
                installMenuBar();
            }
        }
        if(!(parent instanceof Dialog)) {
            if(commandBehavior == Display.COMMAND_BEHAVIOR_BUTTON_BAR_TITLE_BACK  && parent.getTitle() != null && parent.getTitle().length() > 0) {
                synchronizeCommandsWithButtonsInBackbutton();

                /*adaptTitleLayoutBackCommandStructure();
                Container leftContainer = findLeftTitleContainer();
                Container rightContainer = findRightTitleContainer();

                if(parent.getTitle() != null && parent.getTitle().length() > 0) {
                    if(backCommand != null) {
                        Button b = new Button(backCommand);
                        b.setUIID("BackCommand");
                        leftContainer.addComponent(b);
                        removeCommand(backCommand);
                    }

                    if(Display.getInstance().isTablet()) {
                        if(getCommandCount() > 0) {
                            moveCommandToTitle(rightContainer, getCommand(0));
                        }
                        if(getCommandCount() > 1) {
                            moveCommandToTitle(leftContainer, getCommand(1));
                        }
                        if(getCommandCount() > 2) {
                            moveCommandToTitle(rightContainer, getCommand(2));
                        }
                        if(getCommandCount() > 3 && backCommand == null) {
                            moveCommandToTitle(leftContainer, getCommand(3));
                        }
                    } else {
                        if(getCommandCount() > 0) {
                            moveCommandToTitle(rightContainer, getCommand(0));
                        }
                        if(getCommandCount() > 1 && backCommand == null) {
                            moveCommandToTitle(leftContainer, getCommand(1));
                        }
                    }
                }*/

                return;
            } else {
                if(commandBehavior == Display.COMMAND_BEHAVIOR_BUTTON_BAR_TITLE_RIGHT ||
                        commandBehavior == Display.COMMAND_BEHAVIOR_BUTTON_BAR_TITLE_BACK) {
                    if(getParent() != null) {
                        if(getParent() == parent.getTitleArea()) {
                            return;
                        }
                        getParent().removeComponent(this);
                    }
                    parent.getTitleArea().addComponent(BorderLayout.EAST, this);
                    return;
                }
            }
        }
        if(t.getComponentCount() > 1) {
            titleLayout.setCenterBehavior(BorderLayout.CENTER_BEHAVIOR_SCALE);
            Label l = parent.getTitleComponent();
            t.removeAll();
            t.addComponent(BorderLayout.CENTER, l);
        }
    }

    /**
     * Indicates the command that is defined as the back command out of this form.
     * A back command can be used both to map to a hardware button (e.g. on the Sony Ericsson devices)
     * and by elements such as transitions etc. to change the behavior based on 
     * direction (e.g. slide to the left to enter screen and slide to the right to exit with back).
     * 
     * @param backCommand the command to treat as the back Command
     */
    public void setBackCommand(Command backCommand) {
        if(!commands.contains(backCommand)) {
            if(!isNativeCommandBehavior()) {
                addCommand(backCommand);
            }else {
                commands.addElement(backCommand);
            }
        }
        /**
         * The backstack is used to hold previous command that were backcommands.
         * The current backCommand is always in the backCommand-variable and the rest 
         * are in the stack.
         */
        if(this.backCommand != null) {
            backStack.push(this.backCommand);
        }
        else if(backStack.contains(backCommand)) {
            backStack.removeElement(backCommand);
        }
        
        this.backCommand = backCommand;
        
        if(getCommandBehavior() == Display.COMMAND_BEHAVIOR_BUTTON_BAR_TITLE_BACK) {
            int i = commands.indexOf(backCommand);
            if(i > -1) {
                commands.removeElementAt(i);
            }
        }
        updateTitleCommandPlacement();
        // 'back' gets special treatment so update softkey actions
        if(isNativeCommandBehavior()) {
            S40Implementation impl = (S40Implementation)Display.getInstance().getImplementation();
            if(Display.getInstance().getCurrent() == parent) {
                impl.setNativeBackCommand(backCommand);
            }
        }else{
            updateCommands();
        }
    }

    /**
     * Indicates the command that is defined as the back command out of this form.
     * A back command can be used both to map to a hardware button (e.g. on the Sony Ericsson devices)
     * and by elements such as transitions etc. to change the behavior based on 
     * direction (e.g. slide to the left to enter screen and slide to the right to exit with back).
     * 
     * @return the command to treat as the back Command
     */
    public Command getBackCommand() {
        return backCommand;
    }

    /**
     * The selectCommand is the command to invoke when a Component has foucs in
     * Third Soft Button state.
     * 
     * @return the select command
     */
    public Command getSelectCommand() {
        return selectCommand;
    }

    /**
     * Sets the select command
     * 
     * @param selectCommand
     */
    public void setSelectCommand(Command selectCommand) {
        this.selectCommand = selectCommand;
    }

    /**
     * Updates the command mapping to the softbuttons
     */
    private void updateCommands() {
        if(isNativeCommandBehavior()) {
            //prevent platform commands from flickering by making sure
            //the form is visible
            if(Display.getInstance().getCurrent() == parent) {
                Display.getInstance().getImplementation().setNativeCommands(commands);
            }
            return;
        }
        int commandCount = getCommandCount();
        // first we build an array of all the commands that are *not* the back
        // or default command. We can then pick from this array for the rest of
        // the softkeys without danger of picking the back command again.  Also
        // we iterate over the commands in reverse because Form.addCommand
        // always adds commands to the beginning.
        int numberOfMiscCommands = 0;
        Command commandsWithoutBackOrDefault[] = new Command[commandCount];
        for (int i = commandCount-1, j = 0; i >= 0; --i) {
            Command c = getCommand(i);

            if (c != backCommand && c != defaultCommand && 
                c != clearCommand && c != selectCommand) {
                commandsWithoutBackOrDefault[j++] = c;
                numberOfMiscCommands++;
            }
        }
        int unassignedMiscCommands = numberOfMiscCommands;
        // Number of softkey buttons that can have any action. RSK can
        // only have 'back'.
        int freeButtons = soft.length - 1;

        // Reset all softkeys, remove titles, commands and icons.
        // This way we don't have to care about the unused keys in cases
        // where we have more softkeys than commands.
        for (int i = 0; i < soft.length; ++i) {
            soft[i].setText("");
            soft[i].setIcon(null);
            softCommand[i] = null;
        }
        
        /* We do different things for different softkey layouts. Three button
         * case first:
         */
        if (soft.length == 3) {
            /* For three softkeys we would like to put the default or preferred
             * or most-used command on the middle. We really have no way of
             * definitively knowing which one that is however, so we hazard a
             * guess and pick the first one.
             * 
             * The back or exit command always goes on the right, and then
             * we fill the left softkey with the second action added.
             */

            // First handle RSK: put back there if defined
            if (backCommand != null) {
                // There is a back command so put it on the right
                softCommand[2] = backCommand;
            }
            //if clear command is present, overide RSK to that
            if(clearCommand != null) {
                softCommand[2] = clearCommand;
                
            }
            //by default we set empty command in MSK
            if (numberOfMiscCommands > 0) { 
                softCommand[0] = new Command("");
            }
            // Then check default and select cmds. Select has higher
            //priority than default.
            if (defaultCommand != null) {
                softCommand[0] = defaultCommand;
                freeButtons--;
            }
            if(selectCommand != null) {
                softCommand[0] = selectCommand;
                freeButtons--;
                if(defaultCommand != null) {
                    unassignedMiscCommands++;
                }
            }
            
            // If there are more commands left, show a menu
            if (unassignedMiscCommands > 0) {
                softCommand[1] = menuCommand;
            }
        } else if (soft.length == 2) {
            /* For two softkeys we put back on the rsk, and either
             * a) menu on the lsk if there are more than 2 commands
             * b) the other command on the lsk if there are 2
             */
            if (backCommand != null) {
                // There is a back command so put it on the rsk
                softCommand[1] = backCommand;
            }
            if (commandCount > 2) {
                // Too many commands for softkeys, menu on lsk
                softCommand[0] = menuCommand;
            } else if (commandCount == 2) {
                // All commands fit on softkeys. Was there a back command?
                if (backCommand == null) {
                    // No, so put second command on rsk
                    softCommand[1] = commandsWithoutBackOrDefault[1];
                }
                // First command on lsk
                softCommand[0] = commandsWithoutBackOrDefault[0];
            } else if (commandCount == 1) {
                // Fewer than 2 commands -- only possibility is 1, don't do
                // anything for zero commands.
                if (backCommand == null) {
                    // The command is not back so put it on lsk
                    softCommand[0] = commandsWithoutBackOrDefault[0];
                } // if the only command was back, it's handled already.
                
            }
        } else if (soft.length == 1) {
            /* For single softkey situations (probably not even possible but
             * let's handle it anyway) we prefer menu instead of back.
             */
            if (commandCount > 1) {
                // menu on only key
                softCommand[0] = menuCommand;
            } else if (commandCount == 1) {
                // If it was back, it was handled already
                if (backCommand != null) {
                    // But it wasn't, so add it to the key
                    softCommand[0] = commandsWithoutBackOrDefault[0];
                }
            }
        } else {
            throw new RuntimeException("Can't handle " + soft.length + " softkeys");
        }
        
        /* Then set texts and icons for all softkeys
         */
        for (int i = 0; i < soft.length; ++i) {
            if (softCommand[i] != null) {            
                soft[i].setText(softCommand[i].getCommandName());
                soft[i].setIcon(softCommand[i].getIcon());
            }
        }
    }

    /**
     * Invoked when a softbutton is pressed
     */
    public void actionPerformed(ActionEvent evt) {
        if (evt.isConsumed()) {
            return;
        }
        Object src = evt.getSource();
        if (commandList == null) {
            Button source = (Button) src;
            for (int iter = 0; iter < soft.length; iter++) {
                if (source == soft[iter]) {
                    if (softCommand[iter] == menuCommand) {
                        showMenu();
                        return;
                    }
                    if (softCommand[iter] != null) {
                        ActionEvent e = new ActionEvent(softCommand[iter]);
                        softCommand[iter].actionPerformed(e);
                        if (!e.isConsumed()) {
                            parent.actionCommandImpl(softCommand[iter]);
                        }
                    }
                    return;
                }
            }
        } else {
            // the list for the menu sent the event
            if (src instanceof Button) {
                for (int iter = 0; iter < soft.length; iter++) {
                    if (src == soft[iter]) {
                        Container parent = commandList.getParent();
                        while (parent != null) {
                            if (parent instanceof Dialog) {
                                ((Dialog) parent).actionCommand(softCommand[iter]);
                                return;
                            }
                            parent = parent.getParent();
                        }
                    }
                }
            }
            Command c = getComponentSelectedCommand(commandList);
            if(!c.isEnabled()) {
                return;
            }
            Container p = commandList.getParent();
            while (p != null) {
                if (p instanceof Dialog) {
                    ((Dialog) p).actionCommand(c);
                    return;
                }
                p = p.getParent();
            }
        }

    }

    /**
     * Creates a soft button Component
     * @return the softbutton component
     */
    protected Button createSoftButton(String uiid) {
        Button b = new Button();
        b.setUIID(uiid);
        b.addActionListener(this);
        b.setFocusable(false);
        b.setTactileTouch(true);
        updateSoftButtonStyle(b);
        return b;
    }

    private void updateSoftButtonStyle(Button b) {
        if (softkeyCount < 2) {
            b.getStyle().setMargin(0, 0, 0, 0);
            b.getStyle().setPadding(0, 0, 0, 0);
        }
    }

    /**
     * @inheritDoc
     */
    public void setUnselectedStyle(Style style) {
        style.setMargin(Component.TOP, 0, true);
        style.setMargin(Component.BOTTOM, 0, true);
        super.setUnselectedStyle(style);
        if (soft != null) {
            for (int iter = 0; iter < soft.length; iter++) {
                updateSoftButtonStyle(soft[iter]);
            }
        }
    }

    /**
     * Prevents scaling down of the menu when there is no text on the menu bar 
     */
    protected Dimension calcPreferredSize() {
        if (soft.length > 1) {
            Dimension d = super.calcPreferredSize();
            if ((soft[0].getText() == null || soft[0].getText().equals("")) &&
                    (soft[1].getText() == null || soft[1].getText().equals("")) &&
                    soft[0].getIcon() == null && soft[1].getIcon() == null &&
                    (soft.length < 3 ||
                    ((soft[2].getText() == null || soft[2].getText().equals("")) && soft[2].getIcon() == null))) {
                d.setHeight(0);
            }
            return d;
        }
        return super.calcPreferredSize();
    }

    /**
     * Sets the menu transitions for showing/hiding the menu, can be null...
     */
    public void setTransitions(Transition transitionIn, Transition transitionOut) {
        this.transitionIn = transitionIn;
        this.transitionOut = transitionOut;
    }

    /**
     * This method shows the menu on the Form.
     * The method creates a Dialog with the commands and calls showMenuDialog.
     * The method blocks until the user dispose the dialog.
     */
    public void showMenu() {
        final Dialog d = new Dialog("Menu", "");
        d.setDisposeWhenPointerOutOfBounds(true);
        d.setMenu(true);

        d.setTransitionInAnimator(transitionIn);
        d.setTransitionOutAnimator(transitionOut);
        d.setLayout(new BorderLayout());
        d.setScrollable(false);
        
        ((Form) d).getMenuBar().commandList = createCommandComponent(commands);
        if (menuCellRenderer != null && ((Form) d).getMenuBar().commandList instanceof List) {
            ((List) ((Form) d).getMenuBar().commandList).setListCellRenderer(menuCellRenderer);
        }
        d.getContentPane().getStyle().setMargin(0, 0, 0, 0);
        d.addComponent(BorderLayout.CENTER, ((Form) d).getMenuBar().commandList);
        //if (thirdSoftButton) {
            //d.addCommand(selectMenuItem);
            //d.addCommand(cancelMenuItem);
        //} else {
            //d.addCommand(cancelMenuItem);
            //if (soft.length > 1) {
                //d.addCommand(selectMenuItem);
            //}
        //}
        if (!Display.getInstance().isTouchScreenDevice()) {
            d.addCommand(selectMenuItem);
            d.setDefaultCommand(selectMenuItem);
        }
        //d.setClearCommand(cancelMenuItem);
        d.addCommand(cancelMenuItem);
        d.setBackCommand(cancelMenuItem);

        if (((Form) d).getMenuBar().commandList instanceof List) {
            ((List) ((Form) d).getMenuBar().commandList).addActionListener(((Form) d).getMenuBar());
        }
        Command result = showMenuDialog(d);
        if (result != cancelMenuItem) {
            Command c = null;
            if (result == selectMenuItem) {
                c = getComponentSelectedCommand(((Form) d).getMenuBar().commandList);
                if (c != null) {
                    ActionEvent e = new ActionEvent(c);
                    c.actionPerformed(e);
                }
            } else {
                c = result;
                // a touch menu will always send its commands on its own...
                if (!UIManager.getInstance().getLookAndFeel().isTouchMenus()) {
                    c = result;
                    if (c != null) {
                        ActionEvent e = new ActionEvent(c);
                        c.actionPerformed(e);
                    }
                }
            }
            // menu item was handled internally in a touch interface that is not a touch menu
            if (c != null) {
                parent.actionCommandImpl(c);
            }
        }
        if (((Form) d).getMenuBar().commandList instanceof List) {
            ((List) ((Form) d).getMenuBar().commandList).removeActionListener(((Form) d).getMenuBar());
        }

        Form upcoming = Display.getInstance().getCurrentUpcoming();
        if (upcoming == parent) {
            d.disposeImpl();
        } else {
            parent.tint = (upcoming instanceof Dialog);
        }
    }

    public Button[] getSoftButtons() {
        return soft;
    }

    private void addTwoTitleButtons(Container leftContainer, Container rightContainer) {
        ensureCommandsInContainer(getCommand(0), null, rightContainer, "TitleCommand", null);
        if(parent.getBackCommand() != null) {
             ensureCommandsInContainer(parent.getBackCommand(), null, leftContainer, "BackCommand", null);
            updateGridCommands(1);
        } else {
             ensureCommandsInContainer(getCommand(1), null, leftContainer, "TitleCommand", null);
            updateGridCommands(2);
        }
    }

    private void updateGridCommands(int startOffset) {
        int cmdCount = getCommandCount() - startOffset;
        if(cmdCount <= 0) {
            return;
        }
        setLayout(new GridLayout(1, cmdCount));
        while(cmdCount < getComponentCount()) {
            removeComponent(getComponentAt(getComponentCount() - 1));
        }
        int off = startOffset;
        while(getComponentCount() < cmdCount) {
            Button btn = new Button(getCommand(off));
            btn.setUIID("TouchCommand");
            off++;
            addComponent(btn);
        }
        for(int iter = 0 ; iter < cmdCount ; iter++) {
            Button btn = (Button)getComponentAt(iter);
            if(btn.getCommand() != getCommand(iter + startOffset)) {
                btn.setCommand(getCommand(iter + startOffset));
            }
        }
    }

    private void synchronizeCommandsWithButtonsInBackbutton() {
        adaptTitleLayoutBackCommandStructure();
        Container leftContainer = findLeftTitleContainer();
        Container rightContainer = findRightTitleContainer();

        int componentCount = getCommandCount();
        if(parent.getBackCommand() != null) {
            if(leftContainer.getComponentCount() == 0) {
                Button back = new Button(parent.getBackCommand());
                leftContainer.addComponent(back);
                back.setUIID("BackCommand");
            } else {
                Button b = (Button)leftContainer.getComponentAt(0);
                if(b.getCommand() != parent.getBackCommand()) {
                    b.setCommand(parent.getBackCommand());
                    b.setUIID("BackCommand");
                }
            }
            componentCount++;
        }
        switch(componentCount) {
            case 0:
                leftContainer.removeAll();
                rightContainer.removeAll();
                removeAll();
                break;
            case 1:
                if(parent.getBackCommand() != null) {
                     rightContainer.removeAll();
                     ensureCommandsInContainer(parent.getBackCommand(), null, leftContainer, "BackCommand", null);
                } else {
                     leftContainer.removeAll();
                     ensureCommandsInContainer(getCommand(0), null, rightContainer, "TitleCommand", null);
                }
                removeAll();
                break;
            case 2:
                addTwoTitleButtons(leftContainer, rightContainer);
                break;
            case 3:
                if(Display.getInstance().isTablet()) {
                    ensureCommandsInContainer(getCommand(0), getCommand(2), rightContainer, "TitleCommand", "TitleCommand");
                    if(parent.getBackCommand() != null) {
                         ensureCommandsInContainer(parent.getBackCommand(), null, leftContainer, "BackCommand", null);
                    } else {
                         ensureCommandsInContainer(getCommand(1), null, leftContainer, "TitleCommand", null);
                    }
                    removeAll();
                } else {
                    addTwoTitleButtons(leftContainer, rightContainer);
                }
                break;
            default:
                if(Display.getInstance().isTablet()) {
                     ensureCommandsInContainer(getCommand(0), getCommand(2), rightContainer, "TitleCommand", "TitleCommand");
                    if(parent.getBackCommand() != null) {
                         ensureCommandsInContainer(parent.getBackCommand(), getCommand(1), leftContainer, "BackCommand", "TitleCommand");
                        updateGridCommands(3);
                    } else {
                         ensureCommandsInContainer(getCommand(1), getCommand(3), leftContainer, "TitleCommand", "TitleCommand");
                        updateGridCommands(4);
                    }
                } else {
                    addTwoTitleButtons(leftContainer, rightContainer);
                }
                break;
        }
    }

    private void ensureCommandsInContainer(Command a, Command b, Container c, String styleA, String styleB) {
        if(c.getComponentCount() == 0) {
            Button btn = new Button(a);
            btn.setUIID(styleA);
            c.addComponent(btn);
            if(b != null) {
                btn = new Button(b);
                btn.setUIID(styleB);
                c.addComponent(btn);
            }
            return;
        }
        if(c.getComponentCount() == 1) {
            Button btn = (Button)c.getComponentAt(0);
            btn.setUIID(styleA);
            if(btn.getCommand() != a) {
                btn.setCommand(a);
            }
            if(b != null) {
                btn = new Button(b);
                btn.setUIID(styleB);
                c.addComponent(btn);
            }
            return;
        }
        if(c.getComponentCount() == 2) {
            Button btn = (Button)c.getComponentAt(0);
            btn.setUIID(styleA);
            if(btn.getCommand() != a) {
                btn.setCommand(a);
            }
            if(b != null) {
                btn = (Button)c.getComponentAt(1);
                btn.setUIID(styleB);
                if(btn.getCommand() != b) {
                    btn.setCommand(b);
                }
            } else {
                c.removeComponent(c.getComponentAt(1));
            }
            return;
        }
    }

    /**
     * Adds a Command to the MenuBar
     * 
     * @param cmd Command to add
     */
    public void addCommand(Command cmd) {
        // prevent duplicate commands which might happen in some edge cases
        // with the select command
        if (commands.contains(cmd)) {
            return;
        }
        
        // special case for default commands which are placed at the end and aren't overriden later
        if (soft.length > 2 && cmd == parent.getDefaultCommand()) {
            commands.addElement(cmd);
        } else {
            commands.insertElementAt(cmd, 0);
        }
        if(!(parent instanceof Dialog)) {
            int behavior = getCommandBehavior();
            if(behavior == Display.COMMAND_BEHAVIOR_BUTTON_BAR || behavior == Display.COMMAND_BEHAVIOR_BUTTON_BAR_TITLE_BACK ||
                    behavior == Display.COMMAND_BEHAVIOR_BUTTON_BAR_TITLE_RIGHT) {
                if(behavior == Display.COMMAND_BEHAVIOR_BUTTON_BAR_TITLE_BACK && (cmd == parent.getBackCommand() ||
                        findCommandComponent(cmd) != null)) {
                    return;
                }
                if(parent.getBackCommand() != cmd) {
                    if(behavior == Display.COMMAND_BEHAVIOR_BUTTON_BAR_TITLE_BACK && parent.getTitle() != null && parent.getTitle().length() > 0) {
                        synchronizeCommandsWithButtonsInBackbutton();
                        return;
                    }
                    
                    setLayout(new GridLayout(1, getCommandCount()));
                    addComponent(createTouchCommandButton(cmd));
                } else {
                    commands.removeElement(cmd);
                }   
                return;
            }
        }
        //for native commands we don't want update everything if the command is at the
        //end of the commands vector
        if(isNativeCommandBehavior()) {
            S40Implementation impl = (S40Implementation)Display.getInstance().getImplementation();
            if(Display.getInstance().getCurrent() == parent) {
                impl.addNativeCommand(cmd);
            }
        }else {
            if(!isMenuBarInstalled()) {
                installMenuBar();
            }
            updateCommands();
            repaint();
        }
    }

    /**
     * Returns the command occupying the given index
     * 
     * @param index offset of the command
     * @return the command at the given index
     */
    public Command getCommand(int index) {
        return (Command) commands.elementAt(index);
    }

    /**
     * Returns number of commands
     * 
     * @return number of commands
     */
    public int getCommandCount() {
        return commands.size();
    }

    /**
     * Add a Command to the MenuBar
     * 
     * @param cmd Command to Add
     * @param index determines the order of the added commands
     */
    protected void addCommand(Command cmd, int index) {
        if(!isMenuBarInstalled()) {
            installMenuBar();
        }
        // prevent duplicate commands which might happen in some edge cases
        // with the select command
        if (commands.contains(cmd)) {
            return;
        }
        commands.insertElementAt(cmd, index);
        if(!(parent instanceof Dialog)) {
            int behavior = getCommandBehavior();
            if(behavior == Display.COMMAND_BEHAVIOR_BUTTON_BAR || 
                    behavior == Display.COMMAND_BEHAVIOR_BUTTON_BAR_TITLE_BACK ||
                    behavior == Display.COMMAND_BEHAVIOR_BUTTON_BAR_TITLE_RIGHT) {
                if(behavior == Display.COMMAND_BEHAVIOR_BUTTON_BAR_TITLE_BACK && cmd == parent.getBackCommand()) {
                    return;
                }
                if(behavior == Display.COMMAND_BEHAVIOR_BUTTON_BAR_TITLE_BACK  && parent.getTitle() != null && parent.getTitle().length() > 0) {
                    synchronizeCommandsWithButtonsInBackbutton();
                    return;
                }
                if(parent.getBackCommand() != cmd) {
                    setLayout(new GridLayout(1, getCommandCount()));
                    addComponent(index, createTouchCommandButton(cmd));
                    revalidate();
                } else {
                    commands.removeElement(cmd);
                }
                return;
            }
        }
        updateCommands();
        
        // If this was the first command we added, we apparently need to
        // trigger a repaint on the parent to make the menubar visible.
        // This used to be at the end of updateCommands() but I really 
        // think the more proper place is here.
        if (getCommandCount() == 1) {
            if (parent.isVisible()) {
                parent.revalidate();
            }
        }
        repaint();
    }

    /**
     * Adds the MenuBar on the parent Form
     */
    protected void installMenuBar() {
        LWUITImplementation impl = Display.getInstance().getImplementation();
        if (impl instanceof S40Implementation) {
            if (!((S40Implementation) impl).shouldHideMenu()) {
                if (getParent() == null) {
                    int type = Display.getInstance().getCommandBehavior();
                    if (type == Display.COMMAND_BEHAVIOR_BUTTON_BAR_TITLE_RIGHT) {
                        parent.getTitleArea().addComponent(BorderLayout.EAST, this);
                        return;
                    }
                    if (softkeyCount > 1 || type == Display.COMMAND_BEHAVIOR_BUTTON_BAR
                            || type == Display.COMMAND_BEHAVIOR_BUTTON_BAR_TITLE_BACK) {
                        parent.addComponentToForm(BorderLayout.SOUTH, this);
                    }
                }
            }
        }
    }

    /**
     * Removes the MenuBar from the parent Form
     */
    protected void unInstallMenuBar() {
        parent.removeComponentFromForm(this);
        Container t = parent.getTitleArea();
        BorderLayout titleLayout = (BorderLayout)t.getLayout();
        titleLayout.setCenterBehavior(BorderLayout.CENTER_BEHAVIOR_SCALE);
        Label l = parent.getTitleComponent();
        t.removeAll();
        if(l.getParent() != null) {
            l.getParent().removeComponent(l);
        }
        t.addComponent(BorderLayout.CENTER, l);
    }

    /**
     * Remove all commands from the menuBar
     */
    protected void removeAllCommands() {
        defaultCommand = null;
        backStack.removeAllElements();
        backCommand = null;
        clearCommand = null;
        commands.removeAllElements();
        int behavior = getCommandBehavior();
        if(behavior == Display.COMMAND_BEHAVIOR_BUTTON_BAR || 
                behavior == Display.COMMAND_BEHAVIOR_BUTTON_BAR_TITLE_BACK ||
                behavior == Display.COMMAND_BEHAVIOR_BUTTON_BAR_TITLE_RIGHT) {
            parent.getTitleArea().removeAll();
            parent.getTitleArea().addComponent(BorderLayout.CENTER, parent.getTitleComponent());
            removeAll();
            return;
        }
        updateCommands();
    }

    /**
     * Removes a Command from the MenuBar
     * 
     * @param cmd Command to remove
     */
    protected void removeCommand(Command cmd) {
        if (cmd != null) {
            int behavior = getCommandBehavior();
            if (behavior == Display.COMMAND_BEHAVIOR_BUTTON_BAR
                    || behavior == Display.COMMAND_BEHAVIOR_BUTTON_BAR_TITLE_BACK
                    || behavior == Display.COMMAND_BEHAVIOR_BUTTON_BAR_TITLE_RIGHT) {
                int i = commands.indexOf(cmd);
                if (i > -1) {
                    commands.removeElementAt(i);
                    Button b = findCommandComponent(cmd);
                    if (b != null) {
                        removeComponent(b);
                    }
                    if (getCommandCount() > 0) {
                        setLayout(new GridLayout(1, getCommandCount()));
                    }
                }
                return;
            }
            commands.removeElement(cmd);
            backStack.removeElement(cmd);
            if(cmd == defaultCommand) {
                defaultCommand = null;
            }
            if(cmd == clearCommand) {
                clearCommand = null;
            }
            if(cmd == backCommand) {
                backCommand = (backStack.size() > 0) ? (Command)backStack.pop() : null;
            }
            if(isNativeCommandBehavior()) {
                if(Display.getInstance().getImplementation() instanceof S40Implementation) {
                    S40Implementation impl = (S40Implementation) Display.getInstance().getImplementation();
                    impl.removeNativeCommand(cmd);
                }
            }else {
                updateCommands();
                repaint();
            }
        }
    }

    void addSelectCommand(String selectText) {
        if (selectText != null && selectText.length() > 0) {
            if (thirdSoftButton) {
                if (selectCommand == null) {
                    selectCommand = createSelectCommand();
                }
                selectCommand.setCommandName(selectText);
                addCommand(selectCommand);
            }
        }
    }

    void removeSelectCommand() {
        if (thirdSoftButton) {
            removeCommand(selectCommand);
        }
    }

    /**
     * Factory method that returns the Form select Command.
     * This Command is used when Display.getInstance().isThirdSoftButton() 
     * returns true.
     * This method can be overridden to customize the Command on the Form.
     * 
     * @return Command
     */
    protected Command createSelectCommand() {
        return new Command(UIManager.getInstance().localize("select", "Select"));
    }

    /**
     * Factory method that returns the Form Menu select Command.
     * This method can be overridden to customize the Command on the Form.
     * 
     * @return Command
     */
    protected Command createMenuSelectCommand() {
        LookAndFeel lf = UIManager.getInstance().getLookAndFeel();
        return new Command(UIManager.getInstance().localize("select", "Select"), lf.getMenuIcons()[0]);
    }

    /**
     * Factory method that returns the Form Menu cancel Command.
     * This method can be overridden to customize the Command on the Form.
     * 
     * @return Command
     */
    protected Command createMenuCancelCommand() {
        LookAndFeel lf = UIManager.getInstance().getLookAndFeel();
        return new Command(UIManager.getInstance().localize("cancel", "Cancel"), lf.getMenuIcons()[1]);
    }

    /**
     * The MenuBar default implementation shows the menu commands in a List 
     * contained in a Dialog.
     * This method replaces the menu ListCellRenderer of the Menu List.
     * 
     * @param menuCellRenderer
     */
    public void setMenuCellRenderer(ListCellRenderer menuCellRenderer) {
        this.menuCellRenderer = menuCellRenderer;
    }

    /**
     * Returns the Menu Dialog Style
     * 
     * @return Menu Dialog Style
     */
    public Style getMenuStyle() {
        return menuStyle;
    }

    static boolean isLSK(int keyCode) {
        return keyCode == leftSK;
    }

    static boolean isRSK(int keyCode) {
        return keyCode == rightSK || keyCode == rightSK2;
    }

    /**
     * This method returns true if the MenuBar should handle the given keycode.
     * 
     * @param keyCode to determine if the MenuBar is responsible for.
     * @return true if the keycode is a MenuBar related keycode such as softkey,
     * back button, clear button, ...
     */
    public boolean handlesKeycode(int keyCode) {
        int game = Display.getInstance().getGameAction(keyCode);
        if (keyCode == leftSK || (keyCode == rightSK || keyCode == rightSK2) || keyCode == backSK ||
                (keyCode == clearSK && clearCommand != null) ||
                (keyCode == backspaceSK && clearCommand != null) ||
                (thirdSoftButton && game == Display.GAME_FIRE)) {
            return true;
        }
        return false;
    }

    /**
     * @inheritDoc
     */
    public void keyPressed(int keyCode) {
        int commandBehavior = getCommandBehavior();
        if(commandBehavior >= Display.COMMAND_BEHAVIOR_BUTTON_BAR) {
            return;
        }
        if (getCommandCount() > 0) {
            if (keyCode == leftSK) {
                if (left != null) {
                    left.pressed();
                }
            } else {
                // it might be a back command or the fire...
                if ((keyCode == rightSK || keyCode == rightSK2)) {
                    if (right != null) {
                        right.pressed();
                    }
                } else {
                    if (Display.getInstance().getGameAction(keyCode) == Display.GAME_FIRE) {
                        main.pressed();
                    }
                }
            }
        }
    }

    /**
     * @inheritDoc
     */
    public void keyReleased(int keyCode) {
        int commandBehavior = getCommandBehavior();
        if(commandBehavior >= Display.COMMAND_BEHAVIOR_BUTTON_BAR && keyCode != backSK && keyCode != clearSK && keyCode != backspaceSK) {
            return;
        }
        if (getCommandCount() > 0) {
            if (softkeyCount < 2 && keyCode == leftSK) {
                if (commandList != null) {
                    Container parent = commandList.getParent();
                    while (parent != null) {
                        if (parent instanceof Dialog && ((Dialog) parent).isMenu()) {
                            return;
                        }
                        parent = parent.getParent();
                    }
                }
                showMenu();
                return;
            } else {
                if (keyCode == leftSK) {
                    if (left != null) {
                        left.released();
                    }
                    return;
                } else {
                    // it might be a back command...
                    if ((keyCode == rightSK || keyCode == rightSK2)) {
                        if (right != null) {
                            right.released();
                        }
                        return;
                    } else {
                        if (Display.getInstance().getGameAction(keyCode) == Display.GAME_FIRE) {
                            main.released();
                            return;
                        }
                    }
                }
            }
        }

        // allows a back/clear command to occur regardless of whether the
        // command was added to the form
        Command c = null;
        if (keyCode == backSK) {
            // the back command should be invoked
            c = parent.getBackCommand();
        } else {
            if (keyCode == clearSK || keyCode == backspaceSK) {
                c = getClearCommand();
            }
        }
        if (c != null) {
            ActionEvent ev = new ActionEvent(c, keyCode);
            c.actionPerformed(ev);
            if (!ev.isConsumed()) {
                parent.actionCommandImpl(c);
            }
        }
    }

    /**
     * @inheritDoc
     */
    public void refreshTheme() {
        super.refreshTheme();
        if (menuStyle.isModified()) {
            menuStyle.merge(UIManager.getInstance().getComponentStyle("Menu"));
        } else {
            menuStyle = UIManager.getInstance().getComponentStyle("Menu");
        }
        if (menuCellRenderer != null) {
            List tmp = new List();
            tmp.setListCellRenderer(menuCellRenderer);
            tmp.refreshTheme();
        }
        for (int iter = 0; iter < soft.length; iter++) {
            updateSoftButtonStyle(soft[iter]);
        }

        revalidate();
    }

    /*private void fixCommandAlignment() {
        if (left != null) {
            if (parent.isRTL()) {
                left.setAlignment(Label.RIGHT);
                right.setAlignment(Label.LEFT);
            } else {
                left.setAlignment(Label.LEFT);
                right.setAlignment(Label.RIGHT);
            }
            if(main != null && main != left && main != right) {
                main.setAlignment(CENTER);
            }
        }
    }*/

    /**
     * A menu is implemented as a dialog, this method allows you to override dialog
     * display in order to customize the dialog menu in various ways
     * 
     * @param menu a dialog containing menu options that can be customized
     * @return the command selected by the user in the dialog (not menu) Select 
     * or Cancel
     */
    protected Command showMenuDialog(Dialog menu) {
        boolean pref = UIManager.getInstance().isThemeConstant("menuPrefSizeBool", false);

        Style style = menu.getDialogStyle();
        int height = parent.getHeight();
        int marginLeft = style.getMargin(LEFT);
        int marginRight = style.getMargin(RIGHT);
        int marginTop = style.getMargin(TOP);
        int marginBottom = style.getMargin(BOTTOM);
        int paddingTop = style.getPadding(TOP);
        int paddingBottom = style.getPadding(BOTTOM);

        Container dialogContentPane = menu.getDialogComponent();
        if(pref) {
            marginLeft = parent.getWidth() - (dialogContentPane.getPreferredW() +
                    menu.getStyle().getPadding(LEFT) +
                    menu.getStyle().getPadding(RIGHT));
            marginLeft = Math.max(0, marginLeft);
            if(parent.getSoftButtonCount() > 1) {
                height = height - parent.getSoftButton(0).getParent().getPreferredH() - dialogContentPane.getPreferredH();
            } else {
                height = height - dialogContentPane.getPreferredH();
            }
            height = Math.max(0, height);
        } else {
            // Adjust the menu height according to the amount of commands
            int commandCount = 1;
            int commandHeight = 1;        
            int itemGap = 0;

            Component cmds = ((Form) menu).getMenuBar().commandList;

            // Menu should be either a Container or a List
            if(cmds instanceof Container) {
                commandCount = ((Container) cmds).getComponentCount();
                if(commandCount > 0) {
                    Component cmd = ((Container) cmds).getComponentAt(0);
                    Style cmdStyle = cmd.getStyle();
                    commandHeight = cmd.getPreferredH() + cmdStyle.getMargin(
                            TOP) + cmdStyle.getMargin(BOTTOM);
                }
            } else if(cmds instanceof List) {
                commandCount = ((List) cmds).getModel().getSize();
                Dimension commandSize = ((List) cmds).getElementSize(false, true);
                commandHeight = commandSize.getHeight();
                itemGap = ((List) cmds).getItemGap();
            }

            if(parent instanceof Form) {
                height -= ((Form)parent).getTitleArea().getPreferredH();
            }

            if (parent.getSoftButtonCount() > 0) {
                height -= parent.getSoftButton(0).getParent().getPreferredH();
            }
            height -= (marginBottom + paddingTop + paddingBottom);
            
            int maxVisibleCommands = (height + itemGap) / (commandHeight + itemGap);

            commandCount = Math.min(maxVisibleCommands, commandCount);
            int contentHeight = (commandHeight + itemGap) * commandCount - itemGap;
            Container dcp = menu.getContentPane();
            int innerpadding = dcp.getStyle().getPadding(Component.TOP) +
                                dcp.getStyle().getPadding(Component.BOTTOM);
            
            marginTop = height - contentHeight - innerpadding;
            if(marginTop < 0) {
                marginTop = 0;
            }
            return menu.show(marginTop, marginBottom, marginLeft, marginRight, false, true);
        }

        if (isReverseSoftButtons()) {
            marginRight = marginLeft;
            marginLeft = 0;
        }
        if (UIManager.getInstance().getLookAndFeel().isTouchMenus() && UIManager.getInstance().isThemeConstant("PackTouchMenuBool", true)) {
            return menu.showPacked(BorderLayout.SOUTH, true);
        } else {
            return menu.show(height, 0, marginLeft, marginRight, true);
        }
    }

    /**
     * Allows an individual form to reverse the layout direction of the softbuttons, this method is RTL
     * sensitive and might reverse the result based on RTL state
     * 
     * @return The value of UIManager.getInstance().getLookAndFeel().isReverseSoftButtons()
     */
    protected boolean isReverseSoftButtons() {
        LookAndFeel lf = UIManager.getInstance().getLookAndFeel();
        if (isRTL()) {
            return !lf.isReverseSoftButtons();
        }
        return lf.isReverseSoftButtons();
    }

    /**
     * Calculates the amount of columns to give to the touch commands within the 
     * grid
     * 
     * @param grid container that will be arranged in the grid containing the 
     * components
     * @return an integer representing the touch command grid size
     */
    protected int calculateTouchCommandGridColumns(Container grid) {
        int count = grid.getComponentCount();
        int maxWidth = 0;
        for (int iter = 0; iter < count; iter++) {
            Component c = grid.getComponentAt(iter);
            Style s = c.getUnselectedStyle(); 
            // bidi doesn't matter since this is just a summary of width
            maxWidth = Math.max(maxWidth, 
                    c.getPreferredW() + 
                    s.getMargin(false, LEFT) + s.getMargin(false, RIGHT));
        }
        return Math.max(2, Display.getInstance().getDisplayWidth() / maxWidth);
    }

    /**
     * Creates a touch command for use as a touch menu item
     * 
     * @param c command to map into the returned button
     * @return a button that would fire the touch command appropriately
     */
    protected Button createTouchCommandButton(Command c) {
        Button b = new Button(c);
        if(b.getIcon() == null) {
            // some themes look awful without any icon
            b.setIcon((Image)UIManager.getInstance().getThemeImageConstant("defaultCommandImage"));
        }
        b.setTactileTouch(true);
        b.setTextPosition(Label.BOTTOM);
        b.setEndsWith3Points(false);
        b.setUIID("TouchCommand");
        return b;
    }

    /**
     * Creates the component containing the commands within the given vector
     * used for showing the menu dialog, this method calls the createCommandList
     * method by default however it allows more elaborate menu creation.
     *
     * @param commands list of command objects
     * @return Component that will result in the parent menu dialog recieving a command event
     */
    protected Component createCommandComponent(Vector commands) {        
        // Create a touch based menu interface
        if (UIManager.getInstance().getLookAndFeel().isTouchMenus()) {
            Container menu = new Container();
            menu.setScrollableY(true);
            for (int iter = 0; iter < commands.size(); iter++) {
                Command c = (Command)commands.elementAt(iter);
                if (shouldCommandShowInMenu(c)) {
                    menu.addComponent(createTouchCommandButton(c));
                }
            }
            // S40 always has only 1 column in the menu
            int cols = 1;
            int rows = menu.getComponentCount();
            GridLayout g = new GridLayout(rows, cols);
            menu.setLayout(g);
            menu.setPreferredW(Display.getInstance().getDisplayWidth());
            return menu;
        }
        return createCommandList(commands);
    }
    private boolean shouldCommandShowInMenu(Command c) {
        /*
         * Add to menu only if command should not be in RSK or MSK
         * RSK: clear and back should never be in menu
         * MSK: default goes to menu if selectcommand is present otherwise
         * default is shown in MSK. Select never goes to menu.
         */
        return !(soft.length == 3 && c == softCommand[0]) &&
                c != backCommand && c != clearCommand;
    }

    /**
     * This method returns a Vector of Command objects
     * 
     * @return Vector of Command objects
     */
    protected Vector getCommands() {
        return commands;
    }

    
    /**
     * Creates the list component containing the commands within the given vector
     * used for showing the menu dialog
     * 
     * @param commands list of command objects
     * @return List object
     */
    protected List createCommandList(Vector commands) {
        Vector menucommands = new Vector();
        Command cmd = null;
        for(int i = 0; i < commands.size(); i++) {
            cmd = (Command) commands.elementAt(i);
            if(shouldCommandShowInMenu(cmd)) {
                menucommands.addElement(cmd);
            }
        }
        List l = new List(menucommands);
        l.setUIID("CommandList");
        Component c = (Component) l.getRenderer();
        c.setUIID("Command");
        c = l.getRenderer().getListFocusComponent(l);
        c.setUIID("CommandFocus");

        l.setFixedSelection(List.FIXED_NONE_CYCLIC);
        if(UIManager.getInstance().isThemeConstant("menuPrefSizeBool", false)) {
            // an entry way down in the list might be noticeably wider
            l.setListSizeCalculationSampleCount(50);
        }
        return l;
    }

    Command getComponentSelectedCommand(Component cmp) {
        if (cmp instanceof List) {
            List l = (List) cmp;
            return (Command) l.getSelectedItem();
        } else {
            cmp = cmp.getComponentForm().getFocused();
            if (cmp instanceof Button) {
                return ((Button) cmp).getCommand();
            }
        }
        // nothing to do for this case...
        return null;
    }

    /**
     * This method returns the select menu item, when a menu is opened
     * @return select Command
     */
    protected Command getSelectMenuItem() {
        return selectMenuItem;
    }

    /**
     * This method returns the cancel menu item, when a menu is opened
     * @return cancel Command
     */
    protected Command getCancelMenuItem() {
        return cancelMenuItem;
    }
    
    protected boolean isNativeCommandBehavior() {
        return getCommandBehavior() == Display.COMMAND_BEHAVIOR_NATIVE;
    }
    
    /**
     * Get commands associated with the menubar
     * @return 
     */
    public Command [] getSoftCommands() {
        return softCommand;
    }
    
    public boolean isMenuBarInstalled() {
        if(parent != null) {
            return parent.contains(this);
            
        }
        return false;
    }   
}
