/**
 * This file is part of Jahia, next-generation open source CMS:
 * Jahia's next-generation, open source CMS stems from a widely acknowledged vision
 * of enterprise application convergence - web, search, document, social and portal -
 * unified by the simplicity of web content management.
 *
 * For more information, please visit http://www.jahia.com.
 *
 * Copyright (C) 2002-2014 Jahia Solutions Group SA. All rights reserved.
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

package org.jahia.ajax.gwt.client.widget.toolbar.action;

import org.jahia.ajax.gwt.client.data.node.GWTJahiaNode;
import org.jahia.ajax.gwt.client.messages.Messages;
import org.jahia.ajax.gwt.client.util.content.CopyPasteEngine;
import org.jahia.ajax.gwt.client.util.security.PermissionsUtils;
import org.jahia.ajax.gwt.client.widget.LinkerSelectionContext;
import org.jahia.ajax.gwt.client.widget.edit.EditLinker;
import org.jahia.ajax.gwt.client.widget.edit.mainarea.MainModule;

import java.util.Arrays;
import java.util.List;

/**
 * 
* User: toto
* Date: Sep 25, 2009
* Time: 6:57:20 PM
*/
public class PasteActionItem extends BaseActionItem {
    private boolean pasteInMainNode;

    public boolean isPasteInMainNode() {
        return pasteInMainNode;
    }

    public void setPasteInMainNode(boolean pasteInMainNode) {
        this.pasteInMainNode = pasteInMainNode;
    }

    public void onComponentSelection() {
        GWTJahiaNode m = linker.getSelectionContext().getSingleSelection();
        if (pasteInMainNode) {
            m = linker.getSelectionContext().getMainNode();
        }
        if (m != null) {
            linker.loading(Messages.get("statusbar.pasting.label"));
            final CopyPasteEngine copyPasteEngine = CopyPasteEngine.getInstance();
            copyPasteEngine.paste(m, linker);
        }
    }

    public void handleNewLinkerSelection() {
        LinkerSelectionContext lh = linker.getSelectionContext();
        boolean b;

        if (pasteInMainNode) {
            b = lh.getMainNode() != null && hasPermission(lh.getMainNode()) && PermissionsUtils.isPermitted("jcr:addChildNodes", lh.getMainNode());
        } else {
            b = lh.getSingleSelection() != null && hasPermission(lh.getSelectionPermissions()) &&PermissionsUtils.isPermitted("jcr:addChildNodes", lh.getSelectionPermissions()) && lh.isPasteAllowed();
        }

        if (b && linker instanceof EditLinker) {
            String nodeTypes = "";
            for (String nodeType : linker.getSelectionContext().getSingleSelection().getNodeTypes()) {
                nodeTypes += nodeTypes.equals("")?nodeType:","+nodeType;
            }
            b = b && CopyPasteEngine.getInstance().checkNodeType(nodeTypes);
        }

        setEnabled(b);
    }

}
