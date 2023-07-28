# О библиотеке

Это обертка над JPA Criteria API, которая позволяет использовать более лаконичный синтаксис. Библиотека просто конвертирует вызовы в JPA апи, т.е. 
она не делает никакую имплементацию. Библиотека легковесная, в ней есть **единственная зависимость** - javax.persistence-api. Библиотека использует
string-based часть апи JPA, т.е. всякие join, fetch, path работают со строками в путях а не различными наследниками 
javax.persistence.metamodel.Attribute, это сделано для простоты и удобства. Также библиотека позволяет создавать запросы статически.

## Как пользоваться
Сразу приведем примеры запросов т.к. они достаточно простые и наглядные. В примерах используется модель user (пользователи), post (посты которые
пишут пользователи), comment (комментарии к постам).

Выберем пользователей и их посты с фильтрацией:
```java
DetachedCriteriaQuery<UserDb> criteriaQuery = selectEntity(UserDb.class) //Импорт из api.expression.kirill.detachedjpacriteria.DetachedCriteriaBuilder
    .innerFetch("posts")
    .where(like(path("login"), parameter("login"))) //Импорт из api.expression.kirill.detachedjpacriteria.DetachedCriteriaBuilder
    .parameter("login", "user-1%"); //Метод можно использовать если параметр никогда не равен null, иначе - nullableParameter()
if (someBusinessCondition) {
  criteriaQuery.where(like(path("posts", "title"), parameter("title"))); //Вызов добавляет условие к уже имеющемуся
  criteriaQuery.parameter("title", "post-1%");
}
criteriaQuery.orderByDesc(path("login"));
criteriaQuery.distinct(true);

List<UserDb> users = readInTransaction(entityManager -> criteriaQuery.createJpaQuery(entityManager).getResultList());
```

Похожим образом делается update:
```java
DetachedCriteriaUpdate<UserDb> criteriaQuery = update(UserDb.class) //Импорт из api.expression.kirill.detachedjpacriteria.DetachedCriteriaBuilder
    .set("login", "updated login")
    .where(like(path("login"), parameter("login"))) //Импорт из api.expression.kirill.detachedjpacriteria.DetachedCriteriaBuilder
    .parameter("login", "user-1%"); //Метод можно использовать если параметр никогда не равен null, иначе - nullableParameter()

doInTransaction(entityManager -> criteriaQuery.createJpaQuery(entityManager).executeUpdate());
```

или delete:
```java
DetachedCriteriaDelete<CommentDb> criteriaQuery = delete(CommentDb.class) //Импорт из api.expression.kirill.detachedjpacriteria.DetachedCriteriaBuilder
    .where(like(path("text"), parameter("text"))) //Импорт из api.expression.kirill.detachedjpacriteria.DetachedCriteriaBuilder
    .parameter("text", "comment-1%"); //Метод можно использовать если параметр никогда не равен null, иначе - nullableParameter()

doInTransaction(entityManager -> criteriaQuery.createJpaQuery(entityManager).executeUpdate());
```

В классе DetachedCriteriaBuilder есть дополнительные методы для случаев когда нужен не сам класс, а его часть, например, так можно описать выборку 
двух полей id и login в специальный класс-обертку:
```java
DetachedCriteriaQuery<IdWithLoginAndPost> criteriaQuery = multiselect(id(), path("login"), path("posts", "title")) //Что выбираем
     .into(IdWithLoginAndPost.class) //Объекты какого класса будут возвращаться
    .from(UserDb.class) //Для какой сущности
    .leftJoin("posts")
    .where(like(path("login"), parameter("login"))) //Импорт из api.expression.kirill.detachedjpacriteria.DetachedCriteriaBuilder
    .parameter("login", "user-1%"); //Метод можно использовать если параметр никогда не равен null, иначе - nullableParameter()

List<IdWithLoginAndPost> idsWithLoginsAndPosts = readInTransaction(entityManager -> criteriaQuery.createJpaQuery(entityManager).getResultList());
```

В примере выше использовался multiselect, есть еще метод select, разница между ними ровно такая же, как в JPA CriteriaQuery между select и 
multiselect. Select нужно использовать когда выбирается одна колонка, например
```java
DetachedCriteriaQuery<String> criteriaQuery = select(path("login")) //Что выбираем
    .into(String.class) //Объекты какого класса будут возвращаться
    .from(UserDb.class) //Для какой сущности
```

Аналогичный пример для случая когда класс-обертки нет и результат сохраняется в Tuple:
```java
DetachedCriteriaQuery<Tuple> criteriaQuery = multiselect(id(), path("login"), path("posts", "title")) //Что выбираем
    .intoTuple() //Указываем что будет возвращаться Tuple
    .from(UserDb.class) //Для какой сущности
    .leftJoin("posts")
    .where(like(path("login"), parameter("login"))) //Импорт из api.expression.kirill.detachedjpacriteria.DetachedCriteriaBuilder
    .parameter("login", "user-1%"); //Метод можно использовать если параметр никогда не равен null, иначе - nullableParameter()

List<Tuple> idsWithLoginsAndPosts = readInTransaction(entityManager -> criteriaQuery.createJpaQuery(entityManager).getResultList());
```

Пример для случая когда хотим получить только количество строк:
```java
DetachedCriteriaQuery<Long> criteriaQuery = selectCount(root(), UserDb.class)
    .leftJoin("posts")
    .where(like(path("login"), parameter("login"))) //Импорт из api.expression.kirill.detachedjpacriteria.DetachedCriteriaBuilder
    .parameter("login", "user-1%"); //Метод можно использовать если параметр никогда не равен null, иначе - nullableParameter()

long count = readInTransaction(entityManager -> criteriaQuery.createJpaQuery(entityManager).getSingleResult());
```

