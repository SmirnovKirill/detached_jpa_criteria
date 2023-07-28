package kirill.detachedjpacriteria.query;

import java.util.List;
import kirill.detachedjpacriteria.expression.api.DetachedExpression;
import kirill.detachedjpacriteria.expression.api.DetachedPredicate;

interface CriteriaQueryWithGrouping {
  List<DetachedExpression<?>> getGroupByExpressions();
  List<DetachedExpression<Boolean>> getHavingExpressions();
  List<DetachedPredicate> getHavingPredicates();
}
