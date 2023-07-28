package kirill.detachedjpacriteria.expression.impl;

import java.util.List;
import javax.persistence.criteria.Expression;

//Этот интерфейс объединяет обычные выражения и подзапросы
public interface DetachedExpressionCommonImpl<T> {
  Expression<T> toJpaExpression(ExpressionConverterContext context);

  List<DetachedExpressionCommonImpl<?>> getAllExpressionsDeep();
}
