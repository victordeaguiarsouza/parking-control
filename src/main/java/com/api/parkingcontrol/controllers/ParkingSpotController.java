package com.api.parkingcontrol.controllers;

import com.api.parkingcontrol.dtos.ParkingSpotDto;
import com.api.parkingcontrol.models.ParkingSpotModel;
import com.api.parkingcontrol.services.ParkingSpotService;
import org.springframework.beans.BeanUtils;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@RestController
@CrossOrigin(origins = "*", maxAge = 3600)
@RequestMapping("/parking-spot")
@EnableAutoConfiguration
public class ParkingSpotController {

    final ParkingSpotService parkingSpotService;

    public ParkingSpotController(ParkingSpotService parkingSpotService){
        this.parkingSpotService = parkingSpotService;
    }

    @PostMapping
    public ResponseEntity<Object> save(@RequestBody @Valid ParkingSpotDto parkingSpotDto){

        ParkingSpotModel parkingSpotModel = new ParkingSpotModel();
        BeanUtils.copyProperties(parkingSpotDto, parkingSpotModel);
        parkingSpotModel.setRegistrationDate(LocalDateTime.now(ZoneId.of("UTC")));

        if(this.parkingSpotService.existsByLicensePlateCar(parkingSpotDto.getLicensePlateCar()))
            return ResponseEntity.status(HttpStatus.CONFLICT).body("Conflict: Parking Plate Car is already in use!");
        if(this.parkingSpotService.existsByParkingSpotNumber(parkingSpotDto.getParkingSpotNumber()))
            return ResponseEntity.status(HttpStatus.CONFLICT).body("Conflict: Parking Spot is already in use!");
        if(this.parkingSpotService.existsByApartmentAndBlock(parkingSpotDto.getApartment(), parkingSpotDto.getBlock()))
            return ResponseEntity.status(HttpStatus.CONFLICT).body("Conflict: Parking Apartment or Block is already in use!");

        return ResponseEntity.status(HttpStatus.CREATED).body(this.parkingSpotService.save(parkingSpotModel));
    }

    @GetMapping
    public ResponseEntity<Page<ParkingSpotModel>> getAll(@PageableDefault(page = 0, size = 10, sort = "id", direction = Sort.Direction.ASC) Pageable pageable) {

        return ResponseEntity.status(HttpStatus.OK).body(this.parkingSpotService.findAll(pageable));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Object> get(@PathVariable(value = "id") UUID id) {

        Optional<ParkingSpotModel> parkingSpotModelOptional = this.parkingSpotService.findById(id);

        if(!parkingSpotModelOptional.isPresent())
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Parking Spot not found.");

        return ResponseEntity.status(HttpStatus.OK).body(parkingSpotModelOptional.get());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Object> delete(@PathVariable(value = "id") UUID id) {

        Optional<ParkingSpotModel> parkingSpotModelOptional = this.parkingSpotService.findById(id);

        if(!parkingSpotModelOptional.isPresent())
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Parking Spot not found.");

        this.parkingSpotService.delete(parkingSpotModelOptional.get());

        return ResponseEntity.status(HttpStatus.OK).body("Success deleted!");
    }

    @PutMapping("/{id}")
    public ResponseEntity<Object> put(@PathVariable(value = "id") UUID id,
                                      @RequestBody @Valid ParkingSpotDto parkingSpotDto) {

        Optional<ParkingSpotModel> parkingSpotModelOptional = this.parkingSpotService.findById(id);

        if(!parkingSpotModelOptional.isPresent())
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Parking Spot not found.");

        ParkingSpotModel parkingSpotModel = parkingSpotModelOptional.get();
        BeanUtils.copyProperties(parkingSpotDto, parkingSpotModel);
        parkingSpotModel.setId(parkingSpotModelOptional.get().getId());
        parkingSpotModel.setRegistrationDate(parkingSpotModelOptional.get().getRegistrationDate());


        return ResponseEntity.status(HttpStatus.OK).body(this.parkingSpotService.save(parkingSpotModel));
    }

}
