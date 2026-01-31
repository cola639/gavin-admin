-- =========================
-- 10 baseline_request records (different statuses)
-- =========================
insert into baseline_request (id, request_no, title, owner_id, owner_name, reviewer_id, reviewer_name,
                              status, approval_status, current_step,
                              submitted_at, last_reviewed_at, published_at, retired_at,
                              version, created_by, created_at, updated_by, updated_at)
values
-- 1) Draft (not submitted yet)
(1, 'BR-2026-0001', 'Draft request - not submitted', 'owner01', 'Platform Owner A', null, null,
 'DRAFT', 'NOT_REQUIRED', 'OWNER_SUBMIT',
 null, null, null, null,
 0, 'owner01', now(), 'owner01', now()),

-- 2) Submitted -> pending review
(2, 'BR-2026-0002', 'Submitted, waiting for SME review', 'owner02', 'Platform Owner B', 'sme01', 'Cyber SME A',
 'DRAFT', 'PENDING', 'SME_REVIEW',
 now(), null, null, null,
 0, 'owner02', now(), 'owner02', now()),

-- 3) In review (still pending)
(3, 'BR-2026-0003', 'In review (pending)', 'owner03', 'Platform Owner C', 'sme02', 'Cyber SME B',
 'DRAFT', 'PENDING', 'SME_REVIEW',
 date_sub(now(), interval 2 day), null, null, null,
 1, 'owner03', date_sub(now(), interval 2 day), 'sme02', now()),

-- 4) Approved but not yet published (if you allow this intermediate state)
(4, 'BR-2026-0004', 'Approved, waiting to publish', 'owner04', 'Platform Owner D', 'sme01', 'Cyber SME A',
 'DRAFT', 'APPROVED', 'END',
 date_sub(now(), interval 3 day), date_sub(now(), interval 1 day), null, null,
 2, 'owner04', date_sub(now(), interval 3 day), 'sme01', date_sub(now(), interval 1 day)),

-- 5) Published & approved (normal happy path)
(5, 'BR-2026-0005', 'Published baseline (approved)', 'owner05', 'Platform Owner E', 'sme03', 'Cyber SME C',
 'PUBLISHED', 'APPROVED', 'END',
 date_sub(now(), interval 10 day), date_sub(now(), interval 9 day), date_sub(now(), interval 9 day), null,
 1, 'owner05', date_sub(now(), interval 10 day), 'system', date_sub(now(), interval 9 day)),

-- 6) Published but approval still pending (your special requirement)
(6, 'BR-2026-0006', 'Published but not approved yet (pending)', 'owner06', 'Platform Owner F', 'sme02', 'Cyber SME B',
 'PUBLISHED', 'PENDING', 'SME_REVIEW',
 date_sub(now(), interval 5 day), null, date_sub(now(), interval 1 day), null,
 0, 'owner06', date_sub(now(), interval 5 day), 'owner06', now()),

-- 7) Rejected
(7, 'BR-2026-0007', 'Rejected by SME', 'owner07', 'Platform Owner G', 'sme01', 'Cyber SME A',
 'DRAFT', 'REJECTED', 'END',
 date_sub(now(), interval 7 day), date_sub(now(), interval 6 day), null, null,
 0, 'owner07', date_sub(now(), interval 7 day), 'sme01', date_sub(now(), interval 6 day)),

-- 8) Published then retired (fully done)
(8, 'BR-2026-0008', 'Published then retired', 'owner08', 'Platform Owner H', 'sme03', 'Cyber SME C',
 'RETIRED', 'APPROVED', 'END',
 date_sub(now(), interval 40 day), date_sub(now(), interval 39 day), date_sub(now(), interval 39 day),
 date_sub(now(), interval 5 day),
 3, 'owner08', date_sub(now(), interval 40 day), 'owner08', date_sub(now(), interval 5 day)),

-- 9) Retired but approval pending (edge case, usually avoid but possible)
(9, 'BR-2026-0009', 'Retired while approval pending (edge)', 'owner09', 'Platform Owner I', 'sme02', 'Cyber SME B',
 'RETIRED', 'PENDING', 'SME_REVIEW',
 date_sub(now(), interval 15 day), null, date_sub(now(), interval 14 day), date_sub(now(), interval 1 day),
 1, 'owner09', date_sub(now(), interval 15 day), 'owner09', now()),

-- 10) Published & NOT_REQUIRED (no approval required workflow)
(10, 'BR-2026-0010', 'Published (no approval required)', 'owner10', 'Platform Owner J', null, null,
 'PUBLISHED', 'NOT_REQUIRED', 'END',
 date_sub(now(), interval 1 day), null, date_sub(now(), interval 1 day), null,
 0, 'owner10', date_sub(now(), interval 1 day), 'system', date_sub(now(), interval 1 day));



-- =========================
-- baseline_task (1 task per request for demo)
-- =========================
insert into baseline_task (id, request_id, step_code, assignee_role, assignee_id, assignee_name,
                           status, decision, comment, created_at, acted_at)
