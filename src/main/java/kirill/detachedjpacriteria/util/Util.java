package kirill.detachedjpacriteria.util;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import static java.util.stream.StreamSupport.stream;

public class Util {
  public static <T> List<T> toList(Iterable<T> iterable) {
    return stream(iterable.spliterator(), false).collect(Collectors.toList());
  }

  public static <T> List<T> unmodifiableUnion(List<T> list, T element) {
    List<T> newList = new ArrayList<>(list);
    newList.add(element);
    return List.copyOf(newList);
  }

  public static <T> List<T> unmodifiableUnion(Iterable<T> first, Iterable<T> second) {
    return Stream.concat(stream(first.spliterator(), false), stream(second.spliterator(), false)).collect(Collectors.toList());
  }
}
