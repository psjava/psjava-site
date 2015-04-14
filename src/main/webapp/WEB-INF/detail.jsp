<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@taglib prefix="t" tagdir="/WEB-INF/tags" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<t:frame title="${name} in Java - psjava" description="Implementation and example of ${name} in Java">
<link rel="stylesheet" media="screen" href="/stylesheets/prettify-skin-desert-trunk.css">
<script src="/prettify-small-4-Mar-2013/google-code-prettify/prettify.js"></script>

<h2>${name}</h2>
<p><a href="http://www.google.com/search?q=${name}">What is ${name} ?</a></p>

<h4>Download</h4>

<p>Download <a href="http://search.maven.org/remotecontent?filepath=org/psjava/psjava/${psjavaVersion}/psjava-${psjavaVersion}.jar">jar file</a> or use maven. psjava requires Java 1.6 (or above)</p>
<pre><code>&lt;dependency&gt;
    &lt;groupId&gt;org.psjava&lt;/groupId&gt;
    &lt;artifactId&gt;psjava&lt;/artifactId&gt;
    &lt;version&gt;${psjavaVersion}&lt;/version&gt;
&lt;/dependency&gt;</code></pre>

<h4>Example Code</h4>
<pre class="small prettyprint lang-java small-tab">${exampleCode}</pre>


    <c:if test="${not empty seeAlsos}">
        <h4>See Also</h4>
        <ul>
            <c:forEach var="item" items="${seeAlsos}">
                <li><a href="/${item.category}/${item.id}">${item.name}</a></li>
            </c:forEach>
        </ul>
    </c:if>

<c:if test="${not empty implementations}">
    <h4>Implementation</h4>
    <ul>
        <c:forEach var="item" items="${implementations}">
            <li><a href="${item.url}">${item.simpleClassName}.java</a> in GitHub</li>
        </c:forEach>
    </ul>
</c:if>

<script>
    prettyPrint();
</script>

</t:frame>