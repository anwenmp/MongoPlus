package com.mongoplus.listener;

import com.mongodb.event.CommandFailedEvent;
import com.mongodb.event.CommandListener;
import com.mongodb.event.CommandStartedEvent;
import com.mongodb.event.CommandSucceededEvent;
import com.mongoplus.domain.MongoPlusInterceptorException;
import com.mongoplus.logging.Log;
import com.mongoplus.logging.LogFactory;
import com.mongoplus.model.command.CommandFailed;
import com.mongoplus.model.command.CommandStarted;
import com.mongoplus.model.command.CommandSucceeded;

public class BaseListener implements CommandListener {

    private final MongoPlusListener mongoPlusInterceptor = new MongoPlusListener();

    private final Log log = LogFactory.getLog(BaseListener.class);

    @Override
    public void commandStarted(CommandStartedEvent event) {
        try {
            mongoPlusInterceptor.commandStarted(new CommandStarted(event.getCommandName(),event.getCommand(),event.getCommand().toJson(),event));
        }catch (Exception e){
            log.error("interceptor error: ",e);
            throw new MongoPlusInterceptorException(e);
        }
    }

    @Override
    public void commandSucceeded(CommandSucceededEvent event) {
        try {
            mongoPlusInterceptor.commandSucceeded(new CommandSucceeded(event.getCommandName(),event.getResponse(),event));
        }catch (Exception e){
            log.error("interceptor error: ",e);
            throw new MongoPlusInterceptorException(e);
        }
    }

    @Override
    public void commandFailed(CommandFailedEvent event) {
        try {
            mongoPlusInterceptor.commandFailed(new CommandFailed(event.getCommandName(),event.getThrowable(),event));
        }catch (Exception e){
            log.error("interceptor error: ",e);
            throw new MongoPlusInterceptorException(e);
        }
    }
}
