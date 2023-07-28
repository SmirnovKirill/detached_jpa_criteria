package kirill.detachedjpacriteria.expression.impl;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import javax.persistence.EntityManager;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.Expression;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.persistence.criteria.Subquery;
import javax.persistence.metamodel.IdentifiableType;
import javax.persistence.metamodel.SingularAttribute;
import kirill.detachedjpacriteria.expression.api.DetachedExpression;
import static kirill.detachedjpacriteria.expression.impl.AttributePath.emptyPath;
import kirill.detachedjpacriteria.expression.impl.extra.DetachedCaseImpl;
import kirill.detachedjpacriteria.expression.impl.extra.DetachedCoalesceImpl;
import kirill.detachedjpacriteria.expression.impl.extra.DetachedInNotTypeSafeImpl;
import kirill.detachedjpacriteria.expression.impl.extra.DetachedSimpleCaseImpl;
import kirill.detachedjpacriteria.util.Parameter;

/**
 * В этом енуме содержатся все типы выражений которые есть в нашей библиотеке и методы для конвертации наших detached выражений в JPA Criteria
 * выражения. В коде везде отключаются ворнинги про unchecked cast потому что это не страшно, все типы должны быть корректны за счет того что класс
 * DetachedCriteriaBuilder имеет сигнатуры методов такие же как в JPA CriteriaBuilder и валидация будет при вызове этих методов.
 */
public enum DetachedExpressionType {
  TYPE(DetachedExpressionType::convertType),
  PATH(DetachedExpressionType::convertPath),
  AVG(DetachedExpressionType::convertAvg),
  SUM(DetachedExpressionType::convertSum),
  SUM_AS_LONG(DetachedExpressionType::convertSumAsLong),
  SUM_AS_DOUBLE(DetachedExpressionType::convertSumAsDouble),
  MAX(DetachedExpressionType::convertMax),
  MIN(DetachedExpressionType::convertMin),
  GREATEST(DetachedExpressionType::convertGreatest),
  LEAST(DetachedExpressionType::convertLeast),
  COUNT(DetachedExpressionType::convertCount),
  COUNT_DISTINCT(DetachedExpressionType::convertCountDistinct),
  EXISTS(DetachedExpressionType::convertExists),
  ALL(DetachedExpressionType::convertAll),
  SOME(DetachedExpressionType::convertSome),
  ANY(DetachedExpressionType::convertAny),
  AND(DetachedExpressionType::convertAnd),
  OR(DetachedExpressionType::convertOr),
  NOT(DetachedExpressionType::convertNot),
  IS_TRUE(DetachedExpressionType::convertIsTrue),
  IS_FALSE(DetachedExpressionType::convertIsFalse),
  IS_NULL(DetachedExpressionType::convertIsNull),
  IS_NOT_NULL(DetachedExpressionType::convertIsNotNull),
  EQUAL(DetachedExpressionType::convertEqual),
  NOT_EQUAL(DetachedExpressionType::convertNotEqual),
  GREATER_THAN(DetachedExpressionType::convertGreaterThan),
  GREATER_THAN_OR_EQUAL_TO(DetachedExpressionType::convertGreaterThanOrEqualTo),
  LESS_THAN(DetachedExpressionType::convertLessThan),
  LESS_THAN_OR_EQUAL_TO(DetachedExpressionType::convertLessThanOrEqualTo),
  BETWEEN(DetachedExpressionType::convertBetween),
  GT(DetachedExpressionType::convertGt),
  GE(DetachedExpressionType::convertGe),
  LT(DetachedExpressionType::convertLt),
  LE(DetachedExpressionType::convertLe),
  NEG(DetachedExpressionType::convertNeg),
  ABS(DetachedExpressionType::convertAbs),
  PROD(DetachedExpressionType::convertProd),
  DIFF(DetachedExpressionType::convertDiff),
  QUOT(DetachedExpressionType::convertQuot),
  MOD(DetachedExpressionType::convertMod),
  SQRT(DetachedExpressionType::convertSqrt),
  TO_LONG(DetachedExpressionType::convertToLong),
  TO_INTEGER(DetachedExpressionType::convertToInteger),
  TO_FLOAT(DetachedExpressionType::convertToFloat),
  TO_DOUBLE(DetachedExpressionType::convertToDouble),
  TO_BIG_DECIMAL(DetachedExpressionType::convertToBigDecimal),
  TO_BIG_INTEGER(DetachedExpressionType::convertToBigInteger),
  TO_STRING(DetachedExpressionType::convertToString),
  LITERAL(DetachedExpressionType::convertLiteral),
  NULL_LITERAL(DetachedExpressionType::convertNullLiteral),
  PARAMETER(DetachedExpressionType::convertParameter),
  IS_EMPTY(DetachedExpressionType::convertIsEmpty),
  IS_NOT_EMPTY(DetachedExpressionType::convertIsNotEmpty),
  SIZE(DetachedExpressionType::convertSize),
  IS_MEMBER(DetachedExpressionType::convertIsMember),
  IS_NOT_MEMBER(DetachedExpressionType::convertIsNotMember),
  VALUES(DetachedExpressionType::convertValues),
  KEYS(DetachedExpressionType::convertKeys),
  LIKE(DetachedExpressionType::convertLike),
  NOT_LIKE(DetachedExpressionType::convertNotLike),
  CONCAT(DetachedExpressionType::convertConcat),
  SUBSTRING(DetachedExpressionType::convertSubstring),
  TRIM(DetachedExpressionType::convertTrim),
  LOWER(DetachedExpressionType::convertLower),
  UPPER(DetachedExpressionType::convertUpper),
  LENGTH(DetachedExpressionType::convertLength),
  LOCATE(DetachedExpressionType::convertLocate),
  CURRENT_DATE(DetachedExpressionType::convertCurrentDate),
  CURRENT_TIMESTAMP(DetachedExpressionType::convertCurrentTimestamp),
  CURRENT_TIME(DetachedExpressionType::convertCurrentTime),
  IN(DetachedExpressionType::convertIn),
  COALESCE(DetachedExpressionType::convertCoalesce),
  NULL_IF(DetachedExpressionType::convertNullIf),
  SELECT_CASE(DetachedExpressionType::convertSelectCase),
  FUNCTION(DetachedExpressionType::convertFunction);

  private final DetachedExpressionConverter converter;

  DetachedExpressionType(DetachedExpressionConverter converter) {
    this.converter = converter;
  }

  public DetachedExpressionConverter getConverter() {
    return converter;
  }

  private static Expression<?> convertType(
      DetachedExpressionImpl<?> expressionToConvert,
      DetachedExpressionType expressionType,
      List<?> originalArguments,
      List<Expression<?>> convertedArguments,
      ExpressionConverterContext context
  ) {
    assertArgumentCount(1, convertedArguments, expressionType);

    Path<?> detachedPath = (Path<?>) convertedArguments.get(0);

    return detachedPath.type();
  }

