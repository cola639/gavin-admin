-- Email async task tables (MySQL)
create table sys_email_task (
  task_id           bigint primary key auto_increment,
  biz_type          varchar(64),
  biz_id            varchar(64),
  template_name     varchar(128),
  subject           varchar(255) not null,
  content           longtext,
  content_type      varchar(16) not null,
  sender            varchar(255) not null,
  status            varchar(16) not null,
  total_count       int not null default 0,
  success_count     int not null default 0,
  fail_count        int not null default 0,
  idempotency_key   varchar(128),
  created_by        bigint not null,
  created_at        datetime not null,
  updated_at        datetime not null,
  unique key uk_idempotency (biz_type, biz_id, idempotency_key)
);

create table sys_email_task_item (
  item_id             bigint primary key auto_increment,
  task_id             bigint not null,
  to_email            varchar(320) not null,
  cc_emails           text,
  bcc_emails          text,
  variables_json      text,
  rendered_snapshot   longtext,
  provider_message_id varchar(128),
  status              varchar(16) not null,
  retry_count         int not null default 0,
  next_retry_at       datetime,
  last_error          varchar(1000),
  created_at          datetime not null,
  updated_at          datetime not null,
  index idx_task (task_id),
  index idx_status_retry (status, next_retry_at)
);
