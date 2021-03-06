# --- !Ups

create table assistance_call (
  id                        varchar(255) not null,
  hostname                  varchar(255),
  date                      varchar(255),
  details                   varchar(255),
  student_hashed_uuid       varchar(255),
  constraint pk_assistance_call primary key (id))
;

create table course (
  name                      varchar(255) not null,
  display_name              varchar(255),
  programming_language      varchar(255),
  constraint pk_course primary key (name))
;

create table student (
  hashed_uuid               varchar(255) not null,
  uuid                      varchar(255),
  name                      varchar(255),
  mail                      varchar(255),
  constraint pk_student primary key (hashed_uuid))
;

create table teacher (
  name                      varchar(255) not null,
  password                  varchar(255),
  constraint pk_teacher primary key (name))
;


create table course_student (
  course_name                    varchar(255) not null,
  student_hashed_uuid            varchar(255) not null,
  constraint pk_course_student primary key (course_name, student_hashed_uuid))
;

create table course_teacher (
  course_name                    varchar(255) not null,
  teacher_name                   varchar(255) not null,
  constraint pk_course_teacher primary key (course_name, teacher_name))
;
create sequence assistance_call_seq;

create sequence course_seq;

create sequence student_seq;

create sequence teacher_seq;

alter table assistance_call add constraint fk_assistance_call_student_1 foreign key (student_hashed_uuid) references student (hashed_uuid) on delete restrict on update restrict;
create index ix_assistance_call_student_1 on assistance_call (student_hashed_uuid);



alter table course_student add constraint fk_course_student_course_01 foreign key (course_name) references course (name) on delete restrict on update restrict;

alter table course_student add constraint fk_course_student_student_02 foreign key (student_hashed_uuid) references student (hashed_uuid) on delete restrict on update restrict;

alter table course_teacher add constraint fk_course_teacher_course_01 foreign key (course_name) references course (name) on delete restrict on update restrict;

alter table course_teacher add constraint fk_course_teacher_teacher_02 foreign key (teacher_name) references teacher (name) on delete restrict on update restrict;

# --- !Downs

SET REFERENTIAL_INTEGRITY FALSE;

drop table if exists assistance_call;

drop table if exists course;

drop table if exists course_student;

drop table if exists course_teacher;

drop table if exists student;

drop table if exists teacher;

SET REFERENTIAL_INTEGRITY TRUE;

drop sequence if exists assistance_call_seq;

drop sequence if exists course_seq;

drop sequence if exists student_seq;

drop sequence if exists teacher_seq;

