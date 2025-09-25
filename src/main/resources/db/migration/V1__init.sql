CREATE TYPE user_role AS ENUM ('TECHNICIAN', 'TEAM_LEADER');
CREATE TYPE work_order_status AS ENUM ('UNASSIGNED', 'ASSIGNED', 'SCHEDULED', 'COMPLETED', 'CANCELLED');
CREATE TYPE assignment_state AS ENUM ('ACTIVE', 'HISTORICAL');

CREATE TABLE app_user (
                          id BIGSERIAL PRIMARY KEY,
                          username TEXT UNIQUE NOT NULL,
                          full_name TEXT NOT NULL,
                          role user_role NOT NULL,
                          active BOOLEAN NOT NULL DEFAULT true,
                          created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE TABLE customer (
                          id BIGSERIAL PRIMARY KEY,
                          name TEXT NOT NULL,
                          phone TEXT NOT NULL,
                          email TEXT NOT NULL,
                          address_line TEXT NOT NULL,
                          city TEXT NOT NULL,
                          postal_code TEXT
);

CREATE TABLE work_order (
                            id BIGSERIAL PRIMARY KEY,
                            customer_id BIGINT NOT NULL REFERENCES customer(id),
                            title TEXT NOT NULL,
                            description TEXT,
                            status work_order_status NOT NULL DEFAULT 'UNASSIGNED',
                            user_id BIGINT REFERENCES app_user(id),
                            cur_date DATE,
                            cur_slot SMALLINT,
                            cur_tech_id BIGINT REFERENCES app_user(id),
--                            version INTEGER NOT NULL DEFAULT 0,
                            created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
                            updated_at TIMESTAMPTZ NOT NULL DEFAULT now(),
                            CONSTRAINT chk_slot CHECK (cur_slot IS NULL OR cur_slot BETWEEN 1 AND 6)
);

-- CREATE TABLE assignment (
--                             id BIGSERIAL PRIMARY KEY,
--                             work_order_id BIGINT NOT NULL REFERENCES work_order(id) ON DELETE CASCADE,
--                             technician_id BIGINT NOT NULL REFERENCES app_user(id),
--                             work_date DATE NOT NULL,
--                             slot SMALLINT NOT NULL CHECK (slot BETWEEN 1 AND 6),
--                             state assignment_state NOT NULL DEFAULT 'ACTIVE',
--                             reason TEXT,
--                             created_at TIMESTAMPTZ NOT NULL DEFAULT now()
-- );

-- CREATE UNIQUE INDEX ux_tech_date_slot_active
--     ON assignment(technician_id, work_date, slot)
--     WHERE state = 'ACTIVE';
--
-- CREATE UNIQUE INDEX ux_wo_single_active
--     ON assignment(work_order_id)
--     WHERE state = 'ACTIVE';
