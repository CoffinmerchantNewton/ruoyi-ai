package org.ruoyi.graph.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;

/**
 * 实体视图对象
 *
 * @author ruoyi
 * @date 2025-01-04
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class EntityVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 节点ID
     */
    private String nodeId;

    /**
     * 实体名称
     */
    private String name;

    /**
     * 实体标签（类型）
     */
    private String label;

    /**
     * 描述
     */
    private String description;

    /**
     * 置信度
     */
    private Double confidence;
}

