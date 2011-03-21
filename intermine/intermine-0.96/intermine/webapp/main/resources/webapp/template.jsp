<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="/WEB-INF/struts-html.tld" prefix="html" %>
<%@ taglib uri="/WEB-INF/struts-tiles.tld" prefix="tiles" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%@ taglib tagdir="/WEB-INF/tags" prefix="im"%>

<!-- template.jsp -->


<%@page import="java.util.List"%><c:set var="index" value="${0}"/>

<tiles:importAttribute/>
<tiles:importAttribute name="builder" scope="request" ignore="true"/>
<html:xhtml/>
<script type="text/javascript" src="js/templateForm.js"></script>
<script type="text/javascript" src="js/autocompleter.js"></script>
<link rel="stylesheet" href="css/autocompleter.css" type="text/css"/>
<link rel="stylesheet" href="css/template.css" type="text/css"/>

<c:if test="${!empty builder && builder=='yes'}">
  <script language="javascript">
  var previousConstraintsOrder = '';

  jQuery(document).ready(function(){
    jQuery('#constraintList').sortable({dropOnEmpty:true,update:function() {
            reorderConstraintsOnServer();
          }
    });
      recordCurrentConstraintsOrder();
  });

    function codeGenTemplate(method) {
        jQuery('#actionType').val(method);
        jquery('#templateForm').submit();
    }

