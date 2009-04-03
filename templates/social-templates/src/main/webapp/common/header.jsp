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
<%@ include file="declarations.jspf" %>

<meta http-equiv="Content-Type" content="text/html; charset=utf-8" />
<title>${currentSite.title}</title>
<template:themeDisplay defaultTheme="default"/>
<!--[if lte IE 6]>
<link href="misc/ie6.css" rel="stylesheet" type="text/css" />
<![endif]-->
<style type="text/css">
<!--
body { behavior: url(<utility:resolvePath value="scripts/csshover.htc"/>); }
img { behavior: url(<utility:resolvePath value="scripts/iepngfix.htc"/>); }
-->
</style>

<%--Filters Settings--%>
<%--add category Filter--%>
<c:if test="${! empty param.addCategory}">
    <c:choose>
    <c:when test="${empty categoryFilter}">
        <c:set scope="session" var="categoryFilter" value="${param.addCategory}"/>
    </c:when>
    <c:otherwise>
        <c:set scope="session" var="categoryFilter" value="${categoryFilter}$$$${param.addCategory}"/>
    </c:otherwise>
    </c:choose>
</c:if>
<%--remove category Filter--%>
<c:if test="${!empty param.removeCategory}">
    <c:set var="categoriesMap" value="${fn:split(categoryFilter, '$$$')}"/>
    <c:set var="categoriestmp" value=""/>
    <c:forEach var="category" items="${categoriesMap}">
        <c:if test="${!category eq param.removeCategory}">
            <c:choose>
            <c:when test="${empty categoriestmp}">
                <c:set var="categoriestmp" value="${category}"/>
            </c:when>
            <c:otherwise>
                <c:set var="categoriestmp" value="${categoriestmp}$$$${category}"/>
            </c:otherwise>
            </c:choose>
        </c:if>
    </c:forEach>
    <c:set var="categoryFilter" value="${categoriestmp}" scope="session"/>
</c:if>
