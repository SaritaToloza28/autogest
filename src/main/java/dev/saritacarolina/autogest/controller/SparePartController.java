package dev.saritacarolina.autogest.controller;

import dev.saritacarolina.autogest.model.dto.SparePartDTO;
import dev.saritacarolina.autogest.model.entity.SparePart;
import dev.saritacarolina.autogest.service.CarModelService;
import dev.saritacarolina.autogest.service.SparePartService;
import jakarta.persistence.EntityNotFoundException;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.UncheckedIOException;
import java.util.List;
import java.util.Set;

//Inventario de repuestos (administrador): tabla + KPIs + vista de referencia; el formulario es un modal
@Controller
@RequestMapping("/spare-parts")
public class SparePartController {

    private final SparePartService service;
    private final CarModelService carModelService;

    public SparePartController(SparePartService service, CarModelService carModelService) {
        this.service = service;
        this.carModelService = carModelService;
    }

    //Página completa: KPIs, filtro y tabla (la vista de referencia empieza cerrada)
    @GetMapping
    public String getSparePartTemplate(Model model){
        model.addAttribute("spareParts", service.findAllSpareParts());
        //El filtro necesita los modelos para su selector
        model.addAttribute("carModels", carModelService.findAllCarModels());
        return "spare-parts/list";
    }

    //Fragmento: tabla filtrada por nombre o EAN, modelo compatible y stock (con KPIs y aviso)
    @GetMapping("/search")
    public String searchSpareParts(@RequestParam(required = false) String q,
                                   @RequestParam(required = false) Long carModelId,
                                   @RequestParam(required = false, defaultValue = "false") boolean inStock,
                                   HttpServletResponse response,
                                   Model model){
        List<SparePart> spareParts = service.searchSpareParts(q, carModelId, inStock);
        model.addAttribute("spareParts", spareParts);

        //La tabla y los KPIs llegan con hx-swap-oob; el reemplazo principal no se usa
        response.setHeader("HX-Reswap", "none");

        if (spareParts.isEmpty()){
            String message = inStock ? "No hay repuestos con stock para esta búsqueda"
                                     : "No hay repuestos que coincidan con la búsqueda";
            return toast(model, "error", message);
        }

        if (carModelId != null){
            String count = spareParts.size() == 1 ? "1 repuesto compatible" : spareParts.size() + " repuestos compatibles";
            return toast(model, "success", count + " con " + carModelService.findDisplayName(carModelId));
        }

        return "spare-parts/response";
    }

    //Fragmento: vista de referencia de un repuesto (clic en una fila)
    @GetMapping("/{id}/detail")
    public String getDetail(@PathVariable("id") Long sparePartId, HttpServletResponse response, Model model){
        try {
            model.addAttribute("detail", service.findSparePartById(sparePartId));
            return "spare-parts/list :: detail";
        } catch (EntityNotFoundException e) {
            //No reemplaza la vista de referencia, solo muestra el aviso
            response.setHeader("HX-Reswap", "none");
            return toast(model, "error", e.getMessage());
        }
    }

    //Fragmento: modal vacío
    @GetMapping("/form")
    public String getEmptyForm(Model model){
        addFormData(model, emptyForm(), null);
        return "spare-parts/form :: form";
    }

    //Fragmento: modal con los datos del repuesto a editar
    @GetMapping("/{id}/edit")
    public String getEditForm(@PathVariable("id") Long sparePartId, HttpServletResponse response, Model model){
        try {
            addFormData(model, service.toDTO(service.findSparePartById(sparePartId)), sparePartId);
            return "spare-parts/form :: form";
        } catch (EntityNotFoundException e) {
            response.setHeader("HX-Reswap", "none");
            return toast(model, "error", e.getMessage());
        }
    }

    @PostMapping
    public String createSparePart(@ModelAttribute SparePartDTO sparePartRequest,
                                  @RequestParam(value = "image", required = false) MultipartFile image,
                                  Model model){
        try {
            SparePartDTO created = service.createSparePart(sparePartRequest, image);

            model.addAttribute("spareParts", service.findAllSpareParts());
            model.addAttribute("closeModal", true);
            return toast(model, "success", "Repuesto " + created.name() + " creado");
        } catch (IllegalArgumentException | EntityNotFoundException | UncheckedIOException e) {
            //El modal se vuelve a pintar con lo que escribió el usuario
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

            model.addAttribute("spareParts", service.findAllSpareParts());
            model.addAttribute("closeModal", true);
            //La vista de referencia se actualiza con los datos nuevos
            model.addAttribute("detail", service.findSparePartById(sparePartId));
            model.addAttribute("oob", true);
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

    // ---------- Auxiliares ----------

    //El modal siempre necesita los modelos para marcar los compatibles
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
