package org.jetlinks.scene.converter.service;

import org.jetlinks.scene.converter.entity.SceneSourceEntity;
import org.jetlinks.scene.converter.entity.SceneTargetEntity;

import java.util.List;

/**
 * 转换器.
 *
 * @author zhangji 2023/5/9
 */
public interface SceneConverter {

    /**
     * @return 来源类型
     */
    String getType();

    /**
     * 将来源数据转换为场景联动
     *
     * @param source 来源数据（告警配置或场景联动）
     * @return 场景联动数据实体
     */
    List<SceneTargetEntity> convertScene(SceneSourceEntity source);

}
