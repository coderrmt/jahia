/**
 * This file is part of Jahia: An integrated WCM, DMS and Portal Solution
 * Copyright (C) 2002-2009 Jahia Solutions Group SA. All rights reserved.
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
package org.jahia.services.templates;

import org.apache.commons.collections.Factory;
import org.apache.commons.collections.map.LazyMap;
import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;
import org.jahia.data.templates.JahiaTemplatesPackage;
import org.jahia.services.content.nodetypes.NodeTypeRegistry;
import org.jahia.services.content.nodetypes.initializers.ChoiceListInitializer;
import org.jahia.services.content.rules.RulesListener;
import org.jahia.services.render.filter.ModuleFilters;
import org.jahia.services.render.filter.RenderFilter;
import org.jahia.settings.SettingsBean;
import org.jahia.bin.errors.ErrorHandler;
import org.jahia.bin.Action;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;

import java.io.File;
import java.io.IOException;
import java.util.*;
/**
 * Template packages registry service.
 *
 * @author Sergiy Shyrkov
 */
class TemplatePackageRegistry {

    private static Logger logger = Logger
            .getLogger(TemplatePackageRegistry.class);

    static class ModuleRegistry implements BeanPostProcessor {

        private TemplatePackageRegistry templatePackageRegistry;

        public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
            if (bean instanceof ModuleFilters) {
                ModuleFilters filters = (ModuleFilters) bean;
                if (filters.getModule() != null) {
                    templatePackageRegistry.filtersPerModule.get(filters.getModule()).addAll(filters.getFilters());
                } else {
                    templatePackageRegistry.commonFilters.addAll(filters.getFilters());
                }
            } else if (bean instanceof ErrorHandler) {
                templatePackageRegistry.errorHandlers.add((ErrorHandler) bean);
            } else if (bean instanceof Action) {
                Action action = (Action) bean;
                templatePackageRegistry.actions.put(action.getName(), action);
            } else if (bean instanceof ChoiceListInitializer) {

            }
            return bean;
        }

