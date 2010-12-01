<%@ taglib prefix="template" uri="http://www.jahia.org/tags/templateLib" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="jcr" uri="http://www.jahia.org/tags/jcr" %>
<%@ taglib prefix="workflow" uri="http://www.jahia.org/tags/workflow" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%--@elvariable id="currentNode" type="org.jahia.services.content.JCRNodeWrapper"--%>
<%--@elvariable id="propertyDefinition" type="org.jahia.services.content.nodetypes.ExtendedPropertyDefinition"--%>
<%--@elvariable id="type" type="org.jahia.services.content.nodetypes.ExtendedNodeType"--%>
<%--@elvariable id="out" type="java.io.PrintWriter"--%>
<%--@elvariable id="script" type="org.jahia.services.render.scripting.Script"--%>
<%--@elvariable id="scriptInfo" type="java.lang.String"--%>
<%--@elvariable id="workspace" type="java.lang.String"--%>
<%--@elvariable id="renderContext" type="org.jahia.services.render.RenderContext"--%>
<%--@elvariable id="currentResource" type="org.jahia.services.render.Resource"--%>
<%--@elvariable id="url" type="org.jahia.services.render.URLGenerator"--%>
<template:addResources type="css" resources="contentlist.css"/>
<template:addResources type="javascript" resources="jquery.min.js"/>
<template:addResources type="javascript" resources="ajaxreplace.js"/>
<template:addResources type="javascript" resources="jquery.jeditable.js"/>
<template:addResources type="javascript"
                       resources="${url.context}/gwt/resources/${url.ckEditor}/ckeditor.js"/>
<template:addResources type="javascript" resources="jquery.ajaxfileupload.js"/>
<template:addResources type="javascript" resources="jquery.jeditable.ajaxupload.js"/>
<template:addResources type="javascript" resources="jquery.jeditable.ckeditor.js"/>
<template:addResources type="javascript" resources="datepicker.js,jquery.jeditable.datepicker.js"/>
<template:addResources type="javascript" resources="contributedefault.js"/>
<template:addResources type="javascript" resources="i18n/contributedefault-${renderContext.mainResource.locale}.js"/>
<template:addResources type="javascript" resources="animatedcollapse.js"/>
<div id="${currentNode.UUID}">
    <c:set var="animatedTasks" value=""/>
    <c:set var="animatedWFs" value=""/>

            <input type="checkbox" checked="true" name="${currentNode.identifier}" visible="false" style="display:none"/>
    <h3>
        <c:if test="${jcr:isNodeType(currentNode.parent,'jnt:contentFolder') || jcr:isNodeType(currentNode.parent,'jnt:folder')}">
            <a title="parent" href="${url.base}${currentNode.parent.path}.html"><img height="16" width="16"
                                                                                     border="0"
                                                                                     style="cursor: pointer;"
                                                                                     title="parent" alt="parent"
                                                                                     src="${url.templatesPath}/default/images/icons/folder_up.png"></a>
        </c:if>

            <%--<c:if test="${not empty currentNode.primaryNodeType.templatePackage.rootFolder}">--%>
                <%--<img src="${url.templatesPath}/${currentNode.primaryNodeType.templatePackage.rootFolder}/icons/${fn:replace(fn:escapeXml(currentNode.primaryNodeType.name),":","_")}.png"/>--%>
            <%--</c:if>--%>
            <%--<c:if test="${empty currentNode.primaryNodeType.templatePackage.rootFolder}">--%>
                <%--<img src="${url.templatesPath}/default/icons/${fn:replace(fn:escapeXml(currentNode.primaryNodeType.name),":","_")}.png"/>--%>
            <%--</c:if>--%>
            <c:if test="${!empty currentNode.properties['jcr:title'].string}">
                ${fn:escapeXml(currentNode.properties['jcr:title'].string)}
            </c:if>
            <c:if test="${empty currentNode.properties['jcr:title'].string}">
                ${fn:escapeXml(currentNode.name)}
            </c:if>
        </h3>
</div>

