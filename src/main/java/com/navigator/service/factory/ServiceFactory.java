package com.navigator.service.factory;

import com.navigator.service.intarfaces.IGraphService;
import com.navigator.service.mybatisimpl.NavigatorService; // MyBatis as primary

public final class ServiceFactory {
    private ServiceFactory() {}

    public static IGraphService create() {
        return new NavigatorService();
    }
}
