package com.quinzex.service;

import com.quinzex.entity.OurServices;

import java.util.List;

public interface IourSevice {
    public OurServices updateService(Long id, OurServices ourService);
    public OurServices  createOurService(OurServices ourService);
    public void deleteService(Long id);
    public List<OurServices> findServicesWithCursor(Long cursor, int size);
    public OurServices getServiceById(Long id);
}
