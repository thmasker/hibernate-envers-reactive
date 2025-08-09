CREATE SEQUENCE id START WITH 1 INCREMENT BY 50;

CREATE SEQUENCE revinfo_seq START WITH 1 INCREMENT BY 50;

CREATE TABLE address
(
    id      INTEGER NOT NULL,
    city    VARCHAR(255),
    country VARCHAR(255),
    street  VARCHAR(255),
    zip     VARCHAR(255),
    PRIMARY KEY (id)
);

CREATE TABLE address_aud
(
    id      INTEGER NOT NULL,
    rev     INTEGER NOT NULL,
    revtype SMALLINT,
    city    VARCHAR(255),
    country VARCHAR(255),
    street  VARCHAR(255),
    zip     VARCHAR(255),
    PRIMARY KEY (rev, id)
);

CREATE TABLE person
(
    id      INTEGER NOT NULL,
    name    VARCHAR(255),
    surname VARCHAR(255),
    address INTEGER,
    PRIMARY KEY (id)
);

CREATE TABLE person_aud
(
    id      INTEGER NOT NULL,
    rev     INTEGER NOT NULL,
    revtype SMALLINT,
    name    VARCHAR(255),
    surname VARCHAR(255),
    address INTEGER,
    PRIMARY KEY (rev, id)
);

CREATE TABLE revinfo
(
    rev      INTEGER NOT NULL,
    revtstmp BIGINT,
    PRIMARY KEY (rev)
);

ALTER TABLE address_aud
    ADD CONSTRAINT fk_address_aud_rev FOREIGN KEY (rev) REFERENCES revinfo;
ALTER TABLE person
    ADD CONSTRAINT fk_person_address FOREIGN KEY (address) REFERENCES address;
ALTER TABLE person_aud
    ADD CONSTRAINT fk_person_aud_rev FOREIGN KEY (rev) REFERENCES revinfo;
