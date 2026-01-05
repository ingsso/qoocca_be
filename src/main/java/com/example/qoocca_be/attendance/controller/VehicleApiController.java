package com.example.qoocca_be.attendance.controller;/*
package com.example.loginbe.attendance.controller;

import com.example.BackendServer.vehicle.Service.VehicleApiService;
import com.example.BackendServer.vehicle.db.VehicleEntity;
import com.example.BackendServer.vehicle.model.VehicleCreateDto;
import com.example.BackendServer.vehicle.model.VehicleListResponse;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.*;

@Slf4j // 로그 사용을 위해 추가
@RestController
@RequestMapping("/api/vehicle")
@RequiredArgsConstructor
public class VehicleApiController {

    private final VehicleApiService vehicleApiService;

    @PostMapping
    public ResponseEntity<VehicleCreateDto> createVehicle(@Valid @RequestBody VehicleCreateDto dto) {

        VehicleEntity entity = vehicleApiService.createVehicle(dto);

        VehicleCreateDto result = VehicleCreateDto.builder()
                .vehicleNumber(entity.getVehicleNumber())
                .vehicleName(entity.getType())
                .build();

        return ResponseEntity.status(HttpStatus.CREATED).body(result);
    }

    // 삭제
    @DeleteMapping("/{vehicleNumber}")
    public ResponseEntity<Void> deleteByVehicleNumber(@PathVariable String vehicleNumber) {
        log.info("차량번호 삭제 요청: {}", vehicleNumber);

        vehicleApiService.deleteByVehicleNumber(vehicleNumber);

        return ResponseEntity.ok().build(); // 200 OK 반환
    }

    @GetMapping
    public ResponseEntity<Page<VehicleListResponse>> getVehicleList(
        Pageable pageable,
        @RequestParam String vehicleName,
        @RequestParam VehicleEntity.Status status
    ) {
        return ResponseEntity.ok(vehicleApiService.getVehicleList(pageable, vehicleName, status));
    }
}
*/
