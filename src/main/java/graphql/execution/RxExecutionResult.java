package graphql.execution;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import graphql.ExecutionResult;
import graphql.GraphQLError;
import rx.Observable;

public class RxExecutionResult implements ExecutionResult {

    private static final Logger logger = LoggerFactory.getLogger(RxExecutionResult.class);

    private Observable<?> dataObservable;

    private Observable<List<GraphQLError>> errorsObservable;
    
    private Observable<Map<Object,Object>> extensionsObservable;

    public RxExecutionResult(Observable<?> data, Observable<List<GraphQLError>> errors) {
    	this(data, errors, Observable.just(new HashMap<>()));
    }
    
    public RxExecutionResult(Observable<?> data, Observable<List<GraphQLError>> errors, Observable<Map<Object,Object>> extensions) {
        dataObservable = data;
        errorsObservable = errors;
        extensionsObservable = extensions;
    }

    public Observable<?> getDataObservable() {
        return dataObservable;
    }

    public Observable<List<GraphQLError>> getErrorsObservable() {
        return errorsObservable;
    }
    
    public Observable<Map<Object, Object>> getExtensionsObservable() {
		return extensionsObservable;
	}

	@SuppressWarnings("unchecked")
	@Override
    public Object getData() {
        logger.warn("getData() called instead of getDataObservable(), blocking (likely a bug)");
        return dataObservable.toBlocking().first();
    }

    @Override
    public List<GraphQLError> getErrors() {
        logger.warn("getErrors() called instead of getErrorsObservable(), blocking (likely a bug)");
        return errorsObservable.toBlocking().first();
    }
    
    @Override
    public Map<Object, Object> getExtensions() {
    	logger.warn("getExtensions() called instead of getExtensionsObservable(), blocking (likely a bug)");
    	return extensionsObservable.toBlocking().first();
    }
}
