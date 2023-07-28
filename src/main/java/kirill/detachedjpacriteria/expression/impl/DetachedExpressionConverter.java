package kirill.detachedjpacriteria.expression.impl;

import java.util.List;
import javax.persistence.criteria.Expression;

@FunctionalInterface
interface DetachedExpressionConverter {
  Expression<?> convert(
      DetachedExpressionImpl<?> expressionToConvert,
      DetachedExpressionType expressionType,
      List<?> originalArguments,
      List<Expression<?>> convertedArguments,
      ExpressionConverterContext context
  );
}
