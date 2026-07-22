package com.lune.service;

import com.lune.entity.Family;
import java.util.List;

public interface FamilyService {
    List<Family> listFamilies();
    Family createFamily(Family family);
    Family updateFamily(Long id, Family family);
    void deleteFamily(Long id);
}
