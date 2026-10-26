package com.jeequan.jeepay.core.ctrls;

import com.alibaba.fastjson.JSONObject;
import com.jeequan.jeepay.core.exception.BizException;
import org.apache.commons.lang3.tuple.MutablePair;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * 排序字段参数校验回归测试。
 * <p>
 * 排序字段会被拼接进 SQL 的 ORDER BY 语句，因此只允许标准的 SQL 标识符（字母、数字、下划线），
 * 其余字符一律拒绝，避免排序字段被当作 SQL 片段执行。
 */
class AbstractCtrlSortFieldTest {

    @Test
    void rejectsSortFieldThatIsNotPlainSqlIdentifier() {
        String[] illegalFields = new String[]{"role name", "role_name)", "role_name;select 1", "1 role_name"};
        for (String illegalField : illegalFields) {
            SortFieldCtrl ctrl = ctrlWithParams("sortField", illegalField, "sortOrder", "descend");
            BizException bizException = assertThrows(BizException.class, ctrl::sortInfo,
                    "非法的排序字段必须被拒绝: " + illegalField);
            assertTrue(bizException.getApiRes().getMsg().contains("排序字段"),
                    "异常信息应说明排序字段不合法, 实际: " + bizException.getApiRes().getMsg());
        }
    }

    @Test
    void keepsLegitimateSortFieldAndMapsItToColumnName() {
        SortFieldCtrl ctrl = ctrlWithParams("sortField", "roleName", "sortOrder", "descend");

        MutablePair<Boolean, String> sortInfo = ctrl.sortInfo();
        assertFalse(sortInfo.getLeft());
        assertEquals("role_name", sortInfo.getRight());
    }

    @Test
    void keepsAscendingOrderFlag() {
        SortFieldCtrl ctrl = ctrlWithParams("sortField", "roleName", "sortOrder", "ascend");

        MutablePair<Boolean, String> sortInfo = ctrl.sortInfo();
        assertTrue(sortInfo.getLeft());
        assertEquals("role_name", sortInfo.getRight());
    }

    @Test
    void returnsNullWhenSortFieldIsAbsent() {
        SortFieldCtrl ctrl = ctrlWithParams("pageNumber", "1", "pageSize", "20");

        assertNull(ctrl.sortInfo());
    }

    private SortFieldCtrl ctrlWithParams(String... nameValuePairs) {
        JSONObject params = new JSONObject();
        for (int i = 0; i < nameValuePairs.length; i += 2) {
            params.put(nameValuePairs[i], nameValuePairs[i + 1]);
        }
        return new SortFieldCtrl(params);
    }

    /** 模拟请求参数，暴露受保护的 getSortInfo() 供测试调用 */
    static class SortFieldCtrl extends AbstractCtrl {

        private final JSONObject params;

        SortFieldCtrl(JSONObject params) {
            this.params = params;
        }

        @Override
        protected JSONObject getReqParamJSON() {
            return params;
        }

        MutablePair<Boolean, String> sortInfo() {
            return getSortInfo();
        }
    }
}
