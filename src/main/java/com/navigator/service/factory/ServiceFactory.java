package com.navigator.service.factory;

import com.navigator.db.dao.GraphDao;
import com.navigator.db.dao.GraphDaoImpl;
import com.navigator.service.intarfaces.IGraphService;
import com.navigator.service.mybatisimpl.NavigatorService;

public class ServiceFactory {
    public static IGraphService createNavigatorService() {
        GraphDao graphDao = new GraphDaoImpl();
        return new NavigatorService(graphDao);
    }
}
