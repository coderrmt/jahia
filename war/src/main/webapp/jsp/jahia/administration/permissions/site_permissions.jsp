<%--

    
    This file is part of Jahia: An integrated WCM, DMS and Portal Solution
    Copyright (C) 2002-2009 Jahia Limited. All rights reserved.
    
    This program is free software; you can redistribute it and/or
    modify it under the terms of the GNU General Public License
    as published by the Free Software Foundation; either version 2
    of the License, or (at your option) any later version.
    
    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
    GNU General Public License for more details.
    
    You should have received a copy of the GNU General Public License
    along with this program; if not, write to the Free Software
    Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.
    
    As a special exception to the terms and conditions of version 2.0 of
    the GPL (or any later version), you may redistribute this Program in connection
    with Free/Libre and Open Source Software ("FLOSS") applications as described
    in Jahia's FLOSS exception. You should have recieved a copy of the text
    describing the FLOSS exception, and it is also available here:
    http://www.jahia.com/license
    
    Commercial and Supported Versions of the program
    Alternatively, commercial and supported versions of the program may be used
    in accordance with the terms contained in a separate written agreement
    between you and Jahia Limited. If you are unsure which license is appropriate
    for your use, please contact the sales department at sales@jahia.com.

--%>

<%@include file="/jsp/jahia/administration/include/header.inc" %>
<%@page import="org.jahia.admin.permissions.ManageSitePermissions" %>
<%@page import="org.jahia.data.viewhelper.principal.PrincipalViewHelper" %>
<%@ page import="org.jahia.hibernate.model.JahiaAclName" %>
<%@ page import="org.jahia.registries.ServicesRegistry" %>
<%@ page import="org.jahia.security.license.LicenseActionChecker" %>
<%@ page import="org.jahia.services.usermanager.JahiaGroup" %>
<%@ page import="org.jahia.services.usermanager.JahiaUser" %>
<%@ page import="org.jahia.utils.LanguageCodeConverters" %>
<%@ page import="java.util.Iterator" %>
<%@ page import="java.util.List" %>
<%@ page import="java.util.Map" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%
    List aclNameList = (List) request.getAttribute("aclNameList");
    final Integer userNameWidth = new Integer(15);
    request.getSession().setAttribute("userNameWidth", userNameWidth);
    final String selectUsrGrp = (String) request.getAttribute("selectUsrGrp");
    final String curPermissionGroup = (String) session.getAttribute(ManageSitePermissions.class.getName() + ManageSitePermissions.CURGROUP_SESSION_ATTR_NAME); %>
<div id="topTitle">
    <h1>Jahia</h1>

    <h2 class="edit"><internal:adminResourceBundle resourceName="org.jahia.admin.sitepermissions.title.label"/></h2>
</div>
<internal:gwtImport module="org.jahia.ajax.gwt.admin.Administration" />

<div id="main">
<table style="width: 100%;" class="dex-TabPanel" cellpadding="0" cellspacing="0">
<tbody>
<tr>
    <td style="vertical-align: top;" align="left">
        <%@include file="/jsp/jahia/administration/include/tab_menu.inc" %>
    </td>
</tr>
<tr>
<td style="vertical-align: top;" align="left" height="100%">
<div class="dex-TabPanelBottom">
<div class="tabContent">
<jsp:include page="/jsp/jahia/administration/include/left_menu.jsp">
    <jsp:param name="mode" value="site"/>
</jsp:include>
<div id="content" class="fit">
<div class="head headtop">
    <div class="object-title">
        <internal:adminResourceBundle resourceName="org.jahia.admin.sitepermissions.mainMenu.label"/>
    </div>
</div>
<div class="content-item">
<form name="jahiaAdmin"
      action='<%=JahiaAdministration.composeActionURL(request,response,"sitePermissions","&sub=process")%>'
      method="post">
<script type="text/javascript">
    function saveContent() {
        document.jahiaAdmin.submit();
    }
    function changePermissionGroup(newGroup) {
        document.jahiaAdmin.currentGroup.value = newGroup;
        document.jahiaAdmin.submit();
    }
</script>
<input type="hidden" name="currentGroup" value="<%=curPermissionGroup%>"/>

<p>
    <internal:adminResourceBundle resourceName="org.jahia.admin.sitepermissions.introduction.label"/>
</p>

