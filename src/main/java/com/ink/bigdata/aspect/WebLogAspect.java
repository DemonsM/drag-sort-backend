package com.ink.bigdata.aspect;


import com.google.gson.Gson;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.After;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.util.ContentCachingRequestWrapper;
import org.springframework.web.util.WebUtils;

import javax.servlet.http.HttpServletRequest;
import java.lang.reflect.Method;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

@Slf4j
@Aspect
@Component
@Order(1)
@ConditionalOnProperty(value = "spring.weblog", havingValue = "true")
public class WebLogAspect {
    private static final String LINE_SEPARATOR = System.lineSeparator();

    private static String getBody(HttpServletRequest request) {
        ContentCachingRequestWrapper nativeRequest = WebUtils.getNativeRequest(request, ContentCachingRequestWrapper.class);
        if (!Objects.isNull(nativeRequest) && nativeRequest.getContentAsByteArray().length > 0) {
            try {
                return new Gson().fromJson(new String(nativeRequest.getContentAsByteArray()), Object.class).toString();
            } catch (Exception e) {
                return new String(nativeRequest.getContentAsByteArray());
            }
        } else {
            return null;
        }
    }

    private static String getParams(HttpServletRequest request) {
        Map<String, Object> map = new HashMap<>();
        Enumeration<?> paramNames = request.getParameterNames();
        while (paramNames.hasMoreElements()) {
            String paramName = (String) paramNames.nextElement();

            String[] paramValues = request.getParameterValues(paramName);
            if (paramValues.length > 0) {
                String paramValue = paramValues[0];
                if (paramValue.length() != 0) {
                    map.put(paramName, paramValue);
                }
            }
        }
        return new Gson().toJson(map);
    }

    private static String getIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Real-IP");
        if (StringUtils.hasLength(ip)) {
            if (!"unknown".equalsIgnoreCase(ip)) {
                return ip;
            }
            ip = request.getHeader("X-Forwarded-For");
            if (StringUtils.hasLength(ip) && !"unknown".equalsIgnoreCase(ip)) {
                // 多次反向代理后会有多个IP值，第一个为真实IP。
                int index = ip.indexOf(',');
                if (index != -1) {
                    return ip.substring(0, index);
                } else {
                    return ip;
                }
            } else {
                return request.getRemoteAddr();
            }
        } else {
            return request.getRemoteAddr();
        }

    }

    @Pointcut("execution(public * com.ink.bigdata.controller.*.*(..))")
    public void webLog() {
    }

    /**
     * 在切点之前织入
     */
    @Before("webLog()")
    public void doBefore(JoinPoint joinPoint) throws Throwable {
        // 开始打印请求日志
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        assert attributes != null;
        HttpServletRequest request = attributes.getRequest();

        // 获取 @WebLog 注解的描述信息
        String methodDescription = getAspectLogDescription(joinPoint);

        // 打印请求相关参数
        log.info("========================================== Start ==========================================");
        // 打印请求 uri
        log.info("URI            : {}", request.getRequestURI());
        // 打印描述信息
        log.info("Description    : {}", methodDescription);
        // 打印 Http method
        log.info("HTTP Method    : {}", request.getMethod());
        // 打印调用 controller 的全路径以及执行方法
        log.info("Class Method   : {}.{}", joinPoint.getSignature().getDeclaringTypeName(), joinPoint.getSignature().getName());
        // 打印请求的 IP
        log.info("IP             : {}", getIp(request));
        // 打印请求入参
        log.info("Request Params : {}", getParams(request));

        log.info("Request Body : {}", getBody(request));
    }

    /**
     * 在切点之后织入
     */
    @After("webLog()")
    public void doAfter() {
    }

    /**
     * 环绕
     */
    @Around("webLog()")
    public Object doAround(ProceedingJoinPoint proceedingJoinPoint) throws Throwable {
        long startTime = System.currentTimeMillis();
        Object result = proceedingJoinPoint.proceed();
        // 打印出参
        log.info("Response Args  : {}", new Gson().toJson(result));
        // 执行耗时
        log.info("Time-Consuming : {} ms", System.currentTimeMillis() - startTime);
        // 接口结束后换行，方便分割查看
        log.info("=========================================== End ===========================================" + LINE_SEPARATOR);
        return result;
    }


    /**
     * 获取切面注解的描述
     */
    public String getAspectLogDescription(JoinPoint joinPoint) throws Exception {
        String targetName = joinPoint.getTarget().getClass().getName();
        String methodName = joinPoint.getSignature().getName();
        Object[] arguments = joinPoint.getArgs();
        Class<?> targetClass = Class.forName(targetName);
        Method[] methods = targetClass.getMethods();
        StringBuilder description = new StringBuilder();
        for (Method method : methods) {
            if (method.getName().equals(methodName)) {
                Class<?>[] clazzs = method.getParameterTypes();
                if (clazzs.length == arguments.length) {
                    //description.append(method.getAnnotation(WebLog.class).description());
                    description.append(method.getName());
                    break;
                }
            }
        }
        return description.toString();
    }
}
