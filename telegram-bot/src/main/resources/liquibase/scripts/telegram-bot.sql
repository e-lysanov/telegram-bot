-- liquibase formatted sql

--changeset elysanov:1
CREATE TABLE firstbot.notification_task (
    p_key        SERIAL PRIMARY KEY,
    chatId       INTEGER,
    notification TEXT,
    dateTime     TIMESTAMP
);