package ar.edu.utn.frc.tup.piii.exception;

public class InvalidCredentialsException extends RuntimeException{
    public InvalidCredentialsException (String msg){
        super(msg);
    }
}
