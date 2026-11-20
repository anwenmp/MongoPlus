package com.mongoplus.convert.shell;

import com.mongodb.event.CommandEvent;
import com.mongoplus.annotation.comm.NonNull;
import com.mongoplus.model.command.MongoShellResult;

/**
 * command转换器
 *
 * @author anwen
 */
public interface ShellConverterRegistry {

    MongoShellResult apply(@NonNull CommandEvent commandEvent);

}
