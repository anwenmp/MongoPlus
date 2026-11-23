package com.mongoplus.convert.shell;

import com.mongodb.event.CommandStartedEvent;
import org.bson.BsonDocument;
import org.bson.BsonValue;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.function.BiFunction;

/**
 * 将 Driver 实际发送的 BSON 命令转换为 MongoDB Shell 语句。
 *
 * @author anwen
 */
public final class MongoShellConverter {

    private static final List<String> TRANSPORT_FIELDS = Arrays.asList(
            "$db", "lsid", "$clusterTime", "txnNumber", "stmtId", "stmtIds");
    private static final List<String> TRANSACTION_FIELDS = Arrays.asList(
            "autocommit", "startTransaction", "startOrContinueTransaction", "txnRetryCounter", "recoveryToken");
    private static final List<String> SESSION_COMMANDS = Arrays.asList(
            "aborttransaction", "committransaction", "preparetransaction", "coordinatecommittransaction",
            "startsession", "endsessions", "refreshsessions", "killsessions", "killallsessions",
            "killallsessionsbypattern");
    private static final List<String> RETRYABLE_WRITES = Arrays.asList(
            "insert", "update", "delete", "findandmodify", "bulkwrite");

    private MongoShellConverter() {
    }

    /**
     * 在 Driver 回调内同步读取命令并生成文本。调用结束后即可独立使用返回的字符串。
     *
     * @param event 当前回调收到的命令开始事件
     * @return 单行 Shell 语句。跳过的事件和无法独立重放的命令返回空字符串
     */
    public static String toShell(CommandStartedEvent event) {
        if (event == null) {
            return null;
        }
        try {
            String commandName = event.getCommandName();
            if ("getMore".equalsIgnoreCase(commandName) || "killCursors".equalsIgnoreCase(commandName)) {
                return null;
            }
            return convert(event.getDatabaseName(), commandName, event.getCommand());
        } catch (RuntimeException exception) {
            return null;
        }
    }

    /**
     * 校验命令信封，在当前调用内构造用于重放的顶层文档。
     */
    private static String convert(String database, String name, BsonDocument command) {
        if (command == null || name == null || name.isEmpty() || command.isEmpty()
                || !name.equals(command.getFirstKey())) {
            return null;
        }
        String wireDatabase = stringField(command, "$db");
        String targetDatabase = database == null || database.isEmpty() ? wireDatabase : database;
        if (targetDatabase == null || targetDatabase.isEmpty()
                || (command.containsKey("$db") && !targetDatabase.equals(wireDatabase))) {
            return null;
        }

        String key = name.toLowerCase(Locale.ROOT);
        if (requiresOriginalContext(key, command)) {
            return null;
        }
        BsonDocument replay = new BsonDocument();
        for (Map.Entry<String, BsonValue> field : command.entrySet()) {
            if (!TRANSPORT_FIELDS.contains(field.getKey())) {
                replay.put(field.getKey(), field.getValue());
            }
        }

        String databaseExpression = "db.getSiblingDB(" + BsonShellFormatter.quote(targetDatabase) + ")";
        return render(key, databaseExpression, replay);
    }

    /**
     * 优先调用对应的 Shell 方法，无法映射时使用原命令生成 runCommand。
     */
    private static String render(String name, String databaseExpression, BsonDocument command) {
        BiFunction<String, BsonDocument, String> strategy = ShellConverterRegistry.find(name);
        if (strategy != null) {
            try {
                String shell = strategy.apply(databaseExpression, command);
                if (shell != null) {
                    return shell;
                }
            } catch (RuntimeException exception) {
                return FallbackShellConverter.convert(databaseExpression, command);
            }
        }
        return FallbackShellConverter.convert(databaseExpression, command);
    }

    /**
     * @return 命令依赖原有会话、事务或读取上下文时返回 true
     */
    private static boolean requiresOriginalContext(String name, BsonDocument command) {
        if (SESSION_COMMANDS.contains(name) || TRANSACTION_FIELDS.stream().anyMatch(command::containsKey)) {
            return true;
        }
        boolean retryableWrite = command.containsKey("txnNumber") && RETRYABLE_WRITES.contains(name);
        if (!retryableWrite && (command.containsKey("txnNumber")
                || command.containsKey("stmtId") || command.containsKey("stmtIds"))) {
            return true;
        }
        if (!command.isDocument(ShellCommandSupport.READ_CONCERN)) {
            return false;
        }
        BsonDocument concern = command.getDocument(ShellCommandSupport.READ_CONCERN);
        return concern.containsKey("atClusterTime") || concern.containsKey("afterClusterTime")
                || "snapshot".equals(stringField(concern, "level"));
    }

    /**
     * @return 字符串字段值，字段未出现或类型不符时为 null
     */
    private static String stringField(BsonDocument document, String key) {
        return document.isString(key) ? document.getString(key).getValue() : null;
    }
}