</script>
</c:if>
<c:choose>
<c:when test="${!empty templateQuery}">
<%-- object trail --%>
<tiles:get name="objectTrail.tile"/>
<div class="body" align="center">
<im:boxarea titleImage="templates-64.png" stylename="plainbox" fixedWidth="90%">
<html:form styleId="templateForm" action="/templateAction">
    <%-- template title --%>
    <h2 class="templateTitle">
        <c:out value="${fn:replace(templateQuery.title,'-->','&nbsp;<img src=\"images/icons/green-arrow-24.png\" style=\"vertical-align:middle\">&nbsp;')}" escapeXml="false"/>
        <tiles:insert name="setFavourite.tile">
            <tiles:put name="name" value="${templateQuery.name}"/>
            <tiles:put name="type" value="template"/>
        </tiles:insert>
    </h2>

    <%-- description --%>
    <div class="templateDescription">${templateQuery.description}</div>
    <div class="templateFormContainer">
    <ol class="templateForm" id="constraintList">
        <%-- constraint list --%>
            <c:forEach items="${dcl}" var="dec" >
                <c:set var="index" value="${index+1}"/>
                <li id="constraintElement${index}_${index}">
                <%-- builder=yes means we are in template preview --%>
                <c:if test="${!empty builder && builder=='yes'}">
                <div style="border: 1px solid #bbb;" onmouseover="this.style.border='1px solid #444';this.style.cursor='move';"
                    onmouseout="this.style.border='1px solid #bbb';this.style.cursor='default';">
                </c:if>
                <%-- constraint name --%>
            <table class="templateConstraint">
              <tr>

                <c:set var="constraintHeadingClass" value=""/>
                <c:if test="${dec.disabled}">
                    <c:set var="constraintHeadingClass" value="constraintHeadingDisabled"/>
                </c:if>

                <td><div class="constraint_${index} ${constraintHeadingClass}"><span class="templateConstraintPath"> <c:out value="${dec.title}" />
                </span> <c:if test="${not empty dec.description}">
                  <span class="templateConstraintDescription"><c:out value=" - ${dec.description}" /></span>
                </c:if></div></td>
              </tr>
            </table>
            <c:set var="valignExternalTd" value="top"/>
            <c:set var="rowspanExternalTd" value="1"/>
            <%--if the bag constraint is displayed the helpLink and the optional sections have different valign and rowspan --%>
            <c:if test="${!empty dec.bags && !dec.nullSelected}">
              <c:set var="valignExternalTd" value="middle"/>
              <c:set var="rowspanExternalTd" value="2"/>
            </c:if>
              <table cellspacing="4">
              <tr>
              <td class="templateConstraintOptional" rowspan="${rowspanExternalTd}" valign="${valignExternalTd}">
                <div>
                  <c:if test="${!dec.locked}">
                    <c:set var="clickToEnable" value="javascript:enableConstraint(${index});"/>
                    <c:set var="clickToDisable" value="javascript:disableConstraint(${index});"/>
                    <c:if test="${!empty builder}">
                      <c:set var="clickToEnable" value="javascript:;"/>
                      <c:set var="clickToDisable" value="javascript:;"/>
                    </c:if>

                    <span class="optionalText">optional</span><br />

                    <c:choose>
                      <c:when test="${dec.enabled}">
                         <html:hidden property="switchOff(${index})" value="ON" styleId="switchOff(${index})" />

                        <div id="optionalEnabled_${index}" class="optionalOnOff" style="display:inline">
                          <span class="optionalSelected">ON</span>&nbsp;|&nbsp;<span><a href="${clickToDisable}" title="Disable constraint">OFF</a></span>
                        </div>
                        <div id="optionalDisabled_${index}" class="optionalOnOff" style="display:none">
                          <span><a href="${clickToEnable}" title="Enable constraint">ON</a></span>&nbsp;|&nbsp;<span class="optionalSelected">OFF</span>
                        </div>
                      </c:when>
                      <c:otherwise>
                        <html:hidden property="switchOff(${index})" value="OFF" styleId="switchOff(${index})" />

                        <div id="optionalEnabled_${index}" class="optionalOnOff" style="display:none">
                          <span class="optionalSelected">ON</span>&nbsp;|&nbsp;<span><a href="${clickToDisable}" title="Disable constraint">OFF</a></span>
                        </div>
                        <div id="optionalDisabled_${index}" class="optionalOnOff" style="display:inline">
                          <span><a href="${clickToEnable}" title="Enable constraint">ON</a></span>&nbsp;|&nbsp;<span class="optionalSelected">OFF</span>
                        </div>
                      </c:otherwise>
                    </c:choose>
                  </c:if>
                </div></td>

               <c:set var="constraintBodyClass" value=""/>
                <c:if test="${dec.disabled}">
                    <c:set var="constraintBodyClass" value="constraintHeadingDisabled"/>
                </c:if>

              <%-- if Boolean --%>
              <td class="constraint_${index}">
              <c:choose>
              <c:when test="${dec.boolean}">
              <html:hidden property="attributeOps(${index})" value="0" disabled="false" />
              <html:radio property="attributeValues(${index})" value="true"/>
              <fmt:message key="query.constraint.true" />
              <html:radio property="attributeValues(${index})" value="false"/>
              <fmt:message key="query.constraint.false" />
              </c:when>
              <%-- if null or not null value --%>
              <c:when test="${dec.nullSelected}">
                <html:radio property="nullConstraint(${index})" value="IS NULL"/><fmt:message key="query.constraint.null"/>
                <html:radio property="nullConstraint(${index})" value="IS NOT NULL"/><fmt:message key="query.constraint.notnull"/>
              </c:when>
              <c:otherwise>
              <%-- operator --%>
                  <c:choose>
                  <c:when test="${!dec.lookup}">
                    <div style="float:left;margin-right:5px;">
                    <html:select property="attributeOps(${index})" onchange="onChangeAttributeOps(${index});">
                      <c:forEach items="${dec.validOps}" var="op">
                      <option value="${op.property}"
                        <c:if test="${!empty dec.selectedOp && dec.selectedOp.property == op.property}">selected</c:if>>
                        <im:displayableOpName opName="${op.label}" valueType="${op.property}" />
                      </option>
                    </c:forEach>
                    </html:select>
                    </div>
                   </c:when>
                   <c:otherwise>
                   <html:hidden styleId="attributeOps(${index})" property="attributeOps(${index})" value="${dec.lookupOp.property}"/>
                   <fmt:message key="query.lookupConstraintLabel" />&nbsp;<%-- LOOKUP: --%>
                   </c:otherwise>
                   </c:choose>
                   <%-- if can be multi value --%>
               <c:if test="${!empty dec.possibleValues}">
                   <html:hidden property="multiValueAttribute(${index})"/>
                   <html:select property="multiValues(${index})" multiple="true" size="4" onchange="updateMultiValueAttribute(${index});" style="height:auto">
                   <c:forEach items="${dec.possibleValues}" var="multiValue">
                   <html:option value="${multiValue}"><c:out value="${multiValue}"/></html:option>
                   </c:forEach>
                   </html:select>
                </c:if>
              <%-- autocomplete --%>
                <span id="operandEditSpan${index-1}">
                  <c:choose>
                  <%-- inputfield for an autocompletion --%>
                  <c:when test="${!empty dec.autoCompleter}">
                    <input name="attributeValues(${index})" id="attributeId_${index}" size="45"
                      style="background: #ffffc8"
                      value="${dec.selectedValue}"
                      onKeyDown="getId(this.id); isEnter(event);"
                      onKeyUp="readInput(event, '${dec.path.lastClassName}', '${dec.path.fieldName}');"
                      onMouseOver="setMouseOver(${index});"
                      onMouseOut="setMouseOver(0);"
                      onBlur="if(MOUSE_OVER != ${index}) { removeList(); }" />
                    <iframe width="100%" height="0" id="attributeId_${index}_IEbugFixFrame"
                      marginheight="0" marginwidth="0" frameborder="0" style="position: absolute;"> </iframe>
                    <div class="auto_complete" id="attributeId_${index}_display"
                      onMouseOver="setMouseOver(${index});"
                      onMouseOut="setMouseOver(0);"
                      onBlur="if(MOUSE_OVER != ${index}) { removeList(); }"></div>
                    <div class="error_auto_complete" id="attributeId_${index}_error"></div>
                  </c:when>

                  <%-- normal inputfield, no auto completer exists --%>
                  <c:otherwise>
                     <im:dateInput attributeType="${dec.path.type}" property="attributeValues(${index})"
                       styleId="attribute6" value="${(dec.possibleValuesDisplayed && dec.selectedValue == null) ? dec.possibleValues[0] : dec.selectedValue}"/>
                   </c:otherwise>
                </c:choose>
                </span>
                 <%-- dropdown --%>
              <c:if test="${!empty dec.possibleValues}">
                <select name="attributeOptions(${index})" onchange="updateAttributeValues(${index});">
                  <c:forEach items="${dec.possibleValues}" var="option">
                      <option value="${option}" <c:if test="${dec.selectedValue == option}">selected</c:if>>
                      <c:out value="${option}" /></option>
                    </c:forEach>
                </select>
              </c:if>
                </c:otherwise>
                </c:choose>



         <%-- dropdown (probably organism) --%>
         <c:choose>
          <c:when test="${dec.extraConstraint}">
          <span style="color:#eee;">
                <label class="marg">
                  <fmt:message key="bagBuild.extraConstraint">
                    <fmt:param value="${dec.extraConstraintClassName}"/>
                  </fmt:message>
                </label>

            <html:select property="extraValues(${index})" value="${dec.selectedExtraValue}">
              <html:option value="">Any</html:option>
               <!-- this should set to extraValue if editing existing constraint -->
              <c:forEach items="${dec.extraConstraintValues}" var="value">
                <html:option value="${value}">
                  <c:out value="${value}" />
                </html:option>
              </c:forEach>
            </html:select>
          </span>
          </c:when>
          <c:otherwise>
            <html:hidden property="extraValue" value="" />
          </c:otherwise>
        </c:choose>
        </td>

        <%-- help link --%>
        <td rowspan="${rowspanExternalTd}" valign="${valignExternalTd}">
          <c:if test="${!empty dec.helpMessage}">
            <span class="templateConstraintHelp"><im:helplink text="${dec.helpMessage}"/></span>
          </c:if>
        </td>
        </tr>
        <tr>
        <td class="constraint_${index}">
          <c:if test="${!empty dec.bags && !dec.nullSelected}">
            <html:checkbox property="useBagConstraint(${index})" onclick="clickUseBag(${index})" disabled="${empty dec.bags?'true':'false'}" />
            <fmt:message key="template.constraintobe"/>
            <%--Contained in bag:--%>
            <html:select property="bagOp(${index})" disabled="true">
              <c:forEach items="${dec.bagOps}" var="bagOp">
                <option value="${bagOp.property}" <c:if test="${!empty dec.bagSelected && dec.selectedOp.property == bagOp.property}">selected</c:if>>
                  <c:out value="${bagOp.label}" />
                </option>
              </c:forEach>
            </html:select>
            <fmt:message key="template.constraintobelist"><fmt:param value="${dec.bagType}"/></fmt:message>
            <html:select property="bag(${index})" disabled="true">
              <c:forEach items="${dec.bags}" var="bag">
                <option value="${bag}" <c:if test="${!empty dec.bagSelected && dec.selectedValue == bag}">selected</c:if>>
                  <c:out value="${bag}" />
                </option>
              </c:forEach>
            </html:select>
          </c:if>
        </td>

        <%-- AND and OR button --%>
       <%--
         <td valign="middle">
           <a id="orButton(${index})" style="text-decoration:none;" href="javascript:addOR(${index})" title="Add OR constraint">
             <span style="font-size: 12px;color: #477b46;font-weight: bold;margin-left: 15px;margin-right: 5px">OR+</span>
           </a>
           <a id="andButton(${index})" style="text-decoration:none;" href="javascript:addAND(${index})" title="Add AND constraint">
             <span style="font-size: 12px;color: #477b46;font-weight: bold;margin-right: 5px">AND+</span>
           </a>
         </td>
       --%>
      </tr>
                </div>

    </table>
    <c:if test="${!empty builder && builder=='yes'}">
      </div>
    </c:if>
  </li>

       <script type="text/javascript">
         initConstraints(${index});
       </script>
        </c:forEach>
