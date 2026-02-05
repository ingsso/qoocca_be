-- Add class-level payer parent reference for each student enrollment.
ALTER TABLE class_info_student
    ADD COLUMN payer_parent_id BIGINT NULL;

CREATE INDEX idx_class_info_student_payer_parent_id
    ON class_info_student (payer_parent_id);

ALTER TABLE class_info_student
    ADD CONSTRAINT fk_class_info_student_payer_parent
        FOREIGN KEY (payer_parent_id)
            REFERENCES parent (parent_id)
            ON UPDATE CASCADE
            ON DELETE SET NULL;

-- Optional backfill:
-- If you want existing rows to have a payer, fill nulls with the first-registered parent.
-- UPDATE class_info_student cis
-- JOIN (
--   SELECT sp.student_id, MIN(sp.student_parent_id) AS first_sp_id
--   FROM student_parent sp
--   GROUP BY sp.student_id
-- ) x ON x.student_id = cis.student_id
-- JOIN student_parent spf ON spf.student_parent_id = x.first_sp_id
-- SET cis.payer_parent_id = spf.parent_id
-- WHERE cis.payer_parent_id IS NULL;
