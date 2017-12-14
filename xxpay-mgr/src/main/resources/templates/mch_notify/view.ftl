<div style="margin: 15px;">
	<form class="layui-form">
        <div class="layui-form-item">
            <label class="layui-form-label">订单号</label>
            <div class="layui-input-block">
                <input type="text" disabled="disabled" class="layui-input" value="${item.orderId!"" }">
            </div>
        </div>
		<div class="layui-form-item">
			<label class="layui-form-label">商户ID</label>
            <div class="layui-input-block">
            	<input type="text" disabled="disabled" class="layui-input" value="${item.mchId!"" }">
			</div>
		</div>
        <div class="layui-form-item">
            <label class="layui-form-label">商户单号</label>
            <div class="layui-input-block">
                <input type="text" disabled="disabled" class="layui-input" value="${item.mchOrderNo!"" }">
            </div>
        </div>
        <div class="layui-form-item">
            <label class="layui-form-label">订单类型</label>
            <div class="layui-input-block">
            <#if item.status = 1>
                <input type="text" disabled="disabled" class="layui-input" value="支付订单" }">
            <#elseif item.status = 2>
                <input type="text" disabled="disabled" class="layui-input" value="转账订单" }">
            <#elseif item.status = 3>
                <input type="text" disabled="disabled" class="layui-input" value="退款订单" }">
            <#else>
            </#if>
            </div>
        </div>
        <div class="layui-form-item">
            <label class="layui-form-label">通知状态</label>
            <div class="layui-input-block">
                <#if item.status = 1>
                    <input type="text" style="color: blue" disabled="disabled" class="layui-input" value="通知中" }">
				<#elseif item.status = 2>
                    <input type="text" style="color: green" disabled="disabled" class="layui-input" value="通知成功" }">
				<#elseif item.status = 3>
                    <input type="text" style="color: red" disabled="disabled" class="layui-input" value="通知失败" }">
				<#else>
				</#if>
            </div>
        </div>
        <div class="layui-form-item">
            <label class="layui-form-label">通知结果</label>
            <div class="layui-input-block">
                <input type="text" disabled="disabled" class="layui-input" value="${item.result!"" }">
            </div>
        </div>
        <div class="layui-form-item">
            <label class="layui-form-label">通知地址</label>
            <div class="layui-input-block">
                <textarea disabled="disabled" class="layui-textarea">${item.notifyUrl!"" }</textarea>
            </div>
        </div>
        <div class="layui-form-item">
            <label class="layui-form-label">通知次数</label>
            <div class="layui-input-block">
                <input type="text" disabled="disabled" class="layui-input" value="${item.notifyCount!"" }">
            </div>
        </div>
        <div class="layui-form-item">
            <label class="layui-form-label">最后通知</label>
            <div class="layui-input-block">
                <input type="text" disabled="disabled" class="layui-input" value="${item.lastNotifyTime!"" }">
            </div>
        </div>
        <div class="layui-form-item">
            <label class="layui-form-label">创建时间</label>
            <div class="layui-input-block">
                <input type="text" disabled="disabled" class="layui-input" value="${item.createTime!"" } ">
            </div>
        </div>
        <div class="layui-form-item">
            <label class="layui-form-label">更新时间</label>
            <div class="layui-input-block">
                <input type="text" disabled="disabled" class="layui-input" value="${item.updateTime!"" } ">
            </div>
        </div>

		<button lay-filter="edit" lay-submit style="display: none;"></button>
	</form>
</div>