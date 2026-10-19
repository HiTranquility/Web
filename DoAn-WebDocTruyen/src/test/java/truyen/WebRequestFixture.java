package truyen;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.Proxy;
import java.util.HashMap;
import java.util.Map;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

/** Request/response cục bộ, không đọc cấu hình và không kết nối dịch vụ thật. */
final class WebRequestFixture {
    final Map<String, String> params = new HashMap<>();
    final Map<String, String> headers = new HashMap<>();
    final Map<String, Object> attributes = new HashMap<>();
    final Map<String, String> responseHeaders = new HashMap<>();
    String method = "GET";
    String path = "/auth";
    String context = "/app";
    String query;
    int status = 200;
    String redirect;
    String forward;
    final StringWriter output = new StringWriter();
    Session session;

    static final class Session {
        final Map<String, Object> values = new HashMap<>();
        boolean invalidated;
        final HttpSession proxy = mock(HttpSession.class, (name, args) -> {
            if ("invalidate".equals(name)) { invalidated = true; values.clear(); }
            if (invalidated && !"invalidate".equals(name)) throw new IllegalStateException("Expired");
            if ("getAttribute".equals(name)) return values.get(args[0]);
            if ("setAttribute".equals(name)) values.put((String) args[0], args[1]);
            if ("removeAttribute".equals(name)) values.remove(args[0]);
            return null;
        });
    }

    private RequestDispatcher dispatcher() {
        return mock(RequestDispatcher.class, (name, args) -> null);
    }

    final ServletContext servletContext = mock(ServletContext.class, (name, args) -> {
        if ("getContextPath".equals(name)) return context;
        if ("getRequestDispatcher".equals(name)) { forward = (String) args[0]; return dispatcher(); }
        return null;
    });
    final ServletConfig config = mock(ServletConfig.class, (name, args) -> {
        if ("getServletContext".equals(name)) return servletContext;
        if ("getServletName".equals(name)) return "audit";
        return null;
    });
    final HttpServletRequest request = mock(HttpServletRequest.class, (name, args) -> {
        switch (name) {
            case "getMethod": return method;
            case "getServletPath": return path;
            case "getRequestURI": return context + path;
            case "getPathInfo": return path.startsWith("/api") ? path.substring(4) : null;
            case "getContextPath": return context;
            case "getQueryString": return query;
            case "getProtocol": return "HTTP/1.1";
            case "getRemoteAddr": return "127.0.0.1";
            case "getCharacterEncoding": return "UTF-8";
            case "getServletContext": return servletContext;
            case "getParameter": return params.get(args[0]);
            case "getHeader": return headers.get(args[0]);
            case "getAttribute": return attributes.get(args[0]);
            case "setAttribute": attributes.put((String) args[0], args[1]); return null;
            case "getRequestDispatcher": forward = (String) args[0]; return dispatcher();
            case "getSession":
                if (session != null && session.invalidated) session = null;
                if (session == null && (args == null || args.length == 0 || Boolean.TRUE.equals(args[0]))) {
                    session = new Session();
                }
                return session == null ? null : session.proxy;
            default: return null;
        }
    });
    final HttpServletResponse response = mock(HttpServletResponse.class, (name, args) -> {
        if ("getWriter".equals(name)) return new PrintWriter(output);
        if ("sendRedirect".equals(name)) { redirect = (String) args[0]; status = 302; }
        if ("sendError".equals(name) || "setStatus".equals(name)) status = (Integer) args[0];
        if ("setHeader".equals(name)) responseHeaders.put((String) args[0], (String) args[1]);
        return null;
    });

    interface Invocation { Object call(String name, Object[] args) throws Throwable; }

    static <T> T mock(Class<T> type, Invocation call) {
        return type.cast(Proxy.newProxyInstance(type.getClassLoader(), new Class<?>[] { type }, (p, m, a) -> {
            Object value = call.call(m.getName(), a);
            if (value != null || !m.getReturnType().isPrimitive()) return value;
            if (m.getReturnType() == boolean.class) return false;
            if (m.getReturnType() == long.class) return 0L;
            if (m.getReturnType() == int.class) return 0;
            return null;
        }));
    }
}
