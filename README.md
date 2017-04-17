# Criteria Builder

Criteria Builder is integrated project that provides client side as well as server side API's for fetching data directly from the configured Hibernate/JPA data model.

> Basically you don't have to write single Get API using this.

# Key Features
  - Javascript Expression Builder [backend.js](https://github.com/tairmansd/CriteriaBuilder/blob/master/src/main/webapp/scripts/backend.js).
  - Generic REST GET endpoint for data model.

You can also:
  - Fetch data from nested predicate structure like this:  id.empNo or employee.lastName
 ```sh
[
   {
      "id":{
         "empNo":10001,
         "deptNo":"d005"
      },
      "fromDate":"1986-06-26",
      "toDate":"9999-01-01",
      "employee":{
         "empNo":10001,
         "birthDate":"1953-09-02",
         "firstName":"Georgi",
         "gender":"M",
         "hireDate":"1986-06-26",
         "lastName":"Facello"
      },
      "department":{
         "deptNo":"d005",
         "deptName":"Development"
      }
   }
]
```

### Technologies Used

* [Spring MVC](https://docs.spring.io/spring/docs/current/spring-framework-reference/html/mvc.html) - For Rest End Points.
* [Hibernate](http://hibernate.org/orm/) - ORM Framework.
* [Reflections](https://github.com/ronmamo/reflections) - For collecting neccesaary metadata for the data Model.

And of course Criteria Builder itself is open source with a [public repository](https://github.com/tairmansd/CriteriaBuilder)
 on GitHub.

### Installation

Criteria Builder is a [Maven](https://maven.apache.org/) project. So, import the project in [Eclipse](https://eclipse.org/) and run maven install, And Tomcat as deployment container.

```sh
mvn install
```

### Usage

First of all, define name attritube for each of your entity annotation. This is a alias by which you'll create the Expression Builder instances.
```sh
@Entity(name="employee")
public class Employee implements Serializable ...
```

Define your data source properties [criteriaBuilder-local.properties](https://github.com/tairmansd/CriteriaBuilder/blob/master/src/main/resources/criteriaBuilder-local.properties)

There's a reference data model already present in the [com.bats.criteriagenerator.entity](https://github.com/tairmansd/CriteriaBuilder/tree/master/src/main/java/com/bats/criteriagenerator/entity) package. And DB dump for testing in resources folder [dbdump.zip](https://github.com/tairmansd/CriteriaBuilder/tree/master/src/main/resources).

##### Client side - 

Creating Expression builder instance. [Refer](https://github.com/tairmansd/CriteriaBuilder/blob/master/src/main/webapp/scripts/backend.js)

```sh
var eb = new ExpressionBuilder("deptEmp", true);
```

###### Methods supported by Expression Builder

> eb refers to your instance of Expression Builder.

 - eb.sp - Start Paranthesis.
 - eb.ep - End paranthesis.
 - eb.and - add AND Predicate.
 - eb.or - add OR Predicate.
 - eb.addExp - add expressions mentioned below.
 - eb.clear - to clear all criteria's.
 - eb.build - return promise object.

Creating Expressions:
```sh
Expression.eq("id.empNo", "10001")
```

###### Methods supported

 - Expression.eq - create equals predicate.
 - Expression.ne - create not equal to predicate.
 - Expression.lt - create less than predicate.
 - Expression.lte - create less than equal to predicate.
 - Expression.gt - create greater than predicate.
 - Expression.gte - create greater than equal to predicate.
 - Expression.like - create like predicate.

Create expression chaining: 
> for example: (firstName=john AND lastName like 'Doe') AND ( dateOfBirth >= 1991-01-01 AND dateOfBirth <= 2000-01-01 ) OR employee.department.deptName like 'Human res'
```sh
eb.sp()  //eb expression builder
.addExp(Expression.eq("firstName", "john"))
.and()
.addExp(Expression.like("lastName", "doe"))
.ep()
.and()
.sp()
.addExp(Expression.gte("dateOfBirth", "1991-01-01"))
.and()
.addExp(Expression.lte("dateOfBirth", "2000-01-01"))
.ep()
.or()
.addExp(Expression.like("employee.department.deptName", "Human res"))
```

### Getting data:

expressionBuilder.build() returns [promise](https://developer.mozilla.org/en/docs/Web/JavaScript/Reference/Global_Objects/Promise) object
```sh
eb.build().then(function(response) {
    console.log(response); //response as json object
}, function(error) {
    console.log(error)
});
```

Verify the deployment by navigating to your server address in your preferred browser.

```sh
localhost:8000
```

### Todos
 - Write MOAR Tests

License
----

MIT
