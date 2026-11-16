package dev.saritacarolina.autogest.controller;

import dev.saritacarolina.autogest.service.RepairService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

//Consulta pública
@Controller
@RequestMapping("/repair-status")
public class RepairStatusController {

    private final RepairService repairService;

    public RepairStatusController(RepairService repairService) {
        this.repairService = repairService;
    }

    @GetMapping
    public String getRepairStatusTemplate(){
        return "public/repair-status";
    }

    //Fragmento: resultados de la búsqueda.
    //El formulario también envía plateColor, pero se ignora: la placa ya es única,
    //y filtrar por color daría "no encontrado" si el cliente escoge mal el color.
    @GetMapping("/search")
    public String searchRepairs(@RequestParam(required = false) String licensePlate, Model model){
        //Placa escrita, para el mensaje "No encontramos reparaciones para la placa ..."
        model.addAttribute("licensePlate", licensePlate == null ? "" : licensePlate.trim().toUpperCase());
        try {
            model.addAttribute("repairs", repairService.findRepairsByLicensePlate(licensePlate));
        } catch (IllegalArgumentException e) {
            model.addAttribute("message", e.getMessage());
        }
        return "public/repair-status :: results";
    }
}
