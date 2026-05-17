-- V4: Composite indexes for RNF-23 (game listing response time < 500 ms target)
--
-- Rationale:
--   findActiveWithFilters filters by status='ACTIVE' (covered by idx_games_status from V1).
--   Adding (status, price) lets the planner satisfy price-range predicates without a
--   separate sort on price when status is already selective.
--   findByBuyerOrderByCreatedAtDesc benefits from (buyer_id, created_at DESC) so
--   PostgreSQL can avoid a filesort on the already-filtered buyer rows.

CREATE INDEX IF NOT EXISTS idx_games_status_price
    ON games(status, price);

CREATE INDEX IF NOT EXISTS idx_games_publisher_status
    ON games(publisher_id, status);

CREATE INDEX IF NOT EXISTS idx_orders_buyer_created
    ON orders(buyer_id, created_at DESC);
