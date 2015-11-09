# graphql-rxjava

#### Friendly warning: As GraphQL itself is currently a Working Draft, expect changes.

This is an execution strategy for [graphql-java](https://github.com/andimarek/graphql-java) that makes
it easier to use rxjava's Observable.

# Table of Contents

- [Rx Hello World](#hello-world)
- [License](#license)


### Rx Hello World

This is the famous "hello world" in graphql-rxjava:

```java
import graphql.schema.GraphQLObjectType;
import graphql.schema.GraphQLSchema;

import graphql.execution.RxExecutionStrategy;
import graphql.execution.RxExecutionResult;

import static graphql.Scalars.GraphQLString;
import static graphql.schema.GraphQLFieldDefinition.newFieldDefinition;
import static graphql.schema.GraphQLObjectType.newObject;

public class HelloWorld {

    public static void main(String[] args) {

        GraphQLObjectType queryType = newObject()
                        .name("helloWorldQuery")
                        .field(newFieldDefinition()
                                .type(GraphQLString)
                                .name("hello")
                                .staticValue(Observable.just("world")))
                                .build())
                        .build();

        GraphQLSchema schema = GraphQLSchema.newSchema()
                        .query(queryType)
                        .build();

        Observable<?> result = ((RxExecutionResult)new GraphQL(schema, new RxExecutionStrategy()).execute("{hello}")).getDataObservable();

        result.subscribe(System.out::println);
        // Prints: {hello=world}
    }
}
```

### Getting started with gradle

Make sure `mavenCentral` is among your repos:

```groovy
repositories {
    mavenCentral()
}

```
Dependency:

```groovy
dependencies {
  compile 'com.graphql-java:graphql-rxjava:0.0.1'
}

```

### License

graphql-rxjava is licensed under the MIT License. See [LICENSE](LICENSE) for details.

Copyright (c) 2015, NFL and [Contributors](https://github.com/nfl/graphql-java/graphs/contributors)

[graphql-js License](https://github.com/graphql/graphql-js/blob/master/LICENSE)
