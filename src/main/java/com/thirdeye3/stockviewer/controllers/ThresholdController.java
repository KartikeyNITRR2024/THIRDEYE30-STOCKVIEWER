package com.thirdeye3.stockviewer.controllers;

import com.thirdeye3.stockviewer.dtos.Response;
import com.thirdeye3.stockviewer.dtos.ThresholdGroupDto;
import com.thirdeye3.stockviewer.services.ThresholdService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/sv/update/thresholds")
public class ThresholdController {

    @Autowired
    private ThresholdService thresholdService;

    @PostMapping()
    public Response<Boolean> updateOrAddThresholds(@RequestBody ThresholdGroupDto ThresholdGroupdto) {
        thresholdService.updateOrAddThresoldByUserId(ThresholdGroupdto);
        return new Response<>(true, 0, null, true);
    }
}
