/**
 * This file is part of Jahia: An integrated WCM, DMS and Portal Solution
 * Copyright (C) 2002-2010 Jahia Solutions Group SA. All rights reserved.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.
 *
 * As a special exception to the terms and conditions of version 2.0 of
 * the GPL (or any later version), you may redistribute this Program in connection
 * with Free/Libre and Open Source Software ("FLOSS") applications as described
 * in Jahia's FLOSS exception. You should have received a copy of the text
 * describing the FLOSS exception, and it is also available here:
 * http://www.jahia.com/license
 *
 * Commercial and Supported Versions of the program
 * Alternatively, commercial and supported versions of the program may be used
 * in accordance with the terms contained in a separate written agreement
 * between you and Jahia Solutions Group SA. If you are unsure which license is appropriate
 * for your use, please contact the sales department at sales@jahia.com.
 */

package org.jahia.ajax.gwt.client.widget.contentengine;

import com.allen_sauer.gwt.log.client.Log;
import com.extjs.gxt.ui.client.event.*;
import com.extjs.gxt.ui.client.widget.Info;
import com.extjs.gxt.ui.client.widget.TabItem;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.form.Field;
import com.extjs.gxt.ui.client.widget.form.FieldSet;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import org.jahia.ajax.gwt.client.core.BaseAsyncCallback;
import org.jahia.ajax.gwt.client.data.GWTJahiaCreateEngineInitBean;
import org.jahia.ajax.gwt.client.data.GWTJahiaLanguage;
import org.jahia.ajax.gwt.client.data.acl.GWTJahiaNodeACL;
import org.jahia.ajax.gwt.client.data.definition.GWTJahiaNodeProperty;
import org.jahia.ajax.gwt.client.data.definition.GWTJahiaNodeType;
import org.jahia.ajax.gwt.client.data.node.GWTJahiaNode;
import org.jahia.ajax.gwt.client.data.toolbar.GWTEngineTab;
import org.jahia.ajax.gwt.client.messages.Messages;
import org.jahia.ajax.gwt.client.service.content.JahiaContentManagementService;
import org.jahia.ajax.gwt.client.util.acleditor.AclEditor;
import org.jahia.ajax.gwt.client.util.icons.StandardIconsProvider;
import org.jahia.ajax.gwt.client.widget.Linker;
import org.jahia.ajax.gwt.client.widget.definition.PropertiesEditor;

import java.util.*;

/**
 * User: toto
 * Date: Jan 7, 2010
 * Time: 1:55:28 PM
 * To change this template use File | Settings | File Templates.
 */
public class CreateContentEngine extends AbstractContentEngine {

    protected GWTJahiaNodeType type = null;
    protected String targetName = null;
    protected boolean createInParentAndMoveBefore = false;
    private Button ok;
    private Button okAndNew;
    private String parentPath;

    /**
     * Open Edit content engine for a new node creation
     *
     * @param linker                      The linker
     * @param parent                      The parent node where to create the new node - if createInParentAndMoveBefore, the node is sibling
     * @param type                        The selected node type of the new node
     * @param props                       initial values for properties
     * @param targetName                  The name of the new node, or null if automatically defined
     * @param createInParentAndMoveBefore
     */
    public CreateContentEngine(Linker linker, GWTJahiaNode parent, GWTJahiaNodeType type, Map<String, GWTJahiaNodeProperty> props, String targetName, boolean createInParentAndMoveBefore, EngineContainer engineContainer) {
        super(linker.getConfig().getEngineTabs(), linker);
        this.existingNode = false;
        this.targetNode = parent;
        this.type = type;
        if (!"*".equals(targetName)) {
            this.targetName = targetName;
        }
        if (createInParentAndMoveBefore) {
            this.parentPath = parent.getPath().substring(0, parent.getPath().lastIndexOf('/'));
        } else {
            this.parentPath = parent.getPath();
        }
        this.createInParentAndMoveBefore = createInParentAndMoveBefore;

        nodeTypes = new ArrayList<GWTJahiaNodeType>(1);
        nodeTypes.add(type);
        properties = new HashMap<String, GWTJahiaNodeProperty>(props);
        heading = "Create " + type.getLabel();
        init(engineContainer);
        loadEngine();

    }

