package com.eng.util;

import lombok.Data;

import java.util.List;

@Data
public class Result {
    private Boolean success;
    private String errorMsg;
    private Object data;
    public Result(Boolean success, String errorMsg, Object data) {
        this.success = success;
        this.errorMsg = errorMsg;
        this.data = data;
    }

    public static Result ok(){
        return new Result(true, null, null);
    }
    public static Result ok(Object data){
        return new Result(true, null, data);
    }
    public static Result ok(List<?> data){
        return new Result(true, null, data);
    }
    public static Result fail(String errorMsg){
        return new Result(false, errorMsg, null);
    }



}
