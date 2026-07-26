package com.jeequan.jeepay.core.model.params.starpos;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.jeequan.jeepay.core.model.params.NormalMchParams;
import com.jeequan.jeepay.core.utils.StringKit;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;

/**
 * 星驿付普通商户参数。
 */
@Data
public class StarposNormalMchParams extends NormalMchParams {

    /** 环境：test、uat 或 prod。 */
    private String environment;

    /** 代理商编号。 */
    private String agetId;

    /** 商户编号。 */
    private String custId;

    /** 星驿付公钥。 */
    private String publicKey;

    /** 接口版本。 */
    private String version = "1.0.0";

    @Override
    public String deSenData() {
        StarposNormalMchParams mchParams = this;
        if (StringUtils.isNotBlank(this.publicKey)) {
            mchParams.setPublicKey(StringKit.str2Star(this.publicKey, 4, 4, 6));
        }
        return ((JSONObject) JSON.toJSON(mchParams)).toJSONString();
    }
}
