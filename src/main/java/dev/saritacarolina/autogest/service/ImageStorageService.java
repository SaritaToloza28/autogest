package dev.saritacarolina.autogest.service;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Map;
import java.util.UUID;

@Service
public class ImageStorageService {

    //donde se van a guardar
    private static final Path ROOT = Paths.get("uploads");

    //Tipo de archivo permitido -> extensión
    private static final Map<String, String> ALLOWED_TYPES = Map.of(
            "image/jpeg", ".jpg",
            "image/png", ".png",
            "image/webp", ".webp"
    );


    //Guardar la foto
    public String store(MultipartFile file, String folder) throws IllegalArgumentException {
        String extension = ALLOWED_TYPES.get(file.getContentType());

        if (extension == null){
            throw new IllegalArgumentException("Formato de imagen no permitido (solo JPG, PNG o WEBP)");
        }

        //inventa nuevo nombre
        String fileName = UUID.randomUUID() + extension;

        try {
            Path directory = ROOT.resolve(folder);
            Files.createDirectories(directory);
            Files.copy(file.getInputStream(), directory.resolve(fileName), StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            throw new UncheckedIOException("No se pudo guardar la imagen", e);
        }

        //Ruta relativa que se guarda en BD
        return "/uploads/" + folder + "/" + fileName;
    }

    public void delete(String relativePath){
        if (relativePath == null || relativePath.isBlank()){
            return;
        }

        try {
            Files.deleteIfExists(ROOT.resolve(relativePath.replaceFirst("^/uploads/", "")));
        } catch (IOException ignored) {
            //Si no se puede borrar el archivo, no se bloquea la operación
        }
    }
}