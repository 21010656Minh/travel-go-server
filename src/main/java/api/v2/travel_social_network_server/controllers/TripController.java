package api.v2.travel_social_network_server.controllers;

import api.v2.travel_social_network_server.dtos.trip.TripCalendarDto;
import api.v2.travel_social_network_server.dtos.trip.TripDto;
import api.v2.travel_social_network_server.dtos.trip.TripResponseDto;
import api.v2.travel_social_network_server.dtos.trip.TripScheduleDto;
import api.v2.travel_social_network_server.dtos.trip.TripScheduleResponseDto;
import api.v2.travel_social_network_server.entities.User;
import api.v2.travel_social_network_server.responses.Response;
import api.v2.travel_social_network_server.services.trip.ITripService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("${api.base-url}/trips")
@Tag(name = "Trip APIs", description = "Endpoints for managing travel trips and schedules")
@RequiredArgsConstructor
public class TripController {

    private final ITripService tripService;

    // ========== TRIP ENDPOINTS ==========

    @Operation(summary = "Create a new trip", description = "Create a new trip for a conversation with optional cover image")
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Response<TripResponseDto>> createTrip(
            @RequestParam("conversationId") UUID conversationId,
            @RequestParam("tripName") String tripName,
            @RequestParam(value = "tripDescription", required = false) String tripDescription,
            @RequestParam(value = "coverImage", required = false) MultipartFile coverImage,
            @RequestParam(value = "destination", required = false) String destination,
            @RequestParam("startDate") String startDate,
            @RequestParam("endDate") String endDate,
            @RequestParam(value = "budget", required = false) BigDecimal budget,
            @RequestParam(value = "status", defaultValue = "PLANNING") String status,
            @AuthenticationPrincipal User user,
            HttpServletRequest request
    ) {
        // Build TripDto from form parameters
        TripDto tripDto = TripDto.builder()
                .conversationId(conversationId)
                .tripName(tripName)
                .tripDescription(tripDescription)
                .destination(destination)
                .startDate(Instant.parse(startDate))
                .endDate(Instant.parse(endDate))
                .budget(budget)
                .status(status)
                .build();
        
        TripResponseDto trip = tripService.createTrip(user, tripDto, coverImage);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(Response.success(trip, request.getRequestURI(), "Trip created successfully."));
    }

    @Operation(summary = "Update a trip", description = "Update an existing trip with optional cover image")
    @PutMapping(value = "/{tripId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Response<TripResponseDto>> updateTrip(
            @PathVariable UUID tripId,
            @RequestParam("conversationId") UUID conversationId,
            @RequestParam("tripName") String tripName,
            @RequestParam(value = "tripDescription", required = false) String tripDescription,
            @RequestParam(value = "coverImage", required = false) MultipartFile coverImage,
            @RequestParam(value = "destination", required = false) String destination,
            @RequestParam("startDate") String startDate,
            @RequestParam("endDate") String endDate,
            @RequestParam(value = "budget", required = false) BigDecimal budget,
            @RequestParam(value = "status", defaultValue = "PLANNING") String status,
            @AuthenticationPrincipal User user,
            HttpServletRequest request
    ) {
        // Build TripDto from form parameters
        TripDto tripDto = TripDto.builder()
                .conversationId(conversationId)
                .tripName(tripName)
                .tripDescription(tripDescription)
                .destination(destination)
                .startDate(Instant.parse(startDate))
                .endDate(Instant.parse(endDate))
                .budget(budget)
                .status(status)
                .build();
        
        TripResponseDto trip = tripService.updateTrip(tripId, user, tripDto, coverImage);
        return ResponseEntity.ok(Response.success(trip, request.getRequestURI(), "Trip updated successfully."));
    }

    @Operation(summary = "Get trip by ID", description = "Retrieve a specific trip by its ID")
    @GetMapping("/{tripId}")
    public ResponseEntity<Response<TripResponseDto>> getTripById(
            @PathVariable UUID tripId,
            HttpServletRequest request
    ) {
        TripResponseDto trip = tripService.getTripById(tripId);
        return ResponseEntity.ok(Response.success(trip, request.getRequestURI(), "Trip retrieved successfully."));
    }

