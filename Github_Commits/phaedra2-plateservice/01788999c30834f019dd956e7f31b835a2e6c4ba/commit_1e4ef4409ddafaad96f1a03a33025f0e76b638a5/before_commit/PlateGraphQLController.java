/**
 * Phaedra II
 *
 * Copyright (C) 2016-2023 Open Analytics
 *
 * ===========================================================================
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the Apache License as published by
 * The Apache Software Foundation, either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * Apache License for more details.
 *
 * You should have received a copy of the Apache License
 * along with this program.  If not, see <http://www.apache.org/licenses/>
 */
package eu.openanalytics.phaedra.plateservice.api;

import eu.openanalytics.phaedra.metadataservice.client.MetadataServiceClient;
import eu.openanalytics.phaedra.metadataservice.dto.TagDTO;
import eu.openanalytics.phaedra.metadataservice.enumeration.ObjectClass;
import eu.openanalytics.phaedra.plateservice.dto.PlateDTO;
import eu.openanalytics.phaedra.plateservice.dto.PlateMeasurementDTO;
import eu.openanalytics.phaedra.plateservice.dto.WellDTO;
import eu.openanalytics.phaedra.plateservice.service.PlateMeasurementService;
import eu.openanalytics.phaedra.plateservice.service.PlateService;
import eu.openanalytics.phaedra.plateservice.service.WellService;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.stereotype.Controller;

import java.util.List;
import java.util.stream.Collectors;

@Controller
public class PlateGraphQLController {

    private final PlateService plateService;
    private final WellService wellService;
    private final PlateMeasurementService plateMeasurementService;
    private final MetadataServiceClient metadataServiceClient;

    public PlateGraphQLController(PlateService plateService, WellService wellService, PlateMeasurementService plateMeasurementService, MetadataServiceClient metadataServiceClient) {
        this.plateService = plateService;
        this.wellService = wellService;
        this.plateMeasurementService = plateMeasurementService;
        this.metadataServiceClient = metadataServiceClient;
    }

    @QueryMapping
    public List<PlateDTO> getPlatesByExperimentId(@Argument long experimentId) {
        List<PlateDTO> result = plateService.getPlatesByExperimentId(experimentId);
        if (CollectionUtils.isNotEmpty(result)) {
            // Add tags
            result.stream().forEach(plateDTO -> {
                List<TagDTO> plateTags = metadataServiceClient.getTags(ObjectClass.PLATE, plateDTO.getId());
                plateDTO.setTags(plateTags.stream().map(tagDTO -> tagDTO.getTag()).collect(Collectors.toList()));
            });
        }
        return result;
    }

    @QueryMapping
    public List<PlateDTO> getPlatesByBarcode(@Argument String barcode) {
        List<PlateDTO> result = plateService.getPlatesByBarcode(barcode);
        if (CollectionUtils.isNotEmpty(result)) {
            // Add tags
            result.stream().forEach(plateDTO -> {
                List<TagDTO> plateTags = metadataServiceClient.getTags(ObjectClass.PLATE, plateDTO.getId());
                plateDTO.setTags(plateTags.stream().map(tagDTO -> tagDTO.getTag()).collect(Collectors.toList()));
            });
        }
        return result;
    }

    @QueryMapping
    public PlateDTO getPlateById(@Argument long plateId) {
        PlateDTO result = plateService.getPlateById(plateId);
        if (result != null) {
            List<TagDTO> plateTags = metadataServiceClient.getTags(ObjectClass.PLATE, result.getId());
            result.setTags(plateTags.stream().map(tagDTO -> tagDTO.getTag()).collect(Collectors.toList()));
        }
        return result;
    }

    @QueryMapping
    public List<WellDTO> getPlateWells(@Argument long plateId) {
        List<WellDTO> result = wellService.getWellsByPlateId(plateId);
        return result;
    }

    @QueryMapping
    public List<PlateMeasurementDTO> getPlateMeasurements(@Argument long plateId, @Argument boolean active) {
        if (active) {
            return List.of(plateMeasurementService.getActivePlateMeasurement(plateId));
        } else {
            return plateMeasurementService.getPlateMeasurements(plateId);
        }
    }
}
