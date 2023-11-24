-- liquibase formatted sql

--changeset elysanov:1
CREATE TABLE firstbot.notification_task (
    p_key        SERIAL PRIMARY KEY,
    chat_id       INTEGER,
    notification TEXT,
    date_time     TIMESTAMP
);