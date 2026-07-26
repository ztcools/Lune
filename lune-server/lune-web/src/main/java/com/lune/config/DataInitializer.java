package com.lune.config;

import com.lune.entity.Category;
import com.lune.entity.SiteConfig;
import com.lune.entity.User;
import com.lune.mapper.CategoryMapper;
import com.lune.mapper.SiteConfigMapper;
import com.lune.mapper.UserMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class DataInitializer implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataInitializer.class);

    private final UserMapper userMapper;
    private final CategoryMapper categoryMapper;
    private final SiteConfigMapper siteConfigMapper;
    private final PasswordEncoder passwordEncoder;

    @Value("${app.admin.default-password:admin123}")
    private String defaultAdminPassword;

    public DataInitializer(UserMapper userMapper, CategoryMapper categoryMapper,
                           SiteConfigMapper siteConfigMapper, PasswordEncoder passwordEncoder) {
        this.userMapper = userMapper;
        this.categoryMapper = categoryMapper;
        this.siteConfigMapper = siteConfigMapper;
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
        if (siteConfigMapper.selectCount(null) == 0) {
            String[][] configs = {
                {"site_name", "Lune", "public", "网站名称"},
                {"site_title", "Lune - 记录美好生活", "public", "网站标题"},
                {"site_description", "个人博客，记录成长，分享生活", "public", "网站描述"},
                {"site_footer", "© 2024 Lune. All Rights Reserved.", "public", "页脚信息"},
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
                {"record_content_bg", "[]", "public", "记录页内容区背景图"}
            };
            for (String[] c : configs) {
                var sc = new SiteConfig();
                sc.setConfigKey(c[0]);
                sc.setConfigValue(c[1]);
                sc.setConfigType(c[2]);
                sc.setDescription(c[3]);
                siteConfigMapper.insert(sc);
            }
        }
    }
}