  private static Expression<?> convertPath(
      DetachedExpressionImpl<?> expressionToConvert,
      DetachedExpressionType expressionType,
      List<?> originalArguments,
      List<Expression<?>> convertedArguments,
      ExpressionConverterContext context
  ) {
    assertArgumentCount(0, convertedArguments, expressionType);

    DetachedPathImpl<?> detachedPath = (DetachedPathImpl<?>) expressionToConvert;

    if (detachedPath.getMode() == DetachedPathImpl.Mode.ID) {
      return getIdPath(context.getPathContext().getRoot(), context.getEntityManager());
    } else if (detachedPath.getMode() == DetachedPathImpl.Mode.ROOT) {
      return context.getPathContext().getRoot();
    } else if (detachedPath.getMode() == DetachedPathImpl.Mode.PATH) {
      return getPath(detachedPath.getAttributePath(), context.getPathContext());
    } else if (detachedPath.getMode() == DetachedPathImpl.Mode.PARENT_ID) {
      return getIdPath(context.getParentPathContext().getRoot(), context.getEntityManager());
    } else if (detachedPath.getMode() == DetachedPathImpl.Mode.PARENT_ROOT) {
      return context.getParentPathContext().getRoot();
    } else if (detachedPath.getMode() == DetachedPathImpl.Mode.PARENT_PATH) {
      return getPath(detachedPath.getAttributePath(), context.getParentPathContext());
    } else {
      throw new IllegalStateException(String.format("Unexpected mode %s, contact the library developers", detachedPath.getMode()));
    }
  }

  private static <T> Path<?> getIdPath(Root<T> root, EntityManager entityManager) {
    @SuppressWarnings("unchecked")
    IdentifiableType<T> it = (IdentifiableType<T>) entityManager.getMetamodel().managedType(root.getJavaType());
    SingularAttribute<? super T, ?> idAttribute = it.getId(it.getIdType().getJavaType());
    return root.get(idAttribute);
  }

  /**
   * Тут логика довольно непростая. Изначально была простая задумка - для имеющегося пути последовательно делаем .get() начиная от рута и таким
   * образом получаем нужный path. Например, хотим сделать экспрешен на user->posts->comments->text, могли бы просто сделать условно говоря
   * root.get("posts").get("comments").get("text"). Проблема в том что даже если мы все полностью зафетчили, выражение
   * root.get("posts").get("comments") приведет к ошибке Illegal attempt to dereference path source [null.posts] of basic type. Это потому что надо
   * вызывать get("comments") не от root.get("posts") а от результата фетча рута и posts. Поэтому мы заранее когда работаем с Detached Criteria
   * заполняем fetchPaths и joinPaths, и тут по данному пути attributePath пытаемся найти сначала ближайшую точку фетча или джоина и потом добиваем
   * при необходимости .get()'ами.
   */
  private static Expression<?> getPath(AttributePath attributePath, PathContext pathContext) {
    ClosestPathInfo closestPathInfo = getClosestPathInfo(attributePath, pathContext);

    Path<?> path = closestPathInfo.getClosestPath();
    List<String> leftAttributeNames = attributePath.getAttributeNames().subList(
        closestPathInfo.getAttributePath().getAttributeNames().size(),
        attributePath.getAttributeNames().size()
    );
    for (String attributeName : leftAttributeNames) {
      path = path.get(attributeName);
    }

    return path;
  }

  private static ClosestPathInfo getClosestPathInfo(AttributePath attributePath, PathContext pathContext) {
    AttributePath currentPath = attributePath;
    while (!currentPath.isEmpty()) {
      if (pathContext.getFetchPaths().containsKey(currentPath)) {
        return new ClosestPathInfo(pathContext.getFetchPaths().get(currentPath), currentPath);
      } else if (pathContext.getJoinPaths().containsKey(currentPath)) {
        return new ClosestPathInfo(pathContext.getJoinPaths().get(currentPath), currentPath);
      } else {
        currentPath = currentPath.withoutLastAttribute();
      }
    }

    return new ClosestPathInfo(pathContext.getRoot(), emptyPath());
  }

  private static Expression<?> convertAvg(
      DetachedExpressionImpl<?> expressionToConvert,
      DetachedExpressionType expressionType,
      List<?> originalArguments,
      List<Expression<?>> convertedArguments,
      ExpressionConverterContext context
  ) {
    assertArgumentCount(1, convertedArguments, expressionType);

    //noinspection unchecked
    return context.getCriteriaBuilder().avg((Expression<? extends Number>) convertedArguments.get(0));
  }

  private static Expression<?> convertSum(
      DetachedExpressionImpl<?> expressionToConvert,
      DetachedExpressionType expressionType,
      List<?> originalArguments,
      List<Expression<?>> convertedArguments,
      ExpressionConverterContext context
  ) {
    if (convertedArguments.size() == 1) {
      //noinspection unchecked
      return context.getCriteriaBuilder().sum((Expression<? extends Number>) convertedArguments.get(0));
    } else {
      assertArgumentCount(2, convertedArguments, expressionType);

      //noinspection unchecked
      return context.getCriteriaBuilder().sum(
          (Expression<? extends Number>) convertedArguments.get(0),
          (Expression<? extends Number>) convertedArguments.get(1)
      );
    }
  }

  private static Expression<?> convertSumAsLong(
      DetachedExpressionImpl<?> expressionToConvert,
      DetachedExpressionType expressionType,
      List<?> originalArguments,
      List<Expression<?>> convertedArguments,
      ExpressionConverterContext context
  ) {
    assertArgumentCount(1, convertedArguments, expressionType);

    //noinspection unchecked
    return context.getCriteriaBuilder().sumAsLong((Expression<Integer>) convertedArguments.get(0));
  }

  private static Expression<?> convertSumAsDouble(
      DetachedExpressionImpl<?> expressionToConvert,
      DetachedExpressionType expressionType,
      List<?> originalArguments,
      List<Expression<?>> convertedArguments,
      ExpressionConverterContext context
  ) {
    assertArgumentCount(1, convertedArguments, expressionType);

    //noinspection unchecked
    return context.getCriteriaBuilder().sumAsDouble((Expression<Float>) convertedArguments.get(0));
  }

  private static Expression<?> convertMax(
      DetachedExpressionImpl<?> expressionToConvert,
      DetachedExpressionType expressionType,
      List<?> originalArguments,
      List<Expression<?>> convertedArguments,
      ExpressionConverterContext context
  ) {
    assertArgumentCount(1, convertedArguments, expressionType);

    //noinspection unchecked
    return context.getCriteriaBuilder().max((Expression<? extends Number>) convertedArguments.get(0));
  }

