package com.quinzex.service;


import com.quinzex.entity.YoutubeUrls;
import com.quinzex.enums.Language;
import com.quinzex.enums.YoutubeCategory;
import com.quinzex.repository.IYoutubeUrls;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class YoutubeUrlsService implements IYoutubeService {

    private final IYoutubeUrls iYoutubeUrlsRepo;
    @Override
    @Transactional
    public String createYoutubeUrls(List<YoutubeUrls> youtubeUrls) {
        iYoutubeUrlsRepo.saveAll(youtubeUrls);
        return   youtubeUrls.size()+ "youtube urls saved sucessfully";
    }

    @Override
    @Transactional(readOnly = true)
    public List<YoutubeUrls> getYoutubeUrls(YoutubeCategory youtubeCategory,  Language language,Long cursorId) {
     if(cursorId == null){
         return iYoutubeUrlsRepo.findTop10ByCategoryAndLanguageOrderByIdDesc(youtubeCategory,language);
     }
     return iYoutubeUrlsRepo.findTop10ByCategoryAndLanguageAndIdLessThanOrderByIdDesc(youtubeCategory,language,cursorId);
    }

    @Override
    @Transactional
    public String deleteYoutubeUrls(List<Long> ids) {
       iYoutubeUrlsRepo.deleteAllById(ids);
       return ids.size()+ "youtube urls deleted sucessfully";
    }

    @Override
    @Transactional
    public String updateYoutubeUrls(YoutubeUrls youtubeUrls) {
        YoutubeUrls existing = iYoutubeUrlsRepo.findById(youtubeUrls.getId()).orElseThrow(()->new RuntimeException(youtubeUrls.getId()+" not found"));
        existing.setUrl(youtubeUrls.getUrl());
        existing.setCategory(youtubeUrls.getCategory());
        existing.setLanguage(youtubeUrls.getLanguage());
        existing.setTitle(youtubeUrls.getTitle());

        return "Youtube Videos updated successfully";

    }


}