</ol>
</div>
<%-- edit/submit buttons --%>
<c:if test="${empty builder}">
    <div id="templateButtons">
          <html:hidden property="name"/>
          <html:hidden property="scope"/>
          <html:hidden property="actionType" value="" styleId="actionType"/>
          <!-- Twisted by the Dark Side young Skywalker has become... -->
          <div class="floatRight">
          <input type="button" onclick="jQuery('input#editQueryButton').click();" class="editQueryBuilder" value="<fmt:message key="template.submitToQuery"/>" />
          <c:if test="${IS_SUPERUSER}">
            <input type="button" onclick="jQuery('input#editTemplateButton').click();" class="editTemplate" value="<fmt:message key="template.submitToQueryEdit"/>" />
          </c:if>
          </div>
          <!-- default action, if you do not care about submit button ordering -->
          <html:submit property="skipBuilder" styleClass="next" styleId="showResultsButton">
            <fmt:message key="template.submitToResults"/>
          </html:submit>


          <!-- these are not the drones you are looking for... -->
      <html:submit property="editQuery" styleId="editQueryButton" style="display:none;">
            <fmt:message key="template.submitToQuery"/>
          </html:submit>
          <html:submit property="editTemplate" styleId="editTemplateButton" style="display:none;">
            <fmt:message key="template.submitToQueryEdit"/>
          </html:submit>
  </div>
