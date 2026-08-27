<%--
================================================================================
  case08.jsp — phần VIEW của case này
================================================================================

  CÁCH IN RA CHỮ ${...} MÀ KHÔNG BỊ EL NUỐT MẤT
  Trang này cần hiển thị chính cú pháp EL cho bạn đọc. Viết thẳng ${user.email}
  thì EL sẽ THỰC THI nó và in ra giá trị, không phải cú pháp.

  Mẹo: ${'$'}{user.email}
       EL tính ${'$'} ra một dấu $, phần {user.email} còn lại là chữ thường.
  Cách khác: dùng &#36; (mã HTML của dấu $), như trang index.jsp đang dùng.
================================================================================
--%>
<% request.setAttribute("caseNumber", "08");
   request.setAttribute("caseTitle", "Request attributes");
   request.setAttribute("caseSlides", "slides 21-22"); %>
<%@ include file="/WEB-INF/jspf/header.jspf" %>

<div class="problem">
    <strong>The problem</strong>
    The servlet has built a <code>User</code> object. The JSP has to display it &mdash;
    but a JSP shouldn't contain Java. Attributes are the hand-off: the servlet puts the
    object <em>in the request</em>, the JSP reads it back with EL.
</div>

<h3>The code from slide 22</h3>
<pre>// set an attribute
User user = new User(firstName, lastName, email);
<span class="hl">request.setAttribute("user", user);</span>

// get it back - getAttribute returns Object, so it needs a cast
User user = <span class="hl">(User) request.getAttribute("user");</span>

// a primitive type gets boxed on the way in and cast on the way out
int id = 1;
request.setAttribute("id", new Integer(id));
int id = <span class="hl">(Integer)</span> request.getAttribute("id");</pre>

<div class="result">
    <h3>What this page received</h3>
    <table>
        <tr><th>${'$'}{user.email}</th><td><code>${user.email}</code></td></tr>
        <tr><th>${'$'}{user.firstName}</th><td><code>${user.firstName}</code></td></tr>
        <tr><th>${'$'}{user.lastName}</th><td><code>${user.lastName}</code></td></tr>
        <tr><th>the cast returned the same object</th>
            <td><span class="${castWorked ? 'yes' : 'no'}">${castWorked ? "yes" : "no"}</span></td></tr>
        <tr><th>${'$'}{id} after boxing/unboxing</th><td><code>${idBack}</code></td></tr>
        <tr><th>an attribute never set</th>
            <td><span class="${missingIsNull ? 'yes' : 'no'}">${missingIsNull ? "null, as documented" : "not null?!"}</span></td></tr>
    </table>
</div>

<div class="note">
    <strong>"Attributes reset between requests" (slide 21) &mdash; proof.</strong>
    At the top of <code>doGet</code>, before setting anything, this servlet checks
    whether <code>"user"</code> is still there from your last visit. On this request it
    was
    <span class="${leftoverWasNull ? 'yes' : 'no'}">${leftoverWasNull ? "null" : "still present"}</span>.
    <a class="btn ghost" href="${pageContext.request.contextPath}/attributes">Reload and check again</a>
    &mdash; it stays null however many times you refresh, because each refresh is a new
    request object. To keep something across requests you need the session, which is
    chapter 7.
</div>

<div class="warn">
    <strong>EL reads properties, not fields.</strong> <code>${'$'}{user.email}</code> does
    not look for a field called <code>email</code> &mdash; it calls
    <code>getEmail()</code>. That is why the model class has to be a JavaBean with get
    methods. Delete <code>getEmail()</code> from <code>User</code> and this page
    silently prints nothing.
</div>

<%@ include file="/WEB-INF/jspf/footer.jspf" %>
