<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="jcr" uri="http://www.jahia.org/tags/jcr" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="template" uri="http://www.jahia.org/tags/templateLib" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<template:addResources type="css" resources="tasks.css"/>

<template:addWrapper name="wrapper.dashboard"/>

<template:addResources type="javascript" resources="ajaxreplace.js"/>

<form name="myform" method="post">
    <input type="hidden" name="nodeType" value="jnt:task">
    <input type="hidden" name="redirectTo" value="${url.base}${currentNode.path}.tasklist">
    <input type="hidden" name="state">
</form>


<script type="text/javascript" >
    function send(task, state) {
        form = document.forms['myform'];
        form.action='${url.base}'+task;
        form.elements.state.value = state;
        form.submit();
    }
</script>
<h4 class="boxtasks-title"><fmt:message key='jnt_task.myTasks'/></h4>
<div class="boxtasks">
    <div class=" boxtasksgrey boxtaskspadding16 boxtasksmarginbottom16">
        <div class="boxtasks-inner">
            <div class="boxtasks-inner-border"><!--start boxtasks -->
<div id="${currentNode.UUID}-tasks">



    <table width="100%" class="table tableTasks" summary="Tasks">
        <caption>
            <fmt:message key='jnt_task.myTasks'/>
        </caption>
        <colgroup>
            <col span="1" width="10%" class="col1"/>
            <col span="1" width="50%" class="col2"/>
            <col span="1" width="10%" class="col3"/>
            <col span="1" width="25%" class="col4"/>
            <col span="1" width="15%" class="col5"/>
        </colgroup>
        <thead>
        <tr>
            <th class="center" id="Type" scope="col"><fmt:message key="jnt_task.type"/> <a href="#" title="sort up"><img
                    src="${url.currentModule}/images/sort-arrow-up.png" alt="up"/></a><a title="sort down"
                                                                                         href="#"> <img
                    src="${url.currentModule}/images/sort-arrow-down.png" alt="down"/></a></th>
            <th id="Title" scope="col"><fmt:message key="mix_title.jcr_title"/> <a href="#" title="sort up"><img
                    src="${url.currentModule}/images/sort-arrow-up.png"
                    alt="up"/></a><a
                    title="sort down" href="#"> <img src="${url.currentModule}/images/sort-arrow-down.png"
                                                     alt="down"/></a></th>
            <th class="center" id="State" scope="col"><fmt:message key="jnt_task.state"/> <a href="#" title="sort up"><img
                    src="${url.currentModule}/images/sort-arrow-up.png" alt="up"/></a><a title="sort down"
                                                                                         href="#"> <img
                    src="${url.currentModule}/images/sort-arrow-down.png" alt="down"/></a></th>
            <th class="center" id="Priority" scope="col"><fmt:message key="jnt_task.priority"/> <a href="#" title="sort up"><img
                    src="${url.currentModule}/images/sort-arrow-up.png" alt="up"/></a><a title="sort down"
                                                                                         href="#"> <img
                    src="${url.currentModule}/images/sort-arrow-down.png" alt="down"/></a></th>
            <th id="Date" scope="col"><fmt:message key="jnt_task.dueDate"/> <a href="#" title="sort up"><img
                    src="${url.currentModule}/images/sort-arrow-up.png"
                    alt="up"/></a><a
                    title="sort down" href="#"> <img src="${url.currentModule}/images/sort-arrow-down.png"
                                                     alt="down"/></a></th>
        </tr>
        </thead>

        <tbody>

        <jcr:sql var="tasks" sql="select * from [jnt:task] as task where task.assignee='${currentNode.identifier}'"/>
        <%--<c:set value="${jcr:getNodes(currentNode,'jnt:task')}" var="tasks"/>--%>
        <template:initPager pageSize="10" totalSize="${tasks.nodes.size}"/>

        <c:forEach items="${tasks.nodes}" var="task"
                   begin="${begin}" end="${end}" varStatus="status">
            <c:choose>
                <c:when test="${status.count % 2 == 0}">
                    <tr class="odd">
                </c:when>
                <c:otherwise>
                    <tr class="even">
                </c:otherwise>
            </c:choose>
            <td class="center" headers="Type"><img alt="" src="${url.currentModule}/images/flag_16.png"/>
            </td>
            <td headers="Title"><a href="${url.base}${task.path}.html">${task.properties['jcr:title'].string}</a></td>
            <td class="center" headers="Priority">
                    ${currentNode.properties.priority.string}
            </td>
            <td class="center" headers="State">
                <c:choose>
                    <c:when test="${task.properties.state.string == 'active'}">
                        <span><img alt="" src="${url.currentModule}/images/right_16.png"/></span>
                        <span>
                            <a href="javascript:send('${task.path}','suspended')"><fmt:message key="jnt_task.suspended"/></a>&nbsp;
                            <a href="javascript:send('${task.path}','cancelled')"><fmt:message key="jnt_task.cancel"/></a>&nbsp;
                            <a href="javascript:send('${task.path}','finished')"><fmt:message key="jnt_task.complete"/></a>
                        </span>
                    </c:when>
                    <c:when test="${task.properties.state.string == 'finished'}">
                        <img alt="" src="${url.currentModule}/images/tick_16.png"/>
                    </c:when>
                    <c:when test="${task.properties.state.string == 'suspended'}">
                        <span><img alt="" src="${url.currentModule}/images/bubble_16.png"/></span>
                        <span>
                            <a href="javascript:send('${task.path}','cancelled')"><fmt:message key="jnt_task.cancel"/></a>&nbsp;
                            <a href="javascript:send('${task.path}','active')"><fmt:message key="jnt_task.continue"/></a>
                        </span>
                    </c:when>
                    <c:when test="${task.properties.state.string == 'canceled'}">
                        <img alt="" src="${url.currentModule}/images/warning_16.png"/>
                    </c:when>
                </c:choose>
            </td>
            <td headers="Date"><fmt:formatDate value="${currentNode.properties['dueDate'].date.time}"
                                               dateStyle="short" type="date"/></td>
            </tr>
        </c:forEach>
        </tbody>
    </table>
    <div class="pagination"><!--start pagination-->

        <div class="paginationPosition"><span>Page ${currentPage} of ${nbPages} - ${tasks.nodes.size} results</span>
        </div>
        <div class="paginationNavigation">
            <c:if test="${currentPage>1}">
                <a class="previousLink"
                   href="javascript:replace('${currentNode.UUID}-tasks','${url.current}?ajaxcall=true&begin=${ (currentPage-2) * pageSize }&end=${ (currentPage-1)*pageSize-1}')">Previous</a>
            </c:if>
            <c:forEach begin="1" end="${nbPages}" var="i">
                <c:if test="${i != currentPage}">
                    <span><a class="paginationPageUrl"
                             href="javascript:replace('${currentNode.UUID}-tasks','${url.current}?ajaxcall=true&begin=${ (i-1) * pageSize }&end=${ i*pageSize-1}')"> ${ i }</a></span>
                </c:if>
                <c:if test="${i == currentPage}">
                    <span class="currentPage">${ i }</span>
                </c:if>
            </c:forEach>

            <c:if test="${currentPage<nbPages}">
                <a class="nextLink"
                   href="javascript:replace('${currentNode.UUID}-tasks','${url.current}?ajaxcall=true&begin=${ currentPage * pageSize }&end=${ (currentPage+1)*pageSize-1}')">Next</a>
            </c:if>
        </div>

        <div class="clear"></div>
    </div>
    <!--stop pagination-->
</div>
                    <div class="clear"></div>
                </div>
            </div>
        </div>
    </div>