  private static Expression<?> convertMin(
      DetachedExpressionImpl<?> expressionToConvert,
      DetachedExpressionType expressionType,
      List<?> originalArguments,
      List<Expression<?>> convertedArguments,
      ExpressionConverterContext context
  ) {
    assertArgumentCount(1, convertedArguments, expressionType);

    //noinspection unchecked
    return context.getCriteriaBuilder().min((Expression<? extends Number>) convertedArguments.get(0));
  }

  private static Expression<?> convertGreatest(
      DetachedExpressionImpl<?> expressionToConvert,
      DetachedExpressionType expressionType,
      List<?> originalArguments,
      List<Expression<?>> convertedArguments,
      ExpressionConverterContext context
  ) {
    assertArgumentCount(1, convertedArguments, expressionType);

    //noinspection unchecked
    return context.getCriteriaBuilder().greatest((Expression<Comparable<? super Comparable<?>>>) convertedArguments.get(0));
  }

  private static Expression<?> convertLeast(
      DetachedExpressionImpl<?> expressionToConvert,
      DetachedExpressionType expressionType,
      List<?> originalArguments,
      List<Expression<?>> convertedArguments,
      ExpressionConverterContext context
  ) {
    assertArgumentCount(1, convertedArguments, expressionType);

    List<Expression<Comparable<? super Comparable<?>>>> comparableConvertedArguments = castToComparable(convertedArguments);

    return context.getCriteriaBuilder().least(comparableConvertedArguments.get(0));
  }

  private static List<Expression<Comparable<? super Comparable<?>>>> castToComparable(List<Expression<?>> expressions) {
    //noinspection unchecked
    return expressions.stream()
        .map(expression -> (Expression<Comparable<? super Comparable<?>>>) expression)
        .collect(Collectors.toList());
  }

  private static Expression<?> convertCount(
      DetachedExpressionImpl<?> expressionToConvert,
      DetachedExpressionType expressionType,
      List<?> originalArguments,
      List<Expression<?>> convertedArguments,
      ExpressionConverterContext context
  ) {
    assertArgumentCount(1, convertedArguments, expressionType);

    return context.getCriteriaBuilder().count(convertedArguments.get(0));
  }

  private static Expression<?> convertCountDistinct(
      DetachedExpressionImpl<?> expressionToConvert,
      DetachedExpressionType expressionType,
      List<?> originalArguments,
      List<Expression<?>> convertedArguments,
      ExpressionConverterContext context
  ) {
    assertArgumentCount(1, convertedArguments, expressionType);

    return context.getCriteriaBuilder().countDistinct(convertedArguments.get(0));
  }

  private static Expression<?> convertExists(
      DetachedExpressionImpl<?> expressionToConvert,
      DetachedExpressionType expressionType,
      List<?> originalArguments,
      List<Expression<?>> convertedArguments,
      ExpressionConverterContext context
  ) {
    assertArgumentCount(1, convertedArguments, expressionType);

    return context.getCriteriaBuilder().exists((Subquery<?>) convertedArguments.get(0));
  }

  private static Expression<?> convertAll(
      DetachedExpressionImpl<?> expressionToConvert,
      DetachedExpressionType expressionType,
      List<?> originalArguments,
      List<Expression<?>> convertedArguments,
      ExpressionConverterContext context
  ) {
    assertArgumentCount(1, convertedArguments, expressionType);

    return context.getCriteriaBuilder().all((Subquery<?>) convertedArguments.get(0));
  }

  private static Expression<?> convertSome(
      DetachedExpressionImpl<?> expressionToConvert,
      DetachedExpressionType expressionType,
      List<?> originalArguments,
      List<Expression<?>> convertedArguments,
      ExpressionConverterContext context
  ) {
    assertArgumentCount(1, convertedArguments, expressionType);

    return context.getCriteriaBuilder().some((Subquery<?>) convertedArguments.get(0));
  }

  private static Expression<?> convertAny(
      DetachedExpressionImpl<?> expressionToConvert,
      DetachedExpressionType expressionType,
      List<?> originalArguments,
      List<Expression<?>> convertedArguments,
      ExpressionConverterContext context
  ) {
    assertArgumentCount(1, convertedArguments, expressionType);

    return context.getCriteriaBuilder().any((Subquery<?>) convertedArguments.get(0));
  }

  private static Expression<?> convertAnd(
      DetachedExpressionImpl<?> expressionToConvert,
      DetachedExpressionType expressionType,
      List<?> originalArguments,
      List<Expression<?>> convertedArguments,
      ExpressionConverterContext context
  ) {
    if (convertedArguments.size() == 2 && !(convertedArguments.get(0) instanceof Predicate)) {
      //noinspection unchecked
      return context.getCriteriaBuilder().and(
          (Expression<Boolean>) convertedArguments.get(0),
          (Expression<Boolean>) convertedArguments.get(1)
      );
    } else {
      @SuppressWarnings("SuspiciousToArrayCall") Predicate[] predicates = convertedArguments.toArray(Predicate[]::new);
      return context.getCriteriaBuilder().and((predicates));
    }
  }

  private static Expression<?> convertOr(
      DetachedExpressionImpl<?> expressionToConvert,
      DetachedExpressionType expressionType,
      List<?> originalArguments,
      List<Expression<?>> convertedArguments,
      ExpressionConverterContext context
  ) {
    if (convertedArguments.size() == 2 && !(convertedArguments.get(0) instanceof Predicate)) {
      //noinspection unchecked
      return context.getCriteriaBuilder().or(
          (Expression<Boolean>) convertedArguments.get(0),
          (Expression<Boolean>) convertedArguments.get(1)
      );
    } else {
      @SuppressWarnings("SuspiciousToArrayCall") Predicate[] predicates = convertedArguments.toArray(new Predicate[]{});
      return context.getCriteriaBuilder().or((predicates));
    }
  }

  private static Expression<?> convertNot(
      DetachedExpressionImpl<?> expressionToConvert,
      DetachedExpressionType expressionType,
      List<?> originalArguments,
      List<Expression<?>> convertedArguments,
      ExpressionConverterContext context
  ) {
    assertArgumentCount(1, convertedArguments, expressionType);

    //noinspection unchecked
    return context.getCriteriaBuilder().not((Expression<Boolean>) convertedArguments.get(0));
  }

  private static Expression<?> convertIsTrue(
      DetachedExpressionImpl<?> expressionToConvert,
      DetachedExpressionType expressionType,
      List<?> originalArguments,
      List<Expression<?>> convertedArguments,
      ExpressionConverterContext context
  ) {
    assertArgumentCount(1, convertedArguments, expressionType);

    //noinspection unchecked
    return context.getCriteriaBuilder().isTrue((Expression<Boolean>) convertedArguments.get(0));
  }

