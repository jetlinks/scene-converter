package org.jetlinks.scene.converter.service.impl;

import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.jetlinks.pro.TimerSpec;
import org.jetlinks.pro.rule.engine.executor.device.SelectorValue;
import org.jetlinks.pro.rule.engine.scene.DeviceOperation;
import org.jetlinks.pro.rule.engine.scene.DeviceTrigger;
import org.jetlinks.pro.rule.engine.scene.SceneRule;
import org.jetlinks.pro.rule.engine.scene.Trigger;
import org.jetlinks.pro.rule.engine.scene.TriggerType;
import org.jetlinks.scene.converter.entity.SceneConverterContext;
import org.jetlinks.scene.converter.entity.SceneSourceEntity;
import org.jetlinks.scene.converter.entity.SceneTargetEntity;
import org.springframework.util.StringUtils;

import java.util.Collections;

/**
 * 来源类型：设备告警.
 *
 * @author zhangji 2023/5/9
 */
@Slf4j
public class SceneConverterDevice extends AbstractSceneConverter {
    public static final String MODEL_TYPE_DEVICE = "device_alarm";

    @Override
    public String getType() {
        return MODEL_TYPE_DEVICE;
    }

    @Override
    protected JSONObject getModelMeta(SceneSourceEntity source) {
        return super.getModelMeta(source).getJSONObject("alarmRule");
    }

    @Override
    protected SceneRule createSceneRule(int index,
                                        JSONObject trigger,
                                        SceneConverterContext context) {
        SceneRule sceneRule = super.createSceneRule(index, trigger, context);
        sceneRule.setName(context.getModelMeta().getString("name"));
        return sceneRule;
    }

    @Override
    protected Trigger convertTrigger(JSONObject trigger,
                                     JSONObject modelMeta) {
        Trigger targetTrigger = new Trigger();
        String triggerType = trigger.getString("trigger");
        if (!StringUtils.hasText(triggerType)) {
            log.error("triggers.trigger不能为空。");
            return null;
        }
        switch (triggerType) {
            case TRIGGER_MANUAL: {
                targetTrigger.setType(TriggerType.manual);
                break;
            }
            case TRIGGER_TIMER: {
                // 设备告警
                String type = trigger.getString("type");
                if (StringUtils.hasText(type)) {
                    targetTrigger.setType(TriggerType.device);

                    // 设备告警
                    DeviceTrigger deviceTrigger = convertDeviceTrigger(trigger, modelMeta);
                    targetTrigger.setDevice(deviceTrigger);

                    // 防抖
//                        targetTrigger.setShakeLimit(FastBeanCopier.copy(modelMeta.getJSONObject("shakeLimit"), new Trigger.GroupShakeLimit()));
                } else {
                    targetTrigger.setType(TriggerType.timer);
                    targetTrigger.setTimer(TimerSpec.cron(trigger.getString("cron")));
                }
                break;
            }
            case TRIGGER_DEVICE: {
                targetTrigger.setType(TriggerType.device);
                // 设备告警
                DeviceTrigger deviceTrigger = convertDeviceTrigger(trigger, modelMeta);
                targetTrigger.setDevice(deviceTrigger);

                // 防抖
//                    targetTrigger.setShakeLimit(FastBeanCopier.copy(modelMeta.getJSONObject("shakeLimit"), new Trigger.GroupShakeLimit()));
                break;
            }
            default: {
                log.error("不支持的设备触发类型：{}", triggerType);
                return null;
            }
        }

        return targetTrigger;
    }

    @Override
    protected SceneTargetEntity createSceneTargetEntity(SceneSourceEntity source,
                                                        SceneRule sceneRule,
                                                        SceneConverterContext context) {
        SceneTargetEntity entity = super.createSceneTargetEntity(source, sceneRule, context);

        JSONObject modelMeta = JSONObject.parseObject(source.getModelMeta());
        String targetType = modelMeta.getString("target");
        String targetId = modelMeta.getString("targetId");
        Integer level = context.getModelMeta().getInteger("level");

        entity.setTargetType(targetType);
        entity.setTargetId(targetId);
        if ("device".equals(targetType)) {
            entity.setTargetName(context.getModelMeta().getString("deviceName"));
        } else if ("product".equals(targetType)) {
            entity.setTargetName(context.getModelMeta().getString("productName"));
        } else {
            entity.setTargetName(sceneRule.getName());
        }

        entity.setLevel(level == null ? 0 : level);

        return entity;
    }

    private DeviceTrigger convertDeviceTrigger(JSONObject trigger,
                                               JSONObject modelMeta) {
        DeviceTrigger deviceTrigger = new DeviceTrigger();
        deviceTrigger.setProductId(modelMeta.getString("productId"));
        String deviceId = modelMeta.getString("deviceId");
        if (StringUtils.hasText(deviceId)) {
            deviceTrigger.setSelector("fixed");
            SelectorValue selectorValue = SelectorValue.of(deviceId, modelMeta.getString("deviceName"));
            deviceTrigger.setSelectorValues(Collections.singletonList(selectorValue));
        } else {
            deviceTrigger.setSelector("all");
        }

        String operatorType = trigger.getString("type");
        String modelId = trigger.getString("modelId");
        DeviceOperation operation;
        // 根据消息类型转换
        operation = convertDeviceOperation(operatorType, modelId, trigger);


        deviceTrigger.setOperation(operation);
        return deviceTrigger;
    }
}
