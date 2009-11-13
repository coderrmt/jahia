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
package org.jahia.data.beans;

import org.jahia.content.ContentObject;
import org.jahia.content.ContentObjectKey;
import org.jahia.content.JahiaObject;
import org.jahia.content.ObjectKey;
import org.jahia.data.containers.JahiaContainer;
import org.jahia.data.containers.JahiaContainerDefinition;
import org.jahia.data.containers.JahiaContainerList;
import org.jahia.data.containers.JahiaContainerListPagination;
import org.jahia.data.fields.LoadFlags;
import org.jahia.exceptions.JahiaException;
import org.jahia.gui.GuiBean;
import org.jahia.gui.HTMLToolBox;
import org.jahia.params.ParamBean;
import org.jahia.params.ProcessingContext;
import org.jahia.registries.ServicesRegistry;
import org.jahia.services.acl.JahiaBaseACL;
import org.jahia.services.containers.ContentContainer;
import org.jahia.services.containers.ContentContainerList;
import org.jahia.services.containers.JahiaContainersService;
import org.jahia.services.importexport.ImportExportService;
import org.jahia.services.lock.LockKey;
import org.jahia.services.lock.LockService;
import org.jahia.services.pages.ContentPage;
import org.jahia.services.pages.JahiaPage;
import org.jahia.services.usermanager.JahiaUser;
import org.jahia.services.version.EntryLoadRequest;
import org.jahia.services.workflow.WorkflowService;
import org.jahia.utils.InsertionSortedMap;

import java.util.*;

/**
 * <p>Title: Container list bean facade</p>
 * <p>Description: This facade is used for view systems such as JSTL JSPs,
 * and so on. It conforms to a JavaBean pattern and should not be modified
 * out of this pattern. </p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: Jahia Ltd</p>
 *
 * @author Serge Huber, Xavier Lawrence
 * @version 1.0
 */

public class ContainerListBean extends ContentBean {

    private static final org.apache.log4j.Logger logger =
            org.apache.log4j.Logger.getLogger(ContainerListBean.class);

    public static final String TYPE = "ContentContainerList";

    private JahiaContainerList jahiaContainerList;
    private ContentContainerList contentContainerList;
    private List<ContainerBean> containers;
    private ContainerBean parentContainerBean;
    private Map<String, ActionURIBean> actionURIs;
    private boolean completelyLocked = false;
    private boolean independantWorkflow = false;
    private int groupWorkflowState = 0;

    private final ServicesRegistry servicesRegistry = ServicesRegistry.getInstance();
    private final WorkflowService workflowService = servicesRegistry.getWorkflowService();
    private final JahiaContainersService jahiaContainersService = servicesRegistry.getJahiaContainersService();
    private final ImportExportService importExportService = servicesRegistry.getImportExportService();
    private final LockService lockRegistry = servicesRegistry.getLockService();

    public ContainerListBean() {
    }

    static {
        registerType(ContentContainerList.class.getName(), ContainerListBean.class.getName());
    }

    public ContainerListBean(final JahiaContainerList jahiaContainerList,
                             final ProcessingContext processingContext) {
        this.jahiaContainerList = jahiaContainerList;
        this.processingContext = processingContext;

        try {
            init(jahiaContainerList);
        } catch (JahiaException e) {
            logger.error(e.getMessage(), e);
        }
    }

    private void init(final JahiaContainerList jahiaContainerList) throws JahiaException {
        final ContentContainerList theList = jahiaContainerList.getContentContainerList();

        if (theList == null) {
            independantWorkflow = false;
            return;
        }

        final int mode = workflowService.getWorkflowMode(theList);
        independantWorkflow = (mode != WorkflowService.LINKED);
        if (independantWorkflow) {
            final Map<String, Integer> languagesStates = workflowService.getLanguagesStates(theList);
            Integer languageState = languagesStates.get(
                    processingContext.getLocale().toString());
            final Integer sharedLanguageState = languagesStates.
                    get(ContentObject.SHARED_LANGUAGE);
            if (languageState != null && languageState.intValue() != -1) {
                if (sharedLanguageState != null &&
                        languageState.intValue() < sharedLanguageState.intValue()) {
                    languageState = sharedLanguageState;
                }
            } else if (languageState == null) {
                languageState = sharedLanguageState;
            }
            groupWorkflowState = (languageState != null) ? languageState.intValue() : 1;
        }
    }

    public static AbstractJahiaObjectBean getChildInstance(
            final JahiaObject jahiaObject,
            final ProcessingContext processingContext) {
        final ContentContainerList contentContainerList = (ContentContainerList) jahiaObject;
        try {
            return new ContainerListBean(contentContainerList.getJahiaContainerList(
                    processingContext, processingContext.getEntryLoadRequest()), processingContext);
        } catch (JahiaException je) {
            logger.error("Error while converting content container to jahia container", je);
            return null;
        }
    }

    public List<Integer> getContainerIdsInList(final EntryLoadRequest loadRequest) {
        final int id = getID();
        if (id == 0) {
            return new ArrayList<Integer>(0);
        }
        try {
            return jahiaContainersService.getctnidsInList(id, loadRequest);
        } catch (JahiaException e) {
            return null;
        }
    }

    public int getID() {
        return jahiaContainerList.getID();
    }

    public String getBeanType() {
        return TYPE;
    }

    public JahiaContainerList getJahiaContainerList() {
        return jahiaContainerList;
    }

