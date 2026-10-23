package dev.saritacarolina.autogest.controller;

import dev.saritacarolina.autogest.model.dto.SparePartDTO;
import dev.saritacarolina.autogest.model.entity.SparePart;
import dev.saritacarolina.autogest.service.CarModelService;
import dev.saritacarolina.autogest.service.SparePartService;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.UncheckedIOException;
import java.util.List;
import java.util.Set;

@Controller
@RequestMapping("/spare-parts")
public class SparePartController {

    private final SparePartService service;
    private final CarModelService carModelService;

    public SparePartController(SparePartService service, CarModelService carModelService) {
        this.service = service;
        this.carModelService = carModelService;
    }

    //Página completa: formulario vacío + tabla
    @GetMapping
    public String getSparePartTemplate(Model model){

        List<SparePart> spareParts = service.findAllSpareParts();

        model.addAttribute("spareParts", spareParts);
        addFormData(model, emptyForm(), null);
        return "spare-parts/list";
    }

    //Fragmento: formulario vacío (botón "Cancelar")
    @GetMapping("/form")
    public String getEmptyForm(Model model){
        addFormData(model, emptyForm(), null);
        return "spare-parts/list :: form";
    }

    //Fragmento: formulario con los datos del repuesto a editar
    @GetMapping("/{id}/edit")
    public String getEditForm(@PathVariable("id") Long sparePartId, Model model){
        try {
            addFormData(model, service.toDTO(service.findSparePartById(sparePartId)), sparePartId);
            return "spare-parts/list :: form";
        } catch (EntityNotFoundException e) {
            addFormData(model, emptyForm(), null);
            return toast(model, "error", e.getMessage());
        }
    }

    @PostMapping
    public String createSparePart(@ModelAttribute SparePartDTO sparePartRequest,
                                  @RequestParam(value = "image", required = false) MultipartFile image,
                                  Model model){
        try {
            SparePartDTO created = service.createSparePart(sparePartRequest, image);

            addFormData(model, emptyForm(), null);
            model.addAttribute("spareParts", service.findAllSpareParts());
            return toast(model, "success", "Repuesto " + created.name() + " creado");
        } catch (IllegalArgumentException | EntityNotFoundException | UncheckedIOException e) {
            //Se devuelve el formulario con lo que escribió el usuario
            addFormData(model, sparePartRequest, null);
            return toast(model, "error", e.getMessage());
        }
    }

    //POST y no PUT: los formularios con archivos se envían por POST
    @PostMapping("/{id}")
    public String updateSparePart(@PathVariable("id") Long sparePartId,
                                  @ModelAttribute SparePartDTO sparePartRequest,
                                  @RequestParam(value = "image", required = false) MultipartFile image,
                                  Model model){
        try {
            SparePartDTO updated = service.updateSparePart(sparePartId, sparePartRequest, image);

            addFormData(model, emptyForm(), null);
            model.addAttribute("spareParts", service.findAllSpareParts());
            return toast(model, "success", "Repuesto " + updated.name() + " actualizado");
        } catch (IllegalArgumentException | EntityNotFoundException | UncheckedIOException e) {
            addFormData(model, sparePartRequest, sparePartId);
            return toast(model, "error", e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    public String deleteSparePart(@PathVariable("id") Long sparePartId, Model model){
        try {
            service.deleteSparePart(sparePartId);
            model.addAttribute("spareParts", service.findAllSpareParts());
            return toast(model, "success", "Repuesto eliminado");
        } catch (IllegalArgumentException | EntityNotFoundException e) {
            model.addAttribute("spareParts", service.findAllSpareParts());
            return toast(model, "error", e.getMessage());
        }
    }

    //El formulario siempre necesita los modelos para los checkboxes
    //sparePartId va aparte porque el DTO no tiene id (null = formulario de creación)
    private void addFormData(Model model, SparePartDTO sparePart, Long sparePartId){
        model.addAttribute("sparePart", sparePart);
        model.addAttribute("sparePartId", sparePartId);
        model.addAttribute("carModels", carModelService.findAllCarModels());
    }

    private String toast(Model model, String type, String message){
        model.addAttribute("toastType", type);
        model.addAttribute("toastMessage", message);
        return "spare-parts/response";
    }

    private SparePartDTO emptyForm(){
        return new SparePartDTO("", "", null, "", 0, null, Set.of());
    }
}