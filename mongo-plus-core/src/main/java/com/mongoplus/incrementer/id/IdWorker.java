/*
 * Copyright (c) 2011-2023, baomidou (jobob@qq.com).
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.mongoplus.incrementer.id;

import com.mongoplus.incrementer.DefaultIdentifierGenerator;
import com.mongoplus.incrementer.IdentifierGenerator;
import com.mongoplus.toolkit.StringPool;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

/**
 * id 获取器
 * @since mp
 * @author JiaChaoYang
*/
public class IdWorker {

    /**
     * 主机和进程的机器码
     */
    private static IdentifierGenerator IDENTIFIER_GENERATOR = entity -> DefaultIdentifierGenerator.getInstance().nextId(entity);

    /**
     * 毫秒格式化时间
     */
    public static final DateTimeFormatter MILLISECOND = DateTimeFormatter.ofPattern("yyyyMMddHHmmssSSS");

    /**
     * 获取唯一ID
     *
     * @return id
     */
    public static long getId() {
        return getId(null);
    }

    /**
     * 获取唯一ID
     *
     * @return id
     */
    public static long getId(Object entity) {
        return IDENTIFIER_GENERATOR.nextId(entity).longValue();
    }

    /**
     * 获取唯一ID
     *
     * @return id
     */
    public static String getIdStr() {
        return getIdStr(null);
    }

    /**
     * 获取唯一ID
     *
     * @return id
     */
    public static String getIdStr(Object entity) {
        return IDENTIFIER_GENERATOR.nextId(entity).toString();
    }

    /**
     * 格式化的毫秒时间
     */
    public static String getMillisecond() {
        return LocalDateTime.now().format(MILLISECOND);
    }

    /**
     * 时间 ID = Time + ID
     * <p>例如：可用于商品订单 ID</p>
     */
    public static String getTimeId() {
        return getMillisecond() + getIdStr();
    }

    /**
     * 有参构造器
     *
     * @param workerId     工作机器 ID
     * @param dataCenterId 序列号
     * @see #setIdentifierGenerator(IdentifierGenerator)
     */
    public static void initSequence(long workerId, long dataCenterId) {
        IDENTIFIER_GENERATOR = new DefaultIdentifierGenerator(workerId, dataCenterId);
    }

    /**
     * 自定义id 生成方式
     *
     * @param identifierGenerator id 生成器
     * @see IdentifierGenerator
     */
    public static void setIdentifierGenerator(IdentifierGenerator identifierGenerator) {
        IDENTIFIER_GENERATOR = identifierGenerator;
    }

    /**
     * 使用ThreadLocalRandom获取UUID获取更优的效果 去掉"-"
     */
    public static String get32UUID() {
        ThreadLocalRandom random = ThreadLocalRandom.current();
        return new UUID(random.nextLong(), random.nextLong()).toString().replace(StringPool.DASH, StringPool.EMPTY);
    }

    /**
     * 生成一个ULID
     * @author JiaChaoYang
    */
    public static String get26ULID(){
        return new ULID().nextULID();
    }

}
