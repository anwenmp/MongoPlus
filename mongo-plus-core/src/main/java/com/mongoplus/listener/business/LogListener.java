package com.mongoplus.listener.business;

import com.mongodb.event.CommandStartedEvent;
import com.mongoplus.cache.global.OrderCache;
import com.mongoplus.convert.shell.MongoShellConverter;
import com.mongoplus.listener.Listener;
import com.mongoplus.logging.Log;
import com.mongoplus.logging.LogFactory;
import com.mongoplus.model.command.CommandFailed;
import com.mongoplus.model.command.CommandStarted;
import com.mongoplus.model.command.CommandSucceeded;
import com.mongoplus.toolkit.StringUtils;
import org.bson.BsonDocument;
import org.bson.BsonValue;
import org.bson.json.JsonMode;
import org.bson.json.JsonWriterSettings;

import java.util.concurrent.Executor;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

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
        CommandStartedEvent event = shell ? commandStarted.getCommandStartedEvent() : null;
        BsonDocument document = commandStarted.getCommandDocument();
        if (async) {
            // Driver 回调结束后可能回收 BSON 缓冲区，提交任务前须复制。
            BsonDocument snapshot = copyDocument(document);
            CommandStartedEvent eventSnapshot = snapshotEvent(event, document, snapshot);
            executeLog(() -> logCommand(eventSnapshot, snapshot));
        } else {
            logCommand(event, document);
        }
    }

    private static CommandStartedEvent snapshotEvent(CommandStartedEvent event, BsonDocument document, BsonDocument snapshot) {
        if (event == null) {
            return null;
        }
        BsonDocument eventCommand = event.getCommand();
        // 正常分发时两个入口引用同一份 BSON，复用副本。自定义监听器可以替换包装对象中的文档。
        BsonDocument eventSnapshot = eventCommand == document ? snapshot : copyDocument(eventCommand);
        return new CommandStartedEvent(event.getRequestContext(), event.getOperationId(), event.getRequestId(),
                event.getConnectionDescription(), event.getDatabaseName(), event.getCommandName(), eventSnapshot);
    }

    private static BsonDocument copyDocument(BsonDocument document) {
        return document == null ? null : document.clone();
    }

    private void logCommand(CommandStartedEvent event, BsonDocument document) {
        String command = event == null ? null : MongoShellConverter.toShell(event);
        if (StringUtils.isBlank(command)) {
            command = document.toJson(JsonWriterSettings.builder().outputMode(JsonMode.SHELL).build());
        }
        log.debug("==>  Preparing: " + command);
    }

    @Override
    public void commandSucceeded(CommandSucceeded commandSucceeded) {
        if (!log.isDebugEnabled()) {
            return;
        }
        String commandName = commandSucceeded.getCommandName();
        if (commandName == null) {
            return;
        }
        Function<BsonDocument, Long> count;
        switch (commandName) {
            case "find":
            case "aggregate":
            case "listIndexes":
                count = response -> batchSize(response, "firstBatch");
                break;
            case "getMore":
                count = response -> batchSize(response, "nextBatch");
                break;
            case "distinct":
                count = response -> arraySize(response.get("values"));
                break;
            case "update":
                count = LogListener::updateCount;
                break;
            case "findAndModify":
                count = LogListener::findAndModifyCount;
                break;
            case "insert":
            case "delete":
            case "count":
                count = response -> number(response, "n");
                break;
            case "createIndexes":
                count = LogListener::createdIndexCount;
                break;
            default:
                return;
        }
        BsonDocument response = commandSucceeded.getResponse();
        if (response == null) {
            return;
        }
        BsonDocument document = async ? response.clone() : response;
        executeLog(() -> {
            Long total = count.apply(document);
            if (total != null) {
                log.debug("<==  Total    : " + total);
            }
        });
    }

    private static Long number(BsonDocument document, String field) {
        BsonValue value = document.get(field);
        return value != null && value.isNumber() ? value.asNumber().longValue() : null;
    }

    private static Long arraySize(BsonValue value) {
        return value != null && value.isArray() ? (long) value.asArray().size() : null;
    }

    private static Long batchSize(BsonDocument response, String batch) {
        BsonValue cursor = response.get("cursor");
        return cursor != null && cursor.isDocument() ? arraySize(cursor.asDocument().get(batch)) : null;
    }

    private static Long updateCount(BsonDocument response) {
        Long modified = number(response, "nModified");
        if (modified == null) {
            return null;
        }
        Long upserted = arraySize(response.get("upserted"));
        return upserted == null ? modified : modified + upserted;
    }

    private static Long findAndModifyCount(BsonDocument response) {
        // n 同时覆盖匹配、删除和新增。BEFORE 模式下，upsert 插入成功仍可能返回 value:null。
        BsonValue error = response.get("lastErrorObject");
        if (error != null && error.isDocument()) {
            Long count = number(error.asDocument(), "n");
            if (count != null) {
                return count;
            }
        }
        BsonValue value = response.get("value");
        if (value != null) {
            if (value.isDocument()) {
                return 1L;
            }
            if (value.isNull()) {
                return 0L;
            }
        }
        return null;
    }

    private static Long createdIndexCount(BsonDocument response) {
        Long before = number(response, "numIndexesBefore");
        Long after = number(response, "numIndexesAfter");
        return before != null && after != null && after >= before ? after - before : null;
    }

    private void executeLog(Runnable task) {
        if (async) {
            LOG_EXECUTOR.execute(() -> {
                try {
                    task.run();
                } catch (RuntimeException exception) {
                    log.error("Failed to process MongoDB command log", exception);
                }
            });
        } else {
            task.run();
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
