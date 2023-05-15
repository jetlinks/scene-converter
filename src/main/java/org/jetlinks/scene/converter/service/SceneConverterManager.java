package org.jetlinks.scene.converter.service;

import com.alibaba.fastjson.JSONObject;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.hswebframework.web.bean.FastBeanCopier;
import org.hswebframework.web.utils.DigestUtils;
import org.jetlinks.pro.rule.engine.entity.AlarmRuleBindEntity;
import org.jetlinks.pro.rule.engine.enums.AlarmRecordState;
import org.jetlinks.pro.rule.engine.enums.AlarmState;
import org.jetlinks.pro.rule.engine.scene.SceneAction;
import org.jetlinks.pro.rule.engine.scene.SceneConditionAction;
import org.jetlinks.scene.converter.entity.AlarmConfigTargetEntity;
import org.jetlinks.scene.converter.entity.AlarmHistorySourceEntity;
import org.jetlinks.scene.converter.entity.AlarmHistoryTargetEntity;
import org.jetlinks.scene.converter.entity.NotifyConfigEntity;
import org.jetlinks.scene.converter.entity.NotifyTemplateEntity;
import org.jetlinks.scene.converter.entity.Param;
import org.jetlinks.scene.converter.entity.SceneSourceEntity;
import org.jetlinks.scene.converter.entity.SceneTargetEntity;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 转换管理器.
 *
 * @author zhangji 2023/5/6
 */
@Slf4j
@AllArgsConstructor
public class SceneConverterManager {

    private static final Map<String, SceneConverter> converterMap = new HashMap<>();

    private final SceneSourceService sourceService;

    private final SceneTargetService targetService;

    public void register(SceneConverter converter) {
        converterMap.put(converter.getType(), converter);
    }

    /**
     * 查询并转换所有的来源数据
     */
    public void convertAll(Param param) {
        // 默认转换所有支持的类型
        if (CollectionUtils.isEmpty(param.getModelType())) {
            param.setModelType(converterMap.values().stream().map(SceneConverter::getType).collect(Collectors.toList()));
        }

        int total = sourceService.count(param);
        log.info("查询场景联动数量为：{}", total);
        if (total == 0) {
            return;
        }
        int converted = 0;

        // 分页查询并转换
        do {
            List<SceneSourceEntity> sourceList = sourceService.find(param);
            doConvert(sourceList);

            converted += sourceList.size();
            param.nextPage();
        } while (converted < total);
    }

    public void doConvert(List<SceneSourceEntity> sourceList) {
        if (CollectionUtils.isEmpty(sourceList)) {
            return;
        }

        // 将1.0告警转换为场景联动
        List<SceneTargetEntity> targetList = new ArrayList<>();
        for (SceneSourceEntity source : sourceList) {
            SceneConverter converter = converterMap.get(source.getModelType());
            if (converter == null) {
                log.error("不支持的场景联动类型：{}", source.getModelType());
                continue;
            }

            targetList.addAll(converter.convertScene(source));
        }

        if (!CollectionUtils.isEmpty(targetList)) {
            // 按告警分组
            Map<String, List<SceneTargetEntity>> sceneGroupMap = targetList
                    .stream()
                    .collect(Collectors.groupingBy(SceneTargetEntity::getAlarmBindId));

            List<AlarmConfigTargetEntity> alarmList = new ArrayList<>();
            List<AlarmRuleBindEntity> bindList = new ArrayList<>();

            sceneGroupMap.forEach((alarmId, sceneList) -> {
                // 创建告警配置
                AlarmConfigTargetEntity alarm = createAlarm(alarmId, sceneList.get(0));
                alarmList.add(alarm);

                for (SceneTargetEntity rule : sceneList) {
                    // 创建告警的场景联动绑定信息
                    AlarmRuleBindEntity bind = createBind(alarmId, rule);
                    bindList.add(bind);
                }
            });

            // 查询并转换告警记录
            List<AlarmHistorySourceEntity> alarmHistory = sourceService.findDistinctAlarmHistory(sceneGroupMap.keySet());
            List<AlarmHistoryTargetEntity> alarmRecordList = convertAlarmHistory(alarmHistory, sceneGroupMap);

            // 保存场景联动
            targetService.batchAddRuleScene(targetList);
            // 保存告警
            targetService.batchAddAlarmConfig(alarmList);
            // 保存绑定信息
            targetService.batchAddAlarmBind(bindList);
            // 保存告警记录
            targetService.batchAddAlarmRecord(alarmRecordList);
            // 保存通知配置和通知模板
            copyNotify(targetList);
        }

    }

    private List<AlarmHistoryTargetEntity> convertAlarmHistory(List<AlarmHistorySourceEntity> alarmHistory,
                                                               Map<String, List<SceneTargetEntity>> sceneGroupMap) {
        return alarmHistory
                .stream()
                .map(history -> {
                    SceneTargetEntity sceneTargetEntity = sceneGroupMap.get(history.getAlarmId()).get(0);

                    AlarmHistoryTargetEntity record = FastBeanCopier
                            .copy(sceneTargetEntity, new AlarmHistoryTargetEntity(), "state");
                    FastBeanCopier.copy(history, record);
                    record.setAlarmConfigId(history.getAlarmId());
                    record.setHandleTime(history.getUpdateTime().getTime());
                    record.setState("solve".equals(history.getState()) ?
                            AlarmRecordState.normal.getValue() : AlarmRecordState.warning.getValue());
                    record.generateTargetKey();

                    record.generateId();
                    return record;
                })
                .collect(Collectors.toList());
    }

    private AlarmConfigTargetEntity createAlarm(String alarmId,
                                                SceneTargetEntity scene) {
        AlarmConfigTargetEntity alarm = FastBeanCopier.copy(scene, new AlarmConfigTargetEntity());
        alarm.setId(alarmId);
        alarm.setTargetType(scene.getTargetType());
        alarm.setLevel(scene.getLevel());
        alarm.setState("started".equals(scene.getState()) ? AlarmState.enabled.getValue() : AlarmState.disabled.getValue());
        return alarm;
    }

    private AlarmRuleBindEntity createBind(String alarmId,
                                           SceneTargetEntity rule) {
        AlarmRuleBindEntity bind = new AlarmRuleBindEntity();
        bind.setId(DigestUtils.md5Hex(alarmId + "|" + rule.getId()));
        bind.setAlarmId(alarmId);
        bind.setRuleId(rule.getId());
        bind.setBranchIndex(AlarmRuleBindEntity.ANY_BRANCH_INDEX);
        return bind;
    }

    private void copyNotify(List<SceneTargetEntity> targetList) {
        if (CollectionUtils.isEmpty(targetList)) {
            return;
        }
        Set<String> configIds = new HashSet<>();
        Set<String> templateIds = new HashSet<>();

        targetList
                .stream()
                .flatMap(entity -> {
                    List<SceneConditionAction> branches = JSONObject.parseArray(entity.getBranches(), SceneConditionAction.class);
                    return branches.stream();
                })
                .flatMap(branch -> branch.getThen().stream())
                .flatMap(then -> then.getActions().stream())
                .filter(action -> SceneAction.Executor.notify == action.getExecutor())
                .forEach(action -> {
                    configIds.add(action.getNotify().getNotifierId());
                    templateIds.add(action.getNotify().getTemplateId());
                });

        List<NotifyConfigEntity> config = sourceService.findNotifyConfig(configIds);
        targetService.batchAddNotifyConfig(config);

        List<NotifyTemplateEntity> template = sourceService.findNotifyTemplate(templateIds);
        targetService.batchAddNotifyTemplate(template);
    }

}
