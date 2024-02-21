package school.hei.sary.endpoint;

import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.util.StringUtils;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import school.hei.sary.PojaGenerated;
import school.hei.sary.repository.DummyRepository;
import school.hei.sary.repository.DummyUuidRepository;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import javax.imageio.ImageIO;



@PojaGenerated
@RestController
@AllArgsConstructor
@RequestMapping("/blacks")
public class BlackAndWhiteImage {
    DummyRepository dummyRepository;
    DummyUuidRepository dummyUuidRepository;

    @PutMapping("/{id}")
    public ResponseEntity<String> uploadAndConvertToBlackAndWhite(@PathVariable("id") String id, @RequestParam("file") MultipartFile file, @RequestParam("uploadPath") String uploadPath){
        if(file.isEmpty()){
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "No file uploaded");
        }
        try{
            String filename = StringUtils.cleanPath(file.getOriginalFilename());

            String uploadedFileName = id + "_" + filename;
            Path uploadLocation = Paths.get(uploadPath).resolve(uploadedFileName);
            Files.copy(file.getInputStream(), uploadLocation);

            BufferedImage originalImage = ImageIO.read(file.getInputStream());

            BufferedImage blackAndWhiteImage = convertToBlackAndWhite(originalImage);

            String convertedFileName = id + ".png";
            Path convertedLocation = Paths.get(uploadPath).resolve(convertedFileName);
            ImageIO.write(blackAndWhiteImage, "png", convertedLocation.toFile());

            return ResponseEntity.ok("Image uploaded and converted to black and white successfully");
        } catch (IOException e){
            e.printStackTrace();
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to upload and convert image.");
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<Resource> getBlackAndWhiteImage(@PathVariable("id") String id, @RequestParam("uploadPath") String uploadPath){
        try{
            String filename = id + ".png";
            Path filePath = Paths.get(uploadPath).resolve(filename).normalize();
            Resource resource = new UrlResource(filePath.toUri());

            if(resource.exists()){
                return ResponseEntity.ok().body(resource);
            }else{
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Image not found");
            }
        }catch(IOException e){
            e.printStackTrace();
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Image not found");
        }
    }

    private BufferedImage convertToBlackAndWhite(BufferedImage originalImage){
        int width = originalImage.getWidth();
        int height = originalImage.getHeight();

        BufferedImage blackAndWhiteImage = new BufferedImage(width, height, BufferedImage.TYPE_BYTE_GRAY);

        for(int y=0; y < height; y++){
            for(int x=0; x < width; x++){
                int rgb = originalImage.getRGB(x,y);

                int red = (rgb >> 16) & 0xFF;
                int green = rgb & 0xFF;
                int blue = rgb & 0xFF;

                int gray = (red + green + blue) / 3;

                int grayValue = (gray << 16) | (gray << 8) | gray;

                blackAndWhiteImage.setRGB(x, y, grayValue);
            }
        }

        return blackAndWhiteImage;
    }

}