values (1001, 2, 'SME_REVIEW', 'CYBER_SME', 'sme01', 'Cyber SME A', 'PENDING', null, null, now(), null),
       (1002, 3, 'SME_REVIEW', 'CYBER_SME', 'sme02', 'Cyber SME B', 'PENDING', null, null,
        date_sub(now(), interval 2 day), null),
       (1003, 4, 'SME_REVIEW', 'CYBER_SME', 'sme01', 'Cyber SME A', 'APPROVED', 'APPROVE', 'Looks good',
        date_sub(now(), interval 3 day), date_sub(now(), interval 1 day)),
       (1004, 5, 'SME_REVIEW', 'CYBER_SME', 'sme03', 'Cyber SME C', 'APPROVED', 'APPROVE', 'Approved and published',
        date_sub(now(), interval 10 day), date_sub(now(), interval 9 day)),
       (1005, 6, 'SME_REVIEW', 'CYBER_SME', 'sme02', 'Cyber SME B', 'PENDING', null, 'Published early; review pending',
        date_sub(now(), interval 5 day), null),
       (1006, 7, 'SME_REVIEW', 'CYBER_SME', 'sme01', 'Cyber SME A', 'REJECTED', 'REJECT', 'Missing required controls',
        date_sub(now(), interval 7 day), date_sub(now(), interval 6 day)),
       (1007, 8, 'SME_REVIEW', 'CYBER_SME', 'sme03', 'Cyber SME C', 'APPROVED', 'APPROVE', 'Approved',
        date_sub(now(), interval 40 day), date_sub(now(), interval 39 day)),
       (1008, 9, 'SME_REVIEW', 'CYBER_SME', 'sme02', 'Cyber SME B', 'PENDING', null,
        'Pending even though retired (edge)', date_sub(now(), interval 15 day), null);



-- =========================
-- baseline_event (timeline)
-- =========================
insert into baseline_event (id, request_id, task_id, event_type, actor_role, actor_id, actor_name, message, created_at)
values (2001, 2, 1001, 'SUBMIT', 'OWNER', 'owner02', 'Platform Owner B', 'Submitted baseline request', now()),
       (2002, 2, 1001, 'REVIEW_REQUESTED', 'SYSTEM', null, 'System', 'Email sent to Cyber SME', now()),

       (2003, 4, 1003, 'SUBMIT', 'OWNER', 'owner04', 'Platform Owner D', 'Submitted baseline request',
        date_sub(now(), interval 3 day)),
       (2004, 4, 1003, 'APPROVE', 'CYBER_SME', 'sme01', 'Cyber SME A', 'Approved baseline request',
        date_sub(now(), interval 1 day)),

       (2005, 5, 1004, 'SUBMIT', 'OWNER', 'owner05', 'Platform Owner E', 'Submitted baseline request',
        date_sub(now(), interval 10 day)),
       (2006, 5, 1004, 'APPROVE', 'CYBER_SME', 'sme03', 'Cyber SME C', 'Approved baseline request',
        date_sub(now(), interval 9 day)),
       (2007, 5, null, 'PUBLISH', 'SYSTEM', null, 'System', 'Published baseline', date_sub(now(), interval 9 day)),

       (2008, 6, 1005, 'SUBMIT', 'OWNER', 'owner06', 'Platform Owner F', 'Submitted baseline request',
        date_sub(now(), interval 5 day)),
       (2009, 6, null, 'PUBLISH', 'OWNER', 'owner06', 'Platform Owner F', 'Published while approval pending',
        date_sub(now(), interval 1 day)),

       (2010, 7, 1006, 'SUBMIT', 'OWNER', 'owner07', 'Platform Owner G', 'Submitted baseline request',
        date_sub(now(), interval 7 day)),
       (2011, 7, 1006, 'REJECT', 'CYBER_SME', 'sme01', 'Cyber SME A',
        'Rejected baseline request: Missing required controls', date_sub(now(), interval 6 day)),

       (2012, 8, 1007, 'PUBLISH', 'SYSTEM', null, 'System', 'Published baseline', date_sub(now(), interval 39 day)),
       (2013, 8, null, 'RETIRE', 'OWNER', 'owner08', 'Platform Owner H', 'Retired baseline',
        date_sub(now(), interval 5 day));



-- =========================
-- baseline_notification_outbox (a few demo notifications)
-- =========================
insert into baseline_notification_outbox (id, request_id, event_id, channel, template_code, to_address, subject,
                                          payload_json,
                                          status, retry_count, last_error, created_at, sent_at)
values (3001, 2, 2002, 'EMAIL', 'BASELINE_REVIEW_REQUESTED', 'sme01@example.com', 'Baseline review requested',
        json_object('requestNo', 'BR-2026-0002', 'title', 'Submitted, waiting for SME review'),
        'SENT', 0, null, now(), now()),

       (3002, 5, 2007, 'EMAIL', 'BASELINE_PUBLISHED', 'owner05@example.com', 'Baseline published',
        json_object('requestNo', 'BR-2026-0005', 'title', 'Published baseline (approved)'),
        'SENT', 0, null, date_sub(now(), interval 9 day), date_sub(now(), interval 9 day)),

       (3003, 7, 2011, 'EMAIL', 'BASELINE_REJECTED', 'owner07@example.com', 'Baseline rejected',
        json_object('requestNo', 'BR-2026-0007', 'title', 'Rejected by SME', 'reason', 'Missing required controls'),
        'SENT', 0, null, date_sub(now(), interval 6 day), date_sub(now(), interval 6 day)),

       (3004, 6, 2009, 'EMAIL', 'BASELINE_PUBLISHED_PENDING_APPROVAL', 'sme02@example.com',
        'Baseline published (approval pending)',
        json_object('requestNo', 'BR-2026-0006', 'title', 'Published but not approved yet (pending)'),
        'PENDING', 0, null, date_sub(now(), interval 1 day), null);
