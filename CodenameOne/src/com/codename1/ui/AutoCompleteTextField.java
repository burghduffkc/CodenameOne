/*
 * Copyright (c) 2012, Codename One and/or its affiliates. All rights reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation.  Codename One designates this
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
 * Please contact Codename One through http://www.codenameone.com/ if you 
 * need additional information or have any questions.
 */
package com.codename1.ui;

import com.codename1.ui.events.ActionEvent;
import com.codename1.ui.events.ActionListener;
import com.codename1.ui.layouts.BoxLayout;
import com.codename1.ui.list.DefaultListCellRenderer;
import com.codename1.ui.list.DefaultListModel;
import com.codename1.ui.list.FilterProxyListModel;
import com.codename1.ui.list.ListModel;

/**
 * This class is an editable TextField with predefined completion suggestion 
 * that shows up in a drop down menu while the user types in text
 *
 * @author Chen
 */
public class AutoCompleteTextField extends TextField {

    private Container popup;
    private FilterProxyListModel<String> filter;
    private ActionListener listener = new FormPointerListener();

    /**
     * Constructor with completion suggestions
     * @param completion a String array of suggestion for completion
     */ 
    public AutoCompleteTextField(String[] completion) {
        this(new DefaultListModel<String>(completion));
    }

    /**
     * Constructor with completion suggestions, filtering is automatic in this case
     * @param listModel a list model containing potential string suggestions
     */ 
    public AutoCompleteTextField(ListModel<String> listModel) {
        popup = new Container(new BoxLayout(BoxLayout.Y_AXIS));
        filter = new FilterProxyListModel<String>(listModel);
        popup.setScrollable(false);
    }

    /**
     * The default constructor is useful for cases of filter subclasses overriding the
     * getSuggestionModel value
     */
    public AutoCompleteTextField() {
        popup = new Container(new BoxLayout(BoxLayout.Y_AXIS));
        popup.setScrollable(false);
    }
    
    /**
     * @inheritDoc
     */
    @Override
    protected void initComponent() {
        super.initComponent();
        getComponentForm().addPointerPressedListener(listener);
    }

    /**
     * @inheritDoc
     */
    @Override
    protected void deinitialize() {
        super.deinitialize();
        getComponentForm().removePointerPressedListener(listener);
    }

    private void setParentText(String text) {
        super.setText(text);
    }

    /**
     * @inheritDoc
     */
    @Override
    public void setText(String text) {
        super.setText(text);
        if (text == null) {
            return;
        }
        if(filter(text)) {
            updateFilterList();
        } 
    }
    
    /**
     * In a case of an asynchronous filter this method can be invoked to refresh the completion list
     */
    protected void updateFilterList() {
        Form f = getComponentForm();
        if (f != null && popup.getParent() == null) {
            addPopup();
        }
        popup.revalidate();
    }
    
    /**
     * Subclasses can override this method to perform more elaborate filter operations
     * @param text the text to filter
     * @return true if the filter has changed the list, false if it hasn't or is working asynchronously
     */
    protected boolean filter(String text) {
        if(filter != null) {
            filter.filter(text);        
            return true;
        }
        return false;
    }
    
    /**
     * Returns the list model to show within the completion list
     * @return the list model can be anything
     */
    protected ListModel<String> getSuggestionModel() {
        return filter;
    }

    private void removePopup() {
        Form f = getComponentForm();
        if (f != null) {
            f.getLayeredPane().removeComponent(popup);
            popup.setParent(null);
            f.revalidate();
        }
    }

    private void addPopup() {
        Form f = getComponentForm();
        popup.removeAll();
        filter(getText());
        final com.codename1.ui.List l = new com.codename1.ui.List(getSuggestionModel());
        ((DefaultListCellRenderer<String>)l.getRenderer()).setShowNumbers(false);
        l.setUIID("AutoCompletePopup");
        l.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent evt) {
                setParentText((String) l.getSelectedItem());
                removePopup();
            }
        });
        popup.addComponent(l);
        popup.getStyle().setMargin(LEFT, getAbsoluteX());
        int top = getAbsoluteY() - f.getTitleArea().getHeight() + getHeight();
        popup.getStyle().setMargin(TOP, top);
        popup.setPreferredW(getWidth());
        popup.setPreferredH(Display.getInstance().getDisplayHeight() - top);
        if (f != null) {
            if (popup.getParent() == null) {
                f.getLayeredPane().addComponent(popup);
            }
            f.revalidate();
        }
    }

    class FormPointerListener implements ActionListener {

        public void actionPerformed(ActionEvent evt) {
            Form f = getComponentForm();
            if (f.getLayeredPane().getComponentCount() > 0 && popup.getComponentCount() > 0) {
                if (!popup.getComponentAt(0).
                        contains(evt.getX(), evt.getY())) {
                    removePopup();
                }
            } else {
                if (contains(evt.getX(), evt.getY())) {
                    addPopup();
                }

            }

        }
    }
}