    public void close() {
        container.closeEngine();
    }

    /**
     * Creates and initializes all window tabs.
     */
    protected void initTabs() {
        for (GWTEngineTab tabConfig : config) {
            EditEngineTabItem tabItem = tabConfig.getTabItem();
            if (tabItem.isHandleCreate() &&
                    (tabItem.getHideForTypes().isEmpty() || !tabItem.getHideForTypes().contains(type.getName())) &&
                    (tabItem.getShowForTypes().isEmpty() || tabItem.getShowForTypes().contains(type.getName()))) {
                tabs.add(tabItem.create(tabConfig, this));
            }
        }
        tabs.setSelection(tabs.getItem(0));
    }

    /**
     * init buttons
     */
    protected void initFooter() {
        ok = new Button(Messages.get("label.save"));
        ok.setHeight(BUTTON_HEIGHT);
        ok.setIcon(StandardIconsProvider.STANDARD_ICONS.engineButtonOK());
        ok.addSelectionListener(new CreateSelectionListener());

        buttonBar.add(ok);

        okAndNew = new Button(Messages.get("properties.saveAndNew.label", "Save and new"));
        okAndNew.setHeight(BUTTON_HEIGHT);
        okAndNew.setIcon(StandardIconsProvider.STANDARD_ICONS.engineButtonOK());

        okAndNew.addSelectionListener(new CreateAndAddNewSelectionListener());
        buttonBar.add(okAndNew);

        Button cancel = new Button(Messages.get("label.cancel", "Cancel"));
        cancel.setHeight(BUTTON_HEIGHT);
        cancel.setIcon(StandardIconsProvider.STANDARD_ICONS.engineButtonCancel());
        cancel.addSelectionListener(new SelectionListener<ButtonEvent>() {
            @Override
            public void componentSelected(ButtonEvent buttonEvent) {
                close();
            }
        });
        buttonBar.add(cancel);

        setButtonsEnabled(false);
    }

    /**
     * on language chnage, fill currentAzble
     * @param previous
     */
    protected void onLanguageChange(GWTJahiaLanguage previous) {
        if (previous != null) {
            final String lang = previous.getLanguage();
            for (TabItem item : tabs.getItems()) {
                if (!changedI18NProperties.containsKey(lang)) {
                    changedI18NProperties.put(lang, new ArrayList<GWTJahiaNodeProperty>());
                }
                Object itemData = item.getData("item");
                if (itemData instanceof PropertiesTabItem) {
                    PropertiesTabItem propertiesTabItem = (PropertiesTabItem) itemData;
                    changedI18NProperties.get(lang).addAll(propertiesTabItem.getLanguageProperties(true, lang));
                }
            }
        }
        fillCurrentTab();
    }


    /**
     * load mixin
     */
    private void loadEngine() {
        contentService.initializeCreateEngine(nodeTypes.iterator().next().getName(), parentPath, new BaseAsyncCallback<GWTJahiaCreateEngineInitBean>() {
            public void onSuccess(GWTJahiaCreateEngineInitBean result) {
                mixin = result.getMixin();
                initializersValues = result.getInitializersValues();
                defaultLanguageBean = result.getCurrentLocale();
                acl = result.getAcl();
                final List<GWTJahiaLanguage> languages = result.getLanguages();
                setAvailableLanguages(languages);
                setButtonsEnabled(true);

                initTabs();

                tabs.addListener(Events.Select, new Listener<ComponentEvent>() {
                    public void handleEvent(ComponentEvent event) {
                        fillCurrentTab();
                    }
                });

                fillCurrentTab();
                unmask();                
            }

            public void onApplicationFailure(Throwable caught) {
                Log.error("Unable to load avalibale mixin", caught);
            }
        });
    }


