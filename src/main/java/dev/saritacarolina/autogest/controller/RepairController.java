package dev.saritacarolina.autogest.controller;

import dev.saritacarolina.autogest.model.constant.RepairStatus;
import dev.saritacarolina.autogest.model.constant.ServiceType;
import dev.saritacarolina.autogest.model.dto.RepairDTO;
import dev.saritacarolina.autogest.model.entity.Repair;
import dev.saritacarolina.autogest.model.entity.SparePart;
import dev.saritacarolina.autogest.service.AppUserService;
import dev.saritacarolina.autogest.service.RepairService;
import dev.saritacarolina.autogest.service.SparePartService;
import dev.saritacarolina.autogest.service.VehicleService;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

//Rutas /repairs/** para OPERATOR y ADMIN; /repairs/assigned/** para MECHANIC y ADMIN (ver SecurityConfig)
@Controller
@RequestMapping("/repairs")
public class RepairController {

    private final RepairService service;
    private final VehicleService vehicleService;
    private final AppUserService appUserService;
    private final SparePartService sparePartService;

    public RepairController(RepairService service,
                            VehicleService vehicleService,
                            AppUserService appUserService,
                            SparePartService sparePartService) {
        this.service = service;
        this.vehicleService = vehicleService;
        this.appUserService = appUserService;
        this.sparePartService = sparePartService;
    }

    // ---------- Operador: registro y seguimiento ----------

    //Página completa: formulario vacío + tabla
    @GetMapping
    public String getRepairTemplate(Model model){
        model.addAttribute("repairs", service.findAllRepairs());
        addFormData(model, emptyForm(), null);
        return "repairs/list";
    }

    //Fragmento: formulario vacío (botón "Cancelar")
    @GetMapping("/form")
    public String getEmptyForm(Model model){
        addFormData(model, emptyForm(), null);
        return "repairs/list :: form";
    }

    //Fragmento: formulario con los datos de la reparación a editar
    @GetMapping("/{id}/edit")
    public String getEditForm(@PathVariable("id") Long repairId, Model model){
        try {
            addFormData(model, service.toDTO(service.findRepairById(repairId)), repairId);
            return "repairs/list :: form";
        } catch (EntityNotFoundException e) {
            addFormData(model, emptyForm(), null);
            return toast(model, "error", e.getMessage());
        }
    }

    @PostMapping
    public String createRepair(@ModelAttribute RepairDTO repairRequest, Model model){
        try {
            service.createRepair(repairRequest);

            addFormData(model, emptyForm(), null);
            model.addAttribute("repairs", service.findAllRepairs());
            return toast(model, "success", "Reparación registrada");
        } catch (IllegalArgumentException | EntityNotFoundException e) {
            //Se devuelve el formulario con lo que escribió el usuario
            addFormData(model, repairRequest, null);
            return toast(model, "error", e.getMessage());
        }
    }

