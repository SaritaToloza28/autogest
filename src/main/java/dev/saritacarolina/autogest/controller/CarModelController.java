package dev.saritacarolina.autogest.controller;

import dev.saritacarolina.autogest.model.dto.CarModelDTO;
import dev.saritacarolina.autogest.model.entity.CarModel;
import dev.saritacarolina.autogest.service.CarModelService;
import jakarta.persistence.EntityNotFoundException;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
            //el id va aparte porque el DTO no lo tiene
            model.addAttribute("carModelId", carModelId);
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
            model.addAttribute("carModel", carModelRequest);
            model.addAttribute("carModelId", carModelId);
            return toast(model, "error", e.getMessage());
        }
    }

    // ---------- Tabla editable ----------

    //Fragmento: fila en modo lectura (botón "Cancelar" de la fila)
    @GetMapping("/{id}/row")
    public String getRow(@PathVariable("id") Long carModelId, HttpServletResponse response, Model model){
        return rowFragment(carModelId, "car-models/list :: row", response, model);
    }

    //Fragmento: fila en modo edición (lápiz o doble clic en una celda)
    @GetMapping("/{id}/row/edit")
    public String getRowEdit(@PathVariable("id") Long carModelId, HttpServletResponse response, Model model){
        return rowFragment(carModelId, "car-models/list :: row-edit", response, model);
    }

    //Guardar la fila: con errores la fila sigue en edición con lo que se escribió
    @PutMapping("/{id}/row")
    public String updateRow(@PathVariable("id") Long carModelId,
                            @ModelAttribute CarModelDTO carModelRequest,
                            HttpServletResponse response,
                            Model model){
        try {
            CarModelDTO updated = service.updateCarModel(carModelId, carModelRequest);
            model.addAttribute("m", service.findCarModelById(carModelId));
            return rowToast(model, "success", "Modelo " + updated.brand() + " " + updated.name() + " actualizado");
        } catch (EntityNotFoundException e) {
            response.setHeader("HX-Reswap", "none");
            return toast(model, "error", e.getMessage());
        } catch (IllegalArgumentException e) {
            Map<String, Object> typed = new HashMap<>();
            typed.put("id", carModelId);
            typed.put("brand", carModelRequest.brand());
            typed.put("name", carModelRequest.name());
            model.addAttribute("m", typed);
            model.addAttribute("editing", true);
            return rowToast(model, "error", e.getMessage());
        }
    }

    private String rowFragment(Long carModelId, String fragment, HttpServletResponse response, Model model){
        try {
            model.addAttribute("m", service.findCarModelById(carModelId));
            return fragment;
        } catch (EntityNotFoundException e) {
            //No reemplaza la fila, solo muestra el aviso
            response.setHeader("HX-Reswap", "none");
            return toast(model, "error", e.getMessage());
        }
    }

    private String rowToast(Model model, String type, String message){
        model.addAttribute("toastType", type);
        model.addAttribute("toastMessage", message);
        return "car-models/row-response";
    }

    @DeleteMapping("/{id}")
    public String deleteCarModel(@PathVariable("id") Long carModelId, Model model){
        try {
            service.deleteCarModel(carModelId);
            model.addAttribute("carModels", service.findAllCarModels());
            return toast(model, "success", "Modelo eliminado");
        } catch (IllegalArgumentException | EntityNotFoundException e) {
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
        return new CarModelDTO("", "");
    }
}