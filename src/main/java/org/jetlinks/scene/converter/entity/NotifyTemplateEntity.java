package org.jetlinks.scene.converter.entity;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Generated;
import lombok.Getter;
import lombok.Setter;
import org.hswebframework.ezorm.rdb.mapping.annotation.ColumnType;
import org.hswebframework.ezorm.rdb.mapping.annotation.DefaultValue;
import org.hswebframework.ezorm.rdb.mapping.annotation.JsonCodec;
import org.hswebframework.web.crud.generator.Generators;
import org.hswebframework.web.validator.CreateGroup;

import javax.persistence.Column;
import javax.persistence.GeneratedValue;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;
import java.sql.JDBCType;
import java.util.List;
import java.util.Map;

/**
 * 消息通知-模板.
 *
 * @author zhangji 2023/5/11
 */
@Getter
@Setter
@Generated
public class NotifyTemplateEntity {

    private String id;

    @Schema(description = "通知类型ID")
    private String type;

    @Schema(description = "通知服务商ID")
    private String provider;

    @Schema(description = "模版名称")
    private String name;

    @Schema(description = "模版内容(根据服务商不同而不同)")
    private String template;

    @Schema(description = "创建者ID(只读)", accessMode = Schema.AccessMode.READ_ONLY)
    private String creatorId;

    @Schema(description = "创建时间(只读)", accessMode = Schema.AccessMode.READ_ONLY)
    private Long createTime;

    @Schema(description = "变量定义")
    private String variableDefinitions;

//    @Schema(description = "通知配置ID")
//    private String configId;

    @Schema(description = "说明")
    private String description;

}
