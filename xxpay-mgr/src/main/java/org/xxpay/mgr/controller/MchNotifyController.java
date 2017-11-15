package org.xxpay.mgr.controller;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.xxpay.common.util.DateUtil;
import org.xxpay.common.util.MyLog;
import org.xxpay.dal.dao.model.MchNotify;
import org.xxpay.dal.dao.plugin.PageModel;
import org.xxpay.mgr.service.MchNotifyService;

import java.util.List;

@Controller
@RequestMapping("/mch_notify")
public class MchNotifyController {

    private final static MyLog _log = MyLog.getLog(MchNotifyController.class);

    @Autowired
    private MchNotifyService mchNotifyService;

    @RequestMapping("/list.html")
    public String listInput(ModelMap model) {
        return "mch_notify/list";
    }

    @RequestMapping("/list")
    @ResponseBody
    public String list(@ModelAttribute MchNotify mchNotify, Integer pageIndex, Integer pageSize) {
        PageModel pageModel = new PageModel();
        int count = mchNotifyService.count(mchNotify);
        if(count <= 0) return JSON.toJSONString(pageModel);
        List<MchNotify> mchNotifyList = mchNotifyService.getMchNotifyList((pageIndex-1)*pageSize, pageSize, mchNotify);
        if(!CollectionUtils.isEmpty(mchNotifyList)) {
            JSONArray array = new JSONArray();
            for(MchNotify po : mchNotifyList) {
                JSONObject object = (JSONObject) JSONObject.toJSON(po);
                if(po.getCreateTime() != null) object.put("createTime", DateUtil.date2Str(po.getCreateTime()));
                if(po.getLastNotifyTime() != null) object.put("lastNotifyTime", DateUtil.date2Str(po.getLastNotifyTime()));
                array.add(object);
            }
            pageModel.setList(array);
        }
        pageModel.setCount(count);
        pageModel.setMsg("ok");
        pageModel.setRel(true);
        return JSON.toJSONString(pageModel);
    }

    @RequestMapping("/view.html")
    public String viewInput(String orderId, ModelMap model) {
        MchNotify item = null;
        if(StringUtils.isNotBlank(orderId)) {
            item = mchNotifyService.selectMchNotify(orderId);
        }
        if(item == null) {
            item = new MchNotify();
            model.put("item", item);
            return "mch_notify/view";
        }
        JSONObject object = (JSONObject) JSON.toJSON(item);
        if(item.getCreateTime() != null) object.put("createTime", DateUtil.date2Str(item.getCreateTime()));
        if(item.getUpdateTime() != null) object.put("updateTime", DateUtil.date2Str(item.getUpdateTime()));
        if(item.getLastNotifyTime() != null) object.put("lastNotifyTime", DateUtil.date2Str(item.getLastNotifyTime()));
        model.put("item", object);
        return "mch_notify/view";
    }

}