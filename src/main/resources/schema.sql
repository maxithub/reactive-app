drop table if exists app_user;

create table app_user (
  id varchar (20) not null,
  first_name varchar (200) not null,
  last_name varchar (200) not null,
  middle_name varchar (200),
  gender varchar (50) not null,
  age smallint not null,
  province varchar (200) not null,
  city varchar (200),
  primary key (id)
);