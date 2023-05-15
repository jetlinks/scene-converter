package org.jetlinks.scene.converter.service;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jetlinks.pro.rule.engine.entity.AlarmRuleBindEntity;
import org.jetlinks.scene.converter.entity.AlarmConfigTargetEntity;
import org.jetlinks.scene.converter.entity.AlarmHistoryTargetEntity;
import org.jetlinks.scene.converter.entity.NotifyConfigEntity;
import org.jetlinks.scene.converter.entity.NotifyTemplateEntity;
import org.jetlinks.scene.converter.entity.SceneTargetEntity;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.jdbc.core.namedparam.SqlParameterSourceUtils;
import org.springframework.util.CollectionUtils;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 输入描述.
 *
 * @author zhangji 2023/5/6
 */
@Slf4j
@AllArgsConstructor
public class SceneTargetService {

    private final NamedParameterJdbcTemplate namedJdbcTemplate;

    public void deleteExists(String table,
                             List<String> id) {
        String sql = "DELETE from " + table + " WHERE id in (:id)";
        Map<String, Object> paramMap = new HashMap<>();
        paramMap.put("table", table);
        paramMap.put("id", id);
        int deleted = namedJdbcTemplate.update(sql, paramMap);
        log.info("已删除 {} 表中 {} 条重复Id的数据。\nid：{}", table, deleted, id);
    }

    public int[] batchAddRuleScene(Collection<SceneTargetEntity> list) {
        if (CollectionUtils.isEmpty(list)) {
            return new int[0];
        }

        this.deleteExists(
                "rule_scene", list.stream().map(SceneTargetEntity::getId).collect(Collectors.toList())
        );

        String sql = "INSERT INTO rule_scene ( " +
                "   creator_id, " +
                "   modifier_id, " +
                "   description, " +
                "   trigger, " +
                "   branches, " +
                "   modify_time, " +
                "   create_time, " +
                "   parallel, " +
                "   terms, " +
                "   name, " +
                "   options, " +
                "   start_time, " +
                "   id, " +
                "   state, " +
                "   trigger_type, " +
                "   actions " +
                ") VALUES (" +
                "   :creatorId, " +
                "   :modifierId, " +
                "   :description, " +
                "   :trigger, " +
                "   :branches, " +
                "   :modifyTime, " +
                "   :createTime, " +
                "   :parallel, " +
                "   :terms, " +
                "   :name, " +
                "   :options, " +
                "   :startTime, " +
                "   :id, " +
                "   :state, " +
                "   :triggerType, " +
                "   :actions " +
                ") ";
        SqlParameterSource[] beanSources = SqlParameterSourceUtils.createBatch(list.toArray());
        return namedJdbcTemplate.batchUpdate(sql, beanSources);
    }

    public int[] batchAddAlarmConfig(Collection<AlarmConfigTargetEntity> list) {

        if (CollectionUtils.isEmpty(list)) {
            return new int[0];
        }

        this.deleteExists(
                "alarm_config", list.stream().map(AlarmConfigTargetEntity::getId).collect(Collectors.toList())
        );

        String sql = "INSERT INTO alarm_config ( " +
                "   level, " +
                "   creator_id, " +
                "   modifier_id, " +
                "   description, " +
                "   target_type, " +
                "   modify_time, " +
                "   create_time, " +
                "   name, " +
                "   id, " +
                "   state " +
                ") VALUES (" +
                "   :level, " +
                "   :creatorId, " +
                "   :modifierId, " +
                "   :description, " +
                "   :targetType, " +
                "   :modifyTime, " +
                "   :createTime, " +
                "   :name, " +
                "   :id, " +
                "   :state " +
                ") ";
        SqlParameterSource[] beanSources = SqlParameterSourceUtils.createBatch(list.toArray());
        return namedJdbcTemplate.batchUpdate(sql, beanSources);
    }

