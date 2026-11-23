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

/**
 * Mongo拦截器，这里可以打印日志
 *
 * @author JiaChaoYang
 */
public class LogListener implements Listener {

    private static final Log log = LogFactory.getLog("com.mongoplus.mapper");

    private final boolean shell;

    public LogListener(boolean shell) {
        this.shell = shell;
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
        log.debug("==>  Preparing: " + command);
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
            log.debug("<==      Total: " + resultCount);
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
