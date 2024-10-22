package pe.upc.ruedarentprojectmobile.service.Reservation;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import pe.upc.ruedarentprojectmobile.model.Acquirer;
import pe.upc.ruedarentprojectmobile.model.Reservation;
import pe.upc.ruedarentprojectmobile.model.Vehicle;
import pe.upc.ruedarentprojectmobile.repository.AcquirerRepository;
import pe.upc.ruedarentprojectmobile.repository.ReservationRepository;
import pe.upc.ruedarentprojectmobile.repository.VehicleRepository;
import pe.upc.ruedarentprojectmobile.request.AddReservationRequest;
import pe.upc.ruedarentprojectmobile.request.ReservationUpdateRequest;

import java.util.List;
import java.util.Optional;

//para despues

@Service
@RequiredArgsConstructor
public class ReservationService implements IReservationService {
    private final ReservationRepository reservationRepository;
    private final AcquirerRepository acquirerRepository;
    private final VehicleRepository vehicleRepository;


    @Override
    public Reservation getReservationById(Long id) {
        return reservationRepository.findById(id).orElse(null);
    }

    @Override
    public List<Reservation> getAllReservations() {
        return reservationRepository.findAll();
    }

    @Override
    public Reservation addReservation(AddReservationRequest request) {
        // Verificar si el Acquirer existe
        Acquirer acquirer = acquirerRepository.findById(request.getAcquirer().getIdClient())
                .orElseThrow(() -> new IllegalArgumentException("Acquirer not found with id: " + request.getAcquirer().getIdClient()));

        // Verificar si el Vehicle existe
        Vehicle vehicle = vehicleRepository.findById(request.getVehicle().getIdVehicle())
                .orElseThrow(() -> new IllegalArgumentException("Vehicle not found with id: " + request.getVehicle().getIdVehicle()));

        // Verificar si el vehículo está disponible
        if (!vehicle.getIsAvailable()) {
            throw new IllegalStateException("Vehicle is not available for reservation");
        }

        // Cambiar el estado del vehículo a no disponible
        vehicle.setAvailable(false);
        vehicleRepository.save(vehicle); // Guardar los cambios del vehículo

        // Crear la reserva con el acquirer y vehículo existentes
        return reservationRepository.save(createReservation(request, acquirer, vehicle));
    }

    private Reservation createReservation(AddReservationRequest request, Acquirer acquirer, Vehicle vehicle){
        return new Reservation(
                acquirer,
                vehicle
        );
    }

    @Override
    public void deleteReservationById(Long id) {
        reservationRepository.findById(id).ifPresent(reservationRepository::delete);

    }

    @Override
    public Reservation updateReservation(ReservationUpdateRequest request, Long reservationId) {
        return reservationRepository.findById(reservationId)
                .map(existingReservation -> updateExistingReservation(existingReservation, request))
                .map(reservationRepository::save)
                .orElseThrow(() -> new RuntimeException("Reservation not found with id: " + reservationId));
    }

    private Reservation updateExistingReservation (Reservation existingReservation, ReservationUpdateRequest request){
        Vehicle vehicle = vehicleRepository.findById(request.getVehicle().getIdVehicle())
                .orElseThrow(() -> new RuntimeException("Vehicle not found with id: " + request.getVehicle().getIdVehicle()));
        existingReservation.setVehicle(vehicle);

        Acquirer acquirer = acquirerRepository.findById(request.getAcquirer().getIdClient())
                .orElseThrow(() -> new RuntimeException("Acquirer not found with id: " + request.getAcquirer().getIdClient()));
        existingReservation.setAcquirer(acquirer);
        return existingReservation;
    }



    @Override
    public List<Reservation> getReservationsByAcquirer_IdClient(Long idClient) {
        return reservationRepository.findByAcquirer_IdClient(idClient);
    }

    @Override
    public List<Reservation> getReservationsByVehicle_IdVehicle(Long idVehicle) {
        return reservationRepository.findByVehicle_IdVehicle(idVehicle);
    }
}
