package org.ruoyi.graph.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

/**
 * 图谱检索响应
 *
 * @author ruoyi
 * @date 2025-01-04
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class GraphRetrieveResponse implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 检索结果文本内容
     */
    private String content;

    /**
     * 相关实体列表
     */
    private List<EntityVO> relevantEntities;

    /**
     * 相关关系列表
     */
    private List<RelationVO> relevantRelations;
}