  private static Expression<?> convertIsFalse(
      DetachedExpressionImpl<?> expressionToConvert,
      DetachedExpressionType expressionType,
      List<?> originalArguments,
      List<Expression<?>> convertedArguments,
      ExpressionConverterContext context
  ) {
    assertArgumentCount(1, convertedArguments, expressionType);

    //noinspection unchecked
    return context.getCriteriaBuilder().isFalse((Expression<Boolean>) convertedArguments.get(0));
  }

  private static Expression<?> convertIsNull(
      DetachedExpressionImpl<?> expressionToConvert,
      DetachedExpressionType expressionType,
      List<?> originalArguments,
      List<Expression<?>> convertedArguments,
      ExpressionConverterContext context
  ) {
    assertArgumentCount(1, convertedArguments, expressionType);

    return context.getCriteriaBuilder().isNull((convertedArguments.get(0)));
  }

  private static Expression<?> convertIsNotNull(
      DetachedExpressionImpl<?> expressionToConvert,
      DetachedExpressionType expressionType,
      List<?> originalArguments,
      List<Expression<?>> convertedArguments,
      ExpressionConverterContext context
  ) {
    assertArgumentCount(1, convertedArguments, expressionType);

    return context.getCriteriaBuilder().isNotNull(convertedArguments.get(0));
  }

  private static Expression<?> convertEqual(
      DetachedExpressionImpl<?> expressionToConvert,
      DetachedExpressionType expressionType,
      List<?> originalArguments,
      List<Expression<?>> convertedArguments,
      ExpressionConverterContext context
  ) {
    assertArgumentCount(2, convertedArguments, expressionType);

    return context.getCriteriaBuilder().equal(convertedArguments.get(0), convertedArguments.get(1));
  }

  private static Expression<?> convertNotEqual(
      DetachedExpressionImpl<?> expressionToConvert,
      DetachedExpressionType expressionType,
      List<?> originalArguments,
      List<Expression<?>> convertedArguments,
      ExpressionConverterContext context
  ) {
    assertArgumentCount(2, convertedArguments, expressionType);

    return context.getCriteriaBuilder().notEqual(convertedArguments.get(0), convertedArguments.get(1));
  }

  private static Expression<?> convertGreaterThan(
      DetachedExpressionImpl<?> expressionToConvert,
      DetachedExpressionType expressionType,
      List<?> originalArguments,
      List<Expression<?>> convertedArguments,
      ExpressionConverterContext context
  ) {
    assertArgumentCount(2, convertedArguments, expressionType);

    List<Expression<Comparable<? super Comparable<?>>>> comparableconvertedArguments = castToComparable(convertedArguments);

    return context.getCriteriaBuilder().greaterThan(comparableconvertedArguments.get(0), comparableconvertedArguments.get(1));
  }

  private static Expression<?> convertGreaterThanOrEqualTo(
      DetachedExpressionImpl<?> expressionToConvert,
      DetachedExpressionType expressionType,
      List<?> originalArguments,
      List<Expression<?>> convertedArguments,
      ExpressionConverterContext context
  ) {
    assertArgumentCount(2, convertedArguments, expressionType);

    List<Expression<Comparable<? super Comparable<?>>>> comparableconvertedArguments = castToComparable(convertedArguments);

    return context.getCriteriaBuilder().greaterThanOrEqualTo(
        comparableconvertedArguments.get(0),
        comparableconvertedArguments.get(1)
    );
  }

  private static Expression<?> convertLessThan(
      DetachedExpressionImpl<?> expressionToConvert,
      DetachedExpressionType expressionType,
      List<?> originalArguments,
      List<Expression<?>> convertedArguments,
      ExpressionConverterContext context
  ) {
    assertArgumentCount(2, convertedArguments, expressionType);

    List<Expression<Comparable<? super Comparable<?>>>> comparableconvertedArguments = castToComparable(convertedArguments);

    return context.getCriteriaBuilder().lessThan(comparableconvertedArguments.get(0), comparableconvertedArguments.get(1));
  }

  private static Expression<?> convertLessThanOrEqualTo(
      DetachedExpressionImpl<?> expressionToConvert,
      DetachedExpressionType expressionType,
      List<?> originalArguments,
      List<Expression<?>> convertedArguments,
      ExpressionConverterContext context
  ) {
    assertArgumentCount(2, convertedArguments, expressionType);

    List<Expression<Comparable<? super Comparable<?>>>> comparableconvertedArguments = castToComparable(convertedArguments);

    return context.getCriteriaBuilder().lessThanOrEqualTo(comparableconvertedArguments.get(0), comparableconvertedArguments.get(1));
  }

  private static Expression<?> convertBetween(
      DetachedExpressionImpl<?> expressionToConvert,
      DetachedExpressionType expressionType,
      List<?> originalArguments,
      List<Expression<?>> convertedArguments,
      ExpressionConverterContext context
  ) {
    assertArgumentCount(3, convertedArguments, expressionType);

    List<Expression<Comparable<? super Comparable<?>>>> comparableconvertedArguments = castToComparable(convertedArguments);

    return context.getCriteriaBuilder().between(
        comparableconvertedArguments.get(0),
        comparableconvertedArguments.get(1),
        comparableconvertedArguments.get(2)
    );
  }

  private static Expression<?> convertGt(
      DetachedExpressionImpl<?> expressionToConvert,
      DetachedExpressionType expressionType,
      List<?> originalArguments,
      List<Expression<?>> convertedArguments,
      ExpressionConverterContext context
  ) {
    assertArgumentCount(2, convertedArguments, expressionType);

    //noinspection unchecked
    return context.getCriteriaBuilder().gt(
        (Expression<? extends Number>) convertedArguments.get(0),
        (Expression<? extends Number>) convertedArguments.get(1)
    );
  }

  private static Expression<?> convertGe(
      DetachedExpressionImpl<?> expressionToConvert,
      DetachedExpressionType expressionType,
      List<?> originalArguments,
      List<Expression<?>> convertedArguments,
      ExpressionConverterContext context
  ) {
    assertArgumentCount(2, convertedArguments, expressionType);

    //noinspection unchecked
    return context.getCriteriaBuilder().ge(
        (Expression<? extends Number>) convertedArguments.get(0),
        (Expression<? extends Number>) convertedArguments.get(1)
    );
  }

  private static Expression<?> convertLt(
      DetachedExpressionImpl<?> expressionToConvert,
      DetachedExpressionType expressionType,
      List<?> originalArguments,
      List<Expression<?>> convertedArguments,
      ExpressionConverterContext context
  ) {
    assertArgumentCount(2, convertedArguments, expressionType);

    //noinspection unchecked
    return context.getCriteriaBuilder().lt(
        (Expression<? extends Number>) convertedArguments.get(0),
        (Expression<? extends Number>) convertedArguments.get(1)
    );
  }

