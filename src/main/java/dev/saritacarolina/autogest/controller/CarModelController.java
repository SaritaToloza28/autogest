package dev.saritacarolina.autogest.controller;

import dev.saritacarolina.autogest.model.dto.CarModelDTO;
import dev.saritacarolina.autogest.model.entity.CarModel;
import dev.saritacarolina.autogest.service.CarModelService;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/car-models")
public class CarModelController {

    private final CarModelService service;

    public CarModelController(CarModelService service) {
        this.service = service;
    }

    //Página completa: formulario vacío + tabla
    @GetMapping
    public String getCarModelTemplate(Model model){

        List<CarModel> carModels = service.findAllCarModels();

        model.addAttribute("carModels", carModels);
        model.addAttribute("carModel", emptyForm());
        return "car-models/list";
    }

    //Fragmento: formulario vacío (botón "Cancelar")
    @GetMapping("/form")
    public String getEmptyForm(Model model){
        model.addAttribute("carModel", emptyForm());
        return "car-models/list :: form";
    }

    //Fragmento: formulario con los datos del modelo a editar
    @GetMapping("/{id}/edit")
    public String getEditForm(@PathVariable("id") Long carModelId, Model model){
        try {
            model.addAttribute("carModel", service.toDTO(service.findCarModelById(carModelId)));
            return "car-models/list :: form";
        } catch (EntityNotFoundException e) {
            model.addAttribute("carModel", emptyForm());
            return toast(model, "error", e.getMessage());
        }
    }

    @PostMapping
    public String createCarModel(@ModelAttribute CarModelDTO carModelRequest, Model model){
        try {
            CarModelDTO created = service.createCarModel(carModelRequest);

            model.addAttribute("carModel", emptyForm());
            model.addAttribute("carModels", service.findAllCarModels());
            return toast(model, "success", "Modelo " + created.brand() + " " + created.name() + " creado");
        } catch (IllegalArgumentException e) {
            //Se devuelve el formulario con lo que escribió el usuario
            model.addAttribute("carModel", carModelRequest);
            return toast(model, "error", e.getMessage());
        }
    }

    @PutMapping("/{id}")
    public String updateCarModel(@PathVariable("id") Long carModelId,
                                 @ModelAttribute CarModelDTO carModelRequest,
                                 Model model){
        try {
            CarModelDTO updated = service.updateCarModel(carModelId, carModelRequest);

            model.addAttribute("carModel", emptyForm());
            model.addAttribute("carModels", service.findAllCarModels());
            return toast(model, "success", "Modelo " + updated.brand() + " " + updated.name() + " actualizado");
        } catch (IllegalArgumentException | EntityNotFoundException e) {
            model.addAttribute("carModel", new CarModelDTO(carModelId, carModelRequest.name(), carModelRequest.brand()));
            return toast(model, "error", e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    public String deleteCarModel(@PathVariable("id") Long carModelId, Model model){
        try {
            service.deleteCarModel(carModelId);
            model.addAttribute("carModels", service.findAllCarModels());
            return toast(model, "success", "Modelo eliminado");
        } catch (EntityNotFoundException e) {
            model.addAttribute("carModels", service.findAllCarModels());
            return toast(model, "error", e.getMessage());
        }
    }

    private String toast(Model model, String type, String message){
        model.addAttribute("toastType", type);
        model.addAttribute("toastMessage", message);
        return "car-models/response";
    }

    private CarModelDTO emptyForm(){
        return new CarModelDTO(null, "", "");
    }
}