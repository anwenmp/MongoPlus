package com.mongoplus.parser;

import org.bson.BsonArray;
import org.bson.BsonValue;
import org.bson.Document;
import org.bson.conversions.Bson;

import java.util.List;
import java.util.stream.Collectors;

public class CommandParse extends AbstractCommandParse {

    public static Parser parserInstance;

    public static void setParser(Parser parser){
        parserInstance = parser;
    }

    static {
        parserInstance = new CommandParse();
    }

    @Override
    public Bson parseCommand(String command) {
        return Document.parse(command);
    }

    @Override
    public List<Bson> parseArrayCommand(String command) {
        BsonArray bsonArray = BsonArray.parse(command);
        return bsonArray.getValues().stream().map(BsonValue::asDocument).collect(Collectors.toList());
    }
}
