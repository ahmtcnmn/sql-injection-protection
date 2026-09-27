package com.ahmtcnmn.manager.common.exceptionController;
import java.util.*;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RootEntity<T> {

    private boolean result;
    private List<String> messages;
    private T data;
    
    public static <T> RootEntity<T> success(T data) {
        return new RootEntity<>(true, null, data);
    }

    public static <T> RootEntity<T> failure(List<String> messages) {
        return new RootEntity<>(false, messages, null);
    }
    
    public static <T> RootEntity<T> failure(String message) {
        return new RootEntity<>(false, Arrays.asList(message), null);
    }

}
