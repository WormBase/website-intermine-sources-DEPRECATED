<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="/WEB-INF/struts-tiles.tld" prefix="tiles" %>
<%@ taglib uri="/WEB-INF/struts-html.tld" prefix="html" %>

<!-- quickSearch.jsp -->
<c:set var="ids" value="${WEB_PROPERTIES['quickSearch.identifiers']}"/>

<script type="text/javascript">
function clearElement(e) {
   var value =document.getElementById('quickSearchInput').value;
   if(value == '${ids}') {
      e.value = "";
      jQuery('#quickSearchInput').css("color", "#000");
      jQuery('#quickSearchInput').css("fontStyle","normal");
   }
}

</script>
<form action="<c:url value="/keywordSearchResults.do" />" name="search" method="get" style="display:inline;">
<fmt:message key="header.search"/>&nbsp;<input
style="width:150px;color:#666;font-style:italic;font-size:1em" type="text" id="quickSearchInput" name="searchTerm" value="${ids}" onFocus="clearElement(this);" />
<input type="submit" id="quickSearchButton" name="searchSubmit" value="GO" />
</form>
<!-- /quickSearch.jsp -->

 <script type="text/javascript">
  // associate functions with search that redir to a keyword objects listing instead of search results
  jQuery('#quickSearchButton').click(function() {
    // if placeholder text in place, take us elsewhere
    if (jQuery("#quickSearchInput").val() == "${ids}") {
      jQuery(location).attr('href', "/${WEB_PROPERTIES['webapp.path']}/keywordSearchResults.do?searchBag=");
      return false;
    }
  });

</script>
