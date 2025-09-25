ALTER TABLE work_order
DROP
CONSTRAINT work_order_user_id_fkey;

ALTER TABLE work_order
    ADD assigned_at TIMESTAMP WITHOUT TIME ZONE;

ALTER TABLE app_user
    ADD email VARCHAR(100);

ALTER TABLE app_user
    ADD password VARCHAR(255);

ALTER TABLE app_user
    ALTER COLUMN email SET NOT NULL;

ALTER TABLE app_user
    ALTER COLUMN password SET NOT NULL;

ALTER TABLE work_order
    ADD CONSTRAINT uc_work_order_cur_tech UNIQUE (cur_tech_id);

ALTER TABLE work_order
    ADD CONSTRAINT uc_work_order_customer UNIQUE (customer_id);

ALTER TABLE work_order
DROP
COLUMN user_id;

ALTER TABLE customer
ALTER
COLUMN address_line TYPE VARCHAR(255) USING (address_line::VARCHAR(255));

ALTER TABLE customer
ALTER
COLUMN city TYPE VARCHAR(255) USING (city::VARCHAR(255));

ALTER TABLE work_order
ALTER
COLUMN description TYPE VARCHAR(255) USING (description::VARCHAR(255));

ALTER TABLE work_order
    ALTER COLUMN description SET NOT NULL;

ALTER TABLE customer
ALTER
COLUMN email TYPE VARCHAR(255) USING (email::VARCHAR(255));

ALTER TABLE app_user
ALTER
COLUMN full_name TYPE VARCHAR(100) USING (full_name::VARCHAR(100));

ALTER TABLE customer
ALTER
COLUMN name TYPE VARCHAR(255) USING (name::VARCHAR(255));

ALTER TABLE customer
ALTER
COLUMN phone TYPE VARCHAR(255) USING (phone::VARCHAR(255));

ALTER TABLE customer
ALTER
COLUMN postal_code TYPE VARCHAR(255) USING (postal_code::VARCHAR(255));

ALTER TABLE app_user
DROP
COLUMN role;

ALTER TABLE app_user
    ADD role VARCHAR(20) NOT NULL;

ALTER TABLE work_order
ALTER
COLUMN title TYPE VARCHAR(255) USING (title::VARCHAR(255));

ALTER TABLE work_order
    ALTER COLUMN updated_at DROP NOT NULL;

ALTER TABLE app_user
ALTER
COLUMN username TYPE VARCHAR(50) USING (username::VARCHAR(50));