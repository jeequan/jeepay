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
package com.jeequan.jeepay.core.utils;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

/**
 * @description: Spring 框架下, 获取Beans静态函数方法。
 * @Author terrfly
 * @Date 2019/12/31 13:57
 */
@Component
public class SpringBeansUtil implements ApplicationContextAware {

    private static ApplicationContext applicationContext = null;

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        if(SpringBeansUtil.applicationContext == null){
            SpringBeansUtil.applicationContext  = applicationContext;
        }
    }

    /** 获取applicationContext */
    public static ApplicationContext getApplicationContext() {
        return applicationContext;
    }

     /** 通过name获取 Bean. */
    public static Object getBean(String name){

        if(!getApplicationContext().containsBean(name)){
            return null;
        }

        return getApplicationContext().getBean(name);

    }

     /** 通过class获取Bean. */
    public static <T> T getBean(Class<T> clazz){
        try {
            return getApplicationContext().getBean(clazz);
        } catch (BeansException e) {
            return null;
        }
    }

     /** 通过name,以及Clazz返回指定的Bean */
    public static <T> T getBean(String name, Class<T> clazz){
        if(!getApplicationContext().containsBean(name)){
            return null;
        }
        return getApplicationContext().getBean(name, clazz);
    }

}
