package org.jetlinks.scene.converter.entity;

import lombok.Generated;
import lombok.Getter;
import lombok.Setter;
import org.springframework.util.StringUtils;

import java.sql.JDBCType;

/**
 * 输入描述.
 *
 * @author zhangji 2023/5/6
 */
@Getter
@Setter
@Generated
public class SceneSourceEntity {

    // ID
    private String id;

    // 模型ID
    private String modelId;
    
    // 名称
    private String name;

    // 说明
    private String description;

    // 规则类型
    private String modelType;

    // 规则模型配置,不同的类型配置不同.
    private String modelMeta;

    // 版本
    private Integer modelVersion;

    // 创建时间
    private Long createTime;

    // 创建者ID
    private String creatorId;

    // 状态
    private String state;
    
}
