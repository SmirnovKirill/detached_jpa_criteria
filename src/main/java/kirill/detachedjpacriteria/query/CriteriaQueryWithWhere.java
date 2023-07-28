package kirill.detachedjpacriteria.query;

import java.util.List;
import kirill.detachedjpacriteria.expression.api.DetachedExpression;
import kirill.detachedjpacriteria.expression.api.DetachedPredicate;

interface CriteriaQueryWithWhere {
  List<DetachedExpression<Boolean>> getWhereExpressions();

  List<DetachedPredicate> getWherePredicates();
}
