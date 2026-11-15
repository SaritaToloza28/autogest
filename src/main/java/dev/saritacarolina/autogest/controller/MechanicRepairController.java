package dev.saritacarolina.autogest.controller;

import dev.saritacarolina.autogest.model.entity.Repair;
import dev.saritacarolina.autogest.service.RepairService;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

//Reparaciones asignadas al mecánico
@Controller
@RequestMapping("/repairs/assigned")
public class MechanicRepairController {

    private final RepairService service;

    public MechanicRepairController(RepairService service) {
        this.service = service;
    }

    @GetMapping
    public String getAssignedTemplate(Authentication authentication, Model model){
        addAssignedData(model, authentication);
        return "repairs/assigned";
    }

    @PostMapping("/{id}/start")
    public String startRepair(@PathVariable("id") Long repairId, Authentication authentication, Model model){
        try {
            service.startRepair(repairId, authentication.getName());
            return toast(model, authentication, "success", "Reparación iniciada");
        } catch (IllegalArgumentException | EntityNotFoundException e) {
            return toast(model, authentication, "error", e.getMessage());
        }
    }

    @PostMapping("/{id}/complete")
    public String completeRepair(@PathVariable("id") Long repairId, Authentication authentication, Model model){
        try {
            service.completeRepair(repairId, authentication.getName());
            return toast(model, authentication, "success", "Reparación completada");
        } catch (IllegalArgumentException | EntityNotFoundException e) {
            return toast(model, authentication, "error", e.getMessage());
        }
    }

    @PostMapping("/{id}/parts")
    public String addSparePart(@PathVariable("id") Long repairId,
                               @RequestParam(required = false) Long sparePartId,
                               @RequestParam(required = false) Integer quantity,
                               Authentication authentication,
                               Model model){
        try {
            service.addSparePart(repairId, sparePartId, quantity, authentication.getName());
            return toast(model, authentication, "success", "Repuesto registrado");
        } catch (IllegalArgumentException | EntityNotFoundException e) {
            return toast(model, authentication, "error", e.getMessage());
        }
    }

    @DeleteMapping("/{id}/parts/{sparePartId}")
    public String removeSparePart(@PathVariable("id") Long repairId,
                                  @PathVariable("sparePartId") Long sparePartId,
                                  Authentication authentication,
                                  Model model){
        try {
            service.removeSparePart(repairId, sparePartId, authentication.getName());
            return toast(model, authentication, "success", "Repuesto quitado; volvió al stock");
        } catch (IllegalArgumentException | EntityNotFoundException e) {
            return toast(model, authentication, "error", e.getMessage());
        }
    }

    // ---------- Auxiliares ----------

    //Reparaciones del mecánico y los repuestos compatibles con su vehículo
    private void addAssignedData(Model model, Authentication authentication){
        List<Repair> repairs = service.findAssignedRepairs(authentication.getName());

        model.addAttribute("repairs", repairs);
        model.addAttribute("compatibleParts", service.findCompatiblePartsForInProgress(repairs));
    }

    //Después de cada acción se vuelve a pintar la lista con el mensaje
    private String toast(Model model, Authentication authentication, String type, String message){
        addAssignedData(model, authentication);
        model.addAttribute("toastType", type);
        model.addAttribute("toastMessage", message);
        return "repairs/assigned-response";
    }
}
