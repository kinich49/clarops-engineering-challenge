SELECT tt.id, e.event_name, tt.status
FROM trace_transitions as tt
INNER JOIN events e
ON e.event_id = tt.event_id
WHERE tt.trace_id = 'trace-101';

SELECT e.event_id, e.event_name, e.accepted
FROM events as e
WHERE e.trace_id = 'trace-101';