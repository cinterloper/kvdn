package net.iowntheinter.kvdn;

import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import net.iowntheinter.kvdn.storage.meta.types.ValueType;

public class KvdnResult<T> implements Future<T> {

    private final T result;
    private final ValueType type;

    /**
     * Create a future that has already succeeded
     * @param result the result
     */
    public KvdnResult(T result, ValueType type) {
        this.result = result;
        this.type = type;
    }

    @Override
    public boolean isComplete() {
        return true;
    }

    @Override
    public Future<T> setHandler(Handler<AsyncResult<T>> handler) {
        handler.handle(this);
        return this;
    }

    @Override
    public void complete(T result) {
        throw new IllegalStateException("Result is already complete: succeeded");
    }

    @Override
    public void complete() {
        throw new IllegalStateException("Result is already complete: succeeded");
    }

    @Override
    public void fail(Throwable cause) {
        throw new IllegalStateException("Result is already complete: succeeded");
    }

    @Override
    public void fail(String failureMessage) {
        throw new IllegalStateException("Result is already complete: succeeded");
    }

    @Override
    public boolean tryComplete(T result) {
        return false;
    }

    @Override
    public boolean tryComplete() {
        return false;
    }

    @Override
    public boolean tryFail(Throwable cause) {
        return false;
    }

    @Override
    public boolean tryFail(String failureMessage) {
        return false;
    }

    @Override
    public T result() {
        return result;
    }

    public ValueType type() { return type; }

    @Override
    public Throwable cause() {
        return null;
    }

    @Override
    public boolean succeeded() {
        return true;
    }

    @Override
    public boolean failed() {
        return false;
    }

    @Override
    public void handle(AsyncResult<T> asyncResult) {
        throw new IllegalStateException("Result is already complete: succeeded");
    }

    @Override
    public String toString() {
        return "Future{result=" + result + "}";
    }
}

