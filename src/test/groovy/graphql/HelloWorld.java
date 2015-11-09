package graphql;


import graphql.execution.RxExecutionResult;
import graphql.execution.RxExecutionStrategy;
import graphql.schema.GraphQLObjectType;
import graphql.schema.GraphQLSchema;
import org.junit.Test;
import rx.Observable;
import rx.observers.TestSubscriber;

import java.util.Map;

import static graphql.Scalars.GraphQLString;
import static graphql.schema.GraphQLFieldDefinition.newFieldDefinition;
import static graphql.schema.GraphQLObjectType.newObject;
import static org.junit.Assert.assertEquals;

public class HelloWorld {

    public static void main(String[] args) {
        GraphQLObjectType queryType = newObject()
                .name("helloWorldQuery")
                .field(newFieldDefinition()
                        .type(GraphQLString)
                        .name("hello")
                        .staticValue(Observable.just("world"))
                        .build())
                .build();

        GraphQLSchema schema = GraphQLSchema.newSchema()
                .query(queryType)
                .build();

        Observable<?> result = ((RxExecutionResult)new GraphQL(schema, new RxExecutionStrategy()).execute("{hello}")).getDataObservable();

        result.subscribe(System.out::println);
    }

    @Test
    public void helloWorldTest() {
        GraphQLObjectType queryType = newObject()
                .name("helloWorldQuery")
                .field(newFieldDefinition()
                        .type(GraphQLString)
                        .name("hello")
                        .staticValue(Observable.just("world"))
                        .build())
                .build();

        GraphQLSchema schema = GraphQLSchema.newSchema()
                .query(queryType)
                .build();

        RxExecutionResult executionResult = (RxExecutionResult)new GraphQL(schema, new RxExecutionStrategy()).execute("{hello}");

        Observable<Map<String, Object>> result = (Observable<Map<String, Object>>)executionResult.getDataObservable();

        TestSubscriber<Map<String, Object>> testSubscriber = new TestSubscriber<>();

        result.subscribe(testSubscriber);

        testSubscriber.awaitTerminalEvent();

        testSubscriber.assertNoErrors();

        Map<String, Object> response = testSubscriber.getOnNextEvents().get(0);

        assertEquals("world", response.get("hello"));
    }
}
