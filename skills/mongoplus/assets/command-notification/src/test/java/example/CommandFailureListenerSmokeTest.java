package example;

import com.mongoplus.model.command.CommandFailed;
import java.util.concurrent.atomic.AtomicInteger;

/** 纯本地适配器验证；不启动 Spring，不连接 MongoDB，不发送真实通知。 */
public final class CommandFailureListenerSmokeTest {

    private CommandFailureListenerSmokeTest() {
    }

    public static void main(String[] args) {
        AtomicInteger calls = new AtomicInteger();
        CommandFailureListener listener = new CommandFailureListener((commandName, failureType) -> {
            if (!"find".equals(commandName) || !IllegalStateException.class.getName().equals(failureType)) {
                throw new AssertionError("通知字段与真实事件模型不符");
            }
            calls.incrementAndGet();
        });
        listener.commandFailed(new CommandFailed("find", new IllegalStateException("业务数据不得转发"), null));
        if (calls.get() != 1) {
            throw new AssertionError("一次手动回调应通知一次");
        }
        // 模拟业务适配器拒绝或失败，验证异常不会从此 Listener 向外传播。
        CommandFailureListener failing = new CommandFailureListener((commandName, failureType) -> {
            throw new IllegalArgumentException("模拟通知适配器失败");
        });
        failing.commandFailed(new CommandFailed("find", null, null));
        System.out.println("PASS: local listener payload and adapter exception isolation; database NOT_RUN");
    }
}
