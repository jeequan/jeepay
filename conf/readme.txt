
文件夹用途： 用于放置开发环境的通用配置项和生产环境下的项目配置模板。 

1. 【devCommons】： 
开发环境通用配置文件放置目录。 更改此目录后将覆写 manager/merchant/payment 项目下的application.yml文件对应参数，从而达到每个项目不必单独配置的目的，更加节约开发时间。

2. 该文件夹下的【manager/merchant/payment】：
文件为上线部署时与jar同级目录下的application.yml建议配置项的模板。 需更改为实际参数， 也可按需添加。 


扩展知识：
#####################################################

# spring boot支持外部application.yml  读取优先级为：
#   1、file:./config/（当前目录下的config文件夹）
#   2、file:./（当前目录）
#   3、classpath:/config/（classpath下的config目录）
#   4、classpath:/（classpath根目录）

#####################################################