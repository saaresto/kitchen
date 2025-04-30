CREATE TABLE staff_members
(
    id       UUID PRIMARY KEY,
    username VARCHAR(255) NOT NULL,
    chat_id  VARCHAR(255) NOT NULL
);

alter table staff_members
    add constraint staff_members_pk
        unique (chat_id);