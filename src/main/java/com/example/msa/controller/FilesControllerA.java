package com.example.msa.controller;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import com.example.msa.model.FileEntity;
import com.example.msa.model.FileResponse;
import com.example.msa.service.FileService;




@RestController
@RequestMapping("files")
@EnableKafka
public class FilesControllerA {

    private final FileService fileService;
//
//	@Autowired
//	KafKaProducerService producerService;
//
//	@Autowired
//	JsonConverterImpl JsonConverterUtil; 
//
    @Autowired
    public FilesControllerA(FileService fileService) {
        this.fileService = fileService;
    }
//	@PostMapping(value = "/uploadkafka", consumes = { "multipart/form-data" })
//	@Operation(summary = "Upload a single File")
//	public ResponseEntity<?> uploadKafka(@RequestParam("file") MultipartFile file) {
//		String message = "";
//		try {
//			
//			String Json = JsonConverterUtil.preapareJsonObject(file);
//			  //Files.copy(file.getInputStream(), this.root.resolve(file.getOriginalFilename()));
////			file
//			this.producerService.sendMessage(Json);
//			//storageService.save(file);
//
//			message = "Uploaded the file successfully: " + file.getOriginalFilename();
//			return ResponseEntity.status(HttpStatus.OK).body(Json);
//		} catch (Exception e) {
//			message = "Could not upload the file: " + file.getOriginalFilename() + "!";
//			return ResponseEntity.status(HttpStatus.EXPECTATION_FAILED).body("Failed");
//		}
//	}
//
	@PostMapping(value = "/upload", consumes = { "multipart/form-data" })
//	@Operation(summary = "Upload a single File")
    public ResponseEntity<String> upload(@RequestParam("file") MultipartFile file) {
        try {
            fileService.save(file);

            return ResponseEntity.status(HttpStatus.OK)
                                 .body(String.format("File uploaded successfully: %s", file.getOriginalFilename()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                                 .body(String.format("Could not upload the file: %s!", file.getOriginalFilename()));
        }
    }
//	@PostMapping(value = "/uploadbulk", consumes = { "multipart/form-data" })
//	@Operation(summary = "Upload a multiple File")
//    public ResponseEntity<?> uploads(@RequestParam("files") MultipartFile[] files) {
//        try {
//			String uploadedFileName = getAllFilesName(files);
//			Arrays.asList(files).stream().forEach(e->{
//				//logic
//			});
//			fileService.saveAll(Arrays.asList(files));
//
//            return ResponseEntity.status(HttpStatus.OK)
//                                 .body(String.format("File uploaded successfully: %s", uploadedFileName));
//        } catch (Exception e) {
//        	String uploadedFileName = getAllFilesName(files);
//            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
//                                 .body(String.format("Could not upload the file: %s!", uploadedFileName));
//        }
//    }
//
//	private String getAllFilesName(MultipartFile[] files) {
//		String uploadedFileName = Arrays.stream(files).map(x -> x.getOriginalFilename()).filter(x -> !x.isEmpty())
//				.collect(Collectors.joining(" , "));
//		return uploadedFileName;
//	}
//	@PostMapping(value = "/uploademp", consumes = { "multipart/form-data" })
//	@Operation(summary = "Upload a single File")
//    public ResponseEntity<String> uploadEmp(@RequestParam("file") MultipartFile file) {
//        try {
//            fileService.saveEmployee(file);
//
//            return ResponseEntity.status(HttpStatus.OK)
//                                 .body(String.format("File uploaded successfully: %s", file.getOriginalFilename()));
//        } catch (Exception e) {
//            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
//                                 .body(String.format("Could not upload the file: %s!", file.getOriginalFilename()));
//        }
//    }
//
    @GetMapping
    public List<FileResponse> list() {
        return fileService.getAllFiles()
                          .stream()
                          .map(this::mapToFileResponse)
                          .collect(Collectors.toList());
    }

    private FileResponse mapToFileResponse(FileEntity fileEntity) {
        String downloadURL = ServletUriComponentsBuilder.fromCurrentContextPath()
                                                        .path("/files/")
                                                        .path(fileEntity.getId())
                                                        .toUriString();
        FileResponse fileResponse = new FileResponse();
        fileResponse.setId(fileEntity.getId());
        fileResponse.setName(fileEntity.getName());
        fileResponse.setContentType(fileEntity.getContentType());
        fileResponse.setSize(fileEntity.getSize());
        fileResponse.setUrl(downloadURL);

        return fileResponse;
    }

    @GetMapping("{id}")
    public ResponseEntity<byte[]> getFile(@PathVariable String id) {
        Optional<FileEntity> fileEntityOptional = fileService.getFile(id);

        if (!fileEntityOptional.isPresent()) {
            return ResponseEntity.notFound()
                                 .build();
        }

        FileEntity fileEntity = fileEntityOptional.get();
        return ResponseEntity.ok()
                             .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + fileEntity.getName() + "\"")
                             .contentType(MediaType.valueOf(fileEntity.getContentType()))
                             .body(fileEntity.getData());
    }
}