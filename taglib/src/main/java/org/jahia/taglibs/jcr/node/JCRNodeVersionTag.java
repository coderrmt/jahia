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

package org.jahia.taglibs.jcr.node;

import org.jahia.services.content.JCRNodeWrapper;
import org.apache.taglibs.standard.tag.common.core.Util;

import javax.servlet.jsp.PageContext;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.tagext.TagSupport;
import javax.jcr.RepositoryException;

/**
 * 
 * User: toto
 * Date: Dec 3, 2009
 * Time: 7:11:50 PM
 * 
 */
public class JCRNodeVersionTag extends TagSupport {

    private JCRNodeWrapper node;
    private String versionName;
    private String var;
    private int scope = PageContext.PAGE_SCOPE;

    public void setNode(JCRNodeWrapper node) {
        this.node = node;
    }

    public void setVersionName(String versionName) {
        this.versionName = versionName;
    }

    public void setVar(String var) {
        this.var = var;
    }

    public void setScope(String scope) {
        this.scope = Util.getScope(scope);
    }


    @Override
    public int doStartTag() throws JspException {
        try {
            JCRNodeWrapper version = (JCRNodeWrapper) node.getVersionHistory().getVersion(versionName);
            pageContext.setAttribute(var, version, scope);            
        } catch (RepositoryException e) {
            throw new JspException(e);
        }
        return super.doStartTag();
    }

    @Override
    public int doEndTag() throws JspException {
        node = null;
        versionName = null;
        var = null;
        scope = PageContext.PAGE_SCOPE;
        return super.doEndTag();
    }
}
