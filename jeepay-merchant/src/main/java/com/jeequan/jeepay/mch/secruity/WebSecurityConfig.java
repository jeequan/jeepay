/*
 * Copyright (c) 2021-2031, 河北计全科技有限公司 (https://www.jeequan.com & jeequan@126.com).
 * <p>
 * Licensed under the GNU LESSER GENERAL PUBLIC LICENSE 3.0;
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.gnu.org/licenses/lgpl.html
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.jeequan.jeepay.mch.secruity;

import com.jeequan.jeepay.mch.config.SystemYmlConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.annotation.web.configurers.HeadersConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

import static org.springframework.security.config.Customizer.withDefaults;

/*
 * Spring Security 配置项
 *
 * @author terrfly
 * @site https://www.jeequan.com
 * @date 2021/6/8 17:11
 */
@Configuration
@EnableWebSecurity
public class WebSecurityConfig{

    @Autowired private UserDetailsService userDetailsService;
    @Autowired private JeeAuthenticationEntryPoint unauthorizedHandler;
    @Autowired private SystemYmlConfig systemYmlConfig;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // 前后端分离架构不需要csrf保护
                .csrf(AbstractHttpConfigurer::disable)
                .cors(withDefaults())
                .addFilter(corsFilter())
                .headers(httpSecurityHeadersConfigurer -> httpSecurityHeadersConfigurer.cacheControl(HeadersConfigurer.CacheControlConfig::disable))
                // 基于token，所以不需要session
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                // 认证失败处理方式
                .exceptionHandling(exceptions -> exceptions.authenticationEntryPoint(unauthorizedHandler))
                .authenticationProvider(authenticationProvider())
                // 添加JWT filter
                .addFilterBefore(new JeeAuthenticationTokenFilter(), UsernamePasswordAuthenticationFilter.class)
                .authorizeHttpRequests((auth) -> {
                    auth.anyRequest().authenticated();
                });

        // 构建过滤链并返回
        return http.build();
    }

    @Bean
    public WebSecurityCustomizer ignoringCustomizer() {
        return (web) -> web.ignoring()
                .requestMatchers(HttpMethod.GET,
                        "/",
                        "/*.html",
                        "/favicon.ico",
                        "/*/*.html",
                        "/*/*.css",
                        "/*/*.js",
                        "/*/*.png",
                        "/*/*.jpg",
                        "/*/*.jpeg",
                        "/*/*.svg",
                        "/*/*.ico",
                        "/*/*.webp",
                        "/*.txt",
                        "/*/*.xls",
                        "/*/*.mp4"   //支持mp4格式的文件匿名访问
                )
                .requestMatchers(
                        "/api/anon/**", //匿名访问接口
                        "/webjars/**","/v3/api-docs/**", "/doc.html", "/knife4j/**", "/swagger-ui/**", "/swagger-resources/**" // swagger相关
                );
    }

    @Bean
    public UserDetailsService userDetailsService() {
        return username -> userDetailsService.loadUserByUsername(username);
    }

    /**
     * 使用BCrypt强哈希函数 实现PasswordEncoder
     * **/
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * 调用loadUserByUsername获得UserDetail信息，在AbstractUserDetailsAuthenticationProvider里执行用户状态检查
     */
    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        // DaoAuthenticationProvider 从自定义的 userDetailsService.loadUserByUsername 方法获取UserDetails
        authProvider.setUserDetailsService(userDetailsService());
        // 设置密码编辑器
        authProvider.setPasswordEncoder(passwordEncoder());
        return authProvider;
    }

    @Bean
    public AuthenticationManager authenticationManager() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        // DaoAuthenticationProvider 从自定义的 userDetailsService.loadUserByUsername 方法获取UserDetails
        authProvider.setUserDetailsService(userDetailsService());
        // 设置密码编辑器
        authProvider.setPasswordEncoder(passwordEncoder());
        return new ProviderManager(authProvider);
    }

    /** 允许跨域请求 **/
    @Bean
    public CorsFilter corsFilter() {

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        if(systemYmlConfig.getAllowCors()){
            CorsConfiguration config = new CorsConfiguration();
            config.setAllowCredentials(true);   //带上cookie信息
//          config.addAllowedOrigin(CorsConfiguration.ALL);  //允许跨域的域名， *表示允许任何域名使用
            config.addAllowedOriginPattern(CorsConfiguration.ALL);  //使用addAllowedOriginPattern 避免出现 When allowCredentials is true, allowedOrigins cannot contain the special value "*" since that cannot be set on the "Access-Control-Allow-Origin" response header. To allow credentials to a set of origins, list them explicitly or consider using "allowedOriginPatterns" instead.
            config.addAllowedHeader(CorsConfiguration.ALL);   //允许任何请求头
            config.addAllowedMethod(CorsConfiguration.ALL);   //允许任何方法（post、get等）
            source.registerCorsConfiguration("/**", config); // CORS 配置对所有接口都有效
        }
        return new CorsFilter(source);
    }

}
