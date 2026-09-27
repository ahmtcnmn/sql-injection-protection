package com.ahmtcnmn.manager.common;

import com.ahmtcnmn.manager.common.exceptionController.RootEntity;

public class RestBaseController {


    public <T> RootEntity<T> ok(T data) {
        return RootEntity.success(data);
    }
    public  RootEntity<Void> ok() {
        return RootEntity.success(null);
    }

    public <T> RootEntity<T> error(String message) {
        return RootEntity.failure(message);
    }

    public <T> RootEntity<T> error(java.util.List<String> messages) {
        return RootEntity.failure(messages);
    }
    
}
