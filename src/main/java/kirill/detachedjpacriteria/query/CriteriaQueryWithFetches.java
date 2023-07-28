package kirill.detachedjpacriteria.query;

import java.util.List;

interface CriteriaQueryWithFetches {
  List<Fetch> getFetches();
}
