package example;

import com.mongoplus.listener.Listener;
import com.mongoplus.model.command.CommandFailed;
import com.mongoplus.model.command.CommandStarted;
import com.mongoplus.model.command.CommandSucceeded;
import java.util.Objects;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * MongoPlus 命令失败通知入口；仅提供尽力回调，不承诺可靠投递。
 */
public final class CommandFailureListener implements Listener {

    private static final Logger LOGGER = LoggerFactory.getLogger(CommandFailureListener.class);

    private final FailureNotificationSink sink;

    /**
     * @param sink 业务通知适配器，应快速返回并自行处理队列、超时和重试策略
     */
    public CommandFailureListener(FailureNotificationSink sink) {
        this.sink = Objects.requireNonNull(sink, "sink");
    }

    @Override
    public void commandStarted(CommandStarted commandStarted) {
        // 本需求只关注失败事件。
    }

    @Override
    public void commandSucceeded(CommandSucceeded commandSucceeded) {
        // 命令成功与业务事务提交不同，本示例不发送成功通知。
    }

    @Override
    public void commandFailed(CommandFailed commandFailed) {
        Throwable failure = commandFailed.getThrowable();
        String failureType = failure == null ? "unknown" : failure.getClass().getName();
        try {
            // 不传命令 BSON、异常消息或完整异常，避免通知适配器无意暴露业务数据。
            sink.notifyFailure(commandFailed.getCommandName(), failureType);
        } catch (RuntimeException notificationFailure) {
            // 隔离通知适配器异常，避免中断 MongoPlus 对后续 Listener 的遍历。
            LOGGER.warn("命令失败通知适配器异常，类型={}", notificationFailure.getClass().getName());
        }
    }

    @Override
    public int getOrder() {
        return LOWEST_PRECEDENCE;
    }

    /** 由使用者实现的业务适配接口，不属于 MongoPlus API。 */
    @FunctionalInterface
    public interface FailureNotificationSink {

        /**
         * @param commandName 命令名
         * @param failureType 异常类名，不含异常消息或文档内容
         */
        void notifyFailure(String commandName, String failureType);
    }
}
