<div style="margin: 15px;">
	<form class="layui-form">
        <div class="layui-form-item">
            <label class="layui-form-label">支付单号</label>
            <div class="layui-input-block">
                <input type="text" disabled="disabled" class="layui-input" value="${item.payOrderId!"" }">
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
            <label class="layui-form-label">金额(元)</label>
            <div class="layui-input-block">
                <input type="text" disabled="disabled" class="layui-input" value="${item.amount!"" }">
            </div>
        </div>
        <div class="layui-form-item">
            <label class="layui-form-label">币种</label>
            <div class="layui-input-block">
                <input type="text" disabled="disabled" class="layui-input" value="${item.currency!"" }">
            </div>
        </div>
        <div class="layui-form-item">
            <label class="layui-form-label">订单状态</label>
            <div class="layui-input-block">
				<#if item.status = 0>
                    <input type="text" style="color: black" disabled="disabled" class="layui-input" value="订单生成" }">
				<#elseif item.status = 1>
                    <input type="text" style="color: blue" disabled="disabled" class="layui-input" value="支付中" }">
				<#elseif item.status = 2>
                    <input type="text" style="color: green" disabled="disabled" class="layui-input" value="支付成功" }">
				<#elseif item.status = 3>
                    <input type="text" style="color: orange" disabled="disabled" class="layui-input" value="处理完成" }">
				<#else>
				</#if>
            </div>
        </div>
        <div class="layui-form-item">
            <label class="layui-form-label">商品标题</label>
            <div class="layui-input-block">
                <input type="text" disabled="disabled" class="layui-input" value="${item.subject!"" }">
            </div>
        </div>
        <div class="layui-form-item">
            <label class="layui-form-label">商品内容</label>
            <div class="layui-input-block">
                <input type="text" disabled="disabled" class="layui-input" value="${item.body!"" }">
            </div>
        </div>
        <div class="layui-form-item">
            <label class="layui-form-label">渠道商户ID</label>
            <div class="layui-input-block">
                <input type="text" disabled="disabled" class="layui-input" value="${item.channelMchId!"" }">
            </div>
        </div>
        <div class="layui-form-item">
            <label class="layui-form-label">渠道单号</label>
            <div class="layui-input-block">
                <input type="text" disabled="disabled" class="layui-input" value="${item.channelOrderNo!"" }">
            </div>
        </div>
        <div class="layui-form-item">
            <label class="layui-form-label">错误码</label>
            <div class="layui-input-block">
                <input type="text" disabled="disabled" class="layui-input" value="${item.errCode!"" }">
            </div>
        </div>
        <div class="layui-form-item">
            <label class="layui-form-label">错误消息</label>
            <div class="layui-input-block">
                <input type="text" disabled="disabled" class="layui-input" value="${item.errMsg!"" }">
            </div>
        </div>
        <div class="layui-form-item layui-form-text">
            <label class="layui-form-label">扩展参数</label>
            <div class="layui-input-block">
                <textarea disabled="disabled" class="layui-textarea">${item.extra!"" }</textarea>
            </div>
        </div>
        <div class="layui-form-item">
            <label class="layui-form-label">参数1</label>
            <div class="layui-input-block">
                <input type="text" disabled="disabled" class="layui-input" value="${item.param1!"" }">
            </div>
        </div>
        <div class="layui-form-item">
            <label class="layui-form-label">参数2</label>
            <div class="layui-input-block">
                <input type="text" disabled="disabled" class="layui-input" value="${item.param2!"" }">
            </div>
        </div>
        <div class="layui-form-item">
            <label class="layui-form-label">通知地址</label>
            <div class="layui-input-block">
                <input type="text" disabled="disabled" class="layui-input" value="${item.notifyUrl!"" }">
            </div>
        </div>
        <div class="layui-form-item">
            <label class="layui-form-label">失效时间</label>
            <div class="layui-input-block">
                <input type="text" disabled="disabled" class="layui-input" value="${item.expireTime!"" }">
            </div>
        </div>
        <div class="layui-form-item">
            <label class="layui-form-label">成功时间</label>
            <div class="layui-input-block">
                <input type="text" disabled="disabled" class="layui-input" value="${item.paySuccTime!"" }">
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