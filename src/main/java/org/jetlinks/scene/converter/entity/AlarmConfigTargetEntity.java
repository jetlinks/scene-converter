package org.jetlinks.scene.converter.entity;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Generated;
import lombok.Getter;
import lombok.Setter;

/**
 * 输入描述.
 *
 * @author zhangji 2023/5/9
 */
@Getter
@Setter
@Generated
public class AlarmConfigTargetEntity {

    private String id;

    @Schema(description = "名称")
    private String name;

    @Schema(description = "告警目标类型")
    private String targetType;

    @Schema(description = "告警级别")
    private Integer level;

    @Schema(description = "状态")
    private String state;

    @Schema(description = "说明")
    private String description;

    @Schema(description = "创建者ID(只读)", accessMode = Schema.AccessMode.READ_ONLY)
    private String creatorId;

    @Schema(description = "创建时间(只读)", accessMode = Schema.AccessMode.READ_ONLY)
    private Long createTime;

    @Schema(description = "更新者ID", accessMode = Schema.AccessMode.READ_ONLY)
    private String modifierId;

    @Schema(description = "更新时间")
    private Long modifyTime;

}