    protected class CreateSelectionListener extends SelectionListener<ButtonEvent> {
        public void componentSelected(ButtonEvent event) {
            save(true);
        }
    }

    protected class CreateAndAddNewSelectionListener extends SelectionListener<ButtonEvent> {
        public void componentSelected(ButtonEvent event) {
            save(false);
        }
    }

    protected void save(final boolean closeAfterSave) {
        String nodeName = targetName;
        final List<String> mixin = new ArrayList<String>();
        mask(Messages.get("label.saving","Saving..."), "x-mask-loading");
        setButtonsEnabled(false);
        boolean allValid = true;
        TabItem firstErrorTab = null;
        Field<?> firstErrorField = null;
        GWTJahiaLanguage firstErrorLang = null;
        for (TabItem tab : tabs.getItems()) {
            EditEngineTabItem item = tab.getData("item");
            if (item instanceof PropertiesTabItem) {
                PropertiesTabItem propertiesTabItem = (PropertiesTabItem) item;
                PropertiesEditor pe = ((PropertiesTabItem) item).getPropertiesEditor();
                if (pe != null) {
                    for (Field<?> field : pe.getFields()) {
                        if (field.isVisible() && field.isEnabled() && !field.validate() && ((FieldSet)field.getParent()).isExpanded()) {
                            if (allValid || tab.equals(tabs.getSelectedItem())
                                    && !tab.equals(firstErrorTab)) {
                                firstErrorTab = tab;
                                firstErrorField = field;
                            }
                            allValid = false;
                        }
                    }
                    if (!allValid) {
                        continue;
                    }
                }
                if (pe != null) {                    
                    // props.addAll(pe.getProperties());
                    mixin.addAll(pe.getAddedTypes());
                    mixin.addAll(pe.getExternalMixin());
                }

                // handle multilang
                if (propertiesTabItem.isMultiLang()) {
                    // for now only contentTabItem  has multilang. properties
                    if (getSelectedLanguage() != null) {
                        final String lang = getSelectedLanguage();
                        if (!changedI18NProperties.containsKey(lang)) {
                            changedI18NProperties.put(lang, new ArrayList<GWTJahiaNodeProperty>());
                        }

                        changedI18NProperties.get(lang).addAll(propertiesTabItem.getLanguageProperties(true, lang));
                        
                        for (String language : changedI18NProperties.keySet()) {
                            if (!lang.equals(language)) {
                                PropertiesEditor lpe = propertiesTabItem.getPropertiesEditorByLang(language);
                                if (lpe != null) {
                                    for (Field<?> field : lpe.getFields()) {
                                        if (field.isEnabled() && !field.validate() && ((FieldSet)field.getParent()).isExpanded()) {
                                            if (allValid || tab.equals(tabs.getSelectedItem())
                                                    && !tab.equals(firstErrorTab)) {
                                                firstErrorTab = tab;
                                                firstErrorField = field;
                                            }
                                            allValid = false;
                                        }
                                    }
                                    if (!allValid) {
                                        for (GWTJahiaLanguage gwtLang : languageSwitcher.getStore().getModels()) {
                                            if (language.equals(gwtLang.getLanguage())) {
                                                firstErrorLang = gwtLang;
                                                break;
                                            }
                                        }
                                        break;
                                    }
                                }
                            }
                        }
                    }

                    if (pe != null) {
                        changedProperties.addAll(pe.getProperties(false, true, false));
                    }
                } else {
                    if (pe != null) {
                        changedProperties.addAll(pe.getProperties());
                    }
                }
                if (item instanceof ContentTabItem) {
                    if (((ContentTabItem) item).isNodeNameFieldDisplayed()) {
                        String nodeNameValue = ((ContentTabItem) item).getName().getValue();
                        nodeName = Messages.get("label.nodeAutoName", "Automatically Created (you can type your name here if you want)").equals(nodeNameValue) ? targetName : nodeNameValue;
                    }
                }
            } else if (item instanceof RightsTabItem) {
                AclEditor acl = ((RightsTabItem) item).getRightsEditor();
                if (acl != null) {
                    newNodeACL = acl.getAcl();
                    if (newNodeACL.isInheritanceBroken()) {
                        mixin.add("jmix:accessControlled");
                    }
                }
            } else if (item instanceof CategoriesTabItem) {
                ((CategoriesTabItem) item).updateProperties(changedProperties, mixin);
            } else if (item instanceof TagsTabItem) {
                final TagsTabItem tabItem = (TagsTabItem) item;
                if(tabItem.isTagAreI15d()) {
                    tabItem.updateI18NProperties(changedI18NProperties, mixin);
                } else {
                    tabItem.updateProperties(changedProperties, mixin);
                }
            }
        }
        if (!allValid) {
            if (firstErrorLang != null) {
                Window.alert(Messages
                        .getWithArgs(
                                "failure.invalid.constraint.lang.label",
                                "There are some validation errors in the content for language {0}! Please switch to that language, click on the information icon next to the highlighted field(s), correct the input and save again.",
                                new Object[] { firstErrorLang.getDisplayName() }));
             // Unfortunately automatic switching lead to exceptions
             //                languageSwitcher.select(firstErrorLang);                
            } else {
                Window.alert(Messages.get("failure.invalid.constraint.label"));                
            }
            if (firstErrorTab != null && !tabs.getSelectedItem().equals(firstErrorTab)) {
                tabs.setSelection(firstErrorTab);
            }
            if (firstErrorField != null) {
                firstErrorField.focus();
            }
            unmask();
            setButtonsEnabled(true);
        } else {
            doSave(nodeName, changedProperties, changedI18NProperties, mixin, newNodeACL,
                    closeAfterSave);
        }
    }
    
