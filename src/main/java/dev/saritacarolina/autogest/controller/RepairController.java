package dev.saritacarolina.autogest.controller;

import dev.saritacarolina.autogest.model.constant.PlateColor;
import dev.saritacarolina.autogest.model.constant.ServiceType;
import dev.saritacarolina.autogest.model.dto.RepairDTO;
import dev.saritacarolina.autogest.model.dto.VehicleDTO;
import dev.saritacarolina.autogest.model.entity.Repair;
import dev.saritacarolina.autogest.model.entity.Vehicle;
import dev.saritacarolina.autogest.service.AppUserService;
import dev.saritacarolina.autogest.service.CarModelService;
import dev.saritacarolina.autogest.service.RepairService;
import dev.saritacarolina.autogest.service.VehicleService;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

//TODO: revisar nuevos cambios(están más abajo, son varios)

//Órdenes del operador
@Controller
@RequestMapping("/repairs")
public class RepairController {

    private final RepairService service;
    private final VehicleService vehicleService;
    private final AppUserService appUserService;
    private final CarModelService carModelService;

    public RepairController(RepairService service,
                            VehicleService vehicleService,
                            AppUserService appUserService,
                            CarModelService carModelService) {
        this.service = service;
        this.vehicleService = vehicleService;
        this.appUserService = appUserService;
        this.carModelService = carModelService;
    }

    // ---------- Operador: dashboard de órdenes ----------

    //Página completa
    @GetMapping
    public String getRepairTemplate(Model model){
        model.addAttribute("repairs", service.findAllRepairs());
        return "repairs/dashboard";
    }

    //Fragmento: filas de la tabla filtradas por parte de la placa o del propietario
    @GetMapping("/search")
    public String searchRepairs(@RequestParam(required = false) String q, Model model){
        model.addAttribute("repairs", service.searchRepairs(q));
        return "repairs/dashboard :: orders-body";
    }

    //Fragmento: detalle de una orden
    @GetMapping("/{id}/detail")
    public String getDetail(@PathVariable("id") Long repairId, Model model){
        try {
            model.addAttribute("detail", service.findRepairById(repairId));
            return "repairs/dashboard :: order-detail";
        } catch (EntityNotFoundException e) {
            return toast(model, "error", e.getMessage());
        }
    }

    // ---------- Operador: modal (flotante) ----------

    //Fragmento: modal vacío
    @GetMapping("/form")
    public String getEmptyForm(Model model){
        addFormData(model, RepairDTO.empty(), null);
        addVehicleStep(model, "search", null, VehicleDTO.empty());
        return "repairs/create :: modal";
    }

    //Fragmento: Carga el modal pero con los datos de una reparación existente para editarla.
    @GetMapping("/{id}/edit")
    public String getEditForm(@PathVariable("id") Long repairId, Model model){
        try {
            Repair repair = service.findRepairById(repairId);

            addFormData(model, service.toDTO(repair), repairId);
            addVehicleStep(model, "locked", repair.getVehicle(), vehicleService.toDTO(repair.getVehicle()));
            return "repairs/create :: modal";
        } catch (EntityNotFoundException e) {
            return toast(model, "error", e.getMessage());
        }
    }

    @PostMapping
    public String createRepair(@ModelAttribute RepairDTO repairRequest, Model model){
        try {
            service.createRepair(repairRequest);

            refreshDashboard(model);
            model.addAttribute("closeModal", true);
            return toast(model, "success", "Orden de servicio registrada");
        } catch (IllegalArgumentException | EntityNotFoundException e) {
            //el modal queda abierto con lo que escribió el usuario
            return toast(model, "error", e.getMessage());
        }
    }

