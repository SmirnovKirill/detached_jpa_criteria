package kirill.detachedjpacriteria.expression.impl;

import java.util.List;
import java.util.Map;
import javax.persistence.EntityManager;
import javax.persistence.criteria.CommonAbstractCriteria;
import javax.persistence.criteria.CriteriaBuilder;
import kirill.detachedjpacriteria.util.Parameter;

public class ExpressionConverterContext {
  private final PathContext pathContext;
  private final PathContext parentPathContext;
  private final Map<String, Parameter<?>> parameters;
  private final List<?> inValuesToReplace;
  private final CriteriaBuilder criteriaBuilder;
  private final EntityManager entityManager;
  private CommonAbstractCriteria criteria;

  public ExpressionConverterContext(
      PathContext pathContext,
      PathContext parentPathContext,
      Map<String, Parameter<?>> parameters,
      List<?> inValuesToReplace,
      CriteriaBuilder criteriaBuilder,
      EntityManager entityManager
  ) {
    this.pathContext = pathContext;
    this.parentPathContext = parentPathContext;
    this.parameters = parameters;
    this.inValuesToReplace = inValuesToReplace;
    this.criteriaBuilder = criteriaBuilder;
    this.entityManager = entityManager;
  }

  public PathContext getPathContext() {
    return pathContext;
  }

  public PathContext getParentPathContext() {
    return parentPathContext;
  }

  public Map<String, Parameter<?>> getParameters() {
    return parameters;
  }

  public List<?> getInValuesToReplace() {
    return inValuesToReplace;
  }

  public CriteriaBuilder getCriteriaBuilder() {
    return criteriaBuilder;
  }

  public EntityManager getEntityManager() {
    return entityManager;
  }

  public CommonAbstractCriteria getCriteria() {
    return criteria;
  }

  public void setCriteria(CommonAbstractCriteria criteria) {
    this.criteria = criteria;
  }
}
