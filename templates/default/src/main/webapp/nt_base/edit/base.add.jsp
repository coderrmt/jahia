<%@ taglib prefix="jcr" uri="http://www.jahia.org/tags/jcr" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="utility" uri="http://www.jahia.org/tags/utilityLib" %>
<%@ taglib prefix="template" uri="http://www.jahia.org/tags/templateLib" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="functions" uri="http://www.jahia.org/tags/functions" %>
<%@ taglib prefix="ui" uri="http://www.jahia.org/tags/uiComponentsLib" %>
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
<template:addResources type="css" resources="960.css"/>
<template:addResources type="css" resources="formcontribute.css"/>
<template:addResources type="javascript" resources="jquery.form.js"/>
<utility:useConstants var="jcrPropertyTypes" className="org.jahia.services.content.nodetypes.ExtendedPropertyType"
                      scope="application"/>
<utility:useConstants var="selectorType" className="org.jahia.services.content.nodetypes.SelectorType"
                      scope="application"/>
<c:set var="type" value="${currentResource.resourceNodeType}"/>
<c:set var="scriptTypeName" value="${fn:replace(type.name,':','_')}"/>
<div class="FormContribute">
    <form action="${url.base}${currentNode.path}/*" method="post" id="${currentNode.name}${scriptTypeName}">
        <%--<jcr:nodeType name="jnt:news" var="type"/>--%>
        <input type="hidden" name="nodeType" value="${type.name}"/>
        <input type="hidden" name="redirectTo" value="${url.base}${renderContext.mainResource.node.path}"/>
        <%-- Define the output format for the newly created node by default html or by redirectTo--%>
        <input type="hidden" name="newNodeOutputFormat" value="html"/>
        <fieldset>
            <legend>${jcr:label(type,renderContext.mainResourceLocale)}</legend>
            <c:forEach items="${type.propertyDefinitions}" var="propertyDefinition">
                <c:if test="${!propertyDefinition.multiple and propertyDefinition.contentItem}">
                    <p class="field">
                        <c:choose>
                            <c:when test="${(propertyDefinition.requiredType == jcrPropertyTypes.REFERENCE || propertyDefinition.requiredType == jcrPropertyTypes.WEAKREFERENCE)}">
                                <c:if test="${propertyDefinition.selector eq selectorType.FILEUPLOAD or propertyDefinition.selector eq selectorType.FILEPICKER}">
                                    <%@include file="formelements/file.jsp"%>
                                </c:if>
                                <c:if test="${propertyDefinition.selector eq selectorType.CHOICELIST}">
                                    <%@include file="formelements/select.jsp"%>
                                </c:if>
                            </c:when>
                            <c:when test="${propertyDefinition.requiredType == jcrPropertyTypes.DATE}">
                                <%@include file="formelements/datepicker.jsp"%>
                            </c:when>
                            <c:when test="${propertyDefinition.selector eq selectorType.CHOICELIST}">
                                <%@include file="formelements/select.jsp"%>
                            </c:when>
                            <c:when test="${propertyDefinition.selector eq selectorType.RICHTEXT}">
                                <%@include file="formelements/richtext.jsp"%>
                            </c:when>
                            <c:when test="${propertyDefinition.requiredType == jcrPropertyTypes.BOOLEAN}">
                                <label class="left"
                                       for="${fn:replace(propertyDefinition.name,':','_')}">${jcr:label(propertyDefinition,renderContext.mainResourceLocale)}</label>
                                <input type="radio" value="true" class="radio"
                                       id="${scriptTypeName}${fn:replace(propertyDefinition.name,':','_')}"
                                       name="${propertyDefinition.name}" checked="true"/><fmt:message key="label.yes"/>
                                <input type="radio" value="false" class="radio"
                                       id="${scriptTypeName}${fn:replace(propertyDefinition.name,':','_')}"
                                       name="${propertyDefinition.name}"/><fmt:message key="label.no"/>
                            </c:when>
                            <c:otherwise>
                                <label class="left"
                                       for="${fn:replace(propertyDefinition.name,':','_')}">${jcr:label(propertyDefinition,renderContext.mainResourceLocale)}</label>
                                <input type="text" id="${scriptTypeName}${fn:replace(propertyDefinition.name,':','_')}"
                                       name="${propertyDefinition.name}"/>
                            </c:otherwise>
                        </c:choose>
                    </p>
                </c:if>
            </c:forEach>
            <div class="divButton">
                <button type="submit"><span class="icon-contribute icon-accept"></span><fmt:message
                        key="label.add.new.content.submit"/></button>
                <button type="reset"><span class="icon-contribute icon-cancel"></span><fmt:message
                        key="label.add.new.content.reset"/></button>
            </div>
        </fieldset>
    </form>
    <script type="text/javascript">
        var options${currentNode.name}${scriptTypeName} = {
            success: function() {
                replace('${currentNode.identifier}', '${currentResource.moduleParams.currentListURL}', '');
                $.each(richTextEditors, function(key, value) {
                    value.setData("");
                });
            },
            dataType: "json",
            resetForm : true
        };// wait for the DOM to be loaded
        $(document).ready(function() {
            // bind 'myForm' and provide a simple callback function
            $('#${currentNode.name}${scriptTypeName}').ajaxForm(options${currentNode.name}${scriptTypeName});
        });
    </script>
</div>
