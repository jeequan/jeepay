<div style="margin: 15px;">
	<form class="layui-form">
		<div class="layui-form-item">
			<label class="layui-form-label">商户ID</label>
			<div class="layui-input-block">
				<input type="text" class="layui-input" disabled="disabled" value="${item.mchId?if_exists }">
			</div>
		</div>
		<div class="layui-form-item">
			<label class="layui-form-label">商户名称</label>
			<div class="layui-input-block">
				<input type="text" disabled="disabled" class="layui-input" value="${item.name?if_exists }">
			</div>
		</div>
		<div class="layui-form-item">
			<label class="layui-form-label">商户类型</label>
            <div class="layui-input-block">
			<#if item.type = "1">
                <input type="text" disabled="disabled" class="layui-input" value="平台账户" }">
			<#elseif item.type = "2">
                <input type="text" disabled="disabled" class="layui-input" value="私有账户" }">
			<#else>
			</#if>
            </div>
		</div>
		<div class="layui-form-item">
			<label class="layui-form-label">状态</label>
			<div class="layui-input-block">
			<#if item.state = 1>
                <input type="text" style="color: green" disabled="disabled" class="layui-input" value="启用" }">
			<#else>
                <input type="text" style="color: red" disabled="disabled" class="layui-input" value="停止" }">
			</#if>
			</div>
		</div>
		<div class="layui-form-item">
			<label class="layui-form-label">请求私钥</label>
            <div class="layui-input-block">
                <textarea disabled="disabled" class="layui-textarea">${item.reqKey!"" }</textarea>
            </div>
		</div>
		<div class="layui-form-item">
			<label class="layui-form-label">响应私钥</label>
            <div class="layui-input-block">
                <textarea disabled="disabled" class="layui-textarea">${item.resKey!"" }</textarea>
            </div>
		</div>
        <div class="layui-form-item">
            <label class="layui-form-label">创建时间</label>
            <div class="layui-input-block">
                <input type="text" disabled="disabled" class="layui-input" value="${(item.createTime?string("yyyy-MM-dd HH:mm:ss"))!''} ">
            </div>
        </div>
        <div class="layui-form-item">
            <label class="layui-form-label">更新时间</label>
            <div class="layui-input-block">
                <input type="text" disabled="disabled" class="layui-input" value="${(item.updateTime?string("yyyy-MM-dd HH:mm:ss"))!''} ">
            </div>
        </div>
		<button lay-filter="edit" lay-submit style="display: none;"></button>
	</form>
</div>