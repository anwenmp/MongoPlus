package com.mongoplus.listener.business;

import com.mongoplus.cache.global.OrderCache;
import com.mongoplus.convert.shell.MongoShellConverter;
import com.mongoplus.listener.Listener;
import com.mongoplus.logging.Log;
import com.mongoplus.logging.LogFactory;
import com.mongoplus.model.command.CommandFailed;
import com.mongoplus.model.command.CommandStarted;
import com.mongoplus.model.command.CommandSucceeded;
import com.mongoplus.toolkit.StringUtils;
import org.bson.json.JsonMode;
import org.bson.json.JsonWriterSettings;

import java.util.Objects;
import java.util.concurrent.Executor;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * Mongo拦截器，这里可以打印日志
 *
 * @author JiaChaoYang
 */
public class LogListener implements Listener {

    private static final Log log = LogFactory.getLog("com.mongoplus.mapper");

    private static final Executor LOG_EXECUTOR = new ThreadPoolExecutor(
            0, 1, 30, TimeUnit.SECONDS, new LinkedBlockingQueue<>(), task -> {
                Thread thread = new Thread(task, "mongoplus-command-log");
                thread.setDaemon(true);
                return thread;
            });

    private final boolean shell;

    private final boolean async;

    public LogListener(boolean shell) {
        this(shell, false);
    }

    public LogListener(boolean shell, boolean async) {
        this.shell = shell;
        this.async = async;
    }

    @Override
    public void commandStarted(CommandStarted commandStarted) {
        if (!log.isDebugEnabled()) {
            return;
        }
        String command = null;
        if (shell) {
            String shellCommand = MongoShellConverter.toShell(commandStarted.getCommandStartedEvent());
            if (StringUtils.isNotBlank(shellCommand)) {
                command = shellCommand;
            }
        }
        if (StringUtils.isBlank(command)) {
            command = commandStarted.getCommandDocument()
                    .toJson(JsonWriterSettings.builder().outputMode(JsonMode.SHELL).build());
        }
        printDebug("==>  Preparing: " + command);
    }

    @Override
    public void commandSucceeded(CommandSucceeded commandSucceeded) {
        if (!log.isDebugEnabled()) {
            return;
        }
        Integer resultCount = null;
        if (Objects.equals(commandSucceeded.getCommandName(), "find") || Objects.equals(commandSucceeded.getCommandName(), "aggregate")) {
            resultCount = commandSucceeded.getResponse().getDocument("cursor").get("firstBatch").asArray().getValues().size();
        } else if (Objects.equals(commandSucceeded.getCommandName(), "update")) {
            resultCount = commandSucceeded.getResponse().get("nModified").asInt32().getValue();
        } else if (Objects.equals(commandSucceeded.getCommandName(), "insert") || Objects.equals(commandSucceeded.getCommandName(), "delete")) {
            resultCount = commandSucceeded.getResponse().get("n").asInt32().getValue();
        }
        if (resultCount != null) {
            printDebug("<==  Total    : " + resultCount);
        }
    }

    private void printDebug(String message) {
        if (async) {
            // Driver 回调中的 BSON 可能依赖回调期间有效的缓冲区，只传递已生成的文本。
            LOG_EXECUTOR.execute(() -> log.debug(message));
        } else {
            log.debug(message);
        }
    }

    @Override
    public void commandFailed(CommandFailed commandFailed) {
        // Do nothing
    }

    @Override
    public int getOrder() {
        return OrderCache.LOG_ORDER;
    }
}
