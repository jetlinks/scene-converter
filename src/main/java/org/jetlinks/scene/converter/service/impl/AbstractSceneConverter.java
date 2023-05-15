package org.jetlinks.scene.converter.service.impl;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.hswebframework.ezorm.core.param.Term;
import org.hswebframework.web.bean.FastBeanCopier;
import org.hswebframework.web.exception.BusinessException;
import org.hswebframework.web.utils.DigestUtils;
import org.jetlinks.core.message.function.FunctionParameter;
import org.jetlinks.pro.TimerSpec;
import org.jetlinks.pro.rule.engine.enums.RuleInstanceState;
import org.jetlinks.pro.rule.engine.executor.device.SelectorValue;
import org.jetlinks.pro.rule.engine.scene.DeviceOperation;
import org.jetlinks.pro.rule.engine.scene.SceneAction;
import org.jetlinks.pro.rule.engine.scene.SceneActions;
import org.jetlinks.pro.rule.engine.scene.SceneConditionAction;
import org.jetlinks.pro.rule.engine.scene.SceneRule;
import org.jetlinks.pro.rule.engine.scene.Trigger;
import org.jetlinks.scene.converter.entity.SceneConverterContext;
import org.jetlinks.scene.converter.entity.SceneSourceEntity;
import org.jetlinks.scene.converter.entity.SceneTargetEntity;
import org.jetlinks.scene.converter.service.SceneConverter;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 输入描述.
 *
 * @author zhangji 2023/5/9
 */
@Slf4j
public abstract class AbstractSceneConverter implements SceneConverter {

    public static final String EXECUTOR_NOTIFIER = "notifier";
    public static final String EXECUTOR_DEVICE   = "device-message-sender";

    public static final String TRIGGER_MANUAL = "manual";
    public static final String TRIGGER_DEVICE = "device";
    public static final String TRIGGER_TIMER  = "timer";
    public static final String TRIGGER_SCENE  = "scene"; // 暂不支持

    public static final String MESSAGE_TYPE_ONLINE     = "online";
    public static final String MESSAGE_TYPE_OFFLINE    = "offline";
    public static final String MESSAGE_TYPE_PROPERTIES = "properties";
    public static final String MESSAGE_TYPE_EVENT      = "event";
    public static final String MESSAGE_TYPE_FUNCTION   = "function";

    @Override
    public List<SceneTargetEntity> convertScene(SceneSourceEntity source) {

        List<SceneTargetEntity> output = new ArrayList<>();

        // 规则模型
        JSONObject modelMeta = getModelMeta(source);
        // 执行动作
        SceneConditionAction branch = convertActions(modelMeta);
        // 上下文
        SceneConverterContext context = new SceneConverterContext(source.getId(), source.getName(), modelMeta, branch);
        // 触发条件
        JSONArray triggers = context.getModelMeta().getJSONArray("triggers");

        if (!CollectionUtils.isEmpty(triggers)) {
            // 每一个触发条件生成一条场景联动数据，然后绑定到同一个告警配置中
            for (int i = 0; i < triggers.size(); i++) {
                if (triggers.get(i) instanceof JSONObject) {
                    // 生成场景规则，包含触发条件和动作
                    SceneRule sceneRule = createSceneRule(i, (JSONObject) triggers.get(i), context);
                    if (sceneRule == null) {
                        continue;
                    }

                    // 生成场景联动实体
                    try {
                        SceneTargetEntity entity = createSceneTargetEntity(source, sceneRule, context);
                        output.add(entity);
                    } catch (Exception e) {
                        log.error("数据转换错误。id：{}, modelMeta: {} \n", source.getId(), modelMeta, e);
                    }
                }
            }
        }

        return output;
    }

    abstract protected Trigger convertTrigger(JSONObject trigger,
                                              JSONObject modelMeta);

