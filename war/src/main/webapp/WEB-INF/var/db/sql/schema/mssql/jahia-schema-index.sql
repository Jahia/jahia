DROP INDEX jahia_user_prop_index ON jahia_user_prop ;
DROP INDEX jahia_users_index ON jahia_users ;

CREATE INDEX jahia_user_prop_index ON jahia_user_prop (id_jahia_users, provider_jahia_user_prop, userkey_jahia_user_prop);
CREATE INDEX jahia_users_index ON jahia_users (name_jahia_users);
