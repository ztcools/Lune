package com.lune.common;

import java.util.Map;

/**
 * GeoLite2 行政区名称 → ECharts 中国地图 (lune-ui/public/maps/china.json) 名称的归一化。
 *
 * <p>为什么需要这个类：GeoLite2 的 zh-CN 省级名称并不统一，也与地图的 name 字段不一致。
 * 实测同一个库里同时存在：
 * <pre>
 *   浙江 / 广东 / 内蒙古 / 新疆   ← 无后缀
 *   上海市 / 北京市 / 重庆市      ← 带「市」
 *   河北省 / 福建省               ← 带「省」
 * </pre>
 * 而 china.json 统一使用全称（浙江省 / 广东省 / 内蒙古自治区 …）。
 * 若直接把 GeoLite2 的原始名称存库再 GROUP BY 喂给地图，绝大多数省份都无法着色。
 *
 * <p>港澳台另有特殊之处：GeoLite2 中香港/澳门的 subdivision 是「中西區 / Kwun Tong District」
 * 之类的区级名称，台湾则被拆成「台北市 / 高雄市 / 苗栗縣」等市县，都不是省级名。
 * 因此这些地区一律按 country ISO 码直接归到对应的省级单位，不去枚举下辖区县。
 *
 * <p>归一化在写入时完成，库里存的就是规范名称，统计接口可以直接 GROUP BY province。
 */
public final class ChinaRegion {

    private ChinaRegion() {}

    public static final String UNKNOWN = "未知";

    /** 省级核心名（去掉省/市/自治区后缀）→ china.json 全称 */
    private static final Map<String, String> CORE_TO_FULL = Map.ofEntries(
        Map.entry("北京", "北京市"),
        Map.entry("天津", "天津市"),
        Map.entry("河北", "河北省"),
        Map.entry("山西", "山西省"),
        Map.entry("内蒙古", "内蒙古自治区"),
        Map.entry("辽宁", "辽宁省"),
        Map.entry("吉林", "吉林省"),
        Map.entry("黑龙江", "黑龙江省"),
        Map.entry("上海", "上海市"),
        Map.entry("江苏", "江苏省"),
        Map.entry("浙江", "浙江省"),
        Map.entry("安徽", "安徽省"),
        Map.entry("福建", "福建省"),
        Map.entry("江西", "江西省"),
        Map.entry("山东", "山东省"),
        Map.entry("河南", "河南省"),
        Map.entry("湖北", "湖北省"),
        Map.entry("湖南", "湖南省"),
        Map.entry("广东", "广东省"),
        Map.entry("广西", "广西壮族自治区"),
        Map.entry("海南", "海南省"),
        Map.entry("重庆", "重庆市"),
        Map.entry("四川", "四川省"),
        Map.entry("贵州", "贵州省"),
        Map.entry("云南", "云南省"),
        Map.entry("西藏", "西藏自治区"),
        Map.entry("陕西", "陕西省"),
        Map.entry("甘肃", "甘肃省"),
        Map.entry("青海", "青海省"),
        Map.entry("宁夏", "宁夏回族自治区"),
        Map.entry("新疆", "新疆维吾尔自治区"),
        Map.entry("台湾", "台湾省"),
        Map.entry("香港", "香港特别行政区"),
        Map.entry("澳门", "澳门特别行政区")
    );

    /**
     * GeoLite2 英文省名 → china.json 全称。
     * 当 zh-CN 名称缺失时 GeoIpService 会退回英文名，故必须同时支持。
     * 注意 Shanxi(山西) 与 Shaanxi(陕西) 拼写极近，切勿写反。
     */
    private static final Map<String, String> EN_TO_FULL = Map.ofEntries(
        Map.entry("beijing", "北京市"),
        Map.entry("tianjin", "天津市"),
        Map.entry("hebei", "河北省"),
        Map.entry("shanxi", "山西省"),
        Map.entry("inner mongolia", "内蒙古自治区"),
        Map.entry("nei mongol", "内蒙古自治区"),
        Map.entry("liaoning", "辽宁省"),
        Map.entry("jilin", "吉林省"),
        Map.entry("heilongjiang", "黑龙江省"),
        Map.entry("shanghai", "上海市"),
        Map.entry("jiangsu", "江苏省"),
        Map.entry("zhejiang", "浙江省"),
        Map.entry("anhui", "安徽省"),
        Map.entry("fujian", "福建省"),
        Map.entry("jiangxi", "江西省"),
        Map.entry("shandong", "山东省"),
        Map.entry("henan", "河南省"),
        Map.entry("hubei", "湖北省"),
        Map.entry("hunan", "湖南省"),
        Map.entry("guangdong", "广东省"),
        Map.entry("guangxi", "广西壮族自治区"),
        Map.entry("hainan", "海南省"),
        Map.entry("chongqing", "重庆市"),
        Map.entry("sichuan", "四川省"),
        Map.entry("guizhou", "贵州省"),
        Map.entry("yunnan", "云南省"),
        Map.entry("tibet", "西藏自治区"),
        Map.entry("xizang", "西藏自治区"),
        Map.entry("shaanxi", "陕西省"),
        Map.entry("gansu", "甘肃省"),
        Map.entry("qinghai", "青海省"),
        Map.entry("ningxia", "宁夏回族自治区"),
        Map.entry("xinjiang", "新疆维吾尔自治区")
    );

    /**
     * 把 GeoLite2 的省级名称归一化为 china.json 中的全称。
     *
     * @param countryIso 国家 ISO 码（CN/HK/MO/TW），用于处理港澳台
     * @param rawName    GeoLite2 返回的 subdivision 名称，可能为中文、英文或 null
     * @return china.json 中的省级全称；无法判定时返回 {@link #UNKNOWN}
     */
    public static String normalizeProvince(String countryIso, String rawName) {
        // 港澳台：GeoLite2 给的是区/市级名称，直接按国家码归到省级单位
        if (countryIso != null) {
            switch (countryIso.toUpperCase()) {
                case "HK": return "香港特别行政区";
                case "MO": return "澳门特别行政区";
                case "TW": return "台湾省";
                default: break;
            }
        }
        if (rawName == null || rawName.isBlank()) return UNKNOWN;
        String name = rawName.trim();

        // 已经是全称，直接采用
        if (CORE_TO_FULL.containsValue(name)) return name;

        // 中文：去掉行政级别后缀再查表（覆盖「浙江」与「河北省」两种写法）
        String core = stripSuffix(name);
        String hit = CORE_TO_FULL.get(core);
        if (hit != null) return hit;

        // 英文回退
        hit = EN_TO_FULL.get(name.toLowerCase());
        if (hit != null) return hit;

        return UNKNOWN;
    }

    /** 去掉省级行政级别后缀，注意先长后短，避免「自治区」被「区」截断 */
    private static String stripSuffix(String name) {
        String[] suffixes = {
            "维吾尔自治区", "回族自治区", "壮族自治区", "特别行政区", "自治区", "省", "市"
        };
        for (String s : suffixes) {
            if (name.length() > s.length() && name.endsWith(s)) {
                return name.substring(0, name.length() - s.length());
            }
        }
        return name;
    }
}
