package com.mongoplus.service.impl;

import com.mongoplus.repository.impl.RepositoryImpl;
import com.mongoplus.service.IService;

/**
 * IService接口通用方法实现
 *
 * @author JiaChaoYang
 **/
public class ServiceImpl<T> extends RepositoryImpl<T> implements IService<T> {

}
