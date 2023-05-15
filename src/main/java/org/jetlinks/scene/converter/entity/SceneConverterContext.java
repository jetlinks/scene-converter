package org.jetlinks.scene.converter.entity;

import com.alibaba.fastjson.JSONObject;
import lombok.AllArgsConstructor;
import lombok.Generated;
import lombok.Getter;
import lombok.Setter;
import org.jetlinks.pro.rule.engine.scene.SceneConditionAction;

/**
 * 数据转换-上下文.
 *
 * @author zhangji 2023/5/10
 */
@Getter
@Setter
@AllArgsConstructor
@Generated
public class SceneConverterContext {

    private String id;

    private String name;

    private JSONObject modelMeta;

    private SceneConditionAction branch;

}
