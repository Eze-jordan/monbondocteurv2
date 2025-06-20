package com.esiitech.monbondocteurv2.exception;

import org.apache.tomcat.util.http.fileupload.FileUploadException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ResponseStatus;

@ControllerAdvice
public class GlobalExceptionHandler {

    // Exception personnalisée pour les erreurs de validation
    @ExceptionHandler(IllegalArgumentException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)  // Erreur 400 - Bad Request
    public ResponseEntity<ErrorDetails> handleInvalidArgument(IllegalArgumentException ex) {
        ErrorDetails errorDetails = new ErrorDetails(HttpStatus.BAD_REQUEST.value(), ex.getMessage());
        return new ResponseEntity<>(errorDetails, HttpStatus.BAD_REQUEST);
    }

    // Exception pour les erreurs liées à des ressources non trouvées
    @ExceptionHandler(ResourceNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)  // Erreur 404 - Not Found
    public ResponseEntity<ErrorDetails> handleResourceNotFound(ResourceNotFoundException ex) {
        ErrorDetails errorDetails = new ErrorDetails(HttpStatus.NOT_FOUND.value(), ex.getMessage());
        return new ResponseEntity<>(errorDetails, HttpStatus.NOT_FOUND);
    }

    // Exception générique pour toutes les autres erreurs
    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)  // Erreur 500 - Internal Server Error
    public ResponseEntity<ErrorDetails> handleGenericException(Exception ex) {
        ErrorDetails errorDetails = new ErrorDetails(HttpStatus.INTERNAL_SERVER_ERROR.value(), "Une erreur interne s'est produite.");
        return new ResponseEntity<>(errorDetails, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    // Gestion des erreurs dans les appels HTTP de type JSON
    @ExceptionHandler(HttpMessageNotReadableException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)  // Erreur 400 - Bad Request
    public ResponseEntity<ErrorDetails> handleHttpMessageNotReadableException(HttpMessageNotReadableException ex) {
        ErrorDetails errorDetails = new ErrorDetails(HttpStatus.BAD_REQUEST.value(), "Erreur de lecture du message. Vérifiez le format JSON.");
        return new ResponseEntity<>(errorDetails, HttpStatus.BAD_REQUEST);
    }

    // Exception pour les erreurs liées à des erreurs de validation de fichiers (ex: taille, type de fichier)
    @ExceptionHandler(FileUploadException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ResponseEntity<ErrorDetails> handleFileUploadException(FileUploadException ex) {
        ErrorDetails errorDetails = new ErrorDetails(HttpStatus.BAD_REQUEST.value(), ex.getMessage());
        return new ResponseEntity<>(errorDetails, HttpStatus.BAD_REQUEST);
    }

}
