CREATE TABLE IF NOT EXISTS `resource_lock` (
  `resource_lock_id` bigint(20) unsigned NOT NULL AUTO_INCREMENT,
  `resource_name` varchar(256),
  `lock_holder` varchar(100) NOT NULL,
  `lock_count` smallint(6) NOT NULL,
  `lock_time` datetime NOT NULL,
  `expiration_time` datetime NOT NULL,
  PRIMARY KEY (`resource_lock_id`),
  UNIQUE KEY `IX1_RESOURCE_LOCK` (`resource_name`)
);

CREATE TABLE IF NOT EXISTS `message` (
  `message_id` bigint(20) unsigned NOT NULL AUTO_INCREMENT,
  `ext_message_id` varchar(64),
  `request_param` TEXT NOT NULL,
  `request_body` TEXT NOT NULL,
  `arrived_timestamp` datetime NOT NULL,
  `started_timestamp` datetime,
  `completed_timestamp` datetime,
  `response_timestamp` datetime,
  `response_param` TEXT,
  `response_body` TEXT,
  `resolution` varchar(20),
  `queue_type` varchar(64),
  `queue_id` varchar(256),
  PRIMARY KEY (`message_id`)
);

CREATE INDEX IF NOT EXISTS `ix1_message`
ON message(queue_type, queue_id);

CREATE TABLE IF NOT EXISTS `message_status` (
  `message_status_id` bigint(20) unsigned NOT NULL AUTO_INCREMENT,
  `message_id` bigint(20) unsigned NOT NULL REFERENCES message(message_id),
  `status` varchar(20) NOT NULL,
  `status_timestamp` datetime NOT NULL,
  PRIMARY KEY (`message_status_id`)
);

CREATE INDEX IF NOT EXISTS `ix1_message_status`
ON message_status(message_id);

CREATE TABLE IF NOT EXISTS `message_action` (
  `message_action_id` bigint(20) unsigned NOT NULL AUTO_INCREMENT,
  `message_id` bigint(20) unsigned NOT NULL REFERENCES message(message_id),
  `action` varchar(20) NOT NULL,
  `action_status` varchar(20) NOT NULL,
  `resolution` varchar(20),
  `action_timestamp` datetime NOT NULL,
  `done_timestamp` datetime,
  `hold_time` int,
  `response_param` TEXT,
  `response_body` TEXT,
  PRIMARY KEY (`message_action_id`)
);

CREATE INDEX IF NOT EXISTS `ix1_message_action`
ON message_action(message_id);