</c:if>
</html:form>

<div class="templateActions">
<table>
  <tr>
    <c:set var="webserviceLink" value="javascript:forwardToLinks()"/>
    <c:if test="${!empty builder}">
      <c:set var="webserviceLink" value="javascript:;"/>
    </c:if>
    <td>
      <a href="${webserviceLink}" title="Results from template queries can be embedded in other web pages">< embed results /></a>
    </td>
    <td>
      <a href="${webserviceLink}" title="Get a URL to run this template from the command line or a script">web service URL</a>
    </td>
    <td>
      <c:choose>
        <c:when test="${empty builder}">
          <a href="javascript:codeGenTemplate('perl');">Perl</a>
          <span>|</span>
          <a href="javascript:codeGenTemplate('java');">Java</a>
          <a href="/${WEB_PROPERTIES['webapp.path']}/api.do" target="_blank"><span>[help]</span></a>
        </c:when>
        <c:otherwise>
          <a href="javascript:;">Perl</a>
          <span>|</span>
          <a href="javascript:;">Java</a>
          <a href="/${WEB_PROPERTIES['webapp.path']}/api.do" target="_blank"><span>[help]</span></a>
        </c:otherwise>
      </c:choose>
    </td>
    <td>
      <c:choose>
        <c:when test="${empty builder}">
          <a href="javascript:exportTemplate()" title="Export this template as XML">export XML</a>
        </c:when>
        <c:otherwise>
          <a href="javascript:;" title=">Export this template as XML">export XML</a>
        </c:otherwise>
      </c:choose>
    </td>
  </tr>
</table>
</div>
</im:boxarea>
</div>
</c:when>
<c:otherwise>
<div class="bigmessage">
 <html:link action="/templates">Find template queries</html:link>
</div>
</c:otherwise>
</c:choose>

<!-- /template.jsp -->