    public int[] batchAddAlarmBind(Collection<AlarmRuleBindEntity> list) {

        if (CollectionUtils.isEmpty(list)) {
            return new int[0];
        }

        this.deleteExists(
                "s_alarm_rule_bind", list.stream().map(AlarmRuleBindEntity::getId).collect(Collectors.toList())
        );

        String sql = "INSERT INTO s_alarm_rule_bind ( " +
                "   alarm_id, " +
                "   branch_index, " +
                "   id, " +
                "   rule_id " +
                ") VALUES (" +
                "   :alarmId, " +
                "   :branchIndex, " +
                "   :id, " +
                "   :ruleId " +
                ") ";
        SqlParameterSource[] beanSources = SqlParameterSourceUtils.createBatch(list.toArray());
        return namedJdbcTemplate.batchUpdate(sql, beanSources);
    }

    public int[] batchAddAlarmRecord(Collection<AlarmHistoryTargetEntity> list) {

        if (CollectionUtils.isEmpty(list)) {
            return new int[0];
        }

        this.deleteExists(
                "alarm_record", list.stream().map(AlarmHistoryTargetEntity::getId).collect(Collectors.toList())
        );

        String sql = "INSERT INTO alarm_record ( " +
                "   source_id, " +
                "   target_name, " +
                "   handle_time, " +
                "   target_id, " +
                "   level, " +
                "   alarm_time, " +
                "   description, " +
                "   alarm_name, " +
                "   target_type, " +
                "   target_key, " +
                "   alarm_config_id, " +
                "   source_type, " +
                "   id, " +
                "   source_name, " +
                "   state " +
                ") VALUES (" +
                "   :sourceId, " +
                "   :targetName, " +
                "   :handleTime, " +
                "   :targetId, " +
                "   :level, " +
                "   :alarmTime, " +
                "   :description, " +
                "   :alarmName, " +
                "   :targetType, " +
                "   :targetKey, " +
                "   :alarmConfigId, " +
                "   :sourceType, " +
                "   :id, " +
                "   :sourceName, " +
                "   :state " +
                ") ";
        SqlParameterSource[] beanSources = SqlParameterSourceUtils.createBatch(list.toArray());
        return namedJdbcTemplate.batchUpdate(sql, beanSources);
    }

    public int[] batchAddNotifyConfig(Collection<NotifyConfigEntity> list) {

        if (CollectionUtils.isEmpty(list)) {
            return new int[0];
        }

        this.deleteExists(
                "notify_config", list.stream().map(NotifyConfigEntity::getId).collect(Collectors.toList())
        );

        String sql = "INSERT INTO notify_config ( " +
                "   retry_policy, " +
                "   configuration, " +
                "   create_time, " +
                "   provider, " +
                "   creator_id, " +
                "   name, " +
                "   description, " +
                "   id, " +
                "   type, " +
                "   max_retry_times " +
                ") VALUES (" +
                "   :retryPolicy, " +
                "   :configuration, " +
                "   :createTime, " +
                "   :provider, " +
                "   :creatorId, " +
                "   :name, " +
                "   :description, " +
                "   :id, " +
                "   :type, " +
                "   :maxRetryTimes " +
                ") ";
        SqlParameterSource[] beanSources = SqlParameterSourceUtils.createBatch(list.toArray());
        return namedJdbcTemplate.batchUpdate(sql, beanSources);
    }

    public int[] batchAddNotifyTemplate(Collection<NotifyTemplateEntity> list) {

        if (CollectionUtils.isEmpty(list)) {
            return new int[0];
        }

        this.deleteExists(
                "notify_template", list.stream().map(NotifyTemplateEntity::getId).collect(Collectors.toList())
        );

        String sql = "INSERT INTO notify_template ( " +
                "   template, " +
                "   variable_definitions, " +
                "   create_time, " +
                "   provider, " +
                "   creator_id, " +
                "   name, " +
                "   id, " +
                "   type " +
                ") VALUES (" +
                "   :template, " +
                "   :variableDefinitions, " +
                "   :createTime, " +
                "   :provider, " +
                "   :creatorId, " +
                "   :name, " +
                "   :id, " +
                "   :type " +
                ") ";
        SqlParameterSource[] beanSources = SqlParameterSourceUtils.createBatch(list.toArray());
        return namedJdbcTemplate.batchUpdate(sql, beanSources);
    }

}
