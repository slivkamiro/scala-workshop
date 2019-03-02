create table if not exists songs (
  id       bigint       not null auto_increment primary key,
  artist   varchar(100) not null,
  title    varchar(100) not null,
  start    int          not null,
  end      int          not null,
  userName varchar(100) not null
);

create table if not exists users (
  login    varchar(100) not null primary key,
  secret varchar(100) not null
);

merge into users key(login) values('test', '1234');