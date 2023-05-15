package org.jetlinks.scene.converter.entity;

import com.alibaba.fastjson.JSONObject;
import lombok.Generated;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.collections.CollectionUtils;
import org.hswebframework.web.bean.FastBeanCopier;
import org.jetlinks.pro.rule.engine.scene.SceneRule;

/**
 * 输入描述.
 *
 * @author zhangji 2023/5/6
 */
@Getter
@Setter
@Generated
public class SceneTargetEntity {

    private String id;

    // 告警名称
    private String name;

    // 触发器类型
    private String triggerType;

    // 触发器
    private String trigger;

    // 触发条件
    private String terms;

    // 是否并行执行动作
    private Boolean parallel;

    // 执行动作
    private String actions;

    // 动作分支
    private String branches;

    // 创建人
    private String creatorId;

    // 创建时间
    private Long createTime;

    // 修改人")
    private String modifierId;

    // 修改时间
    private Long modifyTime;

    // 启动时间
    private Long startTime;

    // 状态
    private String state;

    // 扩展配置
    private String options;

    // 说明
    private String description;

    /*告警配置信息，无需存储*/

    // 告警ID（用于绑定告警）
    private String alarmBindId;

    // 告警对象类型
    private String targetType;

    // 告警对象ID
    private String targetId;

    // 告警对象名称
    private String targetName;

    // 告警级别
    private Integer level = 0;

    public SceneTargetEntity with(SceneRule sceneRule) {
        sceneRule.validate();
        sceneRule.getTrigger().validate();
        if (CollectionUtils.isEmpty(sceneRule.getActions()) && CollectionUtils.isEmpty(sceneRule.getBranches())) {
            throw new NullPointerException("执行动作不能为空");
        }

        FastBeanCopier.copy(sceneRule, this, "trgger", "actions", "terms", "trggerType");
        setTrigger(JSONObject.toJSONString(sceneRule.getTrigger()));
        setBranches(JSONObject.toJSONString(sceneRule.getBranches()));
        setTerms(CollectionUtils.isEmpty(sceneRule.getTerms()) ? null : JSONObject.toJSONString(sceneRule.getTerms()));
        setTriggerType(sceneRule.getTrigger().getType().getValue());
        return this;
    }

}
