<%--
================================================================================
  case15.jsp — phần VIEW của case này
================================================================================

  Bấm F5 từng cái sẽ KHÔNG BAO GIỜ thấy lỗi — một trình duyệt là một
  thread, mà tranh chấp cần ít nhất hai. Phải bấm nút stress test.

  ${lostUpdates gt 0 ? ... : ...} — trong EL viết "gt" thay cho ">" vì dấu >
  sẽ bị hiểu là đóng thẻ HTML. Tương tự: lt (<), ge (>=), le (<=), ne (!=).
================================================================================
--%>
<% request.setAttribute("caseNumber", "15");
   request.setAttribute("caseTitle", "Instance variables aren't thread-safe");
   request.setAttribute("caseSlides", "slides 48-50"); %>
<%@ include file="/WEB-INF/jspf/header.jspf" %>

<div class="problem">
    <strong>The problem</strong>
    Tomcat creates one servlet instance and runs every request through it on a
    different thread. So an instance variable is shared by all of them &mdash; and
    <code>globalCount++</code> is not one operation, it is read, add, write. Two
    threads can read the same value and both write back the same result. One update
    vanishes.
</div>

<h3>The code from slides 48-49</h3>
<pre>public class EmailListServlet extends HttpServlet {

    // declare an instance variable for the page
    private int globalCount; <span class="hl">// not thread-safe</span>

    @Override
    public void init() throws ServletException
    {
        globalCount = 0;
    }

    @Override
    protected void doPost(...)
    {
        globalCount++;   <span class="hl">// this is not thread-safe</span>
    }
}</pre>

<div class="result">
    <h3>Run it &mdash; the counters right now</h3>
    <table>
        <tr><th>globalCount (plain <code>int</code>)</th><td><code>${globalCount}</code></td></tr>
        <tr><th>safeCount (<code>AtomicInteger</code>)</th><td><code>${safeCount}</code></td></tr>
    </table>
    <p>Clicking reload one at a time will <em>never</em> show the bug &mdash; one browser
       is one thread, and the race needs two. So force it:</p>
    <p>
        <a class="btn" href="${pageContext.request.contextPath}/counter?action=stress">
            run ${threads} threads &times; ${increments} increments</a>
        <a class="btn ghost" href="${pageContext.request.contextPath}/counter">just reload (+1)</a>
        <a class="btn ghost" href="${pageContext.request.contextPath}/counter?action=reset">reset both</a>
    </p>

    <table>
        <tr><th>expected total</th><td><code>${empty expected ? "- run the stress test" : expected}</code></td></tr>
        <tr><th>globalCount ended at</th>
            <td><code>${globalCount}</code>
                <span class="${lostUpdates gt 0 ? 'no' : ''}">${empty lostUpdates ? "" : (lostUpdates gt 0 ? " - short by " : " - no loss this time, try again ")}${lostUpdates gt 0 ? lostUpdates : ""}</span></td></tr>
        <tr><th>safeCount ended at</th>
            <td><code>${safeCount}</code> <span class="yes">${empty expected ? "" : "exact, every time"}</span></td></tr>
    </table>
</div>

<div class="warn">
    <strong>Those missing increments are the bug.</strong> Nothing threw an exception,
    nothing appeared in the log, and the number is simply wrong. That is what makes
    this class of bug so nasty &mdash; it does not fail, it just quietly lies. Now
    imagine <code>globalCount</code> was an order total.
</div>

<div class="note">
    <strong>So where does state go?</strong>
    Per-request data &rarr; a local variable inside <code>doGet</code>/<code>doPost</code>,
    which each thread gets its own copy of.
    Per-user data &rarr; the session (chapter 7).
    Application-wide data &rarr; the <code>ServletContext</code>, or an
    <code>AtomicInteger</code> / <code>synchronized</code> block if it really must be a
    field. A <code>final</code> field that is never mutated after
    <code>init()</code> &mdash; a config value, a data-access object &mdash; is fine.
    It is the <em>mutation</em> that is unsafe, not the field.
</div>

<%@ include file="/WEB-INF/jspf/footer.jspf" %>
