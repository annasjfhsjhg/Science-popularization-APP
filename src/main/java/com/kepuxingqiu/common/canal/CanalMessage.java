package com.kepuxingqiu.common.canal;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import java.util.List;
import java.util.Map;

/**
 * Canal 通过 RabbitMQ 推送的 binlog 变更消息结构（flat JSON 模式）。
 * Canal 只在 old 中放发生变化的字段，data 中始终包含该行所有字段的最新值。
 */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class CanalMessage {

    /** 操作类型：INSERT / UPDATE / DELETE */
    private String type;

    private String database;

    private String table;

    /** true 表示 DDL 语句，忽略即可 */
    private boolean isDdl;

    /** 变更后的完整行数据（UPDATE/INSERT），或被删除的行数据（DELETE） */
    private List<Map<String, Object>> data;

    /** UPDATE 时旧值（仅包含发生变化的字段）；INSERT/DELETE 时为 null */
    private List<Map<String, Object>> old;
}
