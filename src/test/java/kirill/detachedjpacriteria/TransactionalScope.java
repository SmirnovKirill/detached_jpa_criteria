package kirill.detachedjpacriteria;

import java.util.function.Supplier;
import javax.transaction.Transactional;

public class TransactionalScope {
  @Transactional
  public <T> T doInTransaction(Supplier<T> supplier) {
    return supplier.get();
  }

  @Transactional
  public void doInTransaction(Runnable runnable) {
    runnable.run();
  }
}
