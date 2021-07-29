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
package com.jeequan.jeepay.core.model.params.ysf;

import lombok.Data;

/*
 * 云闪付 通用配置信息
 *
 * @author pangxiaoyu
 * @site https://www.jeequan.com
 * @date 2021/6/8 18:02
 */
@Data
public class YsfpayConfig {


    /** 网关地址 */
    public static String PROD_SERVER_URL = "https://partner.95516.com";
    public static String SANDBOX_SERVER_URL = "http://ysf.bcbip.cn:10240";

}
