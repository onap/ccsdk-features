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
