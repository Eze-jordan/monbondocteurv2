package com.esiitech.monbondocteurv2.exception;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.web.ErrorResponse;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ResponseStatus;


@ControllerAdvice
public class GlobalExceptionHandler {

    // Exception personnalisée pour les erreurs de validation (400)
    @ExceptionHandler(IllegalArgumentException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ResponseEntity<ErrorDetails> handleInvalidArgument(IllegalArgumentException ex, HttpServletRequest request) {
        ErrorDetails errorDetails = new ErrorDetails(
                HttpStatus.BAD_REQUEST.value(),
                ex.getMessage(),
                request.getRequestURI()
        );
        return new ResponseEntity<>(errorDetails, HttpStatus.BAD_REQUEST);
    }

    // Exception pour les ressources non trouvées (404) - générique
    @ExceptionHandler(ResourceNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ResponseEntity<ErrorDetails> handleResourceNotFound(ResourceNotFoundException ex, HttpServletRequest request) {
        ErrorDetails errorDetails = new ErrorDetails(
                HttpStatus.NOT_FOUND.value(),
                ex.getMessage(),
                request.getRequestURI()
        );
        return new ResponseEntity<>(errorDetails, HttpStatus.NOT_FOUND);
    }

    // Gestion spécifique du médecin non trouvé (404)
    @ExceptionHandler(MedecinNonTrouveException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ResponseEntity<ErrorDetails> handleMedecinNotFound(MedecinNonTrouveException ex, HttpServletRequest request) {
        ErrorDetails errorDetails = new ErrorDetails(
                HttpStatus.NOT_FOUND.value(),
                ex.getMessage(),
                request.getRequestURI()
        );
        return new ResponseEntity<>(errorDetails, HttpStatus.NOT_FOUND);
    }

    // Gestion des erreurs de fichiers (400)
    @ExceptionHandler(FileUploadException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ResponseEntity<ErrorDetails> handleFileUploadException(FileUploadException ex, HttpServletRequest request) {
        ErrorDetails errorDetails = new ErrorDetails(
                HttpStatus.BAD_REQUEST.value(),
                ex.getMessage(),
                request.getRequestURI()
        );
        return new ResponseEntity<>(errorDetails, HttpStatus.BAD_REQUEST);
    }

    // Gestion des erreurs JSON (ex: JSON mal formé) (400)
    @ExceptionHandler(HttpMessageNotReadableException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ResponseEntity<ErrorDetails> handleHttpMessageNotReadable(HttpMessageNotReadableException ex, HttpServletRequest request) {
        ErrorDetails errorDetails = new ErrorDetails(
                HttpStatus.BAD_REQUEST.value(),
                "Erreur de lecture du message. Vérifiez le format JSON.",
                request.getRequestURI()
        );
        return new ResponseEntity<>(errorDetails, HttpStatus.BAD_REQUEST);
    }

    // Exception générique pour toutes les autres erreurs (500)
    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ResponseEntity<ErrorDetails> handleGenericException(Exception ex, HttpServletRequest request) {
        // tu peux logger ex ici si tu veux (logger.error(...))
        ErrorDetails errorDetails = new ErrorDetails(
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                "Une erreur interne s'est produite lors du traitement de la demande. Veuillez réessayer plus tard.",
                request.getRequestURI()
        );
        return new ResponseEntity<>(errorDetails, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(RelationDejaExistanteException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public ResponseEntity<ErrorDetails> handleRelationExists(RelationDejaExistanteException ex, HttpServletRequest request) {
        ErrorDetails err = new ErrorDetails(HttpStatus.CONFLICT.value(), ex.getMessage(), request.getRequestURI());
        return new ResponseEntity<>(err, HttpStatus.CONFLICT);
    }

    @ExceptionHandler(DisponibiliteConflitException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public ResponseEntity<ErrorDetails> handleDisponibiliteConflit(DisponibiliteConflitException ex, HttpServletRequest request) {
        ErrorDetails err = new ErrorDetails(HttpStatus.CONFLICT.value(), ex.getMessage(), request.getRequestURI());
        return new ResponseEntity<>(err, HttpStatus.CONFLICT);
    }
    @ExceptionHandler(SpecialiteIncompatibleException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public ResponseEntity<ErrorDetails> handleSpecialiteIncompatible(SpecialiteIncompatibleException ex, HttpServletRequest request) {
        ErrorDetails err = new ErrorDetails(HttpStatus.CONFLICT.value(), ex.getMessage(), request.getRequestURI());
        return new ResponseEntity<>(err, HttpStatus.CONFLICT);
    }
    /**
     * 403 - Utilisateur authentifié mais sans permission suffisante
     */
    @ExceptionHandler(AccessDeniedException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public ResponseEntity<ErrorDetails> handleAccessDenied(AccessDeniedException ex, HttpServletRequest request) {
        ErrorDetails err = new ErrorDetails(
                HttpStatus.FORBIDDEN.value(),
                "Accès refusé : vous n'avez pas les droits nécessaires.",
                request.getRequestURI()
        );
        // logger.warn("Access denied: {}", ex.getMessage());
        return new ResponseEntity<>(err, HttpStatus.FORBIDDEN);
    }

    /**
     * 401 - Pas authentifié (jeton manquant / non valide)
     */
    @ExceptionHandler(AuthenticationCredentialsNotFoundException.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public ResponseEntity<ErrorDetails> handleAuthenticationMissing(AuthenticationCredentialsNotFoundException ex, HttpServletRequest request) {
        ErrorDetails err = new ErrorDetails(
                HttpStatus.UNAUTHORIZED.value(),
                "Non authentifié : veuillez vous connecter.",
                request.getRequestURI()
        );
        // logger.info("Authentication required: {}", ex.getMessage());
        return new ResponseEntity<>(err, HttpStatus.UNAUTHORIZED);
    }

    @ExceptionHandler(AccesRefuseException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public ResponseEntity<ErrorDetails> handleAccesRefuse(
            AccesRefuseException ex,
            HttpServletRequest request) {

        ErrorDetails err = new ErrorDetails(
                HttpStatus.FORBIDDEN.value(),
                ex.getMessage(),
                request.getRequestURI()
        );
        return new ResponseEntity<>(err, HttpStatus.FORBIDDEN);
    }

    @ExceptionHandler(AgendaNonModifiableException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public ResponseEntity<ErrorDetails> handleAgendaNonModifiable(
            AgendaNonModifiableException ex,
            HttpServletRequest request) {

        ErrorDetails err = new ErrorDetails(
                HttpStatus.CONFLICT.value(),
                ex.getMessage(),
                request.getRequestURI()
        );
        return new ResponseEntity<>(err, HttpStatus.CONFLICT);
    }

    @ExceptionHandler(AgendaExisteDejaException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public ResponseEntity<ErrorDetails> handleAgendaExiste(
            AgendaExisteDejaException ex,
            HttpServletRequest request) {

        ErrorDetails err = new ErrorDetails(
                HttpStatus.CONFLICT.value(),
                ex.getMessage(),
                request.getRequestURI()
        );
        return new ResponseEntity<>(err, HttpStatus.CONFLICT);
    }

}
