package org.ruoyi.graph.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;

/**
 * 关系视图对象
 *
 * @author ruoyi
 * @date 2025-01-04
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RelationVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 关系ID
     */
    private String edgeId;

    /**
     * 源节点ID
     */
    private String sourceNodeId;

    /**
     * 目标节点ID
     */
    private String targetNodeId;

    /**
     * 关系标签（类型）
     */
    private String label;

    /**
     * 置信度
     */
    private Double confidence;
}

