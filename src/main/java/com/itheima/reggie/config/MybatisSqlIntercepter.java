/*
package com.itheima.reggie.config;

import org.apache.ibatis.executor.statement.StatementHandler;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.mapping.SqlCommandType;
import org.apache.ibatis.plugin.*;
import org.apache.ibatis.reflection.MetaObject;
import org.apache.ibatis.reflection.SystemMetaObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.lang.reflect.Field;
import java.sql.Connection;
*/

/**
 * 手写mybatis分页
 */

/*
@Intercepts({ @Signature(type = StatementHandler.class, method = "prepare", args = { Connection.class,Integer.class }) })
@Component
public class MybatisSqlIntercepter implements Interceptor {

    @Value("${pagehelper.rule}")
    private String pagehelperRule;

    @Override
    public Object intercept(Invocation invocation) throws Throwable {
        StatementHandler statementHandler = (StatementHandler) invocation.getTarget();
        MetaObject metaObject = SystemMetaObject.forObject(statementHandler);
        MappedStatement mappedStatement = (MappedStatement) metaObject.getValue("delegate.mappedStatement");
        if (mappedStatement.getSqlCommandType() == SqlCommandType.SELECT){
            extendLimit(statementHandler);
        }
        Object o = invocation.proceed();
        return o;
    }

    @Override
    public Object plugin(Object target) {
        return Plugin.wrap(target,this);
    }


    private void extendLimit(StatementHandler statementHandler) throws Exception {
        BoundSql boundSql = statementHandler.getBoundSql();
        Class<? extends BoundSql> aClass = boundSql.getClass();
        Field sql = aClass.getDeclaredField("sql"); //反射获取属性
        sql.setAccessible(true);
        String oldSql = boundSql.getSql(); //获得原生sql
        sql.set(boundSql,oldSql+" "+pagehelperRule); //进行拼写

    }
}

*/
