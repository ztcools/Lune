package com.lune.config;

import com.lune.entity.Category;
import com.lune.entity.Project;
import com.lune.entity.SiteConfig;
import com.lune.entity.User;
import com.lune.entity.WorkExperience;
import com.lune.mapper.CategoryMapper;
import com.lune.mapper.ProjectMapper;
import com.lune.mapper.SiteConfigMapper;
import com.lune.mapper.UserMapper;
import com.lune.mapper.WorkExperienceMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

@Component
public class DataInitializer implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataInitializer.class);

    private final UserMapper userMapper;
    private final CategoryMapper categoryMapper;
    private final SiteConfigMapper siteConfigMapper;
    private final WorkExperienceMapper workExperienceMapper;
    private final ProjectMapper projectMapper;
    private final PasswordEncoder passwordEncoder;

    @Value("${app.admin.default-password:admin123}")
    private String defaultAdminPassword;

    public DataInitializer(UserMapper userMapper, CategoryMapper categoryMapper,
                           SiteConfigMapper siteConfigMapper, WorkExperienceMapper workExperienceMapper,
                           ProjectMapper projectMapper, PasswordEncoder passwordEncoder) {
        this.userMapper = userMapper;
        this.categoryMapper = categoryMapper;
        this.siteConfigMapper = siteConfigMapper;
        this.workExperienceMapper = workExperienceMapper;
        this.projectMapper = projectMapper;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        if (userMapper.selectCount(null) == 0) {
            var admin = new User();
            admin.setUsername("admin");
            admin.setPassword(passwordEncoder.encode(defaultAdminPassword));
            admin.setNickname("Lune");
            admin.setRole("ADMIN");
            admin.setStatus(1);
            userMapper.insert(admin);
            log.info("默认管理员已创建: admin / (请通过环境变量 ADMIN_DEFAULT_PASSWORD 修改默认密码)");
        }
        if (categoryMapper.selectCount(null) == 0) {
            String[][] cats = {{"技术", "技术相关文章", "article"}, {"生活", "生活随笔", "article"},
                {"学习", "学习笔记", "record"}, {"游戏", "游戏记录", "record"},
                {"旅游", "旅游记录", "record"}, {"美食", "美食记录", "record"},
                {"音乐", "音乐收藏", "record"}};
            for (int i = 0; i < cats.length; i++) {
                var c = new Category();
                c.setName(cats[i][0]);
                c.setDescription(cats[i][1]);
                c.setType(cats[i][2]);
                c.setSortOrder(i + 1);
                c.setStatus(1);
                categoryMapper.insert(c);
            }
        }
        // 站点配置：只插入缺失的 key（幂等，适合现有库升级）
        String[][] configs = {
            {"site_name", "Lune", "public", "网站名称"},
            {"site_title", "Lune - 记录美好生活", "public", "网站标题"},
            {"site_description", "个人博客，记录成长，分享生活", "public", "网站描述"},
            {"site_footer", "© 2024 Lune. All Rights Reserved.", "public", "页脚信息"},
            {"beian_icp", "", "public", "ICP备案号（Landing页脚展示）"},
            {"notices", "[\"欢迎来到 Lune！\"]", "public", "网站公告"},
            {"enable_register", "true", "public", "是否开放注册"},
            {"enable_comment", "true", "public", "是否开放评论"},
            {"landing_bg", "[]", "public", "Landing页背景图"},
            {"home_hero_bg", "[]", "public", "首页顶部背景图"},
            {"home_content_bg", "[]", "public", "首页内容区背景图"},
            {"family_hero_bg", "[]", "public", "家页顶部背景图"},
            {"family_content_bg", "[]", "public", "家页内容区背景图"},
            {"treehole_danmaku_bg", "[]", "public", "树洞弹幕背景图"},
            {"treehole_content_bg", "[]", "public", "树洞时间线背景图"},
            {"essay_hero_bg", "[]", "public", "随笔页顶部背景图"},
            {"essay_content_bg", "[]", "public", "随笔页内容区背景图"},
            {"record_hero_bg", "[]", "public", "记录页顶部背景图"},
            {"record_content_bg", "[]", "public", "记录页内容区背景图"},
            {"wish_hero_bg", "[]", "public", "许愿池顶部背景图"},
            {"wish_content_bg", "[]", "public", "许愿池内容区背景图"},
            {"resume_hero_bg", "[]", "public", "简历页顶部背景图"},
            {"resume_skills", "Vue / Spring Boot / 全栈开发", "public", "简历-擅长技术栈"},
            {"resume_hobbies", "编程 / 摄影 / 旅行 / 音乐", "public", "简历-爱好"},
            {"resume_github", "https://github.com/ztcools", "public", "简历-GitHub地址"},
            {"resume_motto", "时刻保持思考，永远热爱生活", "public", "简历-座右铭"},
            {"resume_tags", "[\"全栈开发\",\"热爱开源\",\"持续学习\",\"生活记录者\"]", "public", "简历-个人标签"},
            {"home_music_list", "[]", "public", "首页音乐播放列表 JSON [{name,artist,url,cover,lrc}]"}
        };
        for (String[] c : configs) {
            var exist = siteConfigMapper.selectOne(
                    new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<SiteConfig>()
                            .eq(SiteConfig::getConfigKey, c[0]).last("LIMIT 1"));
            if (exist == null) {
                var sc = new SiteConfig();
                sc.setConfigKey(c[0]);
                sc.setConfigValue(c[1]);
                sc.setConfigType(c[2]);
                sc.setDescription(c[3]);
                siteConfigMapper.insert(sc);
            }
        }
        seedResume();
    }

    /** 简历页测试数据：仅当两张表都为空时填充 */
    private void seedResume() {
        Long adminId = 1L;
        var admin = userMapper.selectOne(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<User>()
                        .eq(User::getRole, "ADMIN").last("LIMIT 1"));
        if (admin != null) adminId = admin.getId();

        if (workExperienceMapper.selectCount(null) == 0) {
            Long uid = adminId;
            Object[][] rows = {
                {"星辰科技有限公司", "前端开发实习生", "杭州", "2022-06-01", "2022-12-31", false,
                 "负责公司内部管理系统的前端页面开发与维护，参与需求评审与技术方案讨论。",
                 "使用 Vue3 + Element Plus 搭建中后台页面；封装通用表格、表单组件；对接后端 RESTful 接口。", 3},
                {"云帆网络科技公司", "全栈开发工程师", "上海", "2023-01-01", "2024-06-30", false,
                 "独立负责 SaaS 产品从 0 到 1 的设计与开发，覆盖前端、后端与数据库设计。",
                 "设计并实现 Spring Boot + MySQL 微服务；主导前端架构升级；引入 Redis 缓存优化接口性能。", 2},
                {"自由原野工作室", "独立开发者", "远程", "2024-07-01", null, true,
                 "运营个人品牌，承接 Web 全栈项目外包，并持续维护个人博客 Lune。",
                 "负责产品整体规划、UI 设计、前后端开发与服务器运维；探索 AI 辅助开发工作流。", 1}
            };
            for (Object[] r : rows) {
                var w = new WorkExperience();
                w.setUserId(uid);
                w.setCompany((String) r[0]);
                w.setPosition((String) r[1]);
                w.setLocation((String) r[2]);
                w.setStartDate(LocalDate.parse((String) r[3]));
                w.setEndDate(r[4] == null ? null : LocalDate.parse((String) r[4]));
                w.setIsCurrent((Boolean) r[5]);
                w.setDescription((String) r[6]);
                w.setResponsibilities((String) r[7]);
                w.setSortOrder((Integer) r[8]);
                w.setStatus(1);
                workExperienceMapper.insert(w);
            }
            log.info("已填充工作经历测试数据");
        }

        if (projectMapper.selectCount(null) == 0) {
            Long uid = adminId;
            Object[][] rows = {
                {"Lune 个人博客", "记录美好生活的全栈个人博客系统",
                 "集博客、随笔、记录、树洞、许愿池于一体的个人生活记录平台，支持 Markdown 文章、多媒体随笔、弹幕树洞等丰富玩法。",
                 "[\"Vue3\",\"Spring Boot\",\"MySQL\",\"Redis\",\"Docker\"]", "独立全栈", "https://github.com/ztcools/lune",
                 "https://github.com/ztcools/lune", "2024.01 - 至今", 1},
                {"云上笔记", "极简云端 Markdown 笔记应用",
                 "支持实时同步、多人协作、版本回溯的云端笔记工具，主打极简与极速体验。",
                 "[\"React\",\"Node.js\",\"MongoDB\",\"WebSocket\"]", "核心开发", "https://notes.example.com",
                 "", "2023.03 - 2023.12", 2},
                {"数据可视化大屏", "企业级数据可视化监控平台",
                 "基于 ECharts 的企业运营数据大屏，实时展示核心业务指标与告警信息，支持多主题切换。",
                 "[\"Vue3\",\"ECharts\",\"TypeScript\",\"Vite\"]", "前端负责人", "",
                 "", "2022.09 - 2023.02", 3}
            };
            for (Object[] r : rows) {
                var p = new Project();
                p.setUserId(uid);
                p.setName((String) r[0]);
                p.setSummary((String) r[1]);
                p.setDescription((String) r[2]);
                p.setTechStack((String) r[3]);
                p.setRole((String) r[4]);
                p.setProjectUrl((String) r[5]);
                p.setRepoUrl((String) r[6]);
                p.setDevPeriod((String) r[7]);
                p.setSortOrder((Integer) r[8]);
                p.setStatus(1);
                projectMapper.insert(p);
            }
            log.info("已填充项目经历测试数据");
        }
    }
}