  private static Expression<?> convertLe(
      DetachedExpressionImpl<?> expressionToConvert,
      DetachedExpressionType expressionType,
      List<?> originalArguments,
      List<Expression<?>> convertedArguments,
      ExpressionConverterContext context
  ) {
    assertArgumentCount(2, convertedArguments, expressionType);

    //noinspection unchecked
    return context.getCriteriaBuilder().le(
        (Expression<? extends Number>) convertedArguments.get(0),
        (Expression<? extends Number>) convertedArguments.get(1)
    );
  }

  private static Expression<?> convertNeg(
      DetachedExpressionImpl<?> expressionToConvert,
      DetachedExpressionType expressionType,
      List<?> originalArguments,
      List<Expression<?>> convertedArguments,
      ExpressionConverterContext context
  ) {
    assertArgumentCount(1, convertedArguments, expressionType);

    //noinspection unchecked
    return context.getCriteriaBuilder().neg((Expression<? extends Number>) convertedArguments.get(0));
  }

  private static Expression<?> convertAbs(
      DetachedExpressionImpl<?> expressionToConvert,
      DetachedExpressionType expressionType,
      List<?> originalArguments,
      List<Expression<?>> convertedArguments,
      ExpressionConverterContext context
  ) {
    assertArgumentCount(1, convertedArguments, expressionType);

    //noinspection unchecked
    return context.getCriteriaBuilder().abs((Expression<? extends Number>) convertedArguments.get(0));
  }

  private static Expression<?> convertProd(
      DetachedExpressionImpl<?> expressionToConvert,
      DetachedExpressionType expressionType,
      List<?> originalArguments,
      List<Expression<?>> convertedArguments,
      ExpressionConverterContext context
  ) {
    assertArgumentCount(2, convertedArguments, expressionType);

    //noinspection unchecked
    return context.getCriteriaBuilder().prod(
        (Expression<? extends Number>) convertedArguments.get(0),
        (Expression<? extends Number>) convertedArguments.get(1)
    );
  }

  private static Expression<?> convertDiff(
      DetachedExpressionImpl<?> expressionToConvert,
      DetachedExpressionType expressionType,
      List<?> originalArguments,
      List<Expression<?>> convertedArguments,
      ExpressionConverterContext context
  ) {
    assertArgumentCount(2, convertedArguments, expressionType);

    //noinspection unchecked
    return context.getCriteriaBuilder().diff(
        (Expression<? extends Number>) convertedArguments.get(0),
        (Expression<? extends Number>) convertedArguments.get(1)
    );
  }

  private static Expression<?> convertQuot(
      DetachedExpressionImpl<?> expressionToConvert,
      DetachedExpressionType expressionType,
      List<?> originalArguments,
      List<Expression<?>> convertedArguments,
      ExpressionConverterContext context
  ) {
    assertArgumentCount(2, convertedArguments, expressionType);

    //noinspection unchecked
    return context.getCriteriaBuilder().quot(
        (Expression<? extends Number>) convertedArguments.get(0),
        (Expression<? extends Number>) convertedArguments.get(1)
    );
  }

  private static Expression<?> convertMod(
      DetachedExpressionImpl<?> expressionToConvert,
      DetachedExpressionType expressionType,
      List<?> originalArguments,
      List<Expression<?>> convertedArguments,
      ExpressionConverterContext context
  ) {
    assertArgumentCount(2, convertedArguments, expressionType);

    //noinspection unchecked
    return context.getCriteriaBuilder().mod(
        (Expression<Integer>) convertedArguments.get(0),
        (Expression<Integer>) convertedArguments.get(1)
    );
  }

  private static Expression<?> convertSqrt(
      DetachedExpressionImpl<?> expressionToConvert,
      DetachedExpressionType expressionType,
      List<?> originalArguments,
      List<Expression<?>> convertedArguments,
      ExpressionConverterContext context
  ) {
    assertArgumentCount(1, convertedArguments, expressionType);

    //noinspection unchecked
    return context.getCriteriaBuilder().sqrt((Expression<? extends Number>) convertedArguments.get(0));
  }

  private static Expression<?> convertToLong(
      DetachedExpressionImpl<?> expressionToConvert,
      DetachedExpressionType expressionType,
      List<?> originalArguments,
      List<Expression<?>> convertedArguments,
      ExpressionConverterContext context
  ) {
    assertArgumentCount(1, convertedArguments, expressionType);

    //noinspection unchecked
    return context.getCriteriaBuilder().toLong((Expression<? extends Number>) convertedArguments.get(0));
  }

  private static Expression<?> convertToInteger(
      DetachedExpressionImpl<?> expressionToConvert,
      DetachedExpressionType expressionType,
      List<?> originalArguments,
      List<Expression<?>> convertedArguments,
      ExpressionConverterContext context
  ) {
    assertArgumentCount(1, convertedArguments, expressionType);

    //noinspection unchecked
    return context.getCriteriaBuilder().toInteger((Expression<? extends Number>) convertedArguments.get(0));
  }

  private static Expression<?> convertToFloat(
      DetachedExpressionImpl<?> expressionToConvert,
      DetachedExpressionType expressionType,
      List<?> originalArguments,
      List<Expression<?>> convertedArguments,
      ExpressionConverterContext context
  ) {
    assertArgumentCount(1, convertedArguments, expressionType);

    //noinspection unchecked
    return context.getCriteriaBuilder().toFloat((Expression<? extends Number>) convertedArguments.get(0));
  }

  private static Expression<?> convertToDouble(
      DetachedExpressionImpl<?> expressionToConvert,
      DetachedExpressionType expressionType,
      List<?> originalArguments,
      List<Expression<?>> convertedArguments,
      ExpressionConverterContext context
  ) {
    assertArgumentCount(1, convertedArguments, expressionType);

    //noinspection unchecked
    return context.getCriteriaBuilder().toDouble((Expression<? extends Number>) convertedArguments.get(0));
  }

  private static Expression<?> convertToBigDecimal(
      DetachedExpressionImpl<?> expressionToConvert,
      DetachedExpressionType expressionType,
      List<?> originalArguments,
      List<Expression<?>> convertedArguments,
      ExpressionConverterContext context
  ) {
    assertArgumentCount(1, convertedArguments, expressionType);

    //noinspection unchecked
    return context.getCriteriaBuilder().toBigDecimal((Expression<? extends Number>) convertedArguments.get(0));
  }

