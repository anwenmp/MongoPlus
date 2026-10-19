package example;

import com.mongoplus.listener.Listener;
import example.CommandFailureListener.FailureNotificationSink;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/** 在已启用 MongoPlus Boot starter 的应用中注册通知入口。 */
@Configuration
public class CommandNotificationConfiguration {

    /**
     * @param sink 使用者提供的通知适配器 Bean
     * @return 供 MongoPlus 自动配置收集的 Listener Bean
     */
    @Bean
    public Listener commandFailureListener(FailureNotificationSink sink) {
        return new CommandFailureListener(sink);
    }
}
