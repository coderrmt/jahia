/**
 * This file is part of Jahia: An integrated WCM, DMS and Portal Solution
 * Copyright (C) 2002-2011 Jahia Solutions Group SA. All rights reserved.
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

package org.jahia.ajax.gwt.client.widget.toolbar.action;
import com.extjs.gxt.ui.client.Style;
import com.extjs.gxt.ui.client.widget.Dialog;
import com.extjs.gxt.ui.client.widget.layout.FlowLayout;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.HTML;
import org.jahia.ajax.gwt.client.core.BaseAsyncCallback;
import org.jahia.ajax.gwt.client.data.node.GWTJahiaNode;
import org.jahia.ajax.gwt.client.messages.Messages;
import org.jahia.ajax.gwt.client.service.content.JahiaContentManagementService;
import org.jahia.ajax.gwt.client.widget.LinkerSelectionContext;

/**
* User: toto
* Date: Sep 25, 2009
* Time: 6:56:52 PM
* 
*/
public class WebfolderActionItem extends BaseActionItem {
    public void onComponentSelection() {
        final GWTJahiaNode selection = linker.getSelectionContext().getSingleSelection();
        if (selection != null && !selection.isFile()) {
            linker.loading(Messages.get("statusbar.webfoldering.label"));
            JahiaContentManagementService
                    .App.getInstance().getAbsolutePath(selection.getPath(), new BaseAsyncCallback<String>() {
                public void onApplicationFailure(Throwable t) {
                    Window.alert(Messages.get("failure.webfolder.label") + "\n" + t.getLocalizedMessage());
                    linker.loaded();
                }

                public void onSuccess(String url) {
                    if (url != null) {
                        HTML link = new HTML(Messages.get("webFolderMessage.label") + "<br /><br /><a target=\"_new\" folder=\"" + url + "\" style=\"behavior:url(#default#AnchorClick)\">" + selection.getDisplayName() + "</a>");
                        final Dialog dl = new Dialog();
                        dl.setModal(true);
                        dl.setHeading(Messages.get("label.openIEFolder"));
                        dl.setHideOnButtonClick(true);
                        dl.setLayout(new FlowLayout());
                        dl.setScrollMode(Style.Scroll.AUTO);
                        dl.add(link);
                        dl.setHeight(150);
                        linker.loaded();
                        dl.show();
                    }
                }
            });
        }
    }

    public void handleNewLinkerSelection(){
        LinkerSelectionContext lh = linker.getSelectionContext();
        setEnabled(lh.getSingleSelection() != null);
    }
}
