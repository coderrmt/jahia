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
package org.jahia.ajax.gwt.client.service.layoutmanager;

import com.google.gwt.user.client.rpc.AsyncCallback;
import org.jahia.ajax.gwt.client.data.config.GWTJahiaPageContext;
import org.jahia.ajax.gwt.client.data.layoutmanager.GWTJahiaLayoutItem;
import org.jahia.ajax.gwt.client.data.layoutmanager.GWTJahiaLayoutManagerConfig;
import org.jahia.ajax.gwt.client.data.node.GWTJahiaNode;

import java.util.List;

/**
 * User: jahia
 * Date: 19 mars 2008
 * Time: 17:26:16
 */
public interface LayoutmanagerServiceAsync {

    public void saveLayoutItem(GWTJahiaLayoutItem gwtLayoutItem, AsyncCallback<String> async);

    public void saveAsDefault(String containerUUID, AsyncCallback async);

    public void restoreDefault(String containerUUID, AsyncCallback async);
    
    public void removeLayoutItem(GWTJahiaLayoutItem gwtLayoutItem, AsyncCallback async);

    public void saveLayoutItems(List<GWTJahiaLayoutItem> gwtLayoutItems, AsyncCallback async);

    public void addLayoutItem(String containerUUID, GWTJahiaLayoutItem layoutItem, AsyncCallback async);

    public void saveLayoutmanagerConfig(String containerUUID, GWTJahiaLayoutManagerConfig gwtLayoutManagerConfig, AsyncCallback async);

    public void getLayoutmanagerConfig(AsyncCallback<GWTJahiaLayoutManagerConfig> async);

    public void getLayoutItems(String containerUUID, AsyncCallback<List<GWTJahiaLayoutItem>> async);


}
