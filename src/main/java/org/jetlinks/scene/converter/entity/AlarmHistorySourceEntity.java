package org.jetlinks.scene.converter.entity;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Generated;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;

/**
 * 输入描述.
 *
 * @author zhangji 2023/5/9
 */
@Getter
@Setter
@Generated
public class AlarmHistorySourceEntity {

    @Schema(description = "ID")
    private String id;

    @Schema(description = "产品ID")
    private String productId;

    @Schema(description = "产品名称")
    private String productName;

    @Schema(description = "设备ID")
    private String deviceId;

    @Schema(description = "设备名称")
    private String deviceName;

    @Schema(description = "告警ID")
    private String alarmId;

    @Schema(description = "告警名称")
    private String alarmName;

    @Schema(description = "告警时间")
    private Date alarmTime;

    @Schema(description = "告警数据")
    private String alarmData;

    @Schema(description = "状态")
    private String state;

    @Schema(description = "修改时间")
    private Date updateTime;

    @Schema(description = "说明", maxLength = 2000)
    private String description;

    @Schema(description = "告警级别")
    private Integer alarmLevel;
}
