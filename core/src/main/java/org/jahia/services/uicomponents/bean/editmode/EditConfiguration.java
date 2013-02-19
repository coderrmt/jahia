/**
 * This file is part of Jahia, next-generation open source CMS:
 * Jahia's next-generation, open source CMS stems from a widely acknowledged vision
 * of enterprise application convergence - web, search, document, social and portal -
 * unified by the simplicity of web content management.
 *
 * For more information, please visit http://www.jahia.com.
 *
 * Copyright (C) 2002-2012 Jahia Solutions Group SA. All rights reserved.
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
 * Commercial and Supported Versions of the program (dual licensing):
 * alternatively, commercial and supported versions of the program may be used
 * in accordance with the terms and conditions contained in a separate
 * written agreement between you and Jahia Solutions Group SA.
 *
 * If you are unsure which license is appropriate for your use,
 * please contact the sales department at sales@jahia.com.
 */

package org.jahia.services.uicomponents.bean.editmode;

import org.jahia.services.content.JCRNodeWrapper;
import org.jahia.services.render.RenderContext;
import org.jahia.services.uicomponents.bean.toolbar.Toolbar;
import org.springframework.beans.factory.BeanNameAware;

import javax.jcr.RepositoryException;
import java.io.Serializable;
import java.util.*;

/**
 * Represent edit mode configuration, including side panel and side panel toolbar,
 * top toolbar, main module toolbar and context menu.
 * 
 * @author ktlili
 * Date: Apr 14, 2010
 * Time: 12:26:47 PM
 */
public class EditConfiguration implements Serializable, BeanNameAware {
    
    private static final long serialVersionUID = -5469841056796249203L;

    private String name;
    
    private Toolbar topToolbar;
    private Toolbar sidePanelToolbar;
    private Toolbar mainModuleToolbar;
    private Toolbar contextMenu;

    private List<SidePanelTab> tabs;

    private Map<String, EngineConfiguration> engineConfigurations;

    private String sitesLocation;

    private boolean enableDragAndDrop = true;

    private String requiredPermission;
    private String defaultUrlMapping;

    private boolean modulesOnly = false;

    private List<String> componentsPaths = Arrays.asList("$site/components/*");

    private Set<String> editableTypes;
    private Set<String> nonEditableTypes;
    private Set<String> visibleTypes;
    private Set<String> nonVisibleTypes;
    private Set<String> skipMainModuleTypesDomParsing;

    private boolean buttonsInLayer = false;

    public void addTab(SidePanelTab tab) {
        tabs.add(tab);
    }
    
    public void addTab(int index, SidePanelTab tab) {
        tabs.add(index, tab);
    }
    
