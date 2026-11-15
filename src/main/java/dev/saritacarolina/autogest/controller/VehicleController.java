package dev.saritacarolina.autogest.controller;

import dev.saritacarolina.autogest.model.constant.PlateColor;
import dev.saritacarolina.autogest.model.dto.VehicleDTO;
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

    //Página completa: tabla
    @GetMapping
    public String getVehicleTemplate(Model model){

        List<Vehicle> vehicles = service.findAllVehicles();

        model.addAttribute("vehicles", vehicles);
        return "vehicles/list";
    }

    //Fragmento: modal vacío
    @GetMapping("/form")
    public String getEmptyForm(Model model){
        addFormData(model, emptyForm(), null);
        return "vehicles/form :: modal";
    }

    //Fragmento: modal con los datos del vehículo a editar
    @GetMapping("/{id}/edit")
    public String getEditForm(@PathVariable("id") Long vehicleId, Model model){
        try {
            addFormData(model, service.toDTO(service.findVehicleById(vehicleId)), vehicleId);
            return "vehicles/form :: modal";
        } catch (EntityNotFoundException e) {
            return toast(model, "error", e.getMessage());
        }
    }

    //Fragmento: tabla filtrada por parte de la placa o del propietario
    @GetMapping("/search")
    public String searchVehicles(@RequestParam(required = false) String q, Model model){
        model.addAttribute("vehicles", service.searchVehicles(q));
        model.addAttribute("q", q);
        return "vehicles/list :: table";
    }

    //Fragmento: modelos que coinciden con lo que se busca.
    @GetMapping("/car-models/search")
    public String searchCarModels(@RequestParam(required = false) String text, Model model){
        model.addAttribute("carModels", carModelService.searchCarModels(text));
        return "vehicles/form :: car-model-results";
    }

    @PostMapping
    public String createVehicle(@ModelAttribute VehicleDTO vehicleRequest, Model model){
        try {
            VehicleDTO created = service.createVehicle(vehicleRequest);

            model.addAttribute("vehicles", service.findAllVehicles());
            model.addAttribute("closeModal", true);
            return toast(model, "success", "Vehículo " + created.licensePlate() + " registrado");
        } catch (IllegalArgumentException | EntityNotFoundException e) {
            return toast(model, "error", e.getMessage());
        }
    }

    @PutMapping("/{id}")
    public String updateVehicle(@PathVariable("id") Long vehicleId,
                                @ModelAttribute VehicleDTO vehicleRequest,
                                Model model){
        try {
            VehicleDTO updated = service.updateVehicle(vehicleId, vehicleRequest);

            model.addAttribute("vehicles", service.findAllVehicles());
            model.addAttribute("closeModal", true);
            return toast(model, "success", "Vehículo " + updated.licensePlate() + " actualizado");
        } catch (IllegalArgumentException | EntityNotFoundException e) {
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

    //El modal necesita los colores y el texto del modelo elegido para el buscador
    private void addFormData(Model model, VehicleDTO vehicle, Long vehicleId){
        model.addAttribute("vehicle", vehicle);
        model.addAttribute("vehicleId", vehicleId);
        //Texto que se muestra en el buscador al editar o al devolver un error: "Mazda 3"
        model.addAttribute("carModelLabel", carModelService.findDisplayName(vehicle.carModelId()));
        model.addAttribute("plateColors", PlateColor.values());
    }

    private String toast(Model model, String type, String message){
        model.addAttribute("toastType", type);
        model.addAttribute("toastMessage", message);
        return "vehicles/response";
    }

    private VehicleDTO emptyForm(){
        return VehicleDTO.empty();
    }
}