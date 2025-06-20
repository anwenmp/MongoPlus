package com.mongoplus.handler;

import com.github.houbb.sensitive.word.api.IWordAllow;
import com.github.houbb.sensitive.word.api.IWordDeny;
import com.github.houbb.sensitive.word.support.allow.WordAllows;
import com.github.houbb.sensitive.word.support.deny.WordDenys;

import java.util.List;

/**
 * 动态加载敏感词
 */
public interface DynamicLoadWord extends IWordDeny, IWordAllow {

    @Override
    default List<String> allow() {
        return WordAllows.defaults().allow();
    }

    @Override
    default List<String> deny() {
        return WordDenys.defaults().deny();
    }
}
