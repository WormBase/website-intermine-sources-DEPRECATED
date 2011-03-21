<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="/WEB-INF/struts-html.tld" prefix="html" %>
<%@ taglib uri="/WEB-INF/struts-tiles.tld" prefix="tiles" %>
<%@ taglib tagdir="/WEB-INF/tags" prefix="im"%>

<html:xhtml/>

<!-- apiPerl.jsp -->
<script type="text/javascript">
<!--//<![CDATA[
jQuery(document).ready(function() {
    // jQuery("p").hide();
 });

 function showText(pid) {
   // jQuery("#" + pid).slideToggle("slow");
 }

//]]>-->
</script>

<im:boxarea titleKey="api.perl.titleKey" stylename="gradientbox" fixedWidth="90%" htmlId="apiPerl">

<div>
  <p>
    The Perl web service API makes it easy to run queries in <c:out value="${WEB_PROPERTIES['project.title']}"/> directly from Perl programs.
    You can use the Perl API to construct any query you could run from web interface and fetch the results as tab-separated values.
  </p>
</div>
<br>
<div>

<ul>
  <li>
    <div onclick="javascript:showText('prerequisite')"><h3 style="font-weight: bold;">Prerequisites</h3></div>
    <div id="prerequisite" style="padding: 5px">
      <p>
        You should install the <a href="http://search.cpan.org/~intermine/Webservice-InterMine/lib/Webservice/InterMine.pm" target="_blank">
        Perl webservice client library module </a> to get started.
        You can install it directly from
        CPAN (the comprehensive Perl archive network). This is a public
        repository of thousands of modules. Using CPAN will ensure you
        get the latest version, and that your dependencies are
        automatically managed for you. To install the InterMine Perl
        client library type the following command into a shell:
        <br><br>
            <span style="padding:"><i>&gt; sudo cpan Webservice::InterMine</i></span>
        <br><br>
            (you may be asked to configure cpan here if you have not used it
            before). More detailed instructions regarding the installation
            of Perl modules can be found <a href="http://www.intermine.org/wiki/InstallingPerlModules" target="_blank">here</a>.
      </p>
    </div>
  </li>
  <li>
    <div onclick="javascript:showText('examples')"><h3 style="font-weight: bold;">Examples of using the downloaded script</h3></div>
    <div id="examples" style="padding: 5px">

    <p>On each Template Query page and the QueryBuilder there is a link to get Perl code to run that particular
    query using the web service API. Just click the link, save the generated Perl script in a file and execute it.
    You can use the generated code as a starting point for your own programs.</p>
    <p style="padding-top:3px; padding-bottom:3px;">
    You can run the downloaded script by running the following
    command in a shell:
    <br><br>
    <span><i>&gt; perl path/to/downloaded/script.pl</i></span>
    <br><br>
    If you get an error saying "Cannot find Webservice::InterMine in
    @INC" then see 'Prerequisites' above.

    Feel free to edit the script - these are designed to be
    springboards to help you get where you want to. See the <a href="http://search.cpan.org/~intermine/Webservice-InterMine/lib/Webservice/InterMine/Cookbook.pod" target="_blank">Cookbook</a>,
     which contains a set of short tutorial 'recipes' that demonstrate particular features of the Perl API,
    for ways to edit the scripts, and even extend their
    functionality.
    </p>
    <p>
    For a good reference to writing programs in Perl, see <a href="http://www.onyxneon.com/books/modern_perl/index.html" target="_blank">here</a>.
    </p>

    </div>
  </li>
</ul>

</div>

</im:boxarea>
<!-- /apiPerl.jsp -->