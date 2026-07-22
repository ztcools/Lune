package com.lune.service;

import com.lune.common.PageResult;
import com.lune.entity.Essay;

public interface EssayService {
    PageResult<Essay> listEssays(int page, int size);
    Essay getEssayById(Long id);
    Essay createEssay(Essay essay);
    Essay updateEssay(Long id, Essay essay);
    void deleteEssay(Long id);
}