    public int getFullSize() {
        return jahiaContainerList.getFullSize();
    }

    public int getSize() {
        return jahiaContainerList.size();
    }

    public List<ContainerBean> getContainerBeans() {
        if (containers != null) {
            return containers;
        }
        final Iterator<JahiaContainer> containerEnum = jahiaContainerList.getContainers();
        containers = new ArrayList<ContainerBean>();
        while (containerEnum.hasNext()) {
            final JahiaContainer jahiaContainer = (JahiaContainer) containerEnum.next();
            final ContainerBean containerBean = new ContainerBean(jahiaContainer, processingContext);
            containers.add(containerBean);
        }
        return containers;
    }

    public List<ContainerBean> getContainers() {
        return getContainerBeans();
    }

    public ContainerBean getContainerByID(final int containerID) {
        final Iterator<ContainerBean> containerIter = getContainers().iterator();
        while (containerIter.hasNext()) {
            final ContainerBean curContainerBean = containerIter.next();
            if (curContainerBean.getID() == containerID) {
                return curContainerBean;
            }
        }
        return null;
    }

    public ContainerBean getContainerByUUID(String containerUUID) {
        if (containerUUID == null) {
            return null;
        }
//        if (getContainerUUID.indexOf('/')>0) {
//            getContainerUUID = getContainerUUID.substring(0, getContainerUUID.indexOf('/'));
//        }
        final Iterator<ContainerBean> containerIter = getContainers().iterator();
        while (containerIter.hasNext()) {
            final ContainerBean curContainerBean = containerIter.next();
            try {
                ContentContainer contentContainer = curContainerBean.getJahiaContainer().getContentContainer();
                if (containerUUID.equals(contentContainer.getProperty("uuid"))) {
                    return curContainerBean;
                }
            } catch (JahiaException e) {
                logger.warn(e, e);
            }
        }
        return null;
    }

    public JahiaContainerListPagination getJahiaContainerListPagination() {
        return jahiaContainerList.getCtnListPagination();
    }

    public boolean isContainersLoaded() {
        return jahiaContainerList.isContainersLoaded();
    }

    public int getPageID() {
        return jahiaContainerList.getPageID();
    }

    public JahiaContainerDefinition getDefinition() {
        try {
            return jahiaContainerList.getDefinition();
        } catch (JahiaException je) {
            logger.error("Error retrieving container list definition ", je);
            return null;
        }
    }

    public int getDefinitionID() {
        return getDefinition().getID();
    }

    public String getName() {
        try {
            return jahiaContainerList.getDefinition().getName();
        } catch (JahiaException je) {
            logger.error("Error retrieving container list definition ", je);
            return null;
        }
    }

    public String getTitle() {
        try {
            return jahiaContainerList.getDefinition().getTitle(processingContext.getLocale());
        } catch (JahiaException je) {
            logger.error("Error retrieving field definition for field " + getID() + ":", je);
            return null;
        }
    }

    public int getParentContainerID() {
        return jahiaContainerList.getParentEntryID();
    }

    public ContentBean getParent() {
        final int parentContainerID = jahiaContainerList.getParentEntryID();
        if (parentContainerID == 0) {

            // the parent object is then the page
            try {
                final ContentPage parentContentPage = ContentPage.getPage(
                        jahiaContainerList.getPageID());
                final JahiaPage parentJahiaPage = parentContentPage.getPage(processingContext);
                return new PageBean(parentJahiaPage, processingContext);
            } catch (JahiaException je) {
                logger.error("Error while loading parent page " +
                        jahiaContainerList.getPageID() +
                        " for container list " + jahiaContainerList.getID(),
                        je);
            }
            return null;
        }
        if (parentContainerBean != null) {
            return parentContainerBean;
        }
        try {
            final JahiaContainer parentContainer = jahiaContainersService.loadContainer(
                    parentContainerID, LoadFlags.ALL, processingContext, processingContext.getEntryLoadRequest());
            if (parentContainer == null) {
                return null;
            }
            /* let apply lazy loading
            containerFactory.fullyLoadContainer(parentContainer, LoadFlags.ALL,
                    processingContext,
                    processingContext.getEntryLoadRequest(), null, null, null);
            */
            parentContainerBean = new ContainerBean(parentContainer, processingContext);
            return parentContainerBean;
        } catch (JahiaException je) {
            logger.error("Error loading parent container for container list " + getID() + " : ", je);
            return null;
        }
    }

    public ContainerBean getParentContainer() {
        final int parentContainerID = jahiaContainerList.getParentEntryID();
        if (parentContainerID == 0) {
            return null;
        }
        return (ContainerBean) getParent();
    }

    public ContentContainerList getContentContainerList() {
        if (contentContainerList == null) {
            contentContainerList = jahiaContainerList.getContentContainerList();
        }
        return contentContainerList;
    }

    public Properties getProperties() {
        return jahiaContainerList.getProperties();
    }

    public int getAclID() {
        return jahiaContainerList.getAclID();
    }

    public JahiaBaseACL getACL() {
        return jahiaContainerList.getACL();
    }

    public int getContainerDefinitionID() {
        return jahiaContainerList.getctndefid();
    }

    public boolean isPicker() {
        return false;
    }

    public ContentObject getContentObject() {
        return getContentContainerList();
    }

    public String getJCRPath() throws JahiaException {
        return this.jahiaContainerList.getContentContainerList().getJCRPath(this.processingContext);
    }
    
}