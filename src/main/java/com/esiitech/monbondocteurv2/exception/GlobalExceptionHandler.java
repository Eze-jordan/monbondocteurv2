package com.esiitech.monbondocteurv2.exception;

import com.esiitech.monbondocteurv2.dto.ApiErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.OffsetDateTime;

@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * 403 - Abonnement expiré
     */
    @ExceptionHandler(AbonnementExpireException.class)
    public ResponseEntity<ApiErrorResponse> handleAbonnementExpire(
            AbonnementExpireException ex,
            HttpServletRequest request
    ) {
        ApiErrorResponse response = new ApiErrorResponse(
                HttpStatus.FORBIDDEN.value(),
                "ABONNEMENT_EXPIRE",
                ex.getMessage(),
                true,
                OffsetDateTime.now(),
                request.getRequestURI()
        );

        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response);
    }

    /**
     * 400 - Erreur de validation
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorDetails> handleInvalidArgument(
            IllegalArgumentException ex,
            HttpServletRequest request
    ) {
        ErrorDetails errorDetails = new ErrorDetails(
                HttpStatus.BAD_REQUEST.value(),
                ex.getMessage(),
                request.getRequestURI()
        );

        return new ResponseEntity<>(errorDetails, HttpStatus.BAD_REQUEST);
    }

    /**
     * 404 - Ressource introuvable
     */
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorDetails> handleResourceNotFound(
            ResourceNotFoundException ex,
            HttpServletRequest request
    ) {
        ErrorDetails errorDetails = new ErrorDetails(
                HttpStatus.NOT_FOUND.value(),
                ex.getMessage(),
                request.getRequestURI()
        );

        return new ResponseEntity<>(errorDetails, HttpStatus.NOT_FOUND);
    }

    /**
     * 404 - Médecin introuvable
     */
    @ExceptionHandler(MedecinNonTrouveException.class)
    public ResponseEntity<ErrorDetails> handleMedecinNotFound(
            MedecinNonTrouveException ex,
            HttpServletRequest request
    ) {
        ErrorDetails errorDetails = new ErrorDetails(
                HttpStatus.NOT_FOUND.value(),
                ex.getMessage(),
                request.getRequestURI()
        );

        return new ResponseEntity<>(errorDetails, HttpStatus.NOT_FOUND);
    }

    /**
     * 400 - Erreur upload fichier
     */
    @ExceptionHandler(FileUploadException.class)
    public ResponseEntity<ErrorDetails> handleFileUploadException(
            FileUploadException ex,
            HttpServletRequest request
    ) {
        ErrorDetails errorDetails = new ErrorDetails(
                HttpStatus.BAD_REQUEST.value(),
                ex.getMessage(),
                request.getRequestURI()
        );

        return new ResponseEntity<>(errorDetails, HttpStatus.BAD_REQUEST);
    }

    /**
     * 400 - JSON mal formé
     */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorDetails> handleHttpMessageNotReadable(
            HttpMessageNotReadableException ex,
            HttpServletRequest request
    ) {
        ErrorDetails errorDetails = new ErrorDetails(
                HttpStatus.BAD_REQUEST.value(),
                "Erreur de lecture du message. Vérifiez le format JSON.",
                request.getRequestURI()
        );

        return new ResponseEntity<>(errorDetails, HttpStatus.BAD_REQUEST);
    }

    /**
     * 409 - Relation déjà existante
     */
    @ExceptionHandler(RelationDejaExistanteException.class)
    public ResponseEntity<ErrorDetails> handleRelationExists(
            RelationDejaExistanteException ex,
            HttpServletRequest request
    ) {
        ErrorDetails err = new ErrorDetails(
                HttpStatus.CONFLICT.value(),
                ex.getMessage(),
                request.getRequestURI()
        );

        return new ResponseEntity<>(err, HttpStatus.CONFLICT);
    }

    /**
     * 409 - Conflit de disponibilité
     */
    @ExceptionHandler(DisponibiliteConflitException.class)
    public ResponseEntity<ErrorDetails> handleDisponibiliteConflit(
            DisponibiliteConflitException ex,
            HttpServletRequest request
    ) {
        ErrorDetails err = new ErrorDetails(
                HttpStatus.CONFLICT.value(),
                ex.getMessage(),
                request.getRequestURI()
        );

        return new ResponseEntity<>(err, HttpStatus.CONFLICT);
    }

    /**
     * 409 - Spécialité incompatible
     */
    @ExceptionHandler(SpecialiteIncompatibleException.class)
    public ResponseEntity<ErrorDetails> handleSpecialiteIncompatible(
            SpecialiteIncompatibleException ex,
            HttpServletRequest request
    ) {
        ErrorDetails err = new ErrorDetails(
                HttpStatus.CONFLICT.value(),
                ex.getMessage(),
                request.getRequestURI()
        );

        return new ResponseEntity<>(err, HttpStatus.CONFLICT);
    }

    /**
     * 403 - Accès refusé Spring Security
     */
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorDetails> handleAccessDenied(
            AccessDeniedException ex,
            HttpServletRequest request
    ) {
        ErrorDetails err = new ErrorDetails(
                HttpStatus.FORBIDDEN.value(),
                "Accès refusé : vous n'avez pas les droits nécessaires.",
                request.getRequestURI()
        );

        return new ResponseEntity<>(err, HttpStatus.FORBIDDEN);
    }

    /**
     * 401 - Non authentifié
     */
    @ExceptionHandler(AuthenticationCredentialsNotFoundException.class)
    public ResponseEntity<ErrorDetails> handleAuthenticationMissing(
            AuthenticationCredentialsNotFoundException ex,
            HttpServletRequest request
    ) {
        ErrorDetails err = new ErrorDetails(
                HttpStatus.UNAUTHORIZED.value(),
                "Non authentifié : veuillez vous connecter.",
                request.getRequestURI()
        );

        return new ResponseEntity<>(err, HttpStatus.UNAUTHORIZED);
    }

    /**
     * 403 - Accès refusé métier
     */
    @ExceptionHandler(AccesRefuseException.class)
    public ResponseEntity<ErrorDetails> handleAccesRefuse(
            AccesRefuseException ex,
            HttpServletRequest request
    ) {
        ErrorDetails err = new ErrorDetails(
                HttpStatus.FORBIDDEN.value(),
                ex.getMessage(),
                request.getRequestURI()
        );

        return new ResponseEntity<>(err, HttpStatus.FORBIDDEN);
    }

    /**
     * 409 - Agenda non modifiable
     */
    @ExceptionHandler(AgendaNonModifiableException.class)
    public ResponseEntity<ErrorDetails> handleAgendaNonModifiable(
            AgendaNonModifiableException ex,
            HttpServletRequest request
    ) {
        ErrorDetails err = new ErrorDetails(
                HttpStatus.CONFLICT.value(),
                ex.getMessage(),
                request.getRequestURI()
        );

        return new ResponseEntity<>(err, HttpStatus.CONFLICT);
    }

    /**
     * 409 - Agenda existe déjà
     */
    @ExceptionHandler(AgendaExisteDejaException.class)
    public ResponseEntity<ErrorDetails> handleAgendaExiste(
            AgendaExisteDejaException ex,
            HttpServletRequest request
    ) {
        ErrorDetails err = new ErrorDetails(
                HttpStatus.CONFLICT.value(),
                ex.getMessage(),
                request.getRequestURI()
        );

        return new ResponseEntity<>(err, HttpStatus.CONFLICT);
    }

    /**
     * 409 - Créneau complet
     */
    @ExceptionHandler(CreneauCompletException.class)
    public ResponseEntity<ErrorDetails> handleCreneauComplet(
            CreneauCompletException ex,
            HttpServletRequest request
    ) {
        ErrorDetails err = new ErrorDetails(
                HttpStatus.CONFLICT.value(),
                ex.getMessage(),
                request.getRequestURI()
        );

        return new ResponseEntity<>(err, HttpStatus.CONFLICT);
    }

    /**
     * 409 - Semaine non modifiable
     */
    @ExceptionHandler(SemaineNonModifiableException.class)
    public ResponseEntity<ErrorDetails> handleSemaineNonModifiable(
            SemaineNonModifiableException ex,
            HttpServletRequest request
    ) {
        ErrorDetails err = new ErrorDetails(
                HttpStatus.CONFLICT.value(),
                ex.getMessage(),
                request.getRequestURI()
        );

        return new ResponseEntity<>(err, HttpStatus.CONFLICT);
    }

    /**
     * 404 - Agenda introuvable
     */
    @ExceptionHandler(AgendaIntrouvableException.class)
    public ResponseEntity<ErrorDetails> handleAgendaIntrouvable(
            AgendaIntrouvableException ex,
            HttpServletRequest request
    ) {
        ErrorDetails err = new ErrorDetails(
                HttpStatus.NOT_FOUND.value(),
                ex.getMessage(),
                request.getRequestURI()
        );

        return new ResponseEntity<>(err, HttpStatus.NOT_FOUND);
    }

    /**
     * 500 - Erreur serveur générique
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorDetails> handleGenericException(
            Exception ex,
            HttpServletRequest request
    ) {
        ErrorDetails errorDetails = new ErrorDetails(
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                "Une erreur interne s'est produite lors du traitement de la demande. Veuillez réessayer plus tard.",
                request.getRequestURI()
        );

        return new ResponseEntity<>(errorDetails, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}