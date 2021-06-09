package com.gen;

import com.baomidou.mybatisplus.generator.AutoGenerator;
import com.baomidou.mybatisplus.generator.config.*;
import com.baomidou.mybatisplus.generator.config.converts.MySqlTypeConvert;
import com.baomidou.mybatisplus.generator.config.rules.DateType;
import com.baomidou.mybatisplus.generator.config.rules.DbColumnType;
import com.baomidou.mybatisplus.generator.config.rules.NamingStrategy;

import java.io.File;

/*
* 代码生成器
*
* @author terrfly
* @site https://www.jeepay.vip
* @date 2021/6/8 17:47
*/
public class MainGen {

    public static final String THIS_MODULE_NAME = "jeepay-z-codegen"; //当前项目名称

    public static final String DB_URL = "jdbc:mysql://127.0.0.1:3306/jeepay?useUnicode=true&characterEncoding=UTF-8&serverTimezone=GMT%2B8";
    public static final String DB_USERNAME = "root";
    public static final String DB_PASSWORD = "root";

    // 多个用,  拼接
    //public static final String TABLE_NAMES= "t_sys_entitlement,t_sys_role,t_sys_user,t_sys_user_auth";
    public static final String TABLE_NAMES= "t_pay_way";

    public static void main(String[] args) {

        // 代码生成器
        AutoGenerator mpg = new AutoGenerator();

        // 全局配置
        GlobalConfig gc = new GlobalConfig();
        String projectPath = System.getProperty("user.dir");  //获取当前项目的 文件夹地址

        if(!projectPath.endsWith(THIS_MODULE_NAME)){  //解决IDEA中 项目目录问题
            projectPath += File.separator + THIS_MODULE_NAME;
        }

        gc.setOutputDir(projectPath + "/src/main/java");
        gc.setAuthor("[mybatis plus generator]");
        gc.setOpen(false);

        gc.setBaseResultMap(true);
        gc.setDateType(DateType.ONLY_DATE);
        gc.setServiceImplName("%sService");  //不生成 service接口；

        mpg.setGlobalConfig(gc);

        // 数据源配置
        DataSourceConfig dsc = new DataSourceConfig();
        dsc.setUrl(DB_URL);
        dsc.setDriverName("com.mysql.jdbc.Driver");
        dsc.setUsername(DB_USERNAME);
        dsc.setPassword(DB_PASSWORD);

        dsc.setTypeConvert(new MySqlTypeConvert() {
            @Override
            public DbColumnType processTypeConvert(GlobalConfig globalConfig, String fieldType) {
                System.out.println("转换类型：" + fieldType);
                //tinyint转换成Boolean
                if (fieldType.toLowerCase().contains("tinyint")) {
                    return DbColumnType.BYTE;
                }
                return (DbColumnType) super.processTypeConvert(globalConfig, fieldType);
            }

        });

        mpg.setDataSource(dsc);

        // 包配置
        PackageConfig pc = new PackageConfig();
        pc.setParent("com.jeequan.jeepay");  //根目录
        pc.setEntity("core.entity");   //实体目录
        pc.setMapper("service.mapper"); //Mapper接口目录
        pc.setXml("service.mapper"); //xml目录

        pc.setService("delete_delete");  //service目录  不需要，暂时删除
        pc.setServiceImpl("service");  //serviceImpl 目录

        mpg.setPackageInfo(pc);

        // 配置模板
        TemplateConfig templateConfig = new TemplateConfig();
        templateConfig.setController(null);  //不生成controller
        templateConfig.setService(null); //不生成services

        mpg.setTemplate(templateConfig);


        // 策略配置
        StrategyConfig strategy = new StrategyConfig();
        strategy.setNaming(NamingStrategy.underline_to_camel);    //no_change原样输出
        strategy.setColumnNaming(NamingStrategy.underline_to_camel); //no_change原样输出
        strategy.setEntityLombokModel(true);

        strategy.setInclude(TABLE_NAMES.split(","));
        strategy.setTablePrefix("t_");

//        strategy.setEntityTableFieldAnnotationEnable(true); //自动添加 field注解

        mpg.setStrategy(strategy);

        mpg.execute();
    }


}