    @PutMapping("/{id}")
    public String updateRepair(@PathVariable("id") Long repairId,
                               @ModelAttribute RepairDTO repairRequest,
                               Model model){
        try {
            service.updateRepair(repairId, repairRequest);

            refreshDashboard(model);
            model.addAttribute("detail", service.findRepairById(repairId));
            model.addAttribute("closeModal", true);
            return toast(model, "success", "Orden de servicio actualizada");
        } catch (IllegalArgumentException | EntityNotFoundException e) {
            return toast(model, "error", e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    public String deleteRepair(@PathVariable("id") Long repairId, Model model){
        try {
            service.deleteRepair(repairId);
            refreshDashboard(model);
            //el detalle vuelve a "Selecciona una orden..." porque la orden ya no existe
            model.addAttribute("clearDetail", true);
            return toast(model, "success", "Orden de servicio eliminada");
        } catch (IllegalArgumentException | EntityNotFoundException e) {
            return toast(model, "error", e.getMessage());
        }
    }

    //marca la orden como entregada al cliente y refresca la vista.
    @PostMapping("/{id}/deliver")
    public String deliverRepair(@PathVariable("id") Long repairId, Model model){
        try {
            service.deliverRepair(repairId);
            refreshDashboard(model);
            model.addAttribute("detail", service.findRepairById(repairId));
            return toast(model, "success", "Vehículo entregado al cliente");
        } catch (IllegalArgumentException | EntityNotFoundException e) {
            return toast(model, "error", e.getMessage());
        }
    }

    //cancela la orden, devuelve los repuestos al stock y refresca la vista.
    @PostMapping("/{id}/cancel")
    public String cancelRepair(@PathVariable("id") Long repairId, Model model){
        try {
            service.cancelRepair(repairId);
            refreshDashboard(model);
            model.addAttribute("detail", service.findRepairById(repairId));
            return toast(model, "success", "Reparación cancelada; los repuestos volvieron al stock");
        } catch (IllegalArgumentException | EntityNotFoundException e) {
            return toast(model, "error", e.getMessage());
        }
    }

    // ---------- Operador, modal ----------

    //busca un vehículo por placa y muestra si ya existe o si hay que registrarlo.
    @GetMapping("/vehicle-lookup")
    public String lookupVehicle(@RequestParam(required = false) PlateColor plateColor,
                                @RequestParam(required = false) String licensePlate,
                                Model model){
        VehicleDTO typed = VehicleDTO.withPlate(licensePlate, plateColor);

        if (plateColor == null && (licensePlate == null || licensePlate.isBlank())){
            addVehicleStep(model, "search", null, VehicleDTO.empty());
            return "repairs/vehicle-response";
        }

        try {
            Optional<Vehicle> found = vehicleService.findVehicleByPlate(plateColor, licensePlate);

            if (found.isPresent()){
                showFoundVehicle(model, found.get(), plateColor);
            } else {
                //placa ya normalizada ("abc123" -> "ABC-123") para registrarla tal cual
                VehicleDTO newVehicle = VehicleDTO.withPlate(vehicleService.normalizePlate(licensePlate, plateColor), plateColor);
                addVehicleStep(model, "new", null, newVehicle);
            }
        } catch (IllegalArgumentException e) {
            addVehicleStep(model, "search", null, typed);
            model.addAttribute("vehicleError", e.getMessage());
        }

        return "repairs/vehicle-response";
    }

    //registra un vehículo nuevo y lo deja seleccionado para la orden.
    @PostMapping("/vehicle")
    public String createVehicle(@ModelAttribute VehicleDTO vehicleRequest, Model model){
        try {
            //registerVehicle devuelve la entidad guardada, con el id que necesita la orden
            Vehicle vehicle = vehicleService.registerVehicle(vehicleRequest);

            showFoundVehicle(model, vehicle, vehicle.getPlateColor());
            model.addAttribute("toastType", "success");
            model.addAttribute("toastMessage", "Vehículo " + vehicle.getLicensePlate() + " registrado");
        } catch (IllegalArgumentException | EntityNotFoundException e) {
            addVehicleStep(model, "new", null, vehicleRequest);
            model.addAttribute("vehicleError", e.getMessage());
        }

        return "repairs/vehicle-response";
    }

    //actualiza los datos de un vehículo y lo vuelve a mostrar en el modal.
    @PutMapping("/vehicle/{id}")
    public String updateVehicle(@PathVariable("id") Long vehicleId,
                                @ModelAttribute VehicleDTO vehicleRequest,
                                Model model){
        try {
            vehicleService.updateVehicle(vehicleId, vehicleRequest);

            Vehicle vehicle = vehicleService.findVehicleById(vehicleId);
            showFoundVehicle(model, vehicle, vehicle.getPlateColor());
            model.addAttribute("toastType", "success");
            model.addAttribute("toastMessage", "Datos del vehículo actualizados");
        } catch (IllegalArgumentException | EntityNotFoundException e) {
            try {
                addVehicleStep(model, "found", vehicleService.findVehicleById(vehicleId), vehicleRequest);
                model.addAttribute("vehicleError", e.getMessage());
            } catch (EntityNotFoundException notFound) {
                addVehicleStep(model, "search", null, VehicleDTO.empty());
                model.addAttribute("vehicleError", notFound.getMessage());
            }
        }

        return "repairs/vehicle-response";
    }

    // ---------- Auxiliares ----------

    private void addFormData(Model model, RepairDTO repair, Long repairId){
        model.addAttribute("repair", repair);
        model.addAttribute("repairId", repairId);
        model.addAttribute("mechanics", appUserService.findAllMechanics());
        model.addAttribute("serviceTypes", ServiceType.values());
    }

    private void addVehicleStep(Model model, String step, Vehicle vehicle, VehicleDTO vehicleForm){
        model.addAttribute("vehicleStep", step);
        model.addAttribute("vehicle", vehicle);
        model.addAttribute("vehicleForm", vehicleForm);
        model.addAttribute("plateColors", PlateColor.values());

        if (step.equals("new")){
            model.addAttribute("carModelLabel", carModelService.findDisplayName(vehicleForm.carModelId()));
        }
    }

    //Vehículo encontrado: avisa si ya tiene una orden abierta o si el color guardado es otro
    private void showFoundVehicle(Model model, Vehicle vehicle, PlateColor typedColor){
        addVehicleStep(model, "found", vehicle, vehicleService.toDTO(vehicle));

        if (service.hasOpenRepair(vehicle.getId())){
            model.addAttribute("vehicleWarning", "Este vehículo ya tiene una orden pendiente o en progreso.");
        } else if (typedColor != null && typedColor != vehicle.getPlateColor()){
            model.addAttribute("vehicleWarning", "La placa está registrada como " + vehicle.getPlateColor().getLabel() + ".");
        }
    }

    //Datos que se vuelven a pintar en el dashboard
    private void refreshDashboard(Model model){
        model.addAttribute("repairs", service.findAllRepairs());
        model.addAttribute("oob", true);
    }

    private String toast(Model model, String type, String message){
        model.addAttribute("toastType", type);
        model.addAttribute("toastMessage", message);
        return "repairs/response";
    }
}
