CREATE INDEX message_index
ON MESSAGE
USING BTREE
(msg_id);

CREATE INDEX chat_list_index
ON CHAT_LIST
USING BTREE
(chat_id,member);

CREATE INDEX chat_index
ON CHAT
USING BTREE
(chat_id);

CREATE INDEX user_list_contains_index
ON USER_LIST_CONTAINS
USING BTREE
(list_id);

CREATE INDEX usr_index
ON USR
USING BTREE
(login);

CREATE INDEX user_list_index
ON USER_LIST
USING BTREE
(list_id);
