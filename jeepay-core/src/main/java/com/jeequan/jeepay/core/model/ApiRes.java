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
package com.jeequan.jeepay.core.model;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.jeequan.jeepay.core.constants.ApiCodeEnum;
import com.jeequan.jeepay.core.utils.JeepayKit;
import com.jeequan.jeepay.core.utils.JsonKit;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/*
* 接口返回对象
*
* @author terrfly
* @site https://www.jeequan.com
* @date 2021/6/8 16:35
*/
@Schema(description = "响应参数")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ApiRes<M> implements Serializable {

    /** 业务响应码 **/
    @Schema(title = "code", description = "业务响应码")
    private Integer code;

    /** 业务响应信息 **/
    @Schema(title = "msg", description = "业务响应信息")
    private String msg;

    /** 数据对象 **/
    @Schema(title = "data", description = "数据对象")
    private M data;

    /** 签名值 **/
    @Schema(title = "sign", description = "签名值")
    private String sign;

    /** 输出json格式字符串 **/
    public String toJSONString(){
        return JSON.toJSONString(this);
    }

    /** 业务处理成功 **/
    public static ApiRes ok(){
        return ok(null);
    }

    /** 业务处理成功 **/
    public static <M> ApiRes<M> ok(M data){
        return new ApiRes(ApiCodeEnum.SUCCESS.getCode(), ApiCodeEnum.SUCCESS.getMsg(), data, null);
    }

    /** 业务处理成功, 自动签名 **/
    public static ApiRes okWithSign(Object data, String mchKey){

        if(data == null){
            return new ApiRes(ApiCodeEnum.SUCCESS.getCode(), ApiCodeEnum.SUCCESS.getMsg(), null, null);
        }

        JSONObject jsonObject = (JSONObject)JSONObject.toJSON(data);
        String sign = JeepayKit.getSign(jsonObject, mchKey);
        return new ApiRes(ApiCodeEnum.SUCCESS.getCode(), ApiCodeEnum.SUCCESS.getMsg(), data, sign);
    }

    /** 业务处理成功, 返回简单json格式 **/
    public static ApiRes ok4newJson(String key, Object val){
        return ok(JsonKit.newJson(key, val));
    }

    /** 业务处理成功， 封装分页数据， 仅返回必要参数 **/
    public static <M> ApiRes<ApiPageRes.PageBean<M>> page(IPage<M> iPage){

        ApiPageRes.PageBean<M> result = new ApiPageRes.PageBean<>();
        result.setRecords(iPage.getRecords());  //记录明细
        result.setTotal(iPage.getTotal()); //总条数
        result.setCurrent(iPage.getCurrent()); //当前页码
        result.setHasNext( iPage.getPages() > iPage.getCurrent()); //是否有下一页
        return ok(result);
    }

    /** 业务处理失败 **/
    public static ApiRes fail(ApiCodeEnum apiCodeEnum, String... params){

        if(params == null || params.length <= 0){
            return new ApiRes(apiCodeEnum.getCode(), apiCodeEnum.getMsg(), null, null);
        }
        return new ApiRes(apiCodeEnum.getCode(), String.format(apiCodeEnum.getMsg(), params), null, null);
    }

    /** 自定义错误信息, 原封不用的返回输入的错误信息 **/
    public static ApiRes customFail(String customMsg){
        return new ApiRes(ApiCodeEnum.CUSTOM_FAIL.getCode(), customMsg, null, null);
    }


}
