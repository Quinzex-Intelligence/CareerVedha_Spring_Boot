package com.quinzex.repository;

import com.quinzex.entity.YoutubeUrls;
import com.quinzex.enums.Language;
import com.quinzex.enums.YoutubeCategory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface IYoutubeUrls extends JpaRepository<YoutubeUrls, Long> {
    List<YoutubeUrls> findTop10ByCategoryAndLanguageOrderByIdDesc(
            YoutubeCategory category,
            Language language
    );

    List<YoutubeUrls> findTop10ByCategoryAndLanguageAndIdLessThanOrderByIdDesc(
            YoutubeCategory category,
            Language language,
            Long id
    );
}