    @Operation(summary = "Delete a trip", description = "Delete a trip (only by creator)")
    @DeleteMapping("/{tripId}")
    public ResponseEntity<Response<Void>> deleteTrip(
            @PathVariable UUID tripId,
            @AuthenticationPrincipal User user,
            HttpServletRequest request
    ) {
        tripService.deleteTrip(tripId, user);
        return ResponseEntity.ok(Response.success(null, request.getRequestURI(), "Trip deleted successfully."));
    }

    @Operation(summary = "Get trips by conversation", description = "Retrieve all trips for a specific conversation")
    @GetMapping("/conversation/{conversationId}")
    public ResponseEntity<Response<Page<TripResponseDto>>> getTripsByConversation(
            @PathVariable UUID conversationId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "startDate") String sortBy,
            @RequestParam(defaultValue = "DESC") String sortDirection,
            HttpServletRequest request
    ) {
        Sort.Direction direction = Sort.Direction.fromString(sortDirection);
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));
        Page<TripResponseDto> trips = tripService.getTripsByConversationId(conversationId, pageable);
        return ResponseEntity.ok(Response.success(trips, request.getRequestURI(), "Trips retrieved successfully."));
    }

    @Operation(summary = "Get trips by user", description = "Retrieve all trips that a user is part of")
    @GetMapping("/user/{userId}")
    public ResponseEntity<Response<Page<TripResponseDto>>> getTripsByUser(
            @PathVariable UUID userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            HttpServletRequest request
    ) {
        Pageable pageable = PageRequest.of(page, size);
        Page<TripResponseDto> trips = tripService.getTripsByUserId(userId, pageable);
        return ResponseEntity.ok(Response.success(trips, request.getRequestURI(), "User trips retrieved successfully."));
    }

    @Operation(summary = "Search trips", description = "Search trips by keyword in a conversation")
    @GetMapping("/conversation/{conversationId}/search")
    public ResponseEntity<Response<Page<TripResponseDto>>> searchTrips(
            @PathVariable UUID conversationId,
            @RequestParam String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            HttpServletRequest request
    ) {
        Pageable pageable = PageRequest.of(page, size);
        Page<TripResponseDto> trips = tripService.searchTrips(conversationId, keyword, pageable);
        return ResponseEntity.ok(Response.success(trips, request.getRequestURI(), "Trips search completed successfully."));
    }

    @Operation(summary = "Get trips by status", description = "Get trips filtered by status in a conversation")
    @GetMapping("/conversation/{conversationId}/status/{status}")
    public ResponseEntity<Response<List<TripResponseDto>>> getTripsByStatus(
            @PathVariable UUID conversationId,
            @PathVariable String status,
            HttpServletRequest request
    ) {
        List<TripResponseDto> trips = tripService.getTripsByStatus(conversationId, status);
        return ResponseEntity.ok(Response.success(trips, request.getRequestURI(), "Trips retrieved successfully."));
    }

    @Operation(summary = "Get trips for calendar", description = "Get all trips by user grouped by schedule dates for calendar view")
    @GetMapping("/user/{userId}/calendar")
    public ResponseEntity<Response<List<TripCalendarDto>>> getTripsByUserForCalendar(
            @PathVariable UUID userId,
            @RequestParam String startDate,
            @RequestParam String endDate,
            HttpServletRequest request
    ) {
        Instant start = Instant.parse(startDate);
        Instant end = Instant.parse(endDate);
        List<TripCalendarDto> calendarTrips = tripService.getTripsByUserForCalendar(userId, start, end);
        return ResponseEntity.ok(Response.success(calendarTrips, request.getRequestURI(), "Calendar trips retrieved successfully."));
    }

    // ========== TRIP SCHEDULE ENDPOINTS ==========

    @Operation(summary = "Create a trip schedule", description = "Create a new schedule/activity for a trip")
    @PostMapping("/schedules")
    public ResponseEntity<Response<TripScheduleResponseDto>> createSchedule(
            @Valid @RequestBody TripScheduleDto scheduleDto,
            @AuthenticationPrincipal User user,
            HttpServletRequest request
    ) {
        TripScheduleResponseDto schedule = tripService.createSchedule(user, scheduleDto);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(Response.success(schedule, request.getRequestURI(), "Schedule created successfully."));
    }

    @Operation(summary = "Update a schedule", description = "Update an existing trip schedule")
    @PutMapping("/schedules/{scheduleId}")
    public ResponseEntity<Response<TripScheduleResponseDto>> updateSchedule(
            @PathVariable UUID scheduleId,
            @Valid @RequestBody TripScheduleDto scheduleDto,
            @AuthenticationPrincipal User user,
            HttpServletRequest request
    ) {
        TripScheduleResponseDto schedule = tripService.updateSchedule(scheduleId, user, scheduleDto);
        return ResponseEntity.ok(Response.success(schedule, request.getRequestURI(), "Schedule updated successfully."));
    }

    @Operation(summary = "Get schedule by ID", description = "Retrieve a specific schedule by its ID")
    @GetMapping("/schedules/{scheduleId}")
    public ResponseEntity<Response<TripScheduleResponseDto>> getScheduleById(
            @PathVariable UUID scheduleId,
            HttpServletRequest request
    ) {
        TripScheduleResponseDto schedule = tripService.getScheduleById(scheduleId);
        return ResponseEntity.ok(Response.success(schedule, request.getRequestURI(), "Schedule retrieved successfully."));
    }

    @Operation(summary = "Delete a schedule", description = "Delete a trip schedule")
    @DeleteMapping("/schedules/{scheduleId}")
    public ResponseEntity<Response<Void>> deleteSchedule(
            @PathVariable UUID scheduleId,
            @AuthenticationPrincipal User user,
            HttpServletRequest request
    ) {
        tripService.deleteSchedule(scheduleId, user);
        return ResponseEntity.ok(Response.success(null, request.getRequestURI(), "Schedule deleted successfully."));
    }

    @Operation(summary = "Get schedules by trip", description = "Retrieve all schedules for a specific trip")
    @GetMapping("/{tripId}/schedules")
    public ResponseEntity<Response<List<TripScheduleResponseDto>>> getSchedulesByTrip(
            @PathVariable UUID tripId,
            HttpServletRequest request
    ) {
        List<TripScheduleResponseDto> schedules = tripService.getSchedulesByTripId(tripId);
        return ResponseEntity.ok(Response.success(schedules, request.getRequestURI(), "Schedules retrieved successfully."));
    }

    @Operation(summary = "Get schedules by date", description = "Retrieve schedules for a specific date")
    @GetMapping("/{tripId}/schedules/date")
    public ResponseEntity<Response<List<TripScheduleResponseDto>>> getSchedulesByDate(
            @PathVariable UUID tripId,
            @RequestParam Instant date,
            HttpServletRequest request
    ) {
        List<TripScheduleResponseDto> schedules = tripService.getSchedulesByDate(tripId, date);
        return ResponseEntity.ok(Response.success(schedules, request.getRequestURI(), "Schedules retrieved successfully."));
    }

    @Operation(summary = "Get schedules by activity type", description = "Retrieve schedules by activity type")
    @GetMapping("/{tripId}/schedules/type/{activityType}")
    public ResponseEntity<Response<List<TripScheduleResponseDto>>> getSchedulesByActivityType(
            @PathVariable UUID tripId,
            @PathVariable String activityType,
            HttpServletRequest request
    ) {
        List<TripScheduleResponseDto> schedules = tripService.getSchedulesByActivityType(tripId, activityType);
        return ResponseEntity.ok(Response.success(schedules, request.getRequestURI(), "Schedules retrieved successfully."));
    }

    @Operation(summary = "Get schedules by date range", description = "Retrieve schedules within a date range")
    @GetMapping("/{tripId}/schedules/range")
    public ResponseEntity<Response<List<TripScheduleResponseDto>>> getSchedulesByDateRange(
            @PathVariable UUID tripId,
            @RequestParam Instant startDate,
            @RequestParam Instant endDate,
            HttpServletRequest request
    ) {
        List<TripScheduleResponseDto> schedules = tripService.getSchedulesByDateRange(tripId, startDate, endDate);
        return ResponseEntity.ok(Response.success(schedules, request.getRequestURI(), "Schedules retrieved successfully."));
    }
}
