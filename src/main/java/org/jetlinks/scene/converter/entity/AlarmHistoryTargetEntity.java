package org.jetlinks.scene.converter.entity;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Generated;
import lombok.Getter;
import lombok.Setter;
import org.hswebframework.web.utils.DigestUtils;

/**
 * 输入描述.
 *
 * @author zhangji 2023/5/9
 */
@Getter
@Setter
@Generated
public class AlarmHistoryTargetEntity {

    private String id;

    @Schema(description = "告警配置ID")
    private String alarmConfigId;

    @Schema(description = "告警配置名称")
    private String alarmName;

    @Schema(description = "告警目标类型")
    private String targetType;

    @Schema(description = "告警目标Id")
    private String targetId;

    @Schema(description = "告警目标Key")
    private String targetKey;

    @Schema(description = "告警目标名称")
    private String targetName;

    @Schema(description = "告警源类型")
    private String sourceType;

    @Schema(description = "告警源Id")
    private String sourceId;

    @Schema(description = "告警源名称")
    private String sourceName;

    @Schema(description = "最近一次告警时间")
    private Long alarmTime;

    @Schema(description = "处理时间")
    private Long handleTime;

    @Schema(description = "告警级别")
    private Integer level;

    @Schema(description = "告警记录状态")
    private String state;

    @Schema(description = "说明")
    private String description;

    public String getTargetKey() {
        if (targetKey == null) {
            generateTargetKey();
        }
        return targetKey;
    }

    public void generateTargetKey() {
        setTargetKey(generateId(targetId, targetType));
    }

    public void generateId() {
        setId(generateId(targetId, targetType, alarmConfigId));
    }

    public static String generateId(String... args) {
        return DigestUtils.md5Hex(String.join("-", args));
    }


}
