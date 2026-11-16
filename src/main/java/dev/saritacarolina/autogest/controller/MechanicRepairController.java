package dev.saritacarolina.autogest.controller;

import dev.saritacarolina.autogest.model.entity.Repair;
import dev.saritacarolina.autogest.service.RepairService;
import jakarta.persistence.EntityNotFoundException;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

//Reparaciones asignadas al mecánico
@Controller
@RequestMapping("/repairs/assigned")
public class MechanicRepairController {

    private final RepairService service;

    public MechanicRepairController(RepairService service) {
        this.service = service;
    }

    //Página completa: resumen + tabla (el detalle empieza vacío)
    @GetMapping
    public String getAssignedTemplate(Authentication authentication, Model model){
        model.addAttribute("repairs", service.findAssignedRepairs(authentication.getName()));
        return "repairs/assigned";
    }

    //Fragmento: detalle de una orden del mecánico, con los repuestos compatibles con su vehículo
    @GetMapping("/{id}/detail")
    public String getDetail(@PathVariable("id") Long repairId, Authentication authentication,
                            HttpServletResponse response, Model model){
        try {
            addDetail(model, service.findAssignedRepairById(repairId, authentication.getName()));
            return "repairs/assigned :: order-detail";
        } catch (IllegalArgumentException | EntityNotFoundException e) {
            //No reemplaza el detalle, solo muestra el aviso
            response.setHeader("HX-Reswap", "none");
            model.addAttribute("toastType", "error");
            model.addAttribute("toastMessage", e.getMessage());
            return "fragments/toast :: toast";
        }
    }

    @PostMapping("/{id}/start")
    public String startRepair(@PathVariable("id") Long repairId, Authentication authentication, Model model){
        try {
            service.startRepair(repairId, authentication.getName());
            return toast(model, authentication, repairId, "success", "Reparación iniciada");
        } catch (IllegalArgumentException | EntityNotFoundException e) {
            return toast(model, authentication, repairId, "error", e.getMessage());
        }
    }

    @PostMapping("/{id}/complete")
    public String completeRepair(@PathVariable("id") Long repairId, Authentication authentication, Model model){
        try {
            service.completeRepair(repairId, authentication.getName());
            return toast(model, authentication, repairId, "success", "Reparación completada");
        } catch (IllegalArgumentException | EntityNotFoundException e) {
            return toast(model, authentication, repairId, "error", e.getMessage());
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
            return toast(model, authentication, repairId, "success", "Repuesto registrado");
        } catch (IllegalArgumentException | EntityNotFoundException e) {
            return toast(model, authentication, repairId, "error", e.getMessage());
        }
    }

    @DeleteMapping("/{id}/parts/{sparePartId}")
    public String removeSparePart(@PathVariable("id") Long repairId,
                                  @PathVariable("sparePartId") Long sparePartId,
                                  Authentication authentication,
                                  Model model){
        try {
            service.removeSparePart(repairId, sparePartId, authentication.getName());
            return toast(model, authentication, repairId, "success", "Repuesto quitado; volvió al stock");
        } catch (IllegalArgumentException | EntityNotFoundException e) {
            return toast(model, authentication, repairId, "error", e.getMessage());
        }
    }

    // ---------- Auxiliares ----------

    //La orden del detalle y los repuestos compatibles con el modelo de su vehículo
    private void addDetail(Model model, Repair repair){
        model.addAttribute("detail", repair);
        model.addAttribute("compatibleParts", service.findCompatibleParts(repair));
    }

    //Después de cada acción se vuelven a pintar el resumen, la tabla y el detalle con el mensaje
    private String toast(Model model, Authentication authentication, Long repairId, String type, String message){
        model.addAttribute("repairs", service.findAssignedRepairs(authentication.getName()));
        model.addAttribute("oob", true);

        //Si la orden no existe o no es suya, solo se refresca la tabla
        try {
            addDetail(model, service.findAssignedRepairById(repairId, authentication.getName()));
        } catch (IllegalArgumentException | EntityNotFoundException ignored) {
        }

        model.addAttribute("toastType", type);
        model.addAttribute("toastMessage", message);
        return "repairs/assigned-response";
    }
}