    protected void doSave(String nodeName, List<GWTJahiaNodeProperty> props, Map<String, List<GWTJahiaNodeProperty>> langCodeProperties, List<String> mixin, GWTJahiaNodeACL newNodeACL, final boolean closeAfterSave) {
        final AsyncCallback<GWTJahiaNode> callback = new BaseAsyncCallback<GWTJahiaNode>() {
            public void onApplicationFailure(Throwable throwable) {
                Window.alert(Messages.get("failure.properties.save", "Properties save failed") + "\n\n" + throwable.getLocalizedMessage());
                Log.error("failed", throwable);
                unmask();
                setButtonsEnabled(true);
            }

            public void onSuccess(GWTJahiaNode node) {
                if (closeAfterSave) {
					Info.display(
					        Messages.get("label.information", "Information"),
					        Messages.get(
					                "org.jahia.engines.contentmanager.addContentWizard.formCard.success.save",
					                "Content node created successfully:")
					                + " " + node.getName());
                    CreateContentEngine.this.container.closeEngine();
                } else {
                    CreateContentEngine.this.tabs.removeAll();
                    CreateContentEngine.this.initTabs();
                    CreateContentEngine.this.tabs.setSelection(tabs.getItem(0));
                    CreateContentEngine.this.layout(true);
                    unmask();
                    setButtonsEnabled(true);
                }


                if (node.isPage() || node.getNodeTypes().contains("jnt:externalLink") || node.getNodeTypes().contains("jnt:nodeLink")) {
                    linker.refresh(Linker.REFRESH_MAIN + Linker.REFRESH_PAGES);
                } else {
                    linker.refresh(Linker.REFRESH_MAIN);
                }
            }
        };
        if (createInParentAndMoveBefore) {
            JahiaContentManagementService.App.getInstance().createNodeAndMoveBefore(targetNode.getPath(), nodeName, type.getName(), mixin, newNodeACL, props, langCodeProperties, callback);
        } else {
            JahiaContentManagementService.App.getInstance().createNode(parentPath, nodeName, type.getName(), mixin, newNodeACL, props, langCodeProperties, callback);
        }
    }

    private void setButtonsEnabled(final boolean enabled) {
        ok.setEnabled(enabled);
        okAndNew.setEnabled(enabled);
    }

}
