package org.jetlinks.scene.converter;

import com.alibaba.fastjson.JSON;
import org.jetlinks.scene.converter.entity.AlarmHistorySourceEntity;
import org.jetlinks.scene.converter.entity.NotifyConfigEntity;
import org.jetlinks.scene.converter.entity.NotifyTemplateEntity;
import org.jetlinks.scene.converter.entity.Param;
import org.jetlinks.scene.converter.entity.SceneSourceEntity;
import org.jetlinks.scene.converter.service.SceneConverterManager;
import org.jetlinks.scene.converter.service.SceneSourceService;
import org.jetlinks.scene.converter.service.SceneTargetService;
import org.jetlinks.scene.converter.service.impl.SceneConverterDevice;
import org.jetlinks.scene.converter.service.impl.SceneConverterRule;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.core.io.ClassPathResource;
import org.springframework.util.StreamUtils;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/**
 * 输入描述.
 *
 * @author zhangji 2023/5/15
 */
public class SceneConverterTest {

    @Test
    void test() {
        SceneSourceService sourceService = Mockito.mock(SceneSourceService.class);
        List<SceneSourceEntity> sourceEntityList = getSceneSource();
        Mockito.when(sourceService.count(Mockito.any(Param.class))).thenReturn(sourceEntityList.size());
        Mockito.when(sourceService.find(Mockito.any(Param.class))).thenReturn(sourceEntityList);
        Mockito.when(sourceService.findDistinctAlarmHistory(Mockito.anySet())).thenReturn(getAlarmHistory());
        Mockito.when(sourceService.findNotifyConfig(Mockito.anySet())).thenReturn(getNotifyConfig());
        Mockito.when(sourceService.findNotifyTemplate(Mockito.anySet())).thenReturn(getNotifyTemplate());

        SceneTargetService targetService = Mockito.mock(SceneTargetService.class);
        Mockito.when(targetService.batchAddRuleScene(Mockito.anyCollection())).thenReturn(new int[sourceEntityList.size()]);
        Mockito.when(targetService.batchAddAlarmConfig(Mockito.anyCollection())).thenReturn(new int[sourceEntityList.size()]);
        Mockito.when(targetService.batchAddAlarmBind(Mockito.anyCollection())).thenReturn(new int[sourceEntityList.size()]);
        Mockito.when(targetService.batchAddAlarmRecord(Mockito.anyCollection())).thenReturn(new int[sourceEntityList.size()]);
        Mockito.when(targetService.batchAddNotifyConfig(Mockito.anyCollection())).thenReturn(new int[sourceEntityList.size()]);
        Mockito.when(targetService.batchAddNotifyTemplate(Mockito.anyCollection())).thenReturn(new int[sourceEntityList.size()]);

        SceneConverterManager manager = new SceneConverterManager(sourceService, targetService);
        manager.register(new SceneConverterDevice());
        manager.register(new SceneConverterRule());

        Param param = new Param();
        manager.convertAll(param);

    }

    private List<SceneSourceEntity> getSceneSource() {
        try (InputStream inputStream = new ClassPathResource("jetlinks_public_rule_instance.json").getInputStream()) {
            String calculationRule = StreamUtils.copyToString(inputStream, StandardCharsets.UTF_8);
            return JSON.parseArray(calculationRule, SceneSourceEntity.class);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return new ArrayList<>();
    }

    private List<AlarmHistorySourceEntity> getAlarmHistory() {

        try (InputStream inputStream = new ClassPathResource("jetlinks_public_rule_dev_alarm_history.json").getInputStream()) {
            String calculationRule = StreamUtils.copyToString(inputStream, StandardCharsets.UTF_8);
            return JSON.parseArray(calculationRule, AlarmHistorySourceEntity.class);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return new ArrayList<>();
    }

    private List<NotifyConfigEntity> getNotifyConfig() {
        try (InputStream inputStream = new ClassPathResource("jetlinks_public_notify_config.json").getInputStream()) {
            String calculationRule = StreamUtils.copyToString(inputStream, StandardCharsets.UTF_8);
            return JSON.parseArray(calculationRule, NotifyConfigEntity.class);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return new ArrayList<>();
    }

    private List<NotifyTemplateEntity> getNotifyTemplate() {
        try (InputStream inputStream = new ClassPathResource("jetlinks_public_notify_template.json").getInputStream()) {
            String calculationRule = StreamUtils.copyToString(inputStream, StandardCharsets.UTF_8);
            return JSON.parseArray(calculationRule, NotifyTemplateEntity.class);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return new ArrayList<>();
    }
}
