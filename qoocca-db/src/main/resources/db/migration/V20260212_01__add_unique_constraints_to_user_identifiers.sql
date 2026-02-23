SET @sql = (
    SELECT IF(
        EXISTS(
            SELECT 1
            FROM information_schema.statistics
            WHERE table_schema = DATABASE()
              AND table_name = 'user'
              AND index_name = 'uk_user_phone_number'
        ),
        'SELECT 1',
        'ALTER TABLE `user` ADD UNIQUE KEY `uk_user_phone_number` (`user_phone_number`)'
    )
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @sql = (
    SELECT IF(
        EXISTS(
            SELECT 1
            FROM information_schema.statistics
            WHERE table_schema = DATABASE()
              AND table_name = 'user'
              AND index_name = 'uk_user_kakao_id'
        ),
        'SELECT 1',
        'ALTER TABLE `user` ADD UNIQUE KEY `uk_user_kakao_id` (`kakao_id`)'
    )
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @sql = (
    SELECT IF(
        EXISTS(
            SELECT 1
            FROM information_schema.statistics
            WHERE table_schema = DATABASE()
              AND table_name = 'user'
              AND index_name = 'uk_user_naver_id'
        ),
        'SELECT 1',
        'ALTER TABLE `user` ADD UNIQUE KEY `uk_user_naver_id` (`naver_id`)'
    )
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;
