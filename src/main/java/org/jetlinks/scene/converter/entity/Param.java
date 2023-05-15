package org.jetlinks.scene.converter.entity;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * 请求参数.
 *
 * @author zhangji 2023/5/6
 */
@Getter
@Setter
public class Param {

    public static final int PAGE_SIZE = 100;

    private List<String> modelType;

    private int pageIndex = 1;

    private int pageSize = PAGE_SIZE;

    public void nextPage() {
        pageIndex = pageIndex + 1;
    }

}
