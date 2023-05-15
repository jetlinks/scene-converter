package org.jetlinks.scene.converter;

import org.jetlinks.scene.converter.entity.Param;
import org.jetlinks.scene.converter.service.SceneConverterManager;
import org.jetlinks.scene.converter.service.SceneSourceService;
import org.jetlinks.scene.converter.service.SceneTargetService;
import org.jetlinks.scene.converter.service.impl.SceneConverterDevice;
import org.jetlinks.scene.converter.service.impl.SceneConverterRule;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.util.StringUtils;

import java.util.Scanner;

/**
 * 输入描述.
 *
 * @author zhangji 2023/5/6
 */
public class SceneConverterApplication {

    public static void main(String[] args) {

        System.out.println("正在启动场景联动数据迁移程序。。。");
        System.out.println("需要配置数据库连接信息。然后程序将把JetLinks 1.0版本的设备告警以及场景联动配置信息，迁移到JetLinks 2.0版本中。");
        Scanner scanner = new Scanner(System.in);

        String sourceUrl = getProperty("source.url", "迁移来源的数据库url", scanner);
        String sourceUsername = getProperty("source.username", "迁移来源的数据库用户名", scanner);
        String sourcePassword = getProperty("source.password", "迁移来源的数据库密码", scanner);
        String sourceDriverClassName = getProperty("source.driver", "迁移来源的数据库Driver", scanner);

        SceneSourceService sourceService = new SceneSourceService(
                template(sourceUrl, sourceUsername, sourcePassword, sourceDriverClassName)
        );

        String targetUrl = getProperty("target.url", "迁移对象的数据库url", scanner);
        String targetUsername = getProperty("target.username", "迁移对象的数据库用户名", scanner);
        String targetPassword = getProperty("target.password", "迁移对象的数据库密码", scanner);
        String targetDriverClassName = getProperty("target.driver", "迁移对象的数据库Driver", scanner);

        SceneTargetService targetService = new SceneTargetService(
                template(targetUrl, targetUsername, targetPassword, targetDriverClassName)
        );

        SceneConverterManager manager = new SceneConverterManager(sourceService, targetService);
        manager.register(new SceneConverterDevice());
        manager.register(new SceneConverterRule());

        Param param = new Param();
        manager.convertAll(param);

    }

    private static NamedParameterJdbcTemplate template(String url,
                                                       String username,
                                                       String password,
                                                       String driverClassName) {
        DriverManagerDataSource dataSource = new DriverManagerDataSource(url, username, password);
        dataSource.setDriverClassName(driverClassName);
        return new NamedParameterJdbcTemplate(dataSource);
    }

    private static String getProperty(String key,
                                      String name,
                                      Scanner scanner) {
        return getProperty(key, null, name, scanner);
    }

    private static String getProperty(String key,
                                      String defaultValue) {
        return getProperty(key, defaultValue, null, null);
    }

    private static String getProperty(String key,
                                      String defaultValue,
                                      String name,
                                      Scanner scanner) {
        String property = System.getProperty(key);
        if (!StringUtils.hasText(property) && scanner != null) {
            System.out.println("请输入" + name + "：");
            property = scanner.nextLine();
        }
        if (!StringUtils.hasText(property) && defaultValue != null) {
            property = defaultValue;
        }
        return property;
    }
}
