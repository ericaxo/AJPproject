package com.tabonfurniture.config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Component;
import org.springframework.ui.Model;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new SessionInterceptor());
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // Serve uploaded files from /uploads/ URL
        registry.addResourceHandler("/uploads/**")
                .addResourceLocations("classpath:/static/uploads/");
    }

    @Component
    public static class SessionInterceptor implements HandlerInterceptor {
        
        @Override
        public boolean preHandle(@NonNull HttpServletRequest request, 
                               @NonNull HttpServletResponse response, 
                               @NonNull Object handler) throws Exception {
            
            HttpSession session = request.getSession();
            Object loggedInUser = session.getAttribute("loggedInUser");
            
            if (loggedInUser != null) {
                request.setAttribute("user", loggedInUser);
            }
            
            return true;
        }
        
        @Override
        public void postHandle(@NonNull HttpServletRequest request, 
                             @NonNull HttpServletResponse response, 
                             @NonNull Object handler, 
                             @Nullable org.springframework.web.servlet.ModelAndView modelAndView) throws Exception {
            
            if (modelAndView != null) {
                HttpSession session = request.getSession();
                Object loggedInUser = session.getAttribute("loggedInUser");
                
                if (loggedInUser != null) {
                    modelAndView.addObject("user", loggedInUser);
                }
            }
        }
    }
} 