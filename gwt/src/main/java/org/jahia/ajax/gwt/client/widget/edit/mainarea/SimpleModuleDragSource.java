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

package org.jahia.ajax.gwt.client.widget.edit.mainarea;

import com.extjs.gxt.ui.client.dnd.DND;
import com.extjs.gxt.ui.client.event.DNDEvent;
import com.google.gwt.user.client.DOM;
import org.jahia.ajax.gwt.client.data.node.GWTJahiaNode;
import org.jahia.ajax.gwt.client.widget.edit.EditModeDNDListener;
import org.jahia.ajax.gwt.client.widget.edit.EditModeDragSource;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: toto
 * Date: Aug 21, 2009
 * Time: 4:16:42 PM
 * To change this template use File | Settings | File Templates.
 */
public class SimpleModuleDragSource extends EditModeDragSource {

    private Module module;

    public SimpleModuleDragSource(Module target) {
        super(target.getContainer());
        this.module = target;
    }

    public Module getModule() {
        return module;
    }

    protected void onDragEnd(DNDEvent e) {
        if (e.getStatus().getData("operationCalled") == null) {
            DOM.setStyleAttribute(module.getHtml().getElement(), "display", "block");
        }
        super.onDragEnd(e);
    }

    @Override
    protected void onDragCancelled(DNDEvent dndEvent) {
        DOM.setStyleAttribute(module.getHtml().getElement(), "display", "block");
        super.onDragCancelled(dndEvent);
    }

    @Override
    protected void onDragStart(DNDEvent e) {
        if (module.isDraggable()) {
            super.onDragStart(e);
            if (module.getNode().isWriteable() && !module.getNode().isLocked()) {
                e.setCancelled(false);
                e.setData(this);
                e.setOperation(DND.Operation.COPY);
                if (getStatusText() == null) {
                    e.getStatus().update(DOM.clone(module.getHtml().getElement(), true));

                    e.getStatus().setData("element", module.getHtml().getElement());
                    DOM.setStyleAttribute(module.getHtml().getElement(), "display", "none");

                }
            } else {
                e.setCancelled(true);
            }

            Selection.getInstance().hide();
//            if(module instanceof LinkerModule) {
//                e.getStatus().setData(EditModeDNDListener.SOURCE_TYPE, EditModeDNDListener.BINDED_REFERENCE_TYPE);
//                if (null != module.getScriptInfo() && !"".equals(module.getScriptInfo())) {
//                    e.getStatus().setData(EditModeDNDListener.BINDED_MIXIN_TYPES, module.getScriptInfo());
//                }
//            } else {
                e.getStatus().setData(EditModeDNDListener.SOURCE_TYPE, EditModeDNDListener.SIMPLEMODULE_TYPE);
//            }
            List<GWTJahiaNode> l = new ArrayList<GWTJahiaNode>();
            l.add(getModule().getNode());
            e.getStatus().setData(EditModeDNDListener.SOURCE_NODES, l);
        }
    }

}
