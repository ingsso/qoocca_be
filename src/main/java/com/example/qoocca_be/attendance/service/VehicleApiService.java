package com.example.qoocca_be.attendance.service;/*
package com.example.loginbe.attendance.service;

import com.example.BackendServer.vehicle.db.VehicleEntity;
import com.example.BackendServer.vehicle.db.VehicleRepository;
import com.example.BackendServer.vehicle.model.VehicleCreateDto;
import com.example.BackendServer.vehicle.model.VehicleListResponse;

import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class VehicleApiService {

    private final VehicleRepository vehicleRepository;

    */
/**
     * 차량 등록 : Repository에 차량 entity를 등록한다.
     *
     * @param dto : vehicleNumber와 vehicleName을 field로 가진다.
     * @return VehicleEntity  : dto를 이용해 Builder로 Entity 생성 후 반환
     *//*

	  @Transactional
    public VehicleEntity createVehicle(VehicleCreateDto dto) {

        VehicleEntity entity = VehicleEntity.builder()
                .vehicleNumber(dto.getVehicleNumber())
                .status(VehicleEntity.Status.INACTIVE)
                .totalDist(0L)
                .type(dto.getVehicleName())
                .createDate(LocalDateTime.now())
                .build();

        VehicleEntity saved = vehicleRepository.save(entity);
        return saved;
    }


    */
/**
     * 차량 리스트 조회 : 차량 타입과 상태에 따라 페이지네이션된 차량 리스트를 반환한다.
     * @param pageable page, sort 정보를 가진다정
     * @param vehicleName 차량 번호
     * @param status 차량 상태
     * @return Page<VehicleListResponse> : 차량 리스트를 반환한다.
     *//*

    public Page<VehicleListResponse> getVehicleList(Pageable pageable, String vehicleName, VehicleEntity.Status status) {
      return vehicleRepository.findAllByStatusAndVehicleNumberContains(status, vehicleName,
        pageable).map(
        vehicle -> VehicleListResponse.builder()
          .carNumber(vehicle.getVehicleNumber())
          .type(vehicle.getType())
          .status(vehicle.getStatus())
          .totalDist(vehicle.getTotalDist())
          .build()
      );
    }

    // 삭제
    @Transactional
    public void deleteByVehicleNumber(String vehicleNumber) {
        VehicleEntity vehicle = vehicleRepository.findByVehicleNumber(vehicleNumber)
                .orElseThrow(() -> new IllegalArgumentException(vehicleNumber));
        vehicleRepository.delete(vehicle);
    }
}
*/
