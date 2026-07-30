package com.lune.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("visit_log")
public class VisitLog {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String ip;
    private String country;
    private String province;
    private String city;
    // 经纬度来自 GeoLite2 的城市中心点，目前仅入库、无接口读取：后台地图按省份聚合
    // （ChinaRegion 归一化后的 province），并不需要坐标。留着是为了以后要做散点地图
    // 时不必回补历史数据 —— 精度只到城市级，不足以定位到人。
    private BigDecimal longitude;
    private BigDecimal latitude;
    private String userAgent;
    private String path;
    private String method;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}
