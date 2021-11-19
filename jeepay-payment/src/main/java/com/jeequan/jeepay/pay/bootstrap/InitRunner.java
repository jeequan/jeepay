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
package com.jeequan.jeepay.pay.bootstrap;

import cn.hutool.core.date.DatePattern;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.serializer.SerializeConfig;
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.alibaba.fastjson.serializer.SimpleDateFormatSerializer;
import com.jeequan.jeepay.pay.config.SystemYmlConfig;
import com.jeequan.jeepay.service.impl.SysConfigService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.Date;

/*
 *  项目初始化操作
 *  比如初始化配置文件， 读取基础数据， 资源初始化等。 避免在Main函数中写业务代码。
 *  CommandLineRunner  / ApplicationRunner都可以达到要求， 只是调用参数有所不同。
 *
*
* @author terrfly
* @site https://www.jeequan.com
* @date 2021/6/8 17:17
*/
@Component
public class InitRunner implements CommandLineRunner {

    @Autowired private SystemYmlConfig systemYmlConfig;


    @Override
    public void run(String... args) throws Exception {

        // 配置是否使用缓存模式
        SysConfigService.IS_USE_CACHE = systemYmlConfig.getCacheConfig();

        //初始化处理fastjson格式
        SerializeConfig serializeConfig = SerializeConfig.getGlobalInstance();
        serializeConfig.put(Date.class, new SimpleDateFormatSerializer(DatePattern.NORM_DATETIME_PATTERN));

        //解决json 序列化时候的  $ref：问题
        JSON.DEFAULT_GENERATE_FEATURE |= SerializerFeature.DisableCircularReferenceDetect.getMask();

    }
}
