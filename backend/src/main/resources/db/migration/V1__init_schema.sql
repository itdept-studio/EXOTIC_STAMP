create table if not exists users (
    id bigserial primary key,
    email varchar(255) not null unique,
    full_name varchar(255),
    created_at timestamp not null default current_timestamp
);

create table if not exists stations (
    id bigserial primary key,
    code varchar(100) not null unique,
    name varchar(255) not null,
    created_at timestamp not null default current_timestamp
);

create table if not exists rewards (
    id bigserial primary key,
    code varchar(100) not null unique,
    name varchar(255) not null,
    required_stamps integer not null default 0,
    created_at timestamp not null default current_timestamp
);