<div class="dex-subTabBar">
    <ul class="dex-subTabBarItem-wrapper">
        <li class="<% if ("administration".equals(curPermissionGroup)) { %>dex-subTabBarItem-selected<% } else { %>dex-subTabBarItem-first<% } %>">
            <div>
                <div>
                              <span>
                                <a href="#"
                                   onclick="changePermissionGroup('administration')"><internal:adminResourceBundle
                                        resourceName="org.jahia.admin.sitepermissions.permissionGroup.administration.label"/></a>
                              </span>
                </div>
            </div>
        </li>
        <li class="dex-subTabBarItem<% if ("data".equals(curPermissionGroup)) { %>-selected<% } %>">
            <div>
                <div>
                              <span>
                                <a href="#" onclick="changePermissionGroup('data')"><internal:adminResourceBundle
                                        resourceName="org.jahia.admin.sitepermissions.permissionGroup.data.label"/></a>
                              </span>
                </div>
            </div>
        </li>
        <li class="dex-subTabBarItem<% if ("tools".equals(curPermissionGroup)) { %>-selected<% } %>">
            <div>
                <div>
                              <span>
                                <a href="#" onclick="changePermissionGroup('tools')"><internal:adminResourceBundle
                                        resourceName="org.jahia.admin.sitepermissions.permissionGroup.tools.label"/></a>
                              </span>
                </div>
            </div>
        </li>
        <li class="dex-subTabBarItem<% if ("actions".equals(curPermissionGroup)) { %>-selected<% } %>">
            <div>
                <div>
                              <span>
                               <a href="#" onclick="changePermissionGroup('actions')"><internal:adminResourceBundle
                                       resourceName="org.jahia.admin.sitepermissions.permissionGroup.actions.label"/></a>
                              </span>
                </div>
            </div>
        </li>
        <li class="dex-subTabBarItem<% if ("languages".equals(curPermissionGroup)) { %>-selected<% } %>">
            <div>
                <div>
                              <span>
                               <a href="#" onclick="changePermissionGroup('languages')"><internal:adminResourceBundle
                                       resourceName="org.jahia.admin.sitepermissions.permissionGroup.languages.label"/></a>
                              </span>
                </div>
            </div>
        </li>
        <li class="dex-subTabBarItem<% if ("toolbars".equals(curPermissionGroup)) { %>-selected<% } %>">
            <div>
                <div>
                              <span>
                               <a href="#" onclick="changePermissionGroup('toolbars')"><internal:adminResourceBundle
                                       resourceName="org.jahia.admin.sitepermissions.permissionGroup.toolbars.label"/></a>
                              </span>
                </div>
            </div>
        </li>
        <li class="dex-subTabBarItem<% if ("integrity".equals(curPermissionGroup)) { %>-selected<% } %>">
            <div>
                <div>
                              <span>
                               <a href="#" onclick="changePermissionGroup('integrity')"><internal:adminResourceBundle
                                       resourceName="org.jahia.admin.sitepermissions.permissionGroup.integrity.label" defaultValue="Integrity checks"/></a>
                              </span>
                </div>
            </div>
        </li>
    </ul>
</div>
<div class="buttonList" style="text-align:right; padding:10px">
                    <span class="dex-PushButton">
                        <span class="first-child">
                          <a class="ico-ok" href="javascript:saveContent();"><internal:adminResourceBundle
                                  resourceName="org.jahia.admin.saveChanges.label"/></a>
                        </span>
                    </span>
</div>
<div style="clear:both">
    <table cellpadding="0" cellspacing="0" border="0" width="100%" class="permissions">
    <c:forEach items="${aclNameList}" var="curAclName" varStatus="status">
        <tr>
            <td>
                <div class="head headtop">
                    <div class="object-title">
                        <c:set var="aclShortName" value="${fn:substringAfter(fn:substringAfter(curAclName.aclName, 'org.jahia.actions.sites.'), '.')}"/>
                        <c:if test="${fn:contains(aclShortName, 'engines.languages.')}" var="languagePermissions">
                            <internal:adminResourceBundle resourceName="org.jahia.admin.sitepermissions.permission.engines.languages.label"/>
                            <%
                            String aclShortName = (String)pageContext.getAttribute("aclShortName");
                            String flagCode = aclShortName.substring(aclShortName.lastIndexOf('.') + 1); 
                            %>
                            <c:set var="curLocale" value="${requestScope['org.jahia.data.JahiaData'].processingContext.locale}"/><%= LanguageCodeConverters.languageCodeToLocale(flagCode).getDisplayName((Locale) pageContext.getAttribute("curLocale")) %>
                            <internal:displayLanguageFlag code="<%=flagCode%>"/>
                        </c:if>
                        <c:if test="${!languagePermissions}">
                            <internal:adminResourceBundle resourceName="org.jahia.admin.sitepermissions.permission.${aclShortName}.label" defaultValue="org.jahia.admin.sitepermissions.permission.${aclShortName}.label"/>
                        </c:if>
                    </div>
                </div>
                <c:set var="aclName" value="${curAclName.aclName}"/>
                <c:set var="fieldName" value="${fn:replace(aclName, '.', '_')}"/>
                <c:set var="readonly"><%= !LicenseActionChecker.isAuthorizedByLicense((String) pageContext.getAttribute("aclName"), siteID.intValue()) %></c:set>
                <input type="hidden" name="${fieldName}" id="${fieldName}" value=""/>
                <span id="gwtaclnameeditor-${status.index}" aclid="${curAclName.acl.id}" height="120px" fieldId="${fieldName}" readonly="${readonly}"></span>
            </td>
        </tr>
    </c:forEach>
    </table>
</div>

</form>
</div>
</div>
</div>
</div>
</td>
</tr>
</tbody>
</table>
</div>
<div id="actionBar">
            <span class="dex-PushButton">
                <span class="first-child">
                  <a class="ico-ok" href="javascript:saveContent();"><internal:adminResourceBundle
                          resourceName="org.jahia.admin.saveChanges.label"/></a>
                </span>
            </span>
</div>
</div>
<%@include file="/jsp/jahia/administration/include/footer.inc" %>
