package kirill.detachedjpacriteria.query;

import java.util.Map;
import kirill.detachedjpacriteria.util.Parameter;

interface CriteriaQueryWithParameters {
  Map<String, Parameter<?>> getParameters();
}