  private static Expression<?> convertToBigInteger(
      DetachedExpressionImpl<?> expressionToConvert,
      DetachedExpressionType expressionType,
      List<?> originalArguments,
      List<Expression<?>> convertedArguments,
      ExpressionConverterContext context
  ) {
    assertArgumentCount(1, convertedArguments, expressionType);

    //noinspection unchecked
    return context.getCriteriaBuilder().toBigInteger((Expression<? extends Number>) convertedArguments.get(0));
  }

  private static Expression<?> convertToString(
      DetachedExpressionImpl<?> expressionToConvert,
      DetachedExpressionType expressionType,
      List<?> originalArguments,
      List<Expression<?>> convertedArguments,
      ExpressionConverterContext context
  ) {
    assertArgumentCount(1, convertedArguments, expressionType);

    //noinspection unchecked
    return context.getCriteriaBuilder().toString((Expression<Character>) convertedArguments.get(0));
  }

  private static Expression<?> convertLiteral(
      DetachedExpressionImpl<?> expressionToConvert,
      DetachedExpressionType expressionType,
      List<?> originalArguments,
      List<Expression<?>> convertedArguments,
      ExpressionConverterContext context
  ) {
    assertArgumentCount(1, convertedArguments, expressionType);

    return convertedArguments.get(0);
  }

  private static Expression<?> convertNullLiteral(
      DetachedExpressionImpl<?> expressionToConvert,
      DetachedExpressionType expressionType,
      List<?> originalArguments,
      List<Expression<?>> convertedArguments,
      ExpressionConverterContext context
  ) {
    assertArgumentCount(1, convertedArguments, expressionType);

    return context.getCriteriaBuilder().nullLiteral((Class<?>) originalArguments.get(0));
  }

  private static Expression<?> convertParameter(
      DetachedExpressionImpl<?> expressionToConvert,
      DetachedExpressionType expressionType,
      List<?> originalArguments,
      List<Expression<?>> convertedArguments,
      ExpressionConverterContext context
  ) {
    assertArgumentCount(0, convertedArguments, expressionType);

    DetachedParameterExpressionImpl<?> detachedParameterExpression = (DetachedParameterExpressionImpl<?>) expressionToConvert;
    Parameter<?> parameter = context.getParameters().get(detachedParameterExpression.getName());
    if (parameter == null) {
      throw new IllegalStateException(String.format("Parameter %s not found", detachedParameterExpression.getName()));
    }

    return context.getCriteriaBuilder().parameter(parameter.getValueClass(), detachedParameterExpression.getName());
  }

  private static Expression<?> convertIsEmpty(
      DetachedExpressionImpl<?> expressionToConvert,
      DetachedExpressionType expressionType,
      List<?> originalArguments,
      List<Expression<?>> convertedArguments,
      ExpressionConverterContext context
  ) {
    assertArgumentCount(1, convertedArguments, expressionType);

    //noinspection unchecked
    return context.getCriteriaBuilder().isEmpty((Expression<Collection<?>>) convertedArguments.get(0));
  }

  private static Expression<?> convertIsNotEmpty(
      DetachedExpressionImpl<?> expressionToConvert,
      DetachedExpressionType expressionType,
      List<?> originalArguments,
      List<Expression<?>> convertedArguments,
      ExpressionConverterContext context
  ) {
    assertArgumentCount(1, convertedArguments, expressionType);

    //noinspection unchecked
    return context.getCriteriaBuilder().isNotEmpty((Expression<Collection<?>>) convertedArguments.get(0));
  }

  private static Expression<?> convertSize(
      DetachedExpressionImpl<?> expressionToConvert,
      DetachedExpressionType expressionType,
      List<?> originalArguments,
      List<Expression<?>> convertedArguments,
      ExpressionConverterContext context
  ) {
    assertArgumentCount(1, convertedArguments, expressionType);

    //noinspection unchecked
    return context.getCriteriaBuilder().size((Expression<Collection<?>>) convertedArguments.get(0));
  }

  private static Expression<?> convertIsMember(
      DetachedExpressionImpl<?> expressionToConvert,
      DetachedExpressionType expressionType,
      List<?> originalArguments,
      List<Expression<?>> convertedArguments,
      ExpressionConverterContext context
  ) {
    assertArgumentCount(2, convertedArguments, expressionType);

    //noinspection unchecked
    return context.getCriteriaBuilder().isMember(
        convertedArguments.get(0),
        (Expression<Collection<Object>>) convertedArguments.get(1)
    );
  }

  private static Expression<?> convertIsNotMember(
      DetachedExpressionImpl<?> expressionToConvert,
      DetachedExpressionType expressionType,
      List<?> originalArguments,
      List<Expression<?>> convertedArguments,
      ExpressionConverterContext context
  ) {
    assertArgumentCount(2, convertedArguments, expressionType);

    //noinspection unchecked
    return context.getCriteriaBuilder().isNotMember(
        convertedArguments.get(0),
        (Expression<Collection<Object>>) convertedArguments.get(1)
    );
  }

  private static Expression<?> convertValues(
      DetachedExpressionImpl<?> expressionToConvert,
      DetachedExpressionType expressionType,
      List<?> originalArguments,
      List<Expression<?>> convertedArguments,
      ExpressionConverterContext context
  ) {
    assertArgumentCount(1, convertedArguments, expressionType);

    return context.getCriteriaBuilder().values((Map<?, ?>) originalArguments.get(0));
  }

  private static Expression<?> convertKeys(
      DetachedExpressionImpl<?> expressionToConvert,
      DetachedExpressionType expressionType,
      List<?> originalArguments,
      List<Expression<?>> convertedArguments,
      ExpressionConverterContext context
  ) {
    assertArgumentCount(1, convertedArguments, expressionType);

    return context.getCriteriaBuilder().keys((Map<?, ?>) originalArguments.get(0));
  }

  private static Expression<?> convertLike(
      DetachedExpressionImpl<?> expressionToConvert,
      DetachedExpressionType expressionType,
      List<?> originalArguments,
      List<Expression<?>> convertedArguments,
      ExpressionConverterContext context
  ) {
    if (convertedArguments.size() == 2) {
      //noinspection unchecked
      return context.getCriteriaBuilder().like(
          (Expression<String>) convertedArguments.get(0),
          (Expression<String>) convertedArguments.get(1)
      );
    } else {
      assertArgumentCount(3, convertedArguments, expressionType);
      //noinspection unchecked
      return context.getCriteriaBuilder().like(
          (Expression<String>) convertedArguments.get(0),
          (Expression<String>) convertedArguments.get(1),
          (Expression<Character>) convertedArguments.get(2)
      );
    }
  }

