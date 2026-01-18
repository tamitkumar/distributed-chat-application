package com.techbrain.chat.cofig;

import org.hibernate.dialect.MySQLDialect;
import org.hibernate.dialect.identity.IdentityColumnSupport;
import org.hibernate.dialect.identity.MySQLIdentityColumnSupport;


public class DialectConfig extends MySQLDialect {
    @Override
    public boolean dropConstraints() {
        System.out.println("DialectConfig ===> Prevents Hibernate from trying to remove foreign keys when dropping tables. Safe for existing DBs.");
        return false;
    }

    @Override
    public IdentityColumnSupport getIdentityColumnSupport() {
        System.out.println("DialectConfig ===> telling Hibernate how to fetch the auto-generated ID after an INSERT");
        return new MySQLIdentityColumnSupport() {
            @Override
            public String getIdentitySelectString(String table, String column, int type) {
                System.out.println("DialectConfig ===>");
                return "select last_insert_id()";
            }
        };
    }
}

