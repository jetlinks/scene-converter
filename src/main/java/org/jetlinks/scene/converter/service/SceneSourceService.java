package org.jetlinks.scene.converter.service;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jetlinks.scene.converter.entity.AlarmHistorySourceEntity;
import org.jetlinks.scene.converter.entity.NotifyConfigEntity;
import org.jetlinks.scene.converter.entity.NotifyTemplateEntity;
import org.jetlinks.scene.converter.entity.Param;
import org.jetlinks.scene.converter.entity.SceneSourceEntity;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 输入描述.
 *
 * @author zhangji 2023/5/6
 */
@Slf4j
@AllArgsConstructor
public class SceneSourceService {

    private final NamedParameterJdbcTemplate namedJdbcTemplate;

    public Integer count(Param param) {
        String sql = "SELECT COUNT(1) FROM rule_instance WHERE model_type in (:modelType) ";
        Map<String, Object> paramMap = new HashMap<>();
        paramMap.put("modelType", param.getModelType());

        return namedJdbcTemplate.queryForObject(sql, paramMap, Integer.class);
    }

    public List<SceneSourceEntity> find(Param param) {
        String sql = "SELECT  " +
                "   model_version, " +
                "   create_time, " +
                "   model_type, " +
                "   name, " +
                "   creator_id, " +
                "   description, " +
                "   state, " +
                "   id, " +
                "   model_id, " +
                "   instance_detail_json, " +
                "   model_meta " +
                "FROM rule_instance " +
                "WHERE model_type in (:modelType) " +
                "ORDER BY create_time DESC, id DESC " +
                "LIMIT :limit OFFSET :offset";
        Map<String, Object> paramMap = new HashMap<>();
        paramMap.put("modelType", param.getModelType());
        paramMap.put("limit", param.getPageSize());
        paramMap.put("offset", (param.getPageIndex() - 1) * param.getPageSize());
        return namedJdbcTemplate.query(sql, paramMap, new BeanPropertyRowMapper<>(SceneSourceEntity.class));
    }

    public List<AlarmHistorySourceEntity> findDistinctAlarmHistory(Set<String> alarmId) {
        String sql = "SELECT *" +
                "FROM (" +
                "         SELECT" +
                "             alarm_name," +
                "             device_name," +
                "             device_id," +
                "             alarm_time," +
                "             product_id," +
                "             id," +
                "             alarm_id," +
                "             alarm_data," +
                "             product_name," +
                "             update_time," +
                "             description," +
                "             state," +
                "             alarm_level," +
                // 根据告警ID去重，只取最新的一条
                "             ROW_NUMBER() OVER(" +
                "                 PARTITION BY alarm_id" +
                "                 ORDER BY alarm_time DESC" +
                "                 ) AS rn" +
                "         FROM rule_dev_alarm_history" +
                "         WHERE alarm_id in (:alarmId)" +
                "     ) r " +
                "WHERE rn = 1";
        Map<String, Object> paramMap = new HashMap<>();
        paramMap.put("alarmId", alarmId);
        return namedJdbcTemplate.query(sql, paramMap, new BeanPropertyRowMapper<>(AlarmHistorySourceEntity.class));
    }

    public List<NotifyConfigEntity> findNotifyConfig(Collection<String> idList) {
        if (CollectionUtils.isEmpty(idList)) {
            return new ArrayList<>();
        }
        String sql = "SELECT  " +
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
                "FROM notify_config " +
                "WHERE id in (:idList) " +
                "ORDER BY create_time DESC, id DESC ";
        Map<String, Object> paramMap = new HashMap<>();
        paramMap.put("idList", idList);
        return namedJdbcTemplate.query(sql, paramMap, new BeanPropertyRowMapper<>(NotifyConfigEntity.class));
    }

    public List<NotifyTemplateEntity> findNotifyTemplate(Collection<String> idList) {
        if (CollectionUtils.isEmpty(idList)) {
            return new ArrayList<>();
        }
        String sql = "SELECT  " +
                "   template, " +
                "   variable_definitions, " +
                "   create_time, " +
                "   provider, " +
                "   creator_id, " +
                "   name, " +
                "   id, " +
                "   type " +
                "FROM notify_template " +
                "WHERE id in (:idList) " +
                "ORDER BY create_time DESC, id DESC ";
        Map<String, Object> paramMap = new HashMap<>();
        paramMap.put("idList", idList);
        return namedJdbcTemplate.query(sql, paramMap, new BeanPropertyRowMapper<>(NotifyTemplateEntity.class));
    }

}
