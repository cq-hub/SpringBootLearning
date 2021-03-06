package com.thinkingcao.encrypt.Inter;

import com.alibaba.fastjson.JSON;
import com.thinkingcao.encrypt.constant.Constants;
import com.thinkingcao.encrypt.result.ApiResult;
import com.thinkingcao.encrypt.utils.ServletUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * @desc: API请求Token鉴权验证
 * @author: cao_wencao
 * @date: 2020-05-15 14:03
 */
@Slf4j
@Component
public class PermissionInterceptor implements HandlerInterceptor {
    @Value("${project.apifilter.excludes}")
    private String excludesUrl;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        //如果不是映射到方法直接放行通过
        if (!(handler instanceof HandlerMethod)){
            return true;
        }
        response.setCharacterEncoding("UTF-8");
        response.setContentType("text/html;charset=UTF-8");
        //请求URL
        String url = request.getServletPath().toString();
        log.debug("请求的URL: {} ", url);
        boolean flag = excludes(url,excludesUrl);
        //如果包括排除的URL，则直接放行
        if (flag){
            return true;
        }
        //获取token
        final String token = request.getHeader(Constants.USER_TOKEN);

        //1. 携带的token是否为空
        if (StringUtils.isBlank(token)){
            log.debug("user-token不能为空");
            ServletUtils.renderString(response, JSON.toJSONString(ApiResult.error("")));
            return false;
        }

        return false;
    }

    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {

    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {

    }


    /**
     *  验证排除
     * @param url
     * @param excludes
     * @return
     */
    public boolean excludes(String url,String excludes) {
        boolean filter = false;
        if (StringUtils.isEmpty(excludes)) {
            return true;
        }
        String[] excludeArray = excludes.split(",");
        for (String e : excludeArray) {
            // /**不需要过滤
            if(e.equals("/**")){
                filter = true;
                break;
            }
        }
        if(!filter){
            for (String e : excludeArray) {
                //将E开头的全部过滤
                if(url.startsWith(e)){
                    filter = true;
                    break;
                }
            }
        }
        return filter;
    }


}
