package org.jetlinks.scene.converter.service.impl;

import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.hswebframework.ezorm.core.param.Term;
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
import java.util.List;

/**
 * 来源类型：场景联动.
 *
 * @author zhangji 2023/5/9
 */
@Slf4j
public class SceneConverterRule extends AbstractSceneConverter {
    public static final String MODEL_TYPE_SCENE = "rule-scene";

    @Override
    public String getType() {
        return MODEL_TYPE_SCENE;
    }

    @Override
    protected List<Term> convertTerms(JSONObject trigger) {
        return super.convertTerms(trigger.getJSONObject("device"));
    }

    @Override
    protected Trigger convertTrigger(JSONObject trigger,
                                     JSONObject modelMeta) {
        // 每一个触发条件生成一条场景联动数据，然后绑定到同一个告警配置中

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
                targetTrigger.setType(TriggerType.timer);
                targetTrigger.setTimer(TimerSpec.cron(trigger.getString("cron")));
                break;
            }
            case TRIGGER_DEVICE: {
                targetTrigger.setType(TriggerType.device);

                JSONObject device = trigger.getJSONObject("device");
                DeviceTrigger deviceTrigger = new DeviceTrigger();
                deviceTrigger.setSelector("fixed");
                SelectorValue selectorValue = SelectorValue
                        .of(device.getString("deviceId"), device.getString("deviceId"));
                deviceTrigger.setSelectorValues(Collections.singletonList(selectorValue));
                deviceTrigger.setProductId(device.getString("productId"));

                // 防抖
//                    targetTrigger.setShakeLimit(FastBeanCopier.copy(device.getJSONObject("shakeLimit"), new Trigger.GroupShakeLimit()));

                DeviceOperation operation = convertDeviceOperation(
                        device.getString("type"), device.getString("modelId"), trigger
                );
                deviceTrigger.setOperation(operation);
                targetTrigger.setDevice(deviceTrigger);
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

        entity.setTargetType("other");
        entity.setTargetId(sceneRule.getId());
        entity.setTargetName(sceneRule.getName());

        return entity;
    }
}
