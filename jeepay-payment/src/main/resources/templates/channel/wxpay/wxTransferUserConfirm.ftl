<!DOCTYPE html>
<html>
<head>
    <meta charset="utf-8">
    <meta name="viewport" content="width=device-width, initial-scale=1, maximum-scale=1">
    <title>用户确认收款</title>
    <link rel="stylesheet" href="https://cdn.staticfile.org/layui/2.4.3/css/layui.css">
    <style>
        .mainDiv1 {color:lightseagreen; text-align:center; margin-top: 50px;}
        .mainDiv2 {text-align:center; margin-top: 10px;}
        .mainDiv3 {text-align:center; margin-top: 200px;}
        .mainDivTitle {text-align:center; margin-top: 20px; color:orangered }
    </style>
</head>
<body>


<#if errMsg != null >

    <div class="mainDiv1 layui-fluid">
        <i class="layui-icon" style="font-size:100px; color:orangered">&#x1007;</i>
    </div>
    <div class="mainDiv2 layui-fluid">
        <span style="font-size:16px; ">失败</span>
        <div class="mainDivTitle layui-fluid">
            <span style="font-size:14px">错误提示：${errMsg!''}
            </span>
        </div>
    </div>

<#else>

    <div class="mainDiv1 layui-fluid">
        <i class="layui-icon" style="font-size:100px;">&#x1005;</i>
    </div>
    <div class="mainDiv2 layui-fluid">
        <span style="font-size:16px">请稍后...</span>
    </div>

</#if>

<script>

    let channelResData = '${channelResData}';
    let channelResDataJSON = JSON.parse(channelResData);

    let onBridgeReady = function () {

        WeixinJSBridge.invoke('requestMerchantTransfer', {
                mchId: channelResDataJSON.mchId,
                appId: channelResDataJSON.appId,
                package: channelResDataJSON.package,
            },
            function (res) {
                if (res.err_msg === 'requestMerchantTransfer:ok') {
                    // res.err_msg将在页面展示成功后返回应用时返回success，并不代表付款成功
                    WeixinJSBridge.call('closeWindow');
                }
            }
        );
    }

    if (typeof WeixinJSBridge == "undefined") {
        if (document.addEventListener) {
            document.addEventListener('WeixinJSBridgeReady', onBridgeReady, false);
        } else if (document.attachEvent) {
            document.attachEvent('WeixinJSBridgeReady', onBridgeReady);
            document.attachEvent('onWeixinJSBridgeReady', onBridgeReady);
        }
    } else {
        onBridgeReady();
    }

</script>
</body>
</html>
