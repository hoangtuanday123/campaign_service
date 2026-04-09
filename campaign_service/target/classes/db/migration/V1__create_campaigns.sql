create table if not exists campaigns (
    id char(36) primary key,
    name varchar(150) not null,
    status varchar(20) not null,
    start_time timestamp(6) not null,
    end_time timestamp(6) not null,
    quota int not null,
    used_count int not null default 0,
    rule_id varchar(100) not null,
    created_at timestamp(6) not null,
    updated_at timestamp(6) not null,
    constraint chk_campaign_quota_non_negative check (quota >= 0),
    constraint chk_campaign_used_count_non_negative check (used_count >= 0),
    constraint chk_campaign_used_count_lte_quota check (used_count <= quota),
    constraint chk_campaign_schedule check (end_time > start_time)
);

create index idx_campaigns_status_schedule on campaigns (status, start_time, end_time);