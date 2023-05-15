# 场景联动迁移程序

用于将JetLinks 1.0版本的设备告警以及场景联动配置信息，迁移到JetLinks 2.0版本中。
  
由于JetLinks 2.0重构了设备告警以及场景联动的功能，数据库表发生了变动。版本升级后无法正常使用之前的配置。需要使用本程序来转换配置。

程序启动后直接连接两个版本的数据库，读取1.0版本的数据，将数据转换后存入2.0版本的数据库

## 涉及的数据表
### 1.0版本
- rule_instance：场景联动/设备告警
- rule_dev_alarm_history：设备告警记录
- notify_config：通知配置
- notify_template：通知模板
### 2.0版本
- rule_scene：场景联动
- alarm_config：告警配置
- s_alarm_rule_bind：场景联动和告警的绑定信息
- alarm_record：告警记录
- notify_config：通知配置
- notify_template：通知模板

## 使用方法
### 方法一
1. 配置jvm启动参数
2. 打包运行
### 方法二
1. 打包运行
2. 在控制台依次输入启动参数

## 启动参数说明
- JetLinks 1.0版本中的数据库配置

|参数|名称
|---|---|
|source.url|迁移来源的数据库url|
|source.username|迁移来源的数据库用户名|
|source.password|迁移来源的数据库密码|
|source.driver|迁移来源的数据库Driver|

- JetLinks 2.0版本中的数据库配置

|参数|名称
|---|---|
|target.url|迁移对象的数据库url|
|target.username|迁移对象的数据库用户名|
|target.password|迁移对象的数据库密码|
|source.driver|迁移对象的数据库Driver|
