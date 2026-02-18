-- Index for duplicate detection in process() method
CREATE INDEX idx_history_duplicate_check ON jahia_contenthistory(uuid, entry_date, property_name, entry_action);

-- Index for date-based queries (getMostRecentTimeInHistory and deleteHistoryBeforeDate)
CREATE INDEX idx_history_date ON jahia_contenthistory(entry_date);

-- Index for getNodeHistory() method
CREATE INDEX idx_history_uuid_date ON jahia_contenthistory(uuid, entry_date);
