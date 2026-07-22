package com.lune.service;

import com.lune.entity.Tag;
import java.util.List;

public interface TagService {
    List<Tag> listAll();
    Tag createTag(Tag tag);
    void deleteTag(Long id);
}
