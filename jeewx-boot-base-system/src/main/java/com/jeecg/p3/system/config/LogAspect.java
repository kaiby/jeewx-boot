package com.jeecg.p3.system.config;

import java.util.Enumeration;

import javax.servlet.http.HttpServletRequest;

import com.jeecg.p3.system.util.JsonUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;


/**
 * AOP统一处理Web请求日志
 *
 * @author kaibyx
 * @date 2018年10月23日 下午4:14:52
 */
@Component
@Aspect
public class LogAspect {
    private static Logger logger = LogManager.getLogger(LogAspect.class);
    private ThreadLocal<Long> startTime = new ThreadLocal<>();

    /**
     * 声明一个 Pointcut ，用来指明要在哪些方法切入。<br/>
     * 我们的 rest 接口都有
     * org.springframework.web.bind.annotation.RequestMapping注解，<br/>
     * 所以我们这里就用了
     * "@annotation(org.springframework.web.bind.annotation.RequestMapping)"
     * 表达式来指明
     */
    @Pointcut("@annotation(org.springframework.web.bind.annotation.RequestMapping) || @annotation(org.springframework.web.bind.annotation.GetMapping) || @annotation(org.springframework.web.bind.annotation.PostMapping)")
    public void webLog() {
    }

    @Before("webLog()")
    public void before(JoinPoint joinPoint) {
        startTime.set(System.currentTimeMillis());
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        HttpServletRequest httpServletRequest = attributes.getRequest();
        String sessionId = httpServletRequest.getSession().getId();
        String url = httpServletRequest.getRequestURL().toString();
        String method = httpServletRequest.getMethod();
        // String queryString = httpServletRequest.getQueryString();

        StringBuffer sf = new StringBuffer();
        sf.append(System.getProperty("line.separator"));
        sf.append("SESSIONID: ").append(sessionId).append(System.getProperty("line.separator"));
        sf.append("URL: ").append(url).append(System.getProperty("line.separator"));
        sf.append("HTTP_METHOD: ").append(method).append(System.getProperty("line.separator"));
        sf.append("BODY PARAMS: {").append(System.getProperty("line.separator"));

        Enumeration<String> enu = httpServletRequest.getParameterNames();
        while (enu.hasMoreElements()) {
            String paraName = enu.nextElement();
            sf.append("    ").append(paraName).append(": ").append(httpServletRequest.getParameter(paraName))
                    .append(System.getProperty("line.separator"));
        }
        sf.append("}").append(System.getProperty("line.separator"));
        sf.append("HEADER PARAMS: {").append(System.getProperty("line.separator"));
        Enumeration<String> headerEnu = httpServletRequest.getHeaderNames();
        while (headerEnu.hasMoreElements()) {
            String paraName = headerEnu.nextElement();
            sf.append("    ").append(paraName).append(": ").append(httpServletRequest.getHeader(paraName))
                    .append(System.getProperty("line.separator"));
        }
        sf.append("}");
        logger.info("REQUEST INTERCEPTOR--->> {}", sf.toString());
    }

    @AfterReturning(returning = "response", pointcut = "webLog()")
    public void doAfterReturning(Object response) throws Throwable {
        StringBuffer sf = new StringBuffer();
        sf.append(System.getProperty("line.separator"));
        if (response instanceof ResponseEntity) {
            HttpHeaders header = ((ResponseEntity) response).getHeaders();
            // Object body = ((ResponseEntity) response).getBody();
            // MediaType mediaType = header.getContentType();
            sf.append(JsonUtils.toJson(header));
        } else {
            sf.append(JsonUtils.toJson(response));
        }
        sf.append(System.getProperty("line.separator"));
        sf.append("SPEND TIME: ");
        sf.append(System.currentTimeMillis() - startTime.get());
        logger.info("RESPONSE INTERCEPTOR--->> {}", sf.toString());
    }

}
