package org.onap.ccsdk.features.lib.doorman.servlet;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.CharArrayWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletInputStream;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;
import org.onap.ccsdk.features.lib.doorman.MessageInterceptor;
import org.onap.ccsdk.features.lib.doorman.MessageInterceptorFactory;
import org.onap.ccsdk.features.lib.doorman.data.MessageData;

public class MessageInterceptorFilter implements Filter {

    private MessageInterceptorFactory messageInterceptorFactory;

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {}

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        RequestWrapper req = new RequestWrapper((HttpServletRequest) request);
        MessageData requestData = getMessageRequest(req);

        MessageInterceptor interceptor = messageInterceptorFactory.create();
        MessageData responseData = interceptor.processRequest(requestData);

        if (responseData == null) {
            ResponseWrapper res = new ResponseWrapper((HttpServletResponse) response);

            chain.doFilter(req, res);

            responseData = res.getMessageResponse();
            interceptor.processResponse(responseData);
        }

        setMessageResponse((HttpServletResponse) response, responseData);
    }

    @SuppressWarnings("unchecked")
    private void setMessageResponse(HttpServletResponse response, MessageData responseData) throws IOException {
        if (responseData.getParam() != null) {
            String contentType = (String) responseData.getParam().get("content_type");
            if (contentType != null) {
                response.setContentType(contentType);
            }
            Integer httpCode = (Integer) responseData.getParam().get("http_code");
            if (httpCode != null) {
                response.setStatus(httpCode);
            }
            Map<String, Object> headers = (Map<String, Object>) responseData.getParam().get("headers");
            if (headers != null) {
                for (Entry<String, Object> entry : headers.entrySet()) {
                    String name = entry.getKey();
                    Object v = entry.getValue();
                    if (v instanceof List) {
                        List<Object> ll = (List<Object>) v;
                        for (Object o : ll) {
                            response.addHeader(name, o.toString());
                        }
                    } else {
                        response.setHeader(name, v.toString());
                    }
                }
            }
        }

        if (responseData.getBody() != null) {
            response.setContentLength(responseData.getBody().length());
            response.getWriter().write(responseData.getBody());
        }
    }

    @SuppressWarnings("unchecked")
    private MessageData getMessageRequest(RequestWrapper request) {
        HashMap<String, Object> param = new HashMap<>();
        param.put("http_method", request.getMethod());
        param.put("uri", request.getPathInfo());
        param.put("param", request.getParameterMap());
        param.put("content_type", request.getContentType());

        Map<String, Object> headers = new HashMap<>();
        Enumeration<String> headerNames = request.getHeaderNames();
        while (headerNames.hasMoreElements()) {
            String name = headerNames.nextElement();
            Enumeration<String> values = request.getHeaders(name);
            List<String> valueList = new ArrayList<>();
            while (values.hasMoreElements()) {
                valueList.add(values.nextElement());
            }
            if (valueList.size() > 1) {
                headers.put(name, valueList);
            } else {
                headers.put(name, valueList.get(0));
            }
        }
        param.put("headers", headers);

        return new MessageData(param, request.getBody());
    }

    @Override
    public void destroy() {}

    private static class RequestWrapper extends HttpServletRequestWrapper {

        private final String body;

        public RequestWrapper(HttpServletRequest request) throws IOException {
            super(request);

            StringBuilder stringBuilder = new StringBuilder();
            try (InputStream inputStream = request.getInputStream()) {
                if (inputStream != null) {
                    try (BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream))) {
                        char[] charBuffer = new char[128];
                        int bytesRead = -1;
                        while ((bytesRead = bufferedReader.read(charBuffer)) > 0) {
                            stringBuilder.append(charBuffer, 0, bytesRead);
                        }
                    }
                }
            }
            body = stringBuilder.toString();
        }

        @Override
        public ServletInputStream getInputStream() throws IOException {
            final ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(body.getBytes());
            ServletInputStream servletInputStream = new ServletInputStream() {

                @Override
                public int read() throws IOException {
                    return byteArrayInputStream.read();
                }
            };
            return servletInputStream;
        }

        @Override
        public BufferedReader getReader() throws IOException {
            return new BufferedReader(new InputStreamReader(getInputStream()));
        }

        public String getBody() {
            return body;
        }
    }

    public class ResponseWrapper extends HttpServletResponseWrapper {

        private CharArrayWriter writer = new CharArrayWriter();
        private Map<String, Object> param = new HashMap<>();

        public ResponseWrapper(HttpServletResponse response) {
            super(response);
        }

        @Override
        public PrintWriter getWriter() {
            return new PrintWriter(writer);
        }

        @Override
        public void setStatus(int status) {
            param.put("http_code", status);
        }

        @SuppressWarnings("unchecked")
        @Override
        public void setHeader(String name, String value) {
            Map<String, Object> headers = (Map<String, Object>) param.get("headers");
            if (headers == null) {
                headers = new HashMap<>();
                param.put("headers", headers);
            }
            headers.put(name, value);
        }

        @SuppressWarnings("unchecked")
        @Override
        public void addHeader(String name, String value) {
            Map<String, Object> headers = (Map<String, Object>) param.get("headers");
            if (headers == null) {
                headers = new HashMap<>();
                param.put("headers", headers);
            }
            Object v = headers.get(name);
            if (v == null) {
                headers.put(name, value);
            } else if (v instanceof List) {
                ((List<Object>) v).add(value);
            } else {
                List<Object> ll = new ArrayList<>();
                ll.add(v);
                ll.add(value);
                headers.put(name, ll);
            }
        }

        public MessageData getMessageResponse() {
            return new MessageData(param, writer.toString());
        }
    }

    public void setMessageInterceptorFactory(MessageInterceptorFactory messageInterceptorFactory) {
        this.messageInterceptorFactory = messageInterceptorFactory;
    }
}