Также библиотека поддерживает подзапросы, для этого надо использоать методы subquerySelect/subquerySelectEntity и в выражениях для ссылки на
родительские пути методы parentId/parentRoot/parentPath, например:
```java
DetachedCriteriaQuery<UserDb> criteriaQuery = selectEntity(UserDb.class)
    .where(
        root().in(
            subquerySelect(path("user", "id")).into(Integer.class).from(PostDb.class)
                .where(like(path("title"), "post-6-%"))
        )
    );

List<UserDb> users = readInTransaction(entityManager -> criteriaQuery.createJpaQuery(entityManager).getResultList());
```

В целом при работе с библиотекой все достаточно просто - есть три основных класса на каждый тип запроса - DetachedCriteriaQuery (для select),
DetachedCriteriaUpdate и DetachedCriteriaDelete, начинать построение запроса надо с них.
1. Сначала надо получить инстанс нужного класса запроса: 
    * Для update - DetachedCriteriaBuilder.update()
    * Для delete - DetachedCriteriaBuilder.delete()
    * Для select - в зависимости от того что нужно выбрать:
      * Если сущность целиком, то DetachedCriteriaBuilder.selectEntity()
      * Если какой-то класс, то DetachedCriteriaBuilder.select()/multiselect() + into() + from
      * Если Tuple, то DetachedCriteriaBuilder.multiselect() + intoTuple() + from
      * Если количество строк, то DetachedCriteriaBuilder.selectCount(countExpression, entityClass) или distinct версию
2. Далее в них надо дописывать статические выражения из класса DetachedCriteriaBuilder, данный класс является аналогом класса CriteriaBuilder JPA
Criteria и содержит практически все методы из того класса с сохранением их сигнатуры, так что имеющийся опыт работы с JPA здесь поможет. 
3. В конце нужно у класса-запроса вызвать метод, который создаст JPA Query - createJpaQuery().

## Как выбирать только нужные данные (select/multiselect)
В JPA Criteria используется подход, когда нужно последовательно вызывать .get() начиная от рутовой сущности (или от фетчей/джоинов). Библиотека же
предлагает при построении селектов использовать методы select/multiselect, принимающие выражения, которые можно получить с помощью трех дополнительных
методов в DetachedCriteriaBuilder:
1. id(), выборка айди для сущности, более удобно чем хардкодить название.
2. root(), выбор самой сущности, аналог Root из JPA, можно использовать для count.
3. path(String... attributeNames), последовательный список имен атрибутов для извлечения (как в цепочках .get()). В случае, когда есть джоины и фетчи
библиотека сама определит от какого объекта делать .get().

Для обращения к путям родительских запросов в подзапросах есть аналогичные методы parentId, parentRoot и parentPath.

## Дополнительные возможности

### Получение количества строк одним вызовом метода
Для любого типа запроса (select, update, delete) может захотеться посчитать количество записей, которые удовлетворяют условиям, для этого у каждого 
запроса есть методы toCountCriteriaQuery/toCountDistinctCriteriaQuery, которые принимают выражение, которое будет использовано внутри 
count/count(distinct):
```java
long count = readInTransaction((entityManager) -> criteriaDelete.toCountCriteriaQuery(literal(1)).createJpaQuery(entityManager).getSingleResult());
```
```java
long count = readInTransaction((entityManager) -> criteriaDelete.toCountDistinctCriteriaQuery(id()).createJpaQuery(entityManager).getSingleResult());
```
При вызове этих методов создается и возвращается копия исходного запроса.

### Копирование в запрос заданного набора полей из другого запроса
```java
DetachedCriteriaQuery<CommentDb> criteriaQuery = selectEntity(CommentDb.class)
    .where(like(path("text"), parameter("text")))
    .parameter("text", "comment-1-1-%");

DetachedCriteriaDelete<CommentDb> criteriaDelete = delete(CommentDb.class);
criteriaDelete.copyFromOtherCriteria(criteriaQuery, QueryCopyPart.COPY_WHERE, QueryCopyPart.COPY_PARAMS); //Указываем откуда и что копировать.

int rowsDeleted = readInTransaction(entityManager -> criteriaDelete.createJpaQuery(entityManager).executeUpdate());
```

### Разбиение на батчи при использовании IN
Для любого из типов запросов (select, update, delete) можно сгенерировать батчевые запросы если есть одно "простое" условие IN. Критерии "простого"
условия - оно должно быть ровно одно, должно быть одним из самых верхних предикатов, список значений должен быть тоже "простым" (параметр,
коллекции, примитивы, даты, строки).
```java
DetachedCriteriaQuery<UserDb> criteriaQuery = selectEntity(UserDb.class)
    .where(path("login").in(List.of("user-7", "user-6", "user-1", "user-9", "user-5")));

List<UserDb> users = new ArrayList<>();
doInTransaction(entityManager -> {
  for (TypedQuery<UserDb> query : criteriaQuery.createJpaBatchQueries(entityManager, 2)) {
    users.addAll(query.getResultList());
  }
});
```

## Текущие ограничения
* Нет поддержки не string-based части апи (т.е. работа со всякими SingularAttribute, PluralAttribute, разными видами Join - CollectionJoin, MapJoin и
прочими).
