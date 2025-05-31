package ar.edu.utn.frc.tup.piii.exception;


public class UserNotFoundException extends RuntimeException{
    public UserNotFoundException (String msg){
        super(msg);
    }
}
