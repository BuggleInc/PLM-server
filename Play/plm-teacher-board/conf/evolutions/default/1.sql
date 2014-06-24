# --- Created by Ebean DDL
# To stop Ebean DDL generation, remove this comment and start using Evolutions

# --- !Ups

create table course (
  name                      varchar(255) not null,
  constraint pk_course primary key (name))
;

create table student (
  uuid                      varchar(255) not null,
  name                      varchar(255),
  mail                      varchar(255),
  constraint pk_student primary key (uuid))
;

create table teacher (
  name                      varchar(255) not null,
  constraint pk_teacher primary key (name))
;


create table course_student (
  course_name                    varchar(255) not null,
  student_uuid                   varchar(255) not null,
  constraint pk_course_student primary key (course_name, student_uuid))
;

create table course_teacher (
  course_name                    varchar(255) not null,
  teacher_name                   varchar(255) not null,
  constraint pk_course_teacher primary key (course_name, teacher_name))
;

create table student_course (
  student_uuid                   varchar(255) not null,
  course_name                    varchar(255) not null,
  constraint pk_student_course primary key (student_uuid, course_name))
;

create table teacher_course (
  teacher_name                   varchar(255) not null,
  course_name                    varchar(255) not null,
  constraint pk_teacher_course primary key (teacher_name, course_name))
;
create sequence course_seq;

create sequence student_seq;

create sequence teacher_seq;




alter table course_student add constraint fk_course_student_course_01 foreign key (course_name) references course (name) on delete restrict on update restrict;

alter table course_student add constraint fk_course_student_student_02 foreign key (student_uuid) references student (uuid) on delete restrict on update restrict;

alter table course_teacher add constraint fk_course_teacher_course_01 foreign key (course_name) references course (name) on delete restrict on update restrict;

alter table course_teacher add constraint fk_course_teacher_teacher_02 foreign key (teacher_name) references teacher (name) on delete restrict on update restrict;

alter table student_course add constraint fk_student_course_student_01 foreign key (student_uuid) references student (uuid) on delete restrict on update restrict;

alter table student_course add constraint fk_student_course_course_02 foreign key (course_name) references course (name) on delete restrict on update restrict;

alter table teacher_course add constraint fk_teacher_course_teacher_01 foreign key (teacher_name) references teacher (name) on delete restrict on update restrict;

alter table teacher_course add constraint fk_teacher_course_course_02 foreign key (course_name) references course (name) on delete restrict on update restrict;

# --- !Downs

SET REFERENTIAL_INTEGRITY FALSE;

drop table if exists course;

drop table if exists course_student;

drop table if exists course_teacher;

drop table if exists student;

drop table if exists student_course;

drop table if exists teacher;

drop table if exists teacher_course;

SET REFERENTIAL_INTEGRITY TRUE;

drop sequence if exists course_seq;

drop sequence if exists student_seq;

drop sequence if exists teacher_seq;

