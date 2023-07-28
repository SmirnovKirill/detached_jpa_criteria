package kirill.detachedjpacriteria.query;

import java.util.List;

interface CriteriaQueryWithJoins {
  List<Join> getJoins();
}
