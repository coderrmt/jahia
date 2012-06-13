<%@ page language="java" %>
<%@ page import="org.jahia.services.content.nodetypes.ConstraintsHelper" %>
<%@ page import="org.jahia.services.content.JCRNodeWrapper" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="json" uri="http://www.atg.com/taglibs/json" %>
<%@ taglib prefix="functions" uri="http://www.jahia.org/tags/functions" %>
<%@ taglib prefix="jcr" uri="http://www.jahia.org/tags/jcr" %>
<%@ taglib prefix="template" uri="http://www.jahia.org/tags/templateLib" %>
<%--@elvariable id="currentNode" type="org.jahia.services.content.JCRNodeWrapper"--%>
<%--@elvariable id="prop" type="org.jahia.services.content.JCRPropertyWrapper"--%>
<%--@elvariable id="out" type="java.io.PrintWriter"--%>
<%--@elvariable id="script" type="org.jahia.services.render.scripting.Script"--%>
<%--@elvariable id="scriptInfo" type="java.lang.String"--%>
<%--@elvariable id="workspace" type="java.lang.String"--%>
<%--@elvariable id="renderContext" type="org.jahia.services.render.RenderContext"--%>
<%--@elvariable id="currentResource" type="org.jahia.services.render.Resource"--%>
<%--@elvariable id="url" type="org.jahia.services.render.URLGenerator"--%>
<c:set var="withHidden" value="${functions:default(param.withHidden, false)}" />
<c:set var="withProtected" value="${functions:default(param.withProtected, false)}" />
<json:object name="${currentResource.moduleParams.definitionName}">
    <json:object name="primaryNodeType">
        <c:set var="primaryNodeType" value="${currentNode.primaryNodeType}" />
        <json:property name="name" value="${primaryNodeType.name}" />
        <%-- Property definitions --%>
        <json:array name="propertyDefinitions">
            <c:forEach items="${primaryNodeType.propertyDefinitions}" var="propertyDefinition">
                <c:if test="${((propertyDefinition.hidden && withHidden) || !(propertyDefinition.hidden)) && ((propertyDefinition.protected && withProtected) || !(propertyDefinition.protected))}">
                    <json:object>
                        <json:property name="name" value="${propertyDefinition.name}" />
                        <json:property name="type" value="${jcr:propertyTypeName(propertyDefinition.requiredType)}" />
                        <json:property name="multiple" value="${propertyDefinition.multiple}" />
                        <c:if test="${withHidden}">
                            <json:property name="hidden" value="${propertyDefinition.hidden}" />
                        </c:if>
                        <c:if test="${withProtected}">
                            <json:property name="protected" value="${propertyDefinition.protected}" />
                        </c:if>
                        <json:property name="index" value="${propertyDefinition.index}" />
                        <json:property name="internationalized" value="${propertyDefinition.internationalized}" />
                        <json:array name="defaultValues" items="${propertyDefinition.defaultValues}" var="defaultValue">
                            ${defaultValue.string}
                        </json:array>
                    </json:object>
                </c:if>
            </c:forEach>
        </json:array>
        <json:array name="supertypes" items="${primaryNodeType.supertypes}" var="supertype">
            ${supertype.name}
        </json:array>
        <json:array name="subtypes" items="${primaryNodeType.subtypesAsList}" var="subtype">
            ${subtype.name}
        </json:array>
        <%-- Child definitions --%>
        <json:array name="childDefinitions">
            <c:forEach items="${primaryNodeType.childNodeDefinitions}" var="childDefinition">
                <c:if test="${((childDefinition.hidden && withHidden) || !(childDefinition.hidden)) && ((childDefinition.protected && withProtected) || !(childDefinition.protected))}">
                    <json:object>
                        <json:property name="name" value="${childDefinition.name}" />
                        <json:property name="defaultPrimaryTypeName" value="${childDefinition.defaultPrimaryTypeName}" />
                        <json:property name="mandatory" value="${childDefinition.mandatory}" />
                        <json:array name="requiredPrimaryTypeNames" items="${childDefinition.requiredPrimaryTypeNames}" />
                        <json:property name="prefix" value="${childDefinition.prefix}" />
                        <c:if test="${withHidden}">
                            <json:property name="hidden" value="${childDefinition.hidden}" />
                        </c:if>
                        <c:if test="${withProtected}">
                            <json:property name="protected" value="${childDefinition.protected}" />
                        </c:if>
                    </json:object>
                </c:if>
            </c:forEach>
        </json:array>
    </json:object>
    <json:array name="mixinTypes" items="${currentNode.mixinNodeTypes}" var="mixinNodeType">
        <json:object>
            <json:property name="name" value="${mixinNodeType.name}" />
            <%-- Property definitions --%>
            <json:array name="propertyDefinitions">
                <c:forEach items="${mixinNodeType.propertyDefinitions}" var="propertyDefinition">
                    <c:if test="${((propertyDefinition.hidden && withHidden) || !(propertyDefinition.hidden)) && ((propertyDefinition.protected && withProtected) || !(propertyDefinition.protected))}">
                        <json:object>
                            <json:property name="name" value="${propertyDefinition.name}" />
                            <json:property name="type" value="${jcr:propertyTypeName(propertyDefinition.requiredType)}" />
                            <json:property name="multiple" value="${propertyDefinition.multiple}" />
                            <c:if test="${withHidden}">
                                <json:property name="hidden" value="${propertyDefinition.hidden}" />
                            </c:if>
                            <c:if test="${withProtected}">
                                <json:property name="protected" value="${propertyDefinition.protected}" />
                            </c:if>
                            <json:property name="index" value="${propertyDefinition.index}" />
                            <json:property name="internationalized" value="${propertyDefinition.internationalized}" />
                            <json:array name="defaultValues" items="${propertyDefinition.defaultValues}" var="defaultValue">
                                ${defaultValue.string}
                            </json:array>
                        </json:object>
                    </c:if>
                </c:forEach>
            </json:array>
            <%-- Child definitions --%>
            <json:array name="childDefinitions">
                <c:forEach items="${mixinNodeType.childNodeDefinitions}" var="childDefinition">
                    <c:if test="${((childDefinition.hidden && withHidden) || !(childDefinition.hidden)) && ((childDefinition.protected && withProtected) || !(childDefinition.protected))}">
                        <json:object>
                            <json:property name="name" value="${childDefinition.name}" />
                            <json:property name="defaultPrimaryTypeName" value="${childDefinition.defaultPrimaryTypeName}" />
                            <json:property name="mandatory" value="${childDefinition.mandatory}" />
                            <json:array name="requiredPrimaryTypeNames" items="${childDefinition.requiredPrimaryTypeNames}" />
                            <json:property name="prefix" value="${childDefinition.prefix}" />
                            <c:if test="${withHidden}">
                                <json:property name="hidden" value="${childDefinition.hidden}" />
                            </c:if>
                            <c:if test="${withProtected}">
                                <json:property name="protected" value="${childDefinition.protected}" />
                            </c:if>
                        </json:object>
                    </c:if>
                </c:forEach>
            </json:array>

        </json:object>
    </json:array>
</json:object>