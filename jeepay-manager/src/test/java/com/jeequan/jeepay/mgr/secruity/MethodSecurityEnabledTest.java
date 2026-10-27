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
package com.jeequan.jeepay.mgr.secruity;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * 运营端方法级安全（{@code @PreAuthorize}）启用性回归测试。
 *
 * <p>背景：运营端 {@code ctrl} 包下大量接口使用
 * {@code @PreAuthorize("hasAuthority('ENT_xxx')")} 声明权限，但 {@link WebSecurityConfig}
 * 只声明了 {@code @EnableWebSecurity}，缺少 {@code @EnableMethodSecurity}。Spring Security 6.x
 * 默认关闭方法级安全，因此这些注解被静默忽略，任何已登录的低权限运营用户都能调用其角色
 * 未授权的接口（垂直越权）。
 *
 * <p>本测试锁定两件事：
 * <ol>
 *   <li>安全配置类确实启用了方法级安全，且 {@code prePostEnabled} 为 true（{@code @PreAuthorize}
 *       依赖它）；</li>
 *   <li>受保护的 Controller 仍然声明着权限表达式，避免"启用了安全但注解被删光"的假修复。</li>
 * </ol>
 */
class MethodSecurityEnabledTest {

    @Test
    @DisplayName("WebSecurityConfig 必须启用 @EnableMethodSecurity 且 prePostEnabled = true")
    void webSecurityConfigEnablesMethodSecurity() {
        EnableMethodSecurity annotation =
                WebSecurityConfig.class.getAnnotation(EnableMethodSecurity.class);

        assertNotNull(annotation,
                "WebSecurityConfig 缺少 @EnableMethodSecurity：Spring Security 6.x 默认不启用"
                        + "方法级安全，@PreAuthorize 会被静默忽略，导致垂直越权");
        assertTrue(annotation.prePostEnabled(),
                "@EnableMethodSecurity 的 prePostEnabled 必须为 true，否则 @PreAuthorize 不生效");
    }

    @Test
    @DisplayName("运营端受保护接口仍然声明 @PreAuthorize 权限表达式")
    void protectedControllersStillDeclarePreAuthorize() throws Exception {
        assertPreAuthorizeOn("com.jeequan.jeepay.mgr.ctrl.sysuser.SysUserController", "list",
                "ENT_UR_USER_LIST");
        assertPreAuthorizeOn("com.jeequan.jeepay.mgr.ctrl.sysuser.SysLogController", "list",
                "ENT_LOG_LIST");
        assertPreAuthorizeOn("com.jeequan.jeepay.mgr.ctrl.config.SysConfigController", "update",
                "ENT_SYS_CONFIG_EDIT");
    }

    private void assertPreAuthorizeOn(String className, String methodName, String expectedAuthority)
            throws Exception {
        Class<?> controller = Class.forName(className);

        // 重载方法可能有多个，取第一个带 @PreAuthorize 的即可（这些接口都只有一个受保护重载）。
        PreAuthorize annotation = null;
        for (Method method : controller.getMethods()) {
            if (!method.getName().equals(methodName)) {
                continue;
            }
            PreAuthorize candidate = method.getAnnotation(PreAuthorize.class);
            if (candidate != null) {
                annotation = candidate;
                break;
            }
        }
        assertNotNull(annotation,
                className + "#" + methodName + " 缺少 @PreAuthorize，权限声明被删除");

        List<String> authorities = new ArrayList<>();
        for (String quoted : annotation.value().split("'")) {
            if (quoted.startsWith("ENT_")) {
                authorities.add(quoted);
            }
        }
        assertTrue(authorities.contains(expectedAuthority),
                className + "#" + methodName + " 的权限表达式应包含 " + expectedAuthority
                        + "，实际为 " + annotation.value());
    }
}
