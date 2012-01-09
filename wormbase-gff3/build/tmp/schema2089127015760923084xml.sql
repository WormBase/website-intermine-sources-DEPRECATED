
-----------------------------------------------------------------------------
-- Item
-----------------------------------------------------------------------------
DROP TABLE Item CASCADE;


CREATE TABLE Item
(
    implementations TEXT,
    className TEXT,
    identifier TEXT,
    id INTEGER NOT NULL,
    CONSTRAINT Item_pkey UNIQUE (id)
);


-----------------------------------------------------------------------------
-- Attribute
-----------------------------------------------------------------------------
DROP TABLE Attribute CASCADE;


CREATE TABLE Attribute
(
    intermine_value TEXT,
    id INTEGER NOT NULL,
    name TEXT,
    itemId INTEGER,
    CONSTRAINT Attribute_pkey UNIQUE (id)
);


-----------------------------------------------------------------------------
-- ReferenceList
-----------------------------------------------------------------------------
DROP TABLE ReferenceList CASCADE;


CREATE TABLE ReferenceList
(
    refIds TEXT,
    id INTEGER NOT NULL,
    name TEXT,
    itemId INTEGER,
    CONSTRAINT ReferenceList_pkey UNIQUE (id)
);


-----------------------------------------------------------------------------
-- Reference
-----------------------------------------------------------------------------
DROP TABLE Reference CASCADE;


CREATE TABLE Reference
(
    id INTEGER NOT NULL,
    name TEXT,
    refId TEXT,
    itemId INTEGER,
    CONSTRAINT Reference_pkey UNIQUE (id)
);


-----------------------------------------------------------------------------
-- intermine_metadata
-----------------------------------------------------------------------------
DROP TABLE intermine_metadata CASCADE;


CREATE TABLE intermine_metadata
(
    key TEXT,
    value TEXT,
    blob_value BYTEA,
    CONSTRAINT intermine_metadata_key UNIQUE (key)
);


-----------------------------------------------------------------------------
-- osbag_int
-----------------------------------------------------------------------------
DROP TABLE osbag_int CASCADE;


CREATE TABLE osbag_int
(
    bagid INTEGER,
    value INTEGER,
    CONSTRAINT osbag_int_index1 UNIQUE (bagid, value)
);
CREATE INDEX osbag_int_index2 ON osbag_int (value, bagid);


-----------------------------------------------------------------------------
-- clob
-----------------------------------------------------------------------------
DROP TABLE clob CASCADE;


CREATE TABLE clob
(
    clobid INTEGER,
    clobpage INTEGER,
    value TEXT,
    CONSTRAINT clob_index UNIQUE (clobid, clobpage)
);


----------------------------------------------------------------------
-- clob                                                      
----------------------------------------------------------------------


----------------------------------------------------------------------
-- Item                                                      
----------------------------------------------------------------------


----------------------------------------------------------------------
-- Attribute                                                      
----------------------------------------------------------------------


----------------------------------------------------------------------
-- ReferenceList                                                      
----------------------------------------------------------------------


----------------------------------------------------------------------
-- Reference                                                      
----------------------------------------------------------------------


----------------------------------------------------------------------
-- intermine_metadata                                                      
----------------------------------------------------------------------


----------------------------------------------------------------------
-- osbag_int                                                      
----------------------------------------------------------------------

