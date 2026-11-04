package dev.saritacarolina.autogest.controller;

import dev.saritacarolina.autogest.model.constant.PlateColor;
import dev.saritacarolina.autogest.model.dto.VehicleDTO;
import dev.saritacarolina.autogest.model.entity.CarModel;
import dev.saritacarolina.autogest.model.entity.Vehicle;
import dev.saritacarolina.autogest.service.CarModelService;
import dev.saritacarolina.autogest.service.VehicleService;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/vehicles")
public class VehicleController {

    private final VehicleService service;
    private final CarModelService carModelService;

    public VehicleController(VehicleService service, CarModelService carModelService) {
        this.service = service;
        this.carModelService = carModelService;
    }

    //Página completa: formulario vacío + tabla
    @GetMapping
    public String getVehicleTemplate(Model model){

        List<Vehicle> vehicles = service.findAllVehicles();

        model.addAttribute("vehicles", vehicles);
        addFormData(model, emptyForm(), null);
        return "vehicles/list";
    }

    //Fragmento: formulario vacío (botón "Cancelar")
    @GetMapping("/form")
    public String getEmptyForm(Model model){
        addFormData(model, emptyForm(), null);
        return "vehicles/list :: form";
    }

    //Fragmento: formulario con los datos del vehículo a editar
    @GetMapping("/{id}/edit")
    public String getEditForm(@PathVariable("id") Long vehicleId, Model model){
        try {
            addFormData(model, service.toDTO(service.findVehicleById(vehicleId)), vehicleId);
            return "vehicles/list :: form";
        } catch (EntityNotFoundException e) {
            addFormData(model, emptyForm(), null);
            return toast(model, "error", e.getMessage());
        }
    }

    //Fragmento: modelos que coinciden con lo que se escribe en el buscador del formulario.
    //Va en /vehicles y no en /car-models porque el operador no puede entrar a /car-models (solo ADMIN)
    @GetMapping("/car-models/search")
    public String searchCarModels(@RequestParam(required = false) String text, Model model){
        model.addAttribute("carModels", carModelService.searchCarModels(text));
        return "vehicles/list :: car-model-results";
    }

    @PostMapping
    public String createVehicle(@ModelAttribute VehicleDTO vehicleRequest, Model model){
        try {
            VehicleDTO created = service.createVehicle(vehicleRequest);

            addFormData(model, emptyForm(), null);
            model.addAttribute("vehicles", service.findAllVehicles());
            return toast(model, "success", "Vehículo " + created.licensePlate() + " registrado");
        } catch (IllegalArgumentException | EntityNotFoundException e) {
            //Se devuelve el formulario con lo que escribió el usuario
            addFormData(model, vehicleRequest, null);
            return toast(model, "error", e.getMessage());
        }
    }

    @PutMapping("/{id}")
    public String updateVehicle(@PathVariable("id") Long vehicleId,
                                @ModelAttribute VehicleDTO vehicleRequest,
                                Model model){
        try {
            VehicleDTO updated = service.updateVehicle(vehicleId, vehicleRequest);

            addFormData(model, emptyForm(), null);
            model.addAttribute("vehicles", service.findAllVehicles());
            return toast(model, "success", "Vehículo " + updated.licensePlate() + " actualizado");
        } catch (IllegalArgumentException | EntityNotFoundException e) {
            addFormData(model, vehicleRequest, vehicleId);
            return toast(model, "error", e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    public String deleteVehicle(@PathVariable("id") Long vehicleId, Model model){
        try {
            service.deleteVehicle(vehicleId);
            model.addAttribute("vehicles", service.findAllVehicles());
            return toast(model, "success", "Vehículo eliminado");
        } catch (IllegalArgumentException | EntityNotFoundException e) {
            model.addAttribute("vehicles", service.findAllVehicles());
            return toast(model, "error", e.getMessage());
        }
    }

    //El formulario necesita los colores para el <select> y el texto del modelo elegido para el buscador
    //vehicleId va aparte porque el DTO no tiene id (null = formulario de creación)
    private void addFormData(Model model, VehicleDTO vehicle, Long vehicleId){
        model.addAttribute("vehicle", vehicle);
        model.addAttribute("vehicleId", vehicleId);
        model.addAttribute("carModelLabel", carModelLabel(vehicle.carModelId()));
        model.addAttribute("plateColors", PlateColor.values());
    }

    //Texto que se muestra en el buscador al editar o al devolver un error: "Mazda 3"
    private String carModelLabel(Long carModelId){
        if (carModelId == null){
            return "";
        }

        try {
            CarModel carModel = carModelService.findCarModelById(carModelId);
            return carModel.getBrand() + " " + carModel.getName();
        } catch (EntityNotFoundException e) {
            return "";
        }
    }

    private String toast(Model model, String type, String message){
        model.addAttribute("toastType", type);
        model.addAttribute("toastMessage", message);
        return "vehicles/response";
    }

    private VehicleDTO emptyForm(){
        return new VehicleDTO("", null, "", "", null);
    }
}