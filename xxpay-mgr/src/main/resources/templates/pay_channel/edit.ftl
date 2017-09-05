<div style="margin: 15px;">
	<form class="layui-form">
		<#if item.id?exists>
            <input type="text" name="id" hidden="hidden" value="${item.id?if_exists }">
		</#if>
		<div class="layui-form-item">
			<label class="layui-form-label">商户ID</label>
			<div class="layui-input-block">
				<input type="text" name="mchId" lay-verify="required" placeholder="请输入商户ID" autocomplete="off" class="layui-input" value="${item.mchId?if_exists }">
			</div>
		</div>
		<div class="layui-form-item">
			<label class="layui-form-label">渠道ID</label>
			<div class="layui-input-block">
				<select name="channelId" lay-verify="required">
					<option value=""></option>
                    <option value="ALIPAY_MOBILE" <#if (item.channelId!"") == "ALIPAY_MOBILE">selected="selected"</#if>>ALIPAY_MOBILE</option>
                    <option value="ALIPAY_PC" <#if (item.channelId!"") == "ALIPAY_PC">selected="selected"</#if>>ALIPAY_PC</option>
                    <option value="ALIPAY_WAP" <#if (item.channelId!"") == "ALIPAY_WAP">selected="selected"</#if>>ALIPAY_WAP</option>
                    <option value="ALIPAY_QR" <#if (item.channelId!"") == "ALIPAY_QR">selected="selected"</#if>>ALIPAY_QR</option>
                    <option value="WX_APP" <#if (item.channelId!"") == "WX_APP">selected="selected"</#if>>WX_APP</option>
                    <option value="WX_JSAPI" <#if (item.channelId!"") == "WX_JSAPI">selected="selected"</#if>>WX_JSAPI</option>
                    <option value="WX_NATIVE" <#if (item.channelId!"") == "WX_NATIVE">selected="selected"</#if>>WX_NATIVE</option>
                    <option value="WX_MWEB" <#if (item.channelId!"") == "WX_MWEB">selected="selected"</#if>>WX_MWEB</option>
				</select>
			</div>
		</div>
		<div class="layui-form-item">
			<label class="layui-form-label">渠道名称</label>
			<div class="layui-input-block">
				<select name="channelName" lay-verify="required">
					<option value=""></option>
					<option value="ALIPAY" <#if (item.channelName!"") == "ALIPAY">selected="selected"</#if>>ALIPAY</option>
					<option value="WX" <#if (item.channelName!"") == "WX">selected="selected"</#if>>WX</option>
				</select>
			</div>
		</div>
		<div class="layui-form-item">
			<label class="layui-form-label">渠道商户ID</label>
			<div class="layui-input-block">
				<input type="text" name="channelMchId" lay-verify="required" placeholder="请输入渠道商户ID" autocomplete="off" class="layui-input" value="${item.channelMchId?if_exists }">
			</div>
		</div>
		<div class="layui-form-item">
			<label class="layui-form-label">是否启用</label>
			<div class="layui-input-block">
				<input type="checkbox" name="state" lay-skin="switch" <#if (item.state!1) == 1>checked="checked"</#if> >
			</div>
		</div>
		<div class="layui-form-item layui-form-text">
			<label class="layui-form-label">参数</label>
			<div class="layui-input-block">
				<textarea name="param" placeholder="请输入参数" lay-verify="required" class="layui-textarea">${item.param?if_exists }</textarea>
			</div>
		</div>
		<div class="layui-form-item">
			<label class="layui-form-label">备注</label>
			<div class="layui-input-block">
				<input type="text" name="remark" placeholder="请输入备注" autocomplete="off" class="layui-input" value="${item.remark?if_exists }">
			</div>
		</div>
		<button lay-filter="edit" lay-submit style="display: none;"></button>
	</form>
</div>