<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@taglib prefix="t" tagdir="/WEB-INF/tags" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<t:frame title="psjava - Java Algorithm Library for Problem Solving" description="psjava is an open source library which contains implementations of algorithms and data structures">

<p>psjava - Java Algorithm Library for Problem Solving</p>
<p>psjava is a collection of implementations of algorithms and data structures.</p>
<p>psjava is designed to provide flexibility and customizability. For example, you can choose heap implementation for Dijkstra's Algorithm. And also you can run it with a graph which has any weight number system, like 32bit integer or even BigInteger. See the detail in <a href="algo/Dijkstra_Algorithm">Dijkstra Algorithm Example</a></p>

<span id="title-download" class="anchor"></span>
<h3>Download</h3>

<p>Download <a href="http://search.maven.org/remotecontent?filepath=org/psjava/psjava/${psjavaVersion}/psjava-${psjavaVersion}.jar">jar file</a> or use maven. psjava requires Java 1.6 (or above)</p>
<pre><code>&lt;dependency&gt;
    &lt;groupId&gt;org.psjava&lt;/groupId&gt;
    &lt;artifactId&gt;psjava&lt;/artifactId&gt;
    &lt;version&gt;${psjavaVersion}&lt;/version&gt;
&lt;/dependency&gt;</code></pre>

<p>Previous releases can be downloaded from <a href="http://search.maven.org/#search%7Cgav%7C1%7Cg%3A%22org.psjava%22%20AND%20a%3A%22psjava%22">here</a>.</p>

<span id="title-getting-started" class="anchor"></span>
<h3>Getting Started</h3>

<p>See some simple example : <a href="algo/Dijkstra_Algorithm">Dijkstra Algorithm</a>, <a href="/algo/Memoization">Memoization</a></p>

<span id="title-algorithms" class="anchor"></span>
<h3>Algorithms</h3>

<table class="table table-condensed table-hover">
    <tr>
        <th>Keyword</th>
    </tr>
    <c:forEach var="item" items="${algoItemList}">
        <tr>
            <td><a href="/algo/${item.id}">${item.name}</a></td>
        </tr>
    </c:forEach>
</table>

<span id="title-data-structures" class="anchor"></span>
<h3>Data Structures</h3>

<table class="table table-condensed table-hover">
    <tr>
        <th>Keyword</th>
    </tr>
    <c:forEach var="item" items="${dsItemList}">
        <tr>
            <td><a href="/ds/${item.id}">${item.name}</a></td>
        </tr>
    </c:forEach>
</table>

<h3>License</h3>
<p>The MIT License (MIT). <a href="/license">See full version</a></p>

<h3>Contact</h3>
<p>Email : <a href="mailto:psjava.leader@gmail.com" target="_top">psjava.leader@gmail.com</a></p>

</t:frame>