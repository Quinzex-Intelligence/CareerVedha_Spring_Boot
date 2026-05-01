package com.quinzex.service;

import com.quinzex.entity.YoutubeUrls;
import com.quinzex.enums.Language;
import com.quinzex.enums.YoutubeCategory;

import java.util.List;

public interface IYoutubeService {

    public String createYoutubeUrls(List<YoutubeUrls> youtubeUrls);

    List<YoutubeUrls> getYoutubeUrls(YoutubeCategory youtubeCategory, Language language, Long cursorId);

    public String deleteYoutubeUrls(List<Long> ids);

    public String updateYoutubeUrls(YoutubeUrls youtubeUrls);
}