  private static Expression<?> convertNotLike(
      DetachedExpressionImpl<?> expressionToConvert,
      DetachedExpressionType expressionType,
      List<?> originalArguments,
      List<Expression<?>> convertedArguments,
      ExpressionConverterContext context
  ) {
    if (convertedArguments.size() == 2) {
      //noinspection unchecked
      return context.getCriteriaBuilder().notLike(
          (Expression<String>) convertedArguments.get(0),
          (Expression<String>) convertedArguments.get(1)
      );
    } else {
      assertArgumentCount(3, convertedArguments, expressionType);
      //noinspection unchecked
      return context.getCriteriaBuilder().notLike(
          (Expression<String>) convertedArguments.get(0),
          (Expression<String>) convertedArguments.get(1),
          (Expression<Character>) convertedArguments.get(2)
      );
    }
  }

  private static Expression<?> convertConcat(
      DetachedExpressionImpl<?> expressionToConvert,
      DetachedExpressionType expressionType,
      List<?> originalArguments,
      List<Expression<?>> convertedArguments,
      ExpressionConverterContext context
  ) {
    assertArgumentCount(2, convertedArguments, expressionType);

    //noinspection unchecked
    return context.getCriteriaBuilder().concat(
        (Expression<String>) convertedArguments.get(0),
        (Expression<String>) convertedArguments.get(1)
    );
  }

  private static Expression<?> convertSubstring(
      DetachedExpressionImpl<?> expressionToConvert,
      DetachedExpressionType expressionType,
      List<?> originalArguments,
      List<Expression<?>> convertedArguments,
      ExpressionConverterContext context
  ) {
    if (convertedArguments.size() == 2) {
      //noinspection unchecked
      return context.getCriteriaBuilder().substring(
          (Expression<String>) convertedArguments.get(0),
          (Expression<Integer>) convertedArguments.get(1)
      );
    } else {
      assertArgumentCount(3, convertedArguments, expressionType);
      //noinspection unchecked
      return context.getCriteriaBuilder().substring(
          (Expression<String>) convertedArguments.get(0),
          (Expression<Integer>) convertedArguments.get(1),
          (Expression<Integer>) convertedArguments.get(2)
      );
    }
  }

  private static Expression<?> convertTrim(
      DetachedExpressionImpl<?> expressionToConvert,
      DetachedExpressionType expressionType,
      List<?> originalArguments,
      List<Expression<?>> convertedArguments,
      ExpressionConverterContext context
  ) {
    if (convertedArguments.size() == 1) {
      //noinspection unchecked
      return context.getCriteriaBuilder().trim((Expression<String>) convertedArguments.get(0));
    } else if (convertedArguments.size() == 2) {
      if (originalArguments.get(0) instanceof CriteriaBuilder.Trimspec) {
        //noinspection unchecked
        return context.getCriteriaBuilder().trim(
            (CriteriaBuilder.Trimspec) originalArguments.get(0),
            (Expression<String>) convertedArguments.get(1)
        );
      } else {
        //noinspection unchecked
        return context.getCriteriaBuilder().trim(
            (Expression<Character>) convertedArguments.get(0),
            (Expression<String>) convertedArguments.get(1)
        );
      }
    } else {
      assertArgumentCount(3, convertedArguments, expressionType);

      //noinspection unchecked
      return context.getCriteriaBuilder().trim(
          (CriteriaBuilder.Trimspec) originalArguments.get(0),
          (Expression<Character>) convertedArguments.get(1),
          (Expression<String>) convertedArguments.get(2)
      );
    }
  }

  private static Expression<?> convertLower(
      DetachedExpressionImpl<?> expressionToConvert,
      DetachedExpressionType expressionType,
      List<?> originalArguments,
      List<Expression<?>> convertedArguments,
      ExpressionConverterContext context
  ) {
    assertArgumentCount(1, convertedArguments, expressionType);

    //noinspection unchecked
    return context.getCriteriaBuilder().lower((Expression<String>) convertedArguments.get(0));
  }

  private static Expression<?> convertUpper(
      DetachedExpressionImpl<?> expressionToConvert,
      DetachedExpressionType expressionType,
      List<?> originalArguments,
      List<Expression<?>> convertedArguments,
      ExpressionConverterContext context
  ) {
    assertArgumentCount(1, convertedArguments, expressionType);

    //noinspection unchecked
    return context.getCriteriaBuilder().upper((Expression<String>) convertedArguments.get(0));
  }

  private static Expression<?> convertLength(
      DetachedExpressionImpl<?> expressionToConvert,
      DetachedExpressionType expressionType,
      List<?> originalArguments,
      List<Expression<?>> convertedArguments,
      ExpressionConverterContext context
  ) {
    assertArgumentCount(1, convertedArguments, expressionType);

    //noinspection unchecked
    return context.getCriteriaBuilder().length((Expression<String>) convertedArguments.get(0));
  }

  private static Expression<?> convertLocate(
      DetachedExpressionImpl<?> expressionToConvert,
      DetachedExpressionType expressionType,
      List<?> originalArguments,
      List<Expression<?>> convertedArguments,
      ExpressionConverterContext context
  ) {
    if (convertedArguments.size() == 2) {
      //noinspection unchecked
      return context.getCriteriaBuilder().locate(
          (Expression<String>) convertedArguments.get(0),
          (Expression<String>) convertedArguments.get(1)
      );
    } else {
      assertArgumentCount(3, convertedArguments, expressionType);
      //noinspection unchecked
      return context.getCriteriaBuilder().locate(
          (Expression<String>) convertedArguments.get(0),
          (Expression<String>) convertedArguments.get(1),
          (Expression<Integer>) convertedArguments.get(2)
      );
    }
  }

  private static Expression<?> convertCurrentDate(
      DetachedExpressionImpl<?> expressionToConvert,
      DetachedExpressionType expressionType,
      List<?> originalArguments,
      List<Expression<?>> convertedArguments,
      ExpressionConverterContext context
  ) {
    assertArgumentCount(0, convertedArguments, expressionType);

    return context.getCriteriaBuilder().currentDate();
  }

  private static Expression<?> convertCurrentTimestamp(
      DetachedExpressionImpl<?> expressionToConvert,
      DetachedExpressionType expressionType,
      List<?> originalArguments,
      List<Expression<?>> convertedArguments,
      ExpressionConverterContext context
  ) {
    assertArgumentCount(0, convertedArguments, expressionType);

    return context.getCriteriaBuilder().currentTimestamp();
  }

  private static Expression<?> convertCurrentTime(
      DetachedExpressionImpl<?> expressionToConvert,
      DetachedExpressionType expressionType,
      List<?> originalArguments,
      List<Expression<?>> convertedArguments,
      ExpressionConverterContext context
  ) {
    assertArgumentCount(0, convertedArguments, expressionType);

    return context.getCriteriaBuilder().currentTime();
  }

