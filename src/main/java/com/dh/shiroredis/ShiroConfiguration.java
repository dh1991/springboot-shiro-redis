package com.dh.shiroredis;

import com.dh.shiroredis.redis.ShiroRedisCacheManager;
import com.dh.shiroredis.redis.ShiroRedisSessionDAO;
import org.apache.shiro.mgt.SecurityManager;
import org.apache.shiro.spring.security.interceptor.AuthorizationAttributeSourceAdvisor;
import org.apache.shiro.spring.web.ShiroFilterFactoryBean;
import org.apache.shiro.web.mgt.DefaultWebSecurityManager;
import org.apache.shiro.web.session.mgt.DefaultWebSessionManager;
import org.springframework.aop.framework.autoproxy.DefaultAdvisorAutoProxyCreator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;

import java.util.LinkedHashMap;

@Configuration
public class ShiroConfiguration {

    @Autowired
    JedisConnectionFactory redisConnectionFactory;

    @Bean
    public ShiroFilterFactoryBean shiroFilter(SecurityManager manager) {
        ShiroFilterFactoryBean bean = new ShiroFilterFactoryBean();
        bean.setSecurityManager(manager);
        bean.setLoginUrl("/login");
        bean.setSuccessUrl("/index");
        bean.setUnauthorizedUrl("/unauthorized");
        LinkedHashMap<String, String> filterChainDefinitionMap = new LinkedHashMap<>();
        filterChainDefinitionMap.put("/index", "authc");
        filterChainDefinitionMap.put("/login", "anon");
        filterChainDefinitionMap.put("/loginUser", "anon");
        filterChainDefinitionMap.put("/admin", "roles[admin]");
        filterChainDefinitionMap.put("/**", "user");
        bean.setFilterChainDefinitionMap(filterChainDefinitionMap);
        return bean;
    }
    @Bean
    public ShiroRedisSessionDAO redisSessionDAO(JedisConnectionFactory redisConnectionFactory) {
        ShiroRedisSessionDAO redisSessionDAO = new ShiroRedisSessionDAO();
        redisSessionDAO.setJedisConnectionFactory(redisConnectionFactory);
        redisSessionDAO.setExpire(300000L);
        return redisSessionDAO;
    }

    @Bean
    public DefaultWebSessionManager sessionManager(ShiroRedisSessionDAO redisSessionDAO) {
        DefaultWebSessionManager sessionManager = new DefaultWebSessionManager();
        sessionManager.setSessionDAO(redisSessionDAO);
        return sessionManager;
    }

    @Bean
    public ShiroRedisCacheManager cacheManager(JedisConnectionFactory redisConnectionFactory) {
        return new ShiroRedisCacheManager(redisConnectionFactory);
    }
    @Bean
    public SecurityManager securityManager(AuthRealm authRealm,DefaultWebSessionManager sessionManager,
                                           ShiroRedisCacheManager cacheManager) {
        DefaultWebSecurityManager manager = new DefaultWebSecurityManager(authRealm);
        manager.setCacheManager(cacheManager);
        manager.setSessionManager(sessionManager);
        return manager;
    }

    @Bean
    public AuthRealm authRealm(CredentialMatcher matcher) {
        AuthRealm authRealm = new AuthRealm();
        authRealm.setCredentialsMatcher(matcher);
        return authRealm;
    }
    @Bean
    public CredentialMatcher credentialMatcher(){
        return new CredentialMatcher();
    }

    @Bean
    public AuthorizationAttributeSourceAdvisor authorizationAttributeSourceAdvisor(SecurityManager manager) {
        AuthorizationAttributeSourceAdvisor advisor = new AuthorizationAttributeSourceAdvisor();
        advisor.setSecurityManager(manager);
        return advisor;
    }

    @Bean
    public DefaultAdvisorAutoProxyCreator defaultAdvisorAutoProxyCreator(){
        DefaultAdvisorAutoProxyCreator creator = new DefaultAdvisorAutoProxyCreator();
        creator.setProxyTargetClass(true);
        return creator;
    }
}
