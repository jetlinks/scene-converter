package org.jetlinks.scene.converter.entity;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Generated;
import lombok.Getter;
import lombok.Setter;

/**
 * 消息通知-配置.
 *
 * @author zhangji 2023/5/11
 */
@Getter
@Setter
@Generated
public class NotifyConfigEntity {

    private String id;

    @Schema(description = "配置名称")
    private String name;

    @Schema(description = "通知类型")
    private String type;

    @Schema(description = "服务提供商")
    private String provider;

    @Schema(description = "描述")
    private String description;

    @Schema(description = "重试策略,如: [\"1s\",\"20s\",\"5m\",\"15m\"]")
    private String retryPolicy;

    @Schema(description = "最大重试次数")
    private Integer maxRetryTimes;

    @Schema(description = "创建者ID(只读)", accessMode = Schema.AccessMode.READ_ONLY)
    private String creatorId;

    @Schema(description = "创建时间(只读)", accessMode = Schema.AccessMode.READ_ONLY)
    private Long createTime;

    @Schema(description = "配置信息")
    private String configuration;

}