    protected SceneTargetEntity createSceneTargetEntity(SceneSourceEntity source,
                                                        SceneRule sceneRule,
                                                        SceneConverterContext context) {
        SceneTargetEntity entity = FastBeanCopier.copy(source, new SceneTargetEntity(), "state");
        // 状态固定为禁用，需手动启动触发业务逻辑
        entity.setState(RuleInstanceState.disable.getValue());
        entity.with(sceneRule);
        entity.setAlarmBindId(source.getId());
        return entity;
    }

    protected SceneConditionAction convertActions(JSONObject modelMeta) {
        SceneConditionAction branch = new SceneConditionAction();
        List<SceneAction> targetActions = new ArrayList<>();
        JSONArray actions = modelMeta.getJSONArray("actions");
        if (CollectionUtils.isEmpty(actions)) {
            throw new BusinessException("actions不能为空");
        }
        for (Object obj : actions) {
            if (obj instanceof JSONObject) {
                JSONObject action = (JSONObject) obj;
                SceneAction targetAction = new SceneAction();
                String executor = action.getString("executor");
                if (!StringUtils.hasText(executor)) {
                    log.error("action.executor不能为空。id: {}", modelMeta.getString("id"));
                    continue;
                }
                JSONObject configuration = action.getJSONObject("configuration");
                switch (executor) {
                    // 消息通知
                    case EXECUTOR_NOTIFIER: {
                        targetAction.setExecutor(SceneAction.Executor.notify);

                        SceneAction.Notify notify = new SceneAction.Notify();
                        notify.setNotifyType(configuration.getString("notifyType"));
                        notify.setNotifierId(configuration.getString("notifierId"));
                        notify.setTemplateId(configuration.getString("templateId"));
                        targetAction.setNotify(notify);
                        targetActions.add(targetAction);
                        break;
                    }
                    // 设备输出
                    case EXECUTOR_DEVICE: {
                        targetAction.setExecutor(SceneAction.Executor.device);

                        SceneAction.Device device = new SceneAction.Device();
                        device.setSelector("fixed");
                        SelectorValue selectorValue = SelectorValue
                                .of(configuration.getString("deviceId"), configuration.getString("deviceId"));
                        device.setSelectorValues(Collections.singletonList(selectorValue));
                        device.setProductId(configuration.getString("productId"));
                        device.setMessage(configuration.getJSONObject("message").getInnerMap());
                        targetAction.setDevice(device);
                        targetActions.add(targetAction);
                        break;
                    }
                    default: {
                        break;
                    }
                }
            }
        }
        SceneActions sceneActions = new SceneActions();
        sceneActions.setActions(targetActions);
        branch.setThen(Collections.singletonList(sceneActions));
        JSONObject shakeLimit = modelMeta.getJSONObject("shakeLimit");
        if (shakeLimit != null) {
            branch.setShakeLimit(FastBeanCopier.copy(modelMeta.getJSONObject("shakeLimit"), new Trigger.GroupShakeLimit()));
        }
        return branch;
    }

