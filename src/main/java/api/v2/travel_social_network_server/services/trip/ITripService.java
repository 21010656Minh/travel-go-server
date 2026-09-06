package api.v2.travel_social_network_server.services.trip;

import api.v2.travel_social_network_server.dtos.trip.TripCalendarDto;
import api.v2.travel_social_network_server.dtos.trip.TripDto;
import api.v2.travel_social_network_server.dtos.trip.TripResponseDto;
import api.v2.travel_social_network_server.dtos.trip.TripScheduleDto;
import api.v2.travel_social_network_server.dtos.trip.TripScheduleResponseDto;
import api.v2.travel_social_network_server.entities.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public interface ITripService {

    // Trip operations
    TripResponseDto createTrip(User user, TripDto tripDto, MultipartFile coverImage);
    
    TripResponseDto updateTrip(UUID tripId, User user, TripDto tripDto, MultipartFile coverImage);
    
    TripResponseDto getTripById(UUID tripId);
    
    void deleteTrip(UUID tripId, User user);
    
    Page<TripResponseDto> getTripsByConversationId(UUID conversationId, Pageable pageable);
    
    Page<TripResponseDto> getTripsByUserId(UUID userId, Pageable pageable);
    
    Page<TripResponseDto> searchTrips(UUID conversationId, String keyword, Pageable pageable);
    
    List<TripResponseDto> getTripsByStatus(UUID conversationId, String status);
    
    // Calendar view - get all trips by user grouped by schedule dates
    List<TripCalendarDto> getTripsByUserForCalendar(UUID userId, Instant startDate, Instant endDate);
    
    // Trip Schedule operations
    TripScheduleResponseDto createSchedule(User user, TripScheduleDto scheduleDto);
    
    TripScheduleResponseDto updateSchedule(UUID scheduleId, User user, TripScheduleDto scheduleDto);
    
    TripScheduleResponseDto getScheduleById(UUID scheduleId);
    
    void deleteSchedule(UUID scheduleId, User user);
    
    List<TripScheduleResponseDto> getSchedulesByTripId(UUID tripId);
    
    List<TripScheduleResponseDto> getSchedulesByDate(UUID tripId, Instant scheduleDate);
    
    List<TripScheduleResponseDto> getSchedulesByActivityType(UUID tripId, String activityType);
    
    List<TripScheduleResponseDto> getSchedulesByDateRange(UUID tripId, Instant startDate, Instant endDate);
}
