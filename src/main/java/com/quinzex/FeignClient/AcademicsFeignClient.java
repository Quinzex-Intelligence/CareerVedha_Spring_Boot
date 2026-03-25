package com.quinzex.FeignClient;

import com.quinzex.dto.ClassHierarchyDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.cloud.openfeign.FeignClientProperties;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

@FeignClient(
        name = "academicsClient",
        url = "https://www.careervedha.com"
)
public interface AcademicsFeignClient {

    @GetMapping("/api/django/taxonomy/academics/levels/")
    List<ClassHierarchyDTO> getHierarchy();
}//end
