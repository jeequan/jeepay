<div style="margin: 15px;">
	<form class="layui-form">
		<div class="layui-form-item">
			<label class="layui-form-label">商户ID</label>
			<div class="layui-input-block">
				<input type="text" disabled="disabled" class="layui-input" value="${item.mchId?if_exists }">
			</div>
		</div>
		<div class="layui-form-item">
			<label class="layui-form-label">渠道ID</label>
			<div class="layui-input-block">
                <input type="text" disabled="disabled" class="layui-input" value="${item.channelId?if_exists }">
			</div>
		</div>
		<div class="layui-form-item">
			<label class="layui-form-label">渠道名称</label>
			<div class="layui-input-block">
                <input type="text" disabled="disabled" class="layui-input" value="${item.channelName?if_exists }">
			</div>
		</div>
		<div class="layui-form-item">
			<label class="layui-form-label">渠道商户ID</label>
			<div class="layui-input-block">
				<input type="text" disabled="disabled" class="layui-input" value="${item.channelMchId?if_exists }">
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
		<div class="layui-form-item layui-form-text">
			<label class="layui-form-label">参数</label>
			<div class="layui-input-block">
				<textarea disabled="disabled" class="layui-textarea">${item.param?if_exists }</textarea>
			</div>
		</div>
		<div class="layui-form-item">
			<label class="layui-form-label">备注</label>
			<div class="layui-input-block">
				<input type="text" disabled="disabled" class="layui-input" value="${item.remark?if_exists }">
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