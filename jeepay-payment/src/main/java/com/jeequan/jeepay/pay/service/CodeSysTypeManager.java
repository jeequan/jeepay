package com.jeequan.jeepay.pay.service;

import com.jeequan.jeepay.core.constants.CS;
import com.jeequan.jeepay.core.service.ICodeSysTypeManager;
import org.springframework.stereotype.Component;

@Component
public class CodeSysTypeManager implements ICodeSysTypeManager {

    @Override
    public String getCodeSysName() {
        return CS.CODE_SYS_NAME_SET.JEEPAY_PAYMENT;
    }
}
