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
package com.jeequan.jeepay.core.beans;

import com.alibaba.fastjson.JSONObject;
import com.jeequan.jeepay.core.constants.ApiCodeEnum;
import com.jeequan.jeepay.core.exception.BizException;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;

import jakarta.servlet.http.HttpServletRequest;
import java.util.Iterator;
import java.util.Map;
import java.util.stream.Collectors;

/*
* 基于spring的 req 工具类
*
* @author terrfly
* @site https://www.jeequan.com
* @date 2021/6/7 12:16
*/
@Slf4j
@Component
public class RequestKitBean {

    @Autowired(required = false)
    protected HttpServletRequest request;   //自动注入request

    /** reqContext对象中的key: 转换好的json对象 */
    private static final String REQ_CONTEXT_KEY_PARAMJSON = "REQ_CONTEXT_KEY_PARAMJSON";

    /** JSON 格式通过请求主体（BODY）传输  获取参数 **/
    public String getReqParamFromBody() {

        String body = "";

        if(isConvertJSON()){

            try {
                String str;
                while((str = request.getReader().readLine()) != null){
                    body += str;
                }

                return body;

            } catch (Exception e) {
                log.error("请求参数转换异常！ params=[{}]", body);
                throw new BizException(ApiCodeEnum.PARAMS_ERROR, "转换异常");
            }
        }else {
            return body;
        }
    }


    /**request.getParameter 获取参数 并转换为JSON格式 **/
    public JSONObject reqParam2JSON() {

        JSONObject returnObject = new JSONObject();

        if(isConvertJSON()){

            String body = "";
            try {
                body=request.getReader().lines().collect(Collectors.joining(""));
                if(StringUtils.isEmpty(body)) {
                    return returnObject;
                }
                return JSONObject.parseObject(body);

            } catch (Exception e) {
                log.error("请求参数转换异常！ params=[{}]", body);
                throw new BizException(ApiCodeEnum.PARAMS_ERROR, "转换异常");
            }
        }

        // 参数Map
        Map properties = request.getParameterMap();

        // 返回值Map
        Iterator entries = properties.entrySet().iterator();
        Map.Entry entry;
        String name;
        String value = "";
        while (entries.hasNext()) {
            entry = (Map.Entry) entries.next();
            name = (String) entry.getKey();
            Object valueObj = entry.getValue();
            if(null == valueObj){
                value = "";
            }else if(valueObj instanceof String[]){
                String[] values = (String[])valueObj;
                for(int i=0;i<values.length;i++){
                    value = values[i] + ",";
                }
                value = value.substring(0, value.length()-1);
            }else{
                value = valueObj.toString();
            }

            if(!name.contains("[")){
                returnObject.put(name, value);
                continue;
            }
            //添加对json对象解析的支持  example: {ps[abc] : 1}
            String mainKey = name.substring(0, name.indexOf("["));
            String subKey = name.substring(name.indexOf("[") + 1 , name.indexOf("]"));
            JSONObject subJson = new JSONObject();
            if(returnObject.get(mainKey) != null) {
                subJson = (JSONObject)returnObject.get(mainKey);
            }
            subJson.put(subKey, value);
            returnObject.put(mainKey, subJson);
        }
        return returnObject;

    }


    /** 获取json格式的请求参数 **/
    public JSONObject getReqParamJSON(){

        //将转换好的reqParam JSON格式的对象保存在当前请求上下文对象中进行保存；
        // 注意1： springMVC的CTRL默认单例模式， 不可使用局部变量保存，会出现线程安全问题；
        // 注意2： springMVC的请求模式为线程池，如果采用ThreadLocal保存对象信息，可能会出现不清空或者被覆盖的问题。
        Object reqParamObject = RequestContextHolder.getRequestAttributes().getAttribute(REQ_CONTEXT_KEY_PARAMJSON, RequestAttributes.SCOPE_REQUEST);
        if(reqParamObject == null){
            JSONObject reqParam = reqParam2JSON();
            RequestContextHolder.getRequestAttributes().setAttribute(REQ_CONTEXT_KEY_PARAMJSON, reqParam, RequestAttributes.SCOPE_REQUEST);
            return reqParam;
        }
        return (JSONObject) reqParamObject;
    }

    /** 判断请求参数是否转换为json格式 */
    private boolean isConvertJSON(){

        String contentType = request.getContentType();

        //有contentType  && json格式，  get请求不转换
        if(contentType != null
                && contentType.toLowerCase().indexOf("application/json") >= 0
                && !request.getMethod().equalsIgnoreCase("GET")
        ){ //application/json 需要转换为json格式；
            return true;
        }

        return false;
    }

    /** 获取客户端ip地址 **/
    public String getClientIp() {
        String ipAddress = null;
        ipAddress = request.getHeader("x-forwarded-for");
        if (ipAddress == null || ipAddress.length() == 0 || "unknown".equalsIgnoreCase(ipAddress)) {
            ipAddress = request.getHeader("Proxy-Client-IP");
        }
        if (ipAddress == null || ipAddress.length() == 0 || "unknown".equalsIgnoreCase(ipAddress)) {
            ipAddress = request.getHeader("WL-Proxy-Client-IP");
        }
        if (ipAddress == null || ipAddress.length() == 0 || "unknown".equalsIgnoreCase(ipAddress)) {
            ipAddress = request.getRemoteAddr();
        }

        // 对于通过多个代理的情况，第一个IP为客户端真实IP,多个IP按照','分割
        if (ipAddress != null && ipAddress.length() > 15) {
            if (ipAddress.indexOf(",") > 0) {
                ipAddress = ipAddress.substring(0, ipAddress.indexOf(","));
            }
        }
        return ipAddress;
    }

}