        public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
            return bean;
        }

        public void setTemplatePackageRegistry(TemplatePackageRegistry templatePackageRegistry) {
            this.templatePackageRegistry = templatePackageRegistry;
        }
    }

    private Map<String, JahiaTemplatesPackage> registry = new TreeMap<String, JahiaTemplatesPackage>();
    private Map<String, JahiaTemplatesPackage> fileNameRegistry = new TreeMap<String, JahiaTemplatesPackage>();
    private Map<String, List<JahiaTemplatesPackage>> packagesPerModule = new HashMap<String, List<JahiaTemplatesPackage>>();
    @SuppressWarnings("unchecked")
    private Map<String, List<RenderFilter>> filtersPerModule = LazyMap.decorate(new HashMap<String, List<RenderFilter>>(), new Factory() {
        public Object create() {
            return new LinkedList<RenderFilter>();
        }
    });
    private List<RenderFilter> commonFilters = new LinkedList<RenderFilter>();
    private List<ErrorHandler> errorHandlers = new LinkedList<ErrorHandler>();
    private Map<String,Action> actions = new HashMap<String,Action>();

    private SettingsBean settingsBean;

    private List<JahiaTemplatesPackage> templatePackages;

    /**
     * Checks if the specified template set is present in the repository.
     *
     * @param packageName the template package name to check for
     * @return <code>true</code>, if the specified template package already
     *         exists in the repository
     */
    public boolean contains(String packageName) {
        return registry.containsKey(packageName);
    }

    /**
     * Checks if specified template sets are all present in the repository.
     *
     * @param packageNames the list of template package names to check for
     * @return <code>true</code>, if specified template packages are all present
     * 		   in the repository
     */
    public boolean containsAll(List<String> packageNames) {
    	return registry.keySet().containsAll(packageNames);
    }

    public boolean containsFileName(String fileName) {
        return fileNameRegistry.containsKey(fileName);
    }

    /**
     * Returns a list of all available template packages.
     *
     * @return a list of all available template packages
     */
    public List<JahiaTemplatesPackage> getAvailablePackages() {
        if (null == templatePackages) {
            templatePackages = Collections
                .unmodifiableList(new LinkedList<JahiaTemplatesPackage>(
                    registry.values()));
        }
        return templatePackages;
    }

    /**
     * Returns the number of available template packages in the registry.
     *
     * @return the number of available template packages in the registry
     */
    public int getAvailablePackagesCount() {
        return registry.size();
    }

    public Map<String, List<JahiaTemplatesPackage>> getPackagesPerModule() {
        return packagesPerModule;
    }

    /**
     * Returns a list of {@link RenderFilter} instances, configured for the specified templates package.
     * 
     * @param moduleName
     *            the template package name to search for
     * @return a list of {@link RenderFilter} instances, configured for the specified templates package
     */
    public List<RenderFilter> getRenderFiltersForModule(String moduleName) {
        List<RenderFilter> filters = new LinkedList<RenderFilter>(filtersPerModule.get(moduleName));
        filters.addAll(commonFilters);
        return filters;
    }

    /**
     * Returns a list of {@link ErrorHandler} instances
     *
     * @return a list of {@link ErrorHandler} instances
     */
    public List<ErrorHandler> getErrorHandlers() {
        return errorHandlers;
    }

    public Map<String, Action> getActions() {
        return actions;
    }

    /**
     * Returns the requested template package or <code>null</code> if the
     * package with the specified name is not registered in the repository.
     *
     * @param packageName the template package name to search for
     * @return the requested template package or <code>null</code> if the
     *         package with the specified name is not registered in the
     *         repository
     */
    public JahiaTemplatesPackage lookup(String packageName) {
        if (packageName == null || registry == null) return null;
        return registry.containsKey(packageName) ? registry.get(packageName)
                : null;
    }

    /**
     * Returns the requested template package or <code>null</code> if the
     * package with the specified file name is not registered in the repository.
     *
     * @param fileName the template package fileName to search for
     * @return the requested template package or <code>null</code> if the
     *         package with the specified name is not registered in the
     *         repository
     */
    public JahiaTemplatesPackage lookupByFileName(String fileName) {
        if (fileName == null || registry == null) return null;
        return fileNameRegistry.containsKey(fileName) ? fileNameRegistry.get(fileName)
                : null;
    }

    /**
     * Adds a collection of template packages to the repository.
     *
     * @param templatePackages a collection of packages to add
     */
    public void register(Collection<JahiaTemplatesPackage> templatePackages) {
        for (JahiaTemplatesPackage pack : templatePackages) {
            register(pack);
        }
    }
    
    /**
     * Adds the template package to the repository.
     *
     * @param templatePackage the template package to add
     */
    public void register(JahiaTemplatesPackage templatePackage) {
        templatePackages = null;
        registry.put(templatePackage.getName(), templatePackage);
        fileNameRegistry.put(templatePackage.getFileName(), templatePackage);
        File rootFolder = new File(settingsBean.getJahiaTemplatesDiskPath(), templatePackage.getRootFolder());
        if (!rootFolder.exists()) {
            rootFolder = new File(templatePackage.getFilePath());
        }

        templatePackage.getLookupPath().add(templatePackage.getRootFolderPath());

        try {
            File classesFolder = new File(rootFolder, "WEB-INF/classes");
            if (classesFolder.exists()) {
                FileUtils.copyDirectory(classesFolder, new File(settingsBean.getClassDiskPath()));
                FileUtils.deleteDirectory(new File(rootFolder, "WEB-INF/classes"));
            }
            File webInfFolder = new File(rootFolder, "WEB-INF");
            if (webInfFolder.exists() && webInfFolder.list().length == 0) {
                webInfFolder.delete();
            }
        } catch (IOException e) {
            logger.error("Cannot deploy classes for templates "+templatePackage.getName(),e);
        }

        // register content definitions
        if (!templatePackage.getDefinitionsFiles().isEmpty()) {
            try {
                for (String name : templatePackage.getDefinitionsFiles()) {
                    NodeTypeRegistry.getInstance().addDefinitionsFile(
                            new File(rootFolder, name),
                            templatePackage.getName(), true);
                }
            } catch (Exception e) {
                logger.warn("Cannot parse definitions for "+templatePackage.getName(),e);
            }
        }

        // add rules
        if (!templatePackage.getRulesFiles().isEmpty()) {
            try {
                for (String name : templatePackage.getRulesFiles()) {
                    for (RulesListener listener : RulesListener.getInstances()) {
                        listener.addRules(new File(rootFolder, name));
                    }
                }
            } catch (Exception e) {
                logger.warn("Cannot parse rules for "+templatePackage.getName(),e);
            }
        }

        // handle resource bundles
        // TODO adjust resource bundle handling after module selection will be implemented for a virtual site
        for (JahiaTemplatesPackage sourcePack : registry.values()) {
        	sourcePack.getResourceBundleHierarchy().clear();
            for (JahiaTemplatesPackage otherPack : registry.values()) {
            	if (otherPack.getResourceBundleName() != null && !otherPack.isDefault()) {
            		sourcePack.getResourceBundleHierarchy().add("templates." + otherPack.getRootFolder() + "." + otherPack.getResourceBundleName());
            	}
            }
            if (!sourcePack.isDefault()) {
            	sourcePack.getResourceBundleHierarchy().add("templates.default.resources.DefaultJahiaTemplates");
            }
        }
        
        File[] files = rootFolder.listFiles();
        for (File file : files) {
            if (file.isDirectory()) {
                String key = file.getName();
                if (!packagesPerModule.containsKey(key)) {
                    packagesPerModule.put(key, new ArrayList<JahiaTemplatesPackage>());
                }
                if (!packagesPerModule.get(key).contains(templatePackage)) {
                    packagesPerModule.get(key).add(templatePackage);
                }
            }
        }
        logger.info("Registered "+templatePackage.getName());
    }

	public void unregister(JahiaTemplatesPackage templatePackage) {
        registry.remove(templatePackage.getName());
        fileNameRegistry.remove(templatePackage.getFileName());
        templatePackages = null;
        NodeTypeRegistry.getInstance().unregisterNodeTypes(templatePackage.getName());
    }

    public void reset() {
        for (JahiaTemplatesPackage pkg : new HashSet<JahiaTemplatesPackage>(registry.values())) {
            unregister(pkg);
        }
        templatePackages = null;
    }

    public void resetBeanModules() {
        commonFilters.clear();
        filtersPerModule.clear();
        errorHandlers.clear();
        actions.clear();
    }

    public void setSettingsBean(SettingsBean settingsBean) {
        this.settingsBean = settingsBean;
    }

    /**
     * Performs a set of validation tests for deployed template packages.
     */
    public void validate() {
        if (getAvailablePackagesCount() == 0) {
            logger.warn("No available template packages found."
                    + " That will prevent creation of a virtual site.");
        }
        // TODO implement dependency (inheritance) validation for template sets
    }
}