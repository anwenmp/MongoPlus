package com.mongoplus.toolkit;

import com.mongodb.ServerAddress;
import com.mongodb.connection.ClusterId;
import com.mongodb.connection.ConnectionDescription;
import com.mongodb.connection.ServerId;
import com.mongodb.event.CommandStartedEvent;
import org.bson.BsonDocument;
import org.junit.Assert;
import org.junit.Test;

/**
 * 验证更新命令省略 multi 时的日志构建，以及单条和批量更新的输出。
 */
public class MongoCommandBuildUtilsTest {

    @Test
    public void mixedBatchShouldDefaultMissingMultiToUpdateOneAndPreserveOrder() {
        CommandStartedEvent event = updateEvent("{update:'users',updates:["
                + "{q:{_id:1},u:{$set:{name:'Alice'}}},"
                + "{q:{_id:2},u:{$inc:{score:1}},multi:false},"
                + "{q:{active:true},u:{$set:{verified:true}},multi:true}]}");
        BsonDocument commandBeforeBuild = event.getCommand().clone();

        Assert.assertEquals(
                "db.users.updateOne({\"_id\": 1}, {\"$set\": {\"name\": \"Alice\"}})"
                        + System.lineSeparator()
                        + "db.users.updateOne({\"_id\": 2}, {\"$inc\": {\"score\": 1}})"
                        + System.lineSeparator()
                        + "db.users.updateMany({\"active\": true}, {\"$set\": {\"verified\": true}})",
                MongoCommandBuildUtils.buildCommand(event)
        );
        Assert.assertEquals(commandBeforeBuild, event.getCommand());
    }

    @Test
    public void singleUpdateWithoutMultiShouldBuildUpdateOne() {
        CommandStartedEvent event = updateEvent("{update:'users',updates:["
                + "{q:{_id:1},u:{$set:{name:'Alice'}}}]}");

        Assert.assertEquals(
                "db.users.updateOne({\"_id\": 1}, {\"$set\": {\"name\": \"Alice\"}})",
                MongoCommandBuildUtils.buildCommand(event)
        );
    }

    @Test
    public void singleUpdateWithFalseMultiShouldBuildUpdateOne() {
        CommandStartedEvent event = updateEvent("{update:'users',updates:["
                + "{q:{_id:1},u:{$set:{name:'Alice'}},multi:false}]}");

        Assert.assertEquals(
                "db.users.updateOne({\"_id\": 1}, {\"$set\": {\"name\": \"Alice\"}})",
                MongoCommandBuildUtils.buildCommand(event)
        );
    }

    @Test
    public void singleUpdateWithTrueMultiShouldBuildUpdateMany() {
        CommandStartedEvent event = updateEvent("{update:'users',updates:["
                + "{q:{active:true},u:{$set:{verified:true}},multi:true}]}");

        Assert.assertEquals(
                "db.users.updateMany({\"active\": true}, {\"$set\": {\"verified\": true}})",
                MongoCommandBuildUtils.buildCommand(event)
        );
    }

    @Test
    public void updateDocumentWithoutMultiShouldBuildUpdateOne() {
        CommandStartedEvent event = updateEvent("{update:'users',updates:"
                + "{q:{_id:1},u:{$set:{name:'Alice'}}}}");

        Assert.assertEquals(
                "db.users.updateOne({\"_id\": 1}, {\"$set\": {\"name\": \"Alice\"}})",
                MongoCommandBuildUtils.buildCommand(event)
        );
    }

    private static CommandStartedEvent updateEvent(String commandJson) {
        ConnectionDescription connection = new ConnectionDescription(
                new ServerId(new ClusterId(), new ServerAddress("localhost", 27017))
        );
        return new CommandStartedEvent(null, 1L, 1, connection,
                "test", "update", BsonDocument.parse(commandJson));
    }
}
