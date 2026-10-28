package dev.saritacarolina.autogest.controller;

import jakarta.servlet.http.HttpServletResponse;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.servlet.ModelAndView;

//Atrapa excepciones de toooodo
@ControllerAdvice
public class GlobalExceptionHandler {

    //La imagen supera el límite: Spring la rechaza antes de llegar al controlador
    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ModelAndView handleMaxUploadSize(HttpServletResponse response){
        //No reemplaza el formulario, solo muestra el Toast
        response.setHeader("HX-Reswap", "none");

        ModelAndView modelAndView = new ModelAndView("fragments/toast :: toast");
        modelAndView.addObject("toastType", "error");
        modelAndView.addObject("toastMessage", "La imagen supera el tamaño máximo de 2 MB");
        return modelAndView;
    }

    //La base de datos rechazó la operación (dato repetido o registro en uso).
    //Red de seguridad: los servicios ya validan esto con mensajes más precisos
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ModelAndView handleDataIntegrity(HttpServletResponse response){
        //No reemplaza el formulario, solo muestra el Toast
        response.setHeader("HX-Reswap", "none");

        ModelAndView modelAndView = new ModelAndView("fragments/toast :: toast");
        modelAndView.addObject("toastType", "error");
        modelAndView.addObject("toastMessage", "No se pudo completar: el dato ya existe o está siendo usado por otro registro");
        return modelAndView;
    }
}