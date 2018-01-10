package com.fisincorporated.exercisetracker.utility;


public class CustomException extends Exception {

    public CustomException(){
        super();
    }

    public CustomException(String message) {
        super(message, null);
    }

    public CustomException(String message, Throwable cause) {
        super(message, cause);
    }

}
