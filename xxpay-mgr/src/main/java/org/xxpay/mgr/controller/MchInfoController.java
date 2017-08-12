package org.xxpay.mgr.controller;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.*;
import org.xxpay.common.util.DateUtil;
import org.xxpay.common.util.MyLog;
import org.xxpay.dal.dao.model.MchInfo;
import org.xxpay.dal.dao.plugin.PageModel;
import org.xxpay.mgr.service.MchInfoService;


import java.util.List;

@Controller
@RequestMapping("/mch_info")
public class MchInfoController {

    private final static MyLog _log = MyLog.getLog(MchInfoController.class);

    @Autowired
    private MchInfoService mchInfoService;

    @RequestMapping("/list.html")
    public String listInput(ModelMap model) {
        return "mch_info/list";
    }

    @RequestMapping("/edit.html")
    public String editInput(String mchId, ModelMap model) {
        MchInfo item = null;
        if(StringUtils.isNotBlank(mchId)) {
           item = mchInfoService.selectMchInfo(mchId);
        }
        if(item == null) item = new MchInfo();
        model.put("item", item);
        return "mch_info/edit";
    }

    @RequestMapping("/list")
    @ResponseBody
    public String list(@ModelAttribute MchInfo mchInfo, Integer pageIndex, Integer pageSize) {
        PageModel pageModel = new PageModel();
        int count = mchInfoService.count(mchInfo);
        if(count <= 0) return JSON.toJSONString(pageModel);
        List<MchInfo> mchInfoList = mchInfoService.getMchInfoList((pageIndex-1)*pageSize, pageSize, mchInfo);
        if(!CollectionUtils.isEmpty(mchInfoList)) {
            JSONArray array = new JSONArray();
            for(MchInfo mi : mchInfoList) {
                JSONObject object = (JSONObject) JSONObject.toJSON(mi);
                object.put("createTime", DateUtil.date2Str(mi.getCreateTime()));
                array.add(object);
            }
            pageModel.setList(array);
        }
        pageModel.setCount(count);
        pageModel.setMsg("ok");
        pageModel.setRel(true);
        return JSON.toJSONString(pageModel);
    }

    @RequestMapping(value = "/save", method = RequestMethod.POST)
    @ResponseBody
    public String save(@RequestParam String params) {
        JSONObject po = JSONObject.parseObject(params);
        MchInfo mchInfo = new MchInfo();
        String mchId = po.getString("mchId");
        mchInfo.setName(po.getString("name"));
        mchInfo.setType(po.getString("type"));
        mchInfo.setState((byte) ("on".equalsIgnoreCase(po.getString("state")) ? 1 : 0));
        mchInfo.setReqKey(po.getString("reqKey"));
        mchInfo.setResKey(po.getString("resKey"));
        int result;
        if(StringUtils.isBlank(mchId)) {
            // 添加
            result = mchInfoService.addMchInfo(mchInfo);
        }else {
            // 修改
            mchInfo.setMchId(mchId);
            result = mchInfoService.updateMchInfo(mchInfo);
        }
        _log.info("保存商户记录,返回:{}", result);
        return result+"";
    }

    @RequestMapping("/view.html")
    public String viewInput(String mchId, ModelMap model) {
        MchInfo item = null;
        if(StringUtils.isNotBlank(mchId)) {
            item = mchInfoService.selectMchInfo(mchId);
        }
        if(item == null) item = new MchInfo();
        model.put("item", item);
        return "mch_info/view";
    }

}