    public void setBeanName(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Toolbar getTopToolbar() {
        return topToolbar;
    }

    public void setTopToolbar(Toolbar topToolbar) {
        this.topToolbar = topToolbar;
    }

    public Toolbar getSidePanelToolbar() {
        return sidePanelToolbar;
    }

    public void setSidePanelToolbar(Toolbar sidePanelToolbar) {
        this.sidePanelToolbar = sidePanelToolbar;
    }

    public Toolbar getMainModuleToolbar() {
        return mainModuleToolbar;
    }

    public void setMainModuleToolbar(Toolbar mainModuleToolbar) {
        this.mainModuleToolbar = mainModuleToolbar;
    }

    public Toolbar getContextMenu() {
        return contextMenu;
    }

    public void setContextMenu(Toolbar contextMenu) {
        this.contextMenu = contextMenu;
    }

    public List<SidePanelTab> getTabs() {
        return tabs;
    }

    public void setTabs(List<SidePanelTab> tabs) {
        this.tabs = tabs;
    }

    public EngineConfiguration getDefaultEditConfiguration() {
        if (engineConfigurations == null) {
            engineConfigurations = new HashMap<String, EngineConfiguration>();
        }
        if (!engineConfigurations.containsKey("nt:base")) {
            engineConfigurations.put("nt:base", new EngineConfiguration());
        }
        return engineConfigurations.get("nt:base");
    }

    public Map<String, EngineConfiguration> getEngineConfigurations() {
        return engineConfigurations;
    }

    public void setEngineConfigurations(Map<String, EngineConfiguration> engineConfigurations) {
        this.engineConfigurations = engineConfigurations;
    }

    public String getSitesLocation() {
        return sitesLocation;
    }

    public void setSitesLocation(String sitesLocation) {
        this.sitesLocation = sitesLocation;
    }

    public boolean isEnableDragAndDrop() {
        return enableDragAndDrop;
    }

    public void setEnableDragAndDrop(boolean enableDragAndDrop) {
        this.enableDragAndDrop = enableDragAndDrop;
    }

    public String getRequiredPermission() {
        return requiredPermission;
    }

    public void setRequiredPermission(String requiredPermission) {
        this.requiredPermission = requiredPermission;
    }

    public String getDefaultUrlMapping() {
        return defaultUrlMapping;
    }

    public void setDefaultUrlMapping(String defaultUrlMapping) {
        this.defaultUrlMapping = defaultUrlMapping;
    }

    public void removeTab(String tabKey) {
        if (tabKey != null && tabKey.length() > 0) {
            for (Iterator<SidePanelTab> iterator = getTabs().iterator(); iterator.hasNext();) {
                SidePanelTab tab = iterator.next();
                if (tab.getKey() != null && tab.getKey().equals(tabKey)) {
                    iterator.remove();
                }
            }
        }
    }

    /**
     * @return when true, the default url and base are /modules/default(.html)
     */
    public boolean isModulesOnly() {
        return modulesOnly;
    }

    public void setModulesOnly(boolean modulesOnly) {
        this.modulesOnly = modulesOnly;
    }

    /**
     * @return List of paths to the components
     */
    public List<String> getComponentsPaths() {
        return componentsPaths;
    }

    public void setComponentsPaths(List<String> componentsPaths) {
        this.componentsPaths = componentsPaths;
    }

    /**
     * @return Set of editable types
     */
    public Set<String> getEditableTypes() {
        return editableTypes;
    }

    public void setEditableTypes(Set<String> editableTypes) {
        this.editableTypes = editableTypes;
    }

    /**
     * @return Set of non editable types
     */
    public Set<String> getNonEditableTypes() {
        return nonEditableTypes;
    }

    public void setNonEditableTypes(Set<String> nonEditableTypes) {
        this.nonEditableTypes = nonEditableTypes;
    }

    /**
     * @return Set of types on witch we avoid to parse dom for module lookup (ex javascript, css, etc ..)
     */
    public Set<String> getSkipMainModuleTypesDomParsing() {
        return skipMainModuleTypesDomParsing;
    }

    public void setSkipMainModuleTypesDomParsing(Set<String> skipMainModuleTypesDomParsing) {
        this.skipMainModuleTypesDomParsing = skipMainModuleTypesDomParsing;
    }

    /**
     * @return Set of visible Types
     */
    public Set<String> getVisibleTypes() {
        return visibleTypes;
    }

    public void setVisibleTypes(Set<String> visibleTypes) {
        this.visibleTypes = visibleTypes;
    }

    /**
     * @return Set of types to hide
     */
    public Set<String> getNonVisibleTypes() {
        return nonVisibleTypes;
    }

    public void setNonVisibleTypes(Set<String> nonVisibleTypes) {
        this.nonVisibleTypes = nonVisibleTypes;
    }

    /**
     * @return if true, display button on the top right of the area
     */
    public boolean isButtonsInLayer() {
        return buttonsInLayer;
    }

    public void setButtonsInLayer(boolean buttonsInLayer) {
        this.buttonsInLayer = buttonsInLayer;
    }

    /**
     * @param node to check
     * @return true if the node is visible (in visibleTypes or without nonVisibleTypes)
     * @throws RepositoryException
     */
    public boolean isVisible(JCRNodeWrapper node) throws RepositoryException {
        if (getNonVisibleTypes() != null && isNodeOfType(node, getNonVisibleTypes())) {
            return false;
        } else if (getVisibleTypes() != null && !isNodeOfType(node, getVisibleTypes())) {
            return false;
        }
        return true;
    }

    /**
     * @param node to check
     * @return true if the node is editable (in editableTypes or without nonEditableTypes)
     * @throws RepositoryException
     */
    public boolean isEditable( JCRNodeWrapper node) throws RepositoryException{
        if (getNonEditableTypes() != null && isNodeOfType(node, getNonEditableTypes())) {
            return false;
        } else if (getEditableTypes() != null && !isNodeOfType(node, getEditableTypes())) {
            return false;
        }
        return true;
    }

    private boolean isNodeOfType(JCRNodeWrapper node, Set<String> types) throws RepositoryException {
        if (types != null && node != null) {
            for (String s : types) {
                if (node.isNodeType(s)) {
                    return true;
                }
            }
        }
        return false;
    }


}
