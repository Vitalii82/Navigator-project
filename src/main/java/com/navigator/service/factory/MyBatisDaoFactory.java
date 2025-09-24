package com.navigator.service.factory;

import java.io.IOException;
import java.io.Reader;

import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@SuppressWarnings("InstantiationOfUtilityClass")
public class MyBatisDaoFactory {
    private static final Logger logger = LogManager.getLogger(MyBatisDaoFactory.class);
    private final static MyBatisDaoFactory myBatisDaoFactory = new MyBatisDaoFactory();

    private static SqlSessionFactory sqlSessionFactory;

    private MyBatisDaoFactory() {
        try {
            Reader reader = Resources.getResourceAsReader("mybatis-config.xml");
            sqlSessionFactory = new SqlSessionFactoryBuilder().build(reader);
            logger.info("MyBatis SqlSessionFactory successfully initialized");
        } catch (IOException e) {
            logger.error("Failed to initialize SqlSessionFactory", e);
        }
    }

    public static SqlSessionFactory getSessionFactory() {
        return sqlSessionFactory;
    }
}
