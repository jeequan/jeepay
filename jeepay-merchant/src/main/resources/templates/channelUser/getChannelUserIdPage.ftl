<!DOCTYPE html>
<html>
<head>
    <meta charset="utf-8">
    <meta name="viewport" content="width=device-width, initial-scale=1, maximum-scale=1">
    <title>提示</title>
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
        <span style="font-size:16px; ">获取失败</span>
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
        <span style="font-size:16px">获取成功</span>
    </div>

</#if>

<div class="mainDiv3 layui-fluid">
    <a class="layui-btn layui-btn-primary closeBtn">关闭页面</a>
</div>

<script src="https://cdn.staticfile.org/layui/2.4.3/layui.min.js"></script>
<script>
    layui.use(['jquery'], function(){

        layui.$(".closeBtn").click(function(){

            var ua = navigator.userAgent.toLowerCase();
            if(ua.match(/MicroMessenger/i)=="micromessenger") {
                WeixinJSBridge.call('closeWindow');
            } else if(ua.indexOf("alipay")!=-1){
                AlipayJSBridge.call('closeWebview');
            }
            else{
                window.close();
            }
        });
    });


</script>
</body>
</html>
