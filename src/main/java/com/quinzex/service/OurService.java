package com.quinzex.service;

import com.quinzex.entity.OurServices;
import com.quinzex.repository.OurServicesRepository;
import jakarta.transaction.Transactional;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import lombok.RequiredArgsConstructor;
import org.jsoup.select.Elements;
import org.springframework.beans.factory.annotation.Value;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.s3.S3Client;
import java.util.List;


@Service
@RequiredArgsConstructor
public class OurService implements IourSevice{
    private final OurServicesRepository ourServicesRepository;
    private final S3PresignedUrlService s3PresignedUrlService;
    private final S3Client s3Client;
    @Value("${aws.s3.bucket}")
    private String bucketName;
    @Transactional
    public OurServices  createOurService(OurServices ourService){
   validate(ourService);
  return ourServicesRepository.save(ourService);

    }

   @Transactional
    public OurServices updateService(Long id, OurServices ourService){
      OurServices existing =  ourServicesRepository.findById(id).orElseThrow(()->new RuntimeException("service not found"));
      validate(ourService);
      existing.setTitle(ourService.getTitle());
      existing.setDescription(ourService.getDescription());
      existing.setContent(ourService.getContent());
      return  ourServicesRepository.save(existing);
    }

    @Transactional
    public void deleteService(Long id) {
        ourServicesRepository.deleteById(id);
    }

    public List<OurServices> findServicesWithCursor(Long cursor,int size) {
        Pageable  pageable = PageRequest.of(0, size);
       List<OurServices> services;
       if(cursor==null){
           services= ourServicesRepository.findAll(pageable).getContent();
       }else {
           services = ourServicesRepository.findNextServices(cursor, pageable);
       }
       return services.stream().map(service->{
           OurServices ourServices = new OurServices();
           ourServices.setId(service.getId());
           ourServices.setTitle(service.getTitle());
           ourServices.setDescription(service.getDescription());
           ourServices.setContent(processHtml(service.getContent()));
           ourServices.setCreatedAt(service.getCreatedAt());
           ourServices.setUpdatedAt(service.getUpdatedAt());
           return ourServices;
       }).toList();
    }
    private void validate(OurServices ourService){
        if(ourService.getTitle() == null || ourService.getTitle().isEmpty()){
             throw new IllegalArgumentException("Title is required");
        }
        if(ourService.getContent() == null || ourService.getContent().isEmpty()){
            throw new IllegalArgumentException("content is required");
        }
    }

    @Transactional
    public OurServices getServiceById(Long id){
        OurServices service = ourServicesRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Service not found"));

        OurServices response = new OurServices();
        response.setId(service.getId());
        response.setTitle(service.getTitle());
        response.setDescription(service.getDescription());
        response.setContent(processHtml(service.getContent()));
        response.setCreatedAt(service.getCreatedAt());
        response.setUpdatedAt(service.getUpdatedAt());

        return response;
    }
    private String processHtml(String html){
        if(html == null || html.isEmpty()){
            return html;
        }
        Document doc = Jsoup.parse(html);
        Elements images = doc.select("img");
images.parallelStream().forEach(image -> {
    String key = image.attr("src");
    if(key == null || key.isEmpty() || key.startsWith("http")){
        return;
    }try{
         String signedurl = s3PresignedUrlService.generateViewUrl(bucketName,key);
         image.attr("src",signedurl);
    } catch (Exception e) {
        System.out.println("Error generating presigned URL for: " + key);
    }
});
return doc.body().html();
    }

}
