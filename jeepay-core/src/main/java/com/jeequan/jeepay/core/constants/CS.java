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
package com.jeequan.jeepay.core.constants;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * @Author terrfly
 * @Date 2019/11/16 15:09
 * @Description Constants 常量对象
 **/
public class CS {

    //登录图形验证码缓存时间，单位：s
    public static final int VERCODE_CACHE_TIME = 60;

    /** 系统类型定义 **/
    public interface SYS_TYPE{
        String MCH = "MCH";
        String MGR = "MGR";
        Map<String, String> SYS_TYPE_MAP = new HashMap<>();
    }
    static {
        SYS_TYPE.SYS_TYPE_MAP.put(SYS_TYPE.MCH, "商户系统");
        SYS_TYPE.SYS_TYPE_MAP.put(SYS_TYPE.MGR, "运营平台");
    }

    /** yes or no **/
    public static final byte NO = 0;
    public static final byte YES = 1;

    /** 通用 可用 / 禁用 **/
    public static final int PUB_USABLE = 1;
    public static final int PUB_DISABLE = 0;

    public static final Map<Integer, String> PUB_USABLE_MAP = new HashMap<>();
    static {
        PUB_USABLE_MAP.put(PUB_USABLE, "正常");
        PUB_USABLE_MAP.put(PUB_DISABLE, "停用");
    }

    /**
     * 账号类型:1-服务商 2-商户 3-商户应用
     */
    public static final byte INFO_TYPE_ISV = 1;
    public static final byte INFO_TYPE_MCH = 2;
    public static final byte INFO_TYPE_MCH_APP = 3;


    /**
     * 商户类型:1-普通商户 2-特约商户
     */
    public static final byte MCH_TYPE_NORMAL = 1;
    public static final byte MCH_TYPE_ISVSUB = 2;

    /**
     * 性别 1- 男， 2-女
     */
    public static final byte SEX_UNKNOWN = 0;
    public static final byte SEX_MALE = 1;
    public static final byte SEX_FEMALE = 2;

    /** 默认密码 */
    public static final String DEFAULT_PWD = "jeepay666";


    /**
     * 允许上传的的图片文件格式，需要与 WebSecurityConfig对应
     */
    public static final Set<String> ALLOW_UPLOAD_IMG_SUFFIX = new HashSet<>();
    static{
        ALLOW_UPLOAD_IMG_SUFFIX.add("jpg");
        ALLOW_UPLOAD_IMG_SUFFIX.add("png");
        ALLOW_UPLOAD_IMG_SUFFIX.add("jpeg");
        ALLOW_UPLOAD_IMG_SUFFIX.add("gif");
        ALLOW_UPLOAD_IMG_SUFFIX.add("mp4");
    }


    public static final long TOKEN_TIME = 60 * 60 * 2; //单位：s,  两小时


    //access_token 名称
    public static final String ACCESS_TOKEN_NAME = "iToken";

    /** ！！不同系统请放置不同的redis库 ！！ **/
    /** 缓存key: 当前用户所有用户的token集合  example: TOKEN_1001_HcNheNDqHzhTIrT0lUXikm7xU5XY4Q */
    public static final String CACHE_KEY_TOKEN = "TOKEN_%s_%s";
    public static String getCacheKeyToken(Long sysUserId, String uuid){
        return String.format(CACHE_KEY_TOKEN, sysUserId, uuid);
    }

    /** 图片验证码 缓存key **/
    public static final String CACHE_KEY_IMG_CODE = "img_code_%s";
    public static String getCacheKeyImgCode(String imgToken){
        return String.format(CACHE_KEY_IMG_CODE, imgToken);
    }

    /** 回调URL的格前缀  */
    public static final String PAY_RETURNURL_FIX_ONLY_JUMP_PREFIX = "ONLYJUMP_";

    /** 登录认证类型 **/
    public interface AUTH_TYPE{

        byte LOGIN_USER_NAME = 1; //登录用户名
        byte TELPHONE = 2; //手机号
        byte EMAIL = 3; //邮箱

        byte WX_UNION_ID = 10; //微信unionId
        byte WX_MINI = 11; //微信小程序
        byte WX_MP = 12; //微信公众号

        byte QQ = 20; //QQ
    }


    //菜单类型
    public interface ENT_TYPE{

        String MENU_LEFT = "ML";  //左侧显示菜单
        String MENU_OTHER = "MO";  //其他菜单
        String PAGE_OR_BTN = "PB";  //页面 or 按钮

    }

    //接口类型
    public interface IF_CODE{

        String ALIPAY = "alipay";   // 支付宝官方支付
        String WXPAY = "wxpay";     // 微信官方支付
        String YSFPAY = "ysfpay";   // 云闪付开放平台
        String XXPAY = "xxpay";     // 小新支付
        String PPPAY = "pppay";     // Paypal 支付
        String PLSPAY = "plspay";     // 计全支付plus
    }


    //支付方式代码
    public interface PAY_WAY_CODE{

        // 特殊支付方式
        String QR_CASHIER = "QR_CASHIER"; //  ( 通过二维码跳转到收银台完成支付， 已集成获取用户ID的实现。  )
        String AUTO_BAR = "AUTO_BAR"; // 条码聚合支付（自动分类条码类型）

        String ALI_BAR = "ALI_BAR";  //支付宝条码支付
        String ALI_JSAPI = "ALI_JSAPI";  //支付宝服务窗支付
        String ALI_LITE = "ALI_LITE";  //支付宝小程序支付
        String ALI_APP = "ALI_APP";  //支付宝 app支付
        String ALI_PC = "ALI_PC";  //支付宝 电脑网站支付
        String ALI_WAP = "ALI_WAP";  //支付宝 wap支付
        String ALI_QR = "ALI_QR";  //支付宝 二维码付款

        String YSF_BAR = "YSF_BAR";  //云闪付条码支付
        String YSF_JSAPI = "YSF_JSAPI";  //云闪付服务窗支付

        String WX_JSAPI = "WX_JSAPI";  //微信jsapi支付
        String WX_LITE = "WX_LITE";  //微信小程序支付
        String WX_BAR = "WX_BAR";  //微信条码支付
        String WX_H5 = "WX_H5";  //微信H5支付
        String WX_NATIVE = "WX_NATIVE";  //微信扫码支付

        String PP_PC = "PP_PC"; // Paypal 支付
    }

    //支付数据包 类型
    public interface PAY_DATA_TYPE {
        String PAY_URL = "payurl";  //跳转链接的方式  redirectUrl
        String FORM = "form";  //表单提交
        String WX_APP = "wxapp";  //微信app参数
        String ALI_APP = "aliapp";  //支付宝app参数
        String YSF_APP = "ysfapp";  //云闪付app参数
        String CODE_URL = "codeUrl";  //二维码URL
        String CODE_IMG_URL = "codeImgUrl";  //二维码图片显示URL
        String NONE = "none";  //无参数
//        String QR_CONTENT = "qrContent";  //二维码实际内容
    }


    //接口版本
    public interface PAY_IF_VERSION{
        String WX_V2 = "V2";  //微信接口版本V2
        String WX_V3 = "V3";  //微信接口版本V3
    }
}
