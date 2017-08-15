package graphql.execution;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import org.apache.commons.lang3.tuple.Pair;

import graphql.ExecutionResult;
import graphql.language.Field;
import graphql.schema.GraphQLList;
import rx.Observable;

public class RxExecutionStrategy extends ExecutionStrategy {

    @Override
    public ExecutionResult execute(ExecutionContext executionContext, ExecutionParameters parameters)
    		throws NonNullableFieldWasNullException 
    {
    	List<Observable<Pair<String, ?>>> observables = new ArrayList<>();
    	Map<String, List<Field>> fields = parameters.fields();
        for (String fieldName : fields.keySet()) {
            final List<Field> fieldList = fields.get(fieldName);

            ExecutionResult executionResult = resolveField(executionContext, parameters, fieldList);

            if (executionResult instanceof RxExecutionResult) {
                RxExecutionResult rxResult = (RxExecutionResult)executionResult;
                Observable<?> unwrapped = rxResult.getDataObservable().flatMap(potentialResult -> {
                    if (potentialResult instanceof RxExecutionResult) {
                        return ((RxExecutionResult) potentialResult).getDataObservable();
                    }

                    if (potentialResult instanceof ExecutionResult) {
                        return Observable.just(((ExecutionResult) potentialResult).getData());
                    }

                    return Observable.just(potentialResult);
                });

                observables.add(Observable.zip(Observable.just(fieldName), unwrapped, Pair::of));
            } else {
                observables.add(Observable.just(Pair.of(fieldName, executionResult != null ? executionResult.getData() : null)));
            }
        }

        Observable<Map<String, Object>> result =
                Observable.merge(observables)
                        .toMap(Pair::getLeft, Pair::getRight);

        return new RxExecutionResult(result, Observable.just(executionContext.getErrors()));
    }
    
    @Override
    protected ExecutionResult completeValue(ExecutionContext executionContext, ExecutionParameters parameters,
    		List<Field> fields) 
    {
        Object result = parameters.source();
    	if (result instanceof Observable) {
            return new RxExecutionResult(((Observable<?>) result).map(r -> {
            	
            	ExecutionParameters newParameters = ExecutionParameters.newParameters()
                        .typeInfo(parameters.typeInfo())
                        .fields(parameters.fields())
                        .source(r).build();
            	
            	return super.completeValue(executionContext, newParameters, fields);
            }), null);
        }
        return super.completeValue(executionContext, parameters, fields);
    }
    
    @Override
    protected ExecutionResult completeValueForList(ExecutionContext executionContext, ExecutionParameters parameters,
    		List<Field> fields, Iterable<Object> result) 
    {
    	TypeInfo typeInfo = parameters.typeInfo();
        GraphQLList fieldType = typeInfo.castType(GraphQLList.class);
    	AtomicInteger idx = new AtomicInteger(0);
    	Observable<?> resultObservable =
                Observable.from(
                		StreamSupport.stream(result.spliterator(), false)
                			.map(i -> new ListTuple(idx.getAndIncrement(), i))
                			.toArray(ListTuple[]::new)
                )
                .flatMap(tuple -> {
                	TypeInfo newType = typeInfo.asType(fieldType.getWrappedType());
					ExecutionParameters newParameters = ExecutionParameters.newParameters()
                            .typeInfo(newType)
                            .fields(parameters.fields())
                            .source(tuple.result).build();
                	
                    ExecutionResult executionResult = completeValue(executionContext, newParameters, fields);
                    if (executionResult instanceof RxExecutionResult) {
                        return Observable.zip(Observable.just(tuple.index), ((RxExecutionResult)executionResult).getDataObservable(), ListTuple::new);
                    }
                    return Observable.just(new ListTuple(tuple.index, executionResult.getData()));
                })
                .toList()
                .map(listTuples -> {
                    return listTuples.stream()
                            .sorted(Comparator.comparingInt(x -> x.index))
                            .map(x -> x.result)
                            .collect(Collectors.toList());
                });

        return new RxExecutionResult(resultObservable, null);
    }
    
    private class ListTuple {
        public int index;
        public Object result;

        public ListTuple(int index, Object result) {
            this.index = index;
            this.result = result;
        }
    }
}
