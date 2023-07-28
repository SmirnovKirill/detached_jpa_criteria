package kirill.detachedjpacriteria.query;

import java.util.List;

interface CriteriaQueryWithOrder {
  List<DetachedCriteriaQueryImpl.Order> getOrders();
}