    protected DeviceOperation convertDeviceOperation(String operatorType,
                                                     String modelId,
                                                     JSONObject trigger) {
        if (!StringUtils.hasText(operatorType)) {
            throw new BusinessException("设备触发条件错误！operatorType不能为空");
        }
        DeviceOperation deviceOperation = new DeviceOperation();
        switch (operatorType) {
            case MESSAGE_TYPE_ONLINE: {
                deviceOperation.setOperator(DeviceOperation.Operator.online);
                break;
            }
            case MESSAGE_TYPE_OFFLINE: {
                deviceOperation.setOperator(DeviceOperation.Operator.offline);
                break;
            }
            case MESSAGE_TYPE_PROPERTIES: {
                String cron = trigger.getString("cron");
                if (StringUtils.hasText(cron)) {
                    // 定时读取属性
                    deviceOperation.setOperator(DeviceOperation.Operator.readProperty);
                    deviceOperation.setTimer(TimerSpec.cron(cron));
                    deviceOperation.setReadProperties(Collections.singletonList(modelId));
                } else {
                    // 属性上报
                    deviceOperation.setOperator(DeviceOperation.Operator.reportProperty);
                }

                break;
            }
            case MESSAGE_TYPE_EVENT: {
                deviceOperation.setOperator(DeviceOperation.Operator.reportEvent);
                deviceOperation.setEventId(modelId);
                break;
            }
            case MESSAGE_TYPE_FUNCTION: {
                deviceOperation.setOperator(DeviceOperation.Operator.invokeFunction);
                deviceOperation.setFunctionId(modelId);
                JSONArray parameter = trigger.getJSONArray("parameters");
                if (parameter != null) {
                    List<FunctionParameter> parameters = parameter
                            .stream()
                            .map(obj -> {
                                JSONObject json = (JSONObject) obj;
                                return new FunctionParameter(json.getString("name"), json.get("value"));
                            })
                            .collect(Collectors.toList());
                    deviceOperation.setFunctionParameters(parameters);
                } else {
                    deviceOperation.setFunctionParameters(new ArrayList<>());
                }
                deviceOperation.setTimer(TimerSpec.cron(trigger.getString("cron")));
                break;
            }
            default: {
                log.error("不支持的设备触发类型：{}", operatorType);
                return null;
            }
        }

        return deviceOperation;
    }

    protected List<Term> convertTerms(JSONObject trigger) {
        if (trigger != null) {
            JSONArray filters = trigger.getJSONArray("filters");
            if (!CollectionUtils.isEmpty(filters)) {
                return filters
                        .stream()
                        .filter(filter -> {
                            Map<String, Object> map = ((JSONObject) filter).getInnerMap();
                            return map.get("key") != null && map.get("operator") != null;
                        })
                        .map(filter -> {
                            Map<String, Object> map = ((JSONObject) filter).getInnerMap();
                            Term term = new Term();
                            String messageType = trigger.getString("type");
                            switch (messageType) {
                                case MESSAGE_TYPE_PROPERTIES: {
                                    term.setColumn("properties." + map.get("key").toString() + ".current");
                                    break;
                                }
                                case MESSAGE_TYPE_EVENT: {
                                    term.setColumn("event.data." + map.get("key").toString());
                                    break;
                                }
                                case MESSAGE_TYPE_FUNCTION: {
                                    term.setColumn("function.output");
                                    break;
                                }
                                default: {
                                    term.setColumn("properties." + map.get("key").toString() + ".last");
                                }
                            }

                            term.setTermType(map.get("operator").toString());
                            term.setValue(map.get("value"));
                            return term;
                        })
                        .collect(Collectors.toList());
            }
        }
        return null;
    }

    protected JSONObject getModelMeta(SceneSourceEntity source) {
        return JSONObject.parseObject(source.getModelMeta());
    }

    protected SceneRule createSceneRule(int index,
                                        JSONObject trigger,
                                        SceneConverterContext context) {
        SceneRule sceneRule = new SceneRule();
        // 固定规则生成id
        sceneRule.setId(DigestUtils.md5Hex(context.getId() + "|" + index));
        sceneRule.setName(context.getName());

        Boolean parallel = context.getModelMeta().getBoolean("parallel");
        sceneRule.setParallel(parallel != null && parallel);

        Trigger targetTrigger;
        try {
            List<Term> terms = convertTerms(trigger);
            context.getBranch().setWhen(terms);
            sceneRule.setBranches(Collections.singletonList(context.getBranch()));

            targetTrigger = convertTrigger(trigger, context.getModelMeta());
            if (targetTrigger == null) {
                log.error("触发条件不能为空。id: {}, modelMeta: {}\n ", context.getId(), context.getModelMeta());
                return null;
            }
            sceneRule.setTrigger(targetTrigger);
            return sceneRule;
        } catch (Exception e) {
            log.error("转换触发条件错误。id: {}, modelMeta: {}\n ", context.getId(), context.getModelMeta(), e);
        }

        return null;
    }
}
