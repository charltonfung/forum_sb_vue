package com.jayfengk.forum.config;

import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import org.apache.ibatis.reflection.MetaObject;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/**
 * MyBatis-Plus 自動填值
 * insert -> 填 createdAt + updateAt
 * update -> 只填 updateAt
 */
@Component
public class MyMetaObjectHandler implements MetaObjectHandler {

    @Override
    public void insertFill(MetaObject metaObject) {
        this.strictInsertFill(metaObject, "createAt", LocalDateTime.class, LocalDateTime.now());
        this.strictInsertFill(metaObject, "updateAt", LocalDateTime.class, LocalDateTime.now());
    }

    @Override
    public void updateFill(MetaObject metaObject) {
        this.strictUpdateFill(metaObject, "updateAt", LocalDateTime.class, LocalDateTime.now());
    }
}