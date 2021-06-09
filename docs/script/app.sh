#!/bin/sh
#功能简介：启动 xxx.jar 文件
#请先cd到项目下执行
#注意：在sh文件中=赋值，左右两侧不能有空格
# .Power by terrfly

#当前所在目录
PROJECT_PATH=$(cd `dirname $0`; pwd)

#当前所在文件夹名
PROJECT_NAME="${PROJECT_PATH##*/}"

#jar名称
APP_NAME='jeepay-'$PROJECT_NAME'.jar'

#=======================================================================

#当前应用进行的变量标识
APP_PID=''


# 重新获取APPID
function refAppPID(){

	APP_PID=`ps -ef|grep $APP_NAME|grep -v grep|grep -v kill|awk '{print $2}'`
}


# 获取运行程序的pid 进程号
function getAppPID(){

	if [ ! $APP_PID ]; then #未获取过
		refAppPID
	fi
}


# 启动
function start(){

	refAppPID #获取进程PID, 需重新获取， 避免restart时无法正确启动。

	if [ $APP_PID ]; then
		echo " [$APP_NAME] App is running.  this start fail.  "
		return 0
	fi

    nohup java -jar $APP_NAME >/dev/null 2>start.log &
	# tail -200f start.log

	echo " [$APP_NAME] App starting ... "
}

# 停止
function stop(){

	getAppPID #获取进程PID


	if [ ! $APP_PID ]; then
		echo " [$APP_NAME] App is NOT running. "
		return 0
	fi

	echo " [$APP_NAME] [pid=$APP_PID] [kill -15] stop process... "
    kill -15 $APP_PID	  # kill-15 ：正常退出程序

	sleep 5 #等待5s

	# 重新获取PID
	refAppPID

	#仍然存在 需要kill -9
    if [ $APP_PID ]; then
		forcekill
    fi

	echo " [$APP_NAME] Stop Success! "

}

# 检查
function check(){

	getAppPID #获取进程PID

	if [ $APP_PID ]; then
		echo " [$APP_NAME] App is running. PID：[$APP_PID] "
	else
		echo " [$APP_NAME] App is NOT running. "
	fi

}

# 强制kill进程
function forcekill(){

	getAppPID #获取进程PID

    if [ $APP_PID ]; then
        echo " [$APP_NAME] [pid=$APP_PID] [kill -9] Kill ing ... "
        kill -9 $APP_PID
		echo " [$APP_NAME] [pid=$APP_PID] [kill -9] Kill Success! "
	else
        echo " [$APP_NAME] App is NOT running. "
    fi

}

echo ''

command=$1

if [ "${command}" ==  "start" ]; then
    start

elif [ "${command}" ==  "stop" ]; then
     stop

elif [ "${command}" ==  "restart" ]; then
     stop
	 start

elif [ "${command}" ==  "check" ]; then
     check

elif [ "${command}" ==  "kill" ]; then
     forcekill

else
    echo "Usage: $0 {start|stop|restart|check|kill|}"
fi

echo ''

