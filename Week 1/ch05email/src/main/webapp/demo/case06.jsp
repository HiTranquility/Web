<%--
================================================================================
  case06.jsp — phần VIEW của case này
================================================================================

  Ba loại control của slide 17 nằm trên cùng một form:
    - <input type="text">                 -> getParameter
    - <input type="checkbox">             -> getParameter, null nếu không tick
    - <select multiple>                   -> getParameterValues

  ${countries} in ra một List. EL tự gọi toString() nên ra dạng [a, b]. Muốn
  hiển thị đẹp từng dòng thì cần <c:forEach> của JSTL — chương 9.
================================================================================
--%>
<% request.setAttribute("caseNumber", "06");
   request.setAttribute("caseTitle", "getParameter and getParameterValues");
   request.setAttribute("caseSlides", "slides 16-17"); %>
<%@ include file="/WEB-INF/jspf/header.jspf" %>

<div class="problem">
    <strong>The problem</strong>
    A text box sends one value. A check box sends a value only when it is checked,
    and <em>nothing at all</em> when it isn't. A multi-select list box sends many
    values under one name. One method can't cover all three.
</div>

<h3>The code from slide 17</h3>
<pre>// a text box
String firstName = <span class="hl">request.getParameter("firstName")</span>;

// a check box: the value if checked, null otherwise
String rockCheckBox = <span class="hl">request.getParameter("rock")</span>;
if (rockCheckBox != null)
{
    // rock music was checked
}

// a list box: every selected item
String[] selectedCountries = <span class="hl">request.getParameterValues("country")</span>;
for (String country : selectedCountries)
{
    // code that processes each country
}</pre>

<div class="result">
    <h3>Run it</h3>
    <p>Try it once with everything filled in, then again with the check box cleared
       and nothing selected in the list.</p>
    <form action="${pageContext.request.contextPath}/controls" method="post" class="stack">
        <p><label>First name:</label><input type="text" name="firstName" value="John"></p>
        <p><label>Rock music:</label><input type="checkbox" name="rock" checked> check box named <code>rock</code></p>
        <p><label style="vertical-align:top">Countries:</label>
           <select name="country" multiple size="4">
               <option value="Vietnam" selected>Vietnam</option>
               <option value="USA">USA</option>
               <option value="Japan" selected>Japan</option>
               <option value="France">France</option>
           </select>
           <span style="color:#5b6b7c;font-size:.9em">ctrl+click for several</span></p>
        <input type="submit" value="Submit">
    </form>

    <h3>What the servlet received</h3>
    <table>
        <tr><th>getParameter("firstName")</th>
            <td><code>${empty firstName ? "null" : firstName}</code></td></tr>
        <tr><th>getParameter("rock")</th>
            <td><code>${empty rockCheckBox ? "null" : rockCheckBox}</code>
                &mdash; ${rockChecked ? "checked" : "not checked, so the browser sent no parameter at all"}</td></tr>
        <tr><th>getParameterValues("country")</th>
            <td><code>${countriesWasNull ? "null" : countries}</code>
                </td></tr>
    </table>
</div>

<div class="warn">
    <strong>The bug this prevents.</strong> Slide 17 writes
    <code>for (String country : selectedCountries)</code> with no null check. If the
    user selects nothing, <code>getParameterValues</code> returns <b>null</b>, not an
    empty array, and that loop throws a <code>NullPointerException</code>. Same for the
    check box: an unchecked box is not "off", it is absent. Always test for null first
    &mdash; which is what <code>FormControlsServlet</code> does before handing the list
    to this page.
</div>

<%@ include file="/WEB-INF/jspf/footer.jspf" %>
