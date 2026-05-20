package deus.brainless.utils;

import java.util.function.Consumer;

public class Result<OK, ERR> {

    private final OK okData;
    private final ERR errData;
    private final boolean success;

    private Result(OK okData, ERR errData, boolean success) {
        this.okData = okData;
        this.errData = errData;
        this.success = success;
    }

    public static <OK, ERR> Result<OK, ERR> ok(OK data) {
        return new Result<>(data, null, true);
    }

    public static <OK, ERR> Result<OK, ERR> err(ERR data) {
        return new Result<>(null, data, false);
    }

    public Result<OK, ERR> ok(Consumer<OK> f) {
        if (success && okData != null) {
            f.accept(okData);
        }
        return this;
    }

    public Result<OK, ERR> err(Consumer<ERR> f) {
        if (!success && errData != null) {
            f.accept(errData);
        }
        return this;
    }

    public boolean isSuccess() {
        return success;
    }

    public boolean isErr() {
        return !success;
    }

    public OK getOk() {
        return okData;
    }

    public ERR getErr() {
        return errData;
    }
}