  private static Expression<?> convertIn(
      DetachedExpressionImpl<?> expressionToConvert,
      DetachedExpressionType expressionType,
      List<?> originalArguments,
      List<Expression<?>> convertedArguments,
      ExpressionConverterContext context
  ) {
    assertArgumentCount(1, convertedArguments, expressionType);

    DetachedInNotTypeSafeImpl<?, ?> detachedIn = (DetachedInNotTypeSafeImpl<?, ?>) expressionToConvert;
    CriteriaBuilder.In<Object> jpaIn = context.getCriteriaBuilder().in(convertedArguments.get(0));

    if (shouldReplaceInValues(detachedIn, context)) {
      jpaIn.value(context.getInValuesToReplace());
    } else {
      for (DetachedExpression<?> value : detachedIn.getValues()) {
        DetachedExpressionCommonImpl<?> castedValue = (DetachedExpressionCommonImpl<?>) value;
        jpaIn.value(castedValue.toJpaExpression(context));
      }
    }

    return jpaIn;
  }

  private static boolean shouldReplaceInValues(DetachedInNotTypeSafeImpl<?, ?> detachedIn, ExpressionConverterContext context) {
    if (context.getInValuesToReplace() == null || context.getInValuesToReplace().isEmpty()) {
      return false;
    }

    return !detachedIn.isValueParameter();
  }

  private static Expression<?> convertCoalesce(
      DetachedExpressionImpl<?> expressionToConvert,
      DetachedExpressionType expressionType,
      List<?> originalArguments,
      List<Expression<?>> convertedArguments,
      ExpressionConverterContext context
  ) {
    assertArgumentCount(0, convertedArguments, expressionType);

    DetachedCoalesceImpl<?> detachedCoalesce = (DetachedCoalesceImpl<?>) expressionToConvert;
    CriteriaBuilder.Coalesce<Object> jpaCoalesce = context.getCriteriaBuilder().coalesce();
    for (DetachedExpression<?> value : detachedCoalesce.getValues()) {
      DetachedExpressionCommonImpl<?> castedValue = (DetachedExpressionCommonImpl<?>) value;
      jpaCoalesce.value(castedValue.toJpaExpression(context));
    }

    return jpaCoalesce;
  }

  private static Expression<?> convertNullIf(
      DetachedExpressionImpl<?> expressionToConvert,
      DetachedExpressionType expressionType,
      List<?> originalArguments,
      List<Expression<?>> convertedArguments,
      ExpressionConverterContext context
  ) {
    assertArgumentCount(2, convertedArguments, expressionType);

    return context.getCriteriaBuilder().nullif(convertedArguments.get(0), convertedArguments.get(1));
  }

  private static Expression<?> convertSelectCase(
      DetachedExpressionImpl<?> expressionToConvert,
      DetachedExpressionType expressionType,
      List<?> originalArguments,
      List<Expression<?>> convertedArguments,
      ExpressionConverterContext context
  ) {
    if (expressionToConvert instanceof DetachedSimpleCaseImpl) {
      assertArgumentCount(1, convertedArguments, expressionType);

      DetachedSimpleCaseImpl<?, ?> detachedSimpleCase = (DetachedSimpleCaseImpl<?, ?>) expressionToConvert;
      CriteriaBuilder.SimpleCase<Object, Object> jpaSimpleCase = context.getCriteriaBuilder().selectCase(convertedArguments.get(0));
      for (DetachedSimpleCaseImpl.When<?, ?> when : detachedSimpleCase.getWhenList()) {
        DetachedExpressionCommonImpl<?> castedWhenResult = (DetachedExpressionCommonImpl<?>) when.getResult();
        jpaSimpleCase.when(when.getCondition(), castedWhenResult.toJpaExpression(context));
      }
      for (DetachedExpression<?> otherwise : detachedSimpleCase.getOtherwiseList()) {
        DetachedExpressionCommonImpl<?> castedOtherwise = (DetachedExpressionCommonImpl<?>) otherwise;
        jpaSimpleCase.otherwise(castedOtherwise.toJpaExpression(context));
      }

      return jpaSimpleCase;
    } else if (expressionToConvert instanceof DetachedCaseImpl) {
      assertArgumentCount(0, convertedArguments, expressionType);

      DetachedCaseImpl<?> detachedCase = (DetachedCaseImpl<?>) expressionToConvert;
      CriteriaBuilder.Case<Object> jpaCase = context.getCriteriaBuilder().selectCase();
      for (DetachedCaseImpl.When<?> when : detachedCase.getWhenList()) {
        @SuppressWarnings("unchecked") //Ок супрессить, точно знаем что типы совпадут.
        DetachedExpressionCommonImpl<Boolean> castedWhenCondition = (DetachedExpressionCommonImpl<Boolean>) when.getCondition();
        DetachedExpressionCommonImpl<?> castedWhenResult = (DetachedExpressionCommonImpl<?>) when.getResult();

        jpaCase.when(castedWhenCondition.toJpaExpression(context), castedWhenResult.toJpaExpression(context));
      }
      for (DetachedExpression<?> otherwise : detachedCase.getOtherwiseList()) {
        DetachedExpressionCommonImpl<?> castedOtherwise = (DetachedExpressionCommonImpl<?>) otherwise;
        jpaCase.otherwise(castedOtherwise.toJpaExpression(context));
      }

      return jpaCase;
    } else {
      throw new IllegalStateException(
          String.format("Unexpected branch, work with %s but the class is %s", expressionType, expressionToConvert.getClass().getSimpleName())
      );
    }
  }

  private static Expression<?> convertFunction(
      DetachedExpressionImpl<?> expressionToConvert,
      DetachedExpressionType expressionType,
      List<?> originalArguments,
      List<Expression<?>> convertedArguments,
      ExpressionConverterContext context
  ) {
    return context.getCriteriaBuilder().function(
        (String) originalArguments.get(0),
        (Class<?>) originalArguments.get(1),
        convertedArguments.size() > 2 ? convertedArguments.subList(2, convertedArguments.size()).toArray(Expression[]::new) : new Expression[]{}
    );
  }

  private static void assertArgumentCount(int expectedCount, List<?> actualArguments, DetachedExpressionType type) {
    if (actualArguments.size() != expectedCount) {
      throw new IllegalStateException(
          String.format("There should be %d arguments for the function %s, got %d", expectedCount, type, actualArguments.size())
      );
    }
  }

  private static class ClosestPathInfo {
    private final Path<?> closestPath;
    private final AttributePath attributePath;

    public ClosestPathInfo(Path<?> closestPath, AttributePath attributePath) {
      this.closestPath = closestPath;
      this.attributePath = attributePath;
    }

    public Path<?> getClosestPath() {
      return closestPath;
    }

    public AttributePath getAttributePath() {
      return attributePath;
    }
  }
}