    @PutMapping("/{id}")
    public String updateRepair(@PathVariable("id") Long repairId,
                               @ModelAttribute RepairDTO repairRequest,
                               Model model){
        try {
            service.updateRepair(repairId, repairRequest);

            addFormData(model, emptyForm(), null);
            model.addAttribute("repairs", service.findAllRepairs());
            return toast(model, "success", "Reparación actualizada");
        } catch (IllegalArgumentException | EntityNotFoundException e) {
            addFormData(model, repairRequest, repairId);
            return toast(model, "error", e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    public String deleteRepair(@PathVariable("id") Long repairId, Model model){
        try {
            service.deleteRepair(repairId);
            model.addAttribute("repairs", service.findAllRepairs());
            return toast(model, "success", "Reparación eliminada");
        } catch (IllegalArgumentException | EntityNotFoundException e) {
            model.addAttribute("repairs", service.findAllRepairs());
            return toast(model, "error", e.getMessage());
        }
    }

    @PostMapping("/{id}/deliver")
    public String deliverRepair(@PathVariable("id") Long repairId, Model model){
        try {
            service.deliverRepair(repairId);
            model.addAttribute("repairs", service.findAllRepairs());
            return toast(model, "success", "Vehículo entregado al cliente");
        } catch (IllegalArgumentException | EntityNotFoundException e) {
            model.addAttribute("repairs", service.findAllRepairs());
            return toast(model, "error", e.getMessage());
        }
    }

    @PostMapping("/{id}/cancel")
    public String cancelRepair(@PathVariable("id") Long repairId, Model model){
        try {
            service.cancelRepair(repairId);
            model.addAttribute("repairs", service.findAllRepairs());
            return toast(model, "success", "Reparación cancelada; los repuestos volvieron al stock");
        } catch (IllegalArgumentException | EntityNotFoundException e) {
            model.addAttribute("repairs", service.findAllRepairs());
            return toast(model, "error", e.getMessage());
        }
    }

    // ---------- Mecánico: reparaciones asignadas ----------

    @GetMapping("/assigned")
    public String getAssignedTemplate(Authentication authentication, Model model){
        addAssignedData(model, authentication);
        return "repairs/assigned";
    }

    @PostMapping("/assigned/{id}/start")
    public String startRepair(@PathVariable("id") Long repairId, Authentication authentication, Model model){
        try {
            service.startRepair(repairId, authentication.getName(), isAdmin(authentication));
            return assignedToast(model, authentication, "success", "Reparación iniciada");
        } catch (IllegalArgumentException | EntityNotFoundException e) {
            return assignedToast(model, authentication, "error", e.getMessage());
        }
    }

    @PostMapping("/assigned/{id}/complete")
    public String completeRepair(@PathVariable("id") Long repairId, Authentication authentication, Model model){
        try {
            service.completeRepair(repairId, authentication.getName(), isAdmin(authentication));
            return assignedToast(model, authentication, "success", "Reparación completada");
        } catch (IllegalArgumentException | EntityNotFoundException e) {
            return assignedToast(model, authentication, "error", e.getMessage());
        }
    }

    @PostMapping("/assigned/{id}/parts")
    public String addSparePart(@PathVariable("id") Long repairId,
                               @RequestParam(required = false) Long sparePartId,
                               @RequestParam(required = false) Integer quantity,
                               Authentication authentication,
                               Model model){
        try {
            service.addSparePart(repairId, sparePartId, quantity, authentication.getName(), isAdmin(authentication));
            return assignedToast(model, authentication, "success", "Repuesto registrado");
        } catch (IllegalArgumentException | EntityNotFoundException e) {
            return assignedToast(model, authentication, "error", e.getMessage());
        }
    }

    @DeleteMapping("/assigned/{id}/parts/{sparePartId}")
    public String removeSparePart(@PathVariable("id") Long repairId,
                                  @PathVariable("sparePartId") Long sparePartId,
                                  Authentication authentication,
                                  Model model){
        try {
            service.removeSparePart(repairId, sparePartId, authentication.getName(), isAdmin(authentication));
            return assignedToast(model, authentication, "success", "Repuesto quitado; volvió al stock");
        } catch (IllegalArgumentException | EntityNotFoundException e) {
            return assignedToast(model, authentication, "error", e.getMessage());
        }
    }

    // ---------- Auxiliares ----------

    //El formulario siempre necesita los vehículos, mecánicos y tipos de servicio para los <select>
    //repairId va aparte porque el DTO no tiene id (null = formulario de creación)
    private void addFormData(Model model, RepairDTO repair, Long repairId){
        model.addAttribute("repair", repair);
        model.addAttribute("repairId", repairId);
        model.addAttribute("vehicles", vehicleService.findAllVehicles());
        model.addAttribute("mechanics", appUserService.findAllMechanics());
        model.addAttribute("serviceTypes", ServiceType.values());
    }

    //Reparaciones del mecánico y, para las que están en progreso, los repuestos compatibles con su vehículo
    private void addAssignedData(Model model, Authentication authentication){
        List<Repair> repairs = service.findAssignedRepairs(authentication.getName(), isAdmin(authentication));

        Map<Long, List<SparePart>> compatibleParts = new HashMap<>();

        for (Repair repair : repairs){
            if (repair.getStatus() == RepairStatus.IN_PROGRESS){
                compatibleParts.put(repair.getId(),
                        sparePartService.findCompatibleSpareParts(repair.getVehicle().getCarModel().getId()));
            }
        }

        model.addAttribute("repairs", repairs);
        model.addAttribute("compatibleParts", compatibleParts);
    }

    private String toast(Model model, String type, String message){
        model.addAttribute("toastType", type);
        model.addAttribute("toastMessage", message);
        return "repairs/response";
    }

    private String assignedToast(Model model, Authentication authentication, String type, String message){
        addAssignedData(model, authentication);
        model.addAttribute("toastType", type);
        model.addAttribute("toastMessage", message);
        return "repairs/assigned-response";
    }

    private boolean isAdmin(Authentication authentication){
        return authentication.getAuthorities().stream()
                .anyMatch(authority -> authority.getAuthority().equals("ROLE_ADMIN"));
    }

    private RepairDTO emptyForm(){
        return new RepairDTO("", null, null, null, null, null);
    }
}
