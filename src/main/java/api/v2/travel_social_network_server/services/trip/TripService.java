package api.v2.travel_social_network_server.services.trip;

import api.v2.travel_social_network_server.dtos.trip.ConversationInfoDto;
import api.v2.travel_social_network_server.dtos.trip.TripCalendarDto;
import api.v2.travel_social_network_server.dtos.trip.TripDto;
import api.v2.travel_social_network_server.dtos.trip.TripResponseDto;
import api.v2.travel_social_network_server.dtos.trip.TripScheduleDto;
import api.v2.travel_social_network_server.dtos.trip.TripScheduleResponseDto;
import api.v2.travel_social_network_server.entities.*;
import api.v2.travel_social_network_server.exceptions.ResourceNotFoundException;
import api.v2.travel_social_network_server.repositories.*;
import api.v2.travel_social_network_server.services.storage.MediaStorage;
import api.v2.travel_social_network_server.utilities.enums.ActivityTypeEnum;
import api.v2.travel_social_network_server.utilities.enums.TripStatusEnum;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Slf4j
public class TripService implements ITripService {

    private static final String TRIP_FOLDER = "trips";

    private final TripRepository tripRepository;
    private final TripScheduleRepository tripScheduleRepository;
    private final ConversationRepository conversationRepository;
    private final ConversationMemberRepository conversationMemberRepository;
    private final UserRepository userRepository;
    private final MediaStorage mediaStorage;

    public TripService(TripRepository tripRepository,
                      TripScheduleRepository tripScheduleRepository,
                      ConversationRepository conversationRepository,
                      ConversationMemberRepository conversationMemberRepository,
                      UserRepository userRepository,
                      @Qualifier("minioStorageAdapter") MediaStorage mediaStorage) {
        this.tripRepository = tripRepository;
        this.tripScheduleRepository = tripScheduleRepository;
        this.conversationRepository = conversationRepository;
        this.conversationMemberRepository = conversationMemberRepository;
        this.userRepository = userRepository;
        this.mediaStorage = mediaStorage;
    }

    // ==================== TRIP OPERATIONS ====================

    @Override
    @Transactional
    public TripResponseDto createTrip(User user, TripDto tripDto, MultipartFile coverImage) {
        log.info("Creating trip for user: {} in conversation: {}", user.getUserId(), tripDto.getConversationId());

        // Validate conversation exists
        Conversation conversation = conversationRepository.findById(tripDto.getConversationId())
                .orElseThrow(() -> new ResourceNotFoundException("Conversation not found with id: " + tripDto.getConversationId()));

        // Verify user is member of conversation
        verifyUserIsMemberOfConversation(user.getUserId(), conversation.getConversationId());

        // Parse status
        TripStatusEnum status = TripStatusEnum.PLANNING;
        if (tripDto.getStatus() != null) {
            try {
                status = TripStatusEnum.fromValue(tripDto.getStatus());
            } catch (IllegalArgumentException e) {
                log.warn("Invalid status: {}, using default PLANNING", tripDto.getStatus());
            }
        }

        // Handle cover image upload
        String coverImageUrl = tripDto.getCoverImageUrl();
        if (coverImage != null && !coverImage.isEmpty()) {
            try {
                String contentType = coverImage.getContentType();
                coverImageUrl = mediaStorage.uploadFile(coverImage.getBytes(), TRIP_FOLDER, contentType);
                log.info("Cover image uploaded successfully: {}", coverImageUrl);
            } catch (IOException e) {
                log.error("Failed to upload cover image for trip '{}': {}", tripDto.getTripName(), e.getMessage());
            }
        }

        // Create trip
        Trip trip = Trip.builder()
                .conversation(conversation)
                .tripName(tripDto.getTripName())
                .tripDescription(tripDto.getTripDescription())
                .coverImageUrl(coverImageUrl)
                .destination(tripDto.getDestination())
                .startDate(tripDto.getStartDate())
                .endDate(tripDto.getEndDate())
                .budget(tripDto.getBudget())
                .status(status)
                .createdBy(user)
                .build();

        Trip savedTrip = tripRepository.save(trip);
        log.info("Trip created successfully with id: {}", savedTrip.getTripId());

        return mapToTripResponseDto(savedTrip);
    }

    @Override
    @Transactional
    public TripResponseDto updateTrip(UUID tripId, User user, TripDto tripDto, MultipartFile coverImage) {
        log.info("Updating trip: {} by user: {}", tripId, user.getUserId());

        Trip trip = tripRepository.findById(tripId)
                .orElseThrow(() -> new ResourceNotFoundException("Trip not found with id: " + tripId));

        // Verify user is member of conversation
        verifyUserIsMemberOfConversation(user.getUserId(), trip.getConversation().getConversationId());

        // Update fields
        if (tripDto.getTripName() != null) {
            trip.setTripName(tripDto.getTripName());
        }
        if (tripDto.getTripDescription() != null) {
            trip.setTripDescription(tripDto.getTripDescription());
        }
        
        // Handle cover image upload
        if (coverImage != null && !coverImage.isEmpty()) {
            try {
                String contentType = coverImage.getContentType();
                String uploadedUrl = mediaStorage.uploadFile(coverImage.getBytes(), TRIP_FOLDER, contentType);
                trip.setCoverImageUrl(uploadedUrl);
                log.info("Cover image uploaded successfully: {}", uploadedUrl);
            } catch (IOException e) {
                log.error("Failed to upload cover image for trip '{}': {}", tripDto.getTripName(), e.getMessage());
                // Keep existing cover image if upload fails
            }
        } else if (tripDto.getCoverImageUrl() != null) {
            trip.setCoverImageUrl(tripDto.getCoverImageUrl());
        }
        if (tripDto.getDestination() != null) {
            trip.setDestination(tripDto.getDestination());
        }
        if (tripDto.getStartDate() != null) {
            trip.setStartDate(tripDto.getStartDate());
        }
        if (tripDto.getEndDate() != null) {
            trip.setEndDate(tripDto.getEndDate());
        }
        if (tripDto.getBudget() != null) {
            trip.setBudget(tripDto.getBudget());
        }
        if (tripDto.getStatus() != null) {
            try {
                trip.setStatus(TripStatusEnum.fromValue(tripDto.getStatus()));
            } catch (IllegalArgumentException e) {
                log.warn("Invalid status: {}, skipping", tripDto.getStatus());
            }
        }

        Trip updatedTrip = tripRepository.save(trip);
        return mapToTripResponseDto(updatedTrip);
    }

    @Override
    @Transactional(readOnly = true)
    public TripResponseDto getTripById(UUID tripId) {
        Trip trip = tripRepository.findById(tripId)
                .orElseThrow(() -> new ResourceNotFoundException("Trip not found with id: " + tripId));

        return mapToTripResponseDto(trip);
    }

    @Override
    @Transactional
    public void deleteTrip(UUID tripId, User user) {
        log.info("Deleting trip: {} by user: {}", tripId, user.getUserId());

        Trip trip = tripRepository.findById(tripId)
                .orElseThrow(() -> new ResourceNotFoundException("Trip not found with id: " + tripId));

        // Only creator can delete trip
        if (!trip.getCreatedBy().getUserId().equals(user.getUserId())) {
            throw new AccessDeniedException("Only the creator can delete this trip");
        }

        tripRepository.delete(trip);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<TripResponseDto> getTripsByConversationId(UUID conversationId, Pageable pageable) {
        Page<Trip> trips = tripRepository.findByConversationId(conversationId, pageable);
        return trips.map(this::mapToTripResponseDto);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<TripResponseDto> getTripsByUserId(UUID userId, Pageable pageable) {
        Page<Trip> trips = tripRepository.findTripsByUserId(userId, pageable);
        return trips.map(this::mapToTripResponseDto);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<TripResponseDto> searchTrips(UUID conversationId, String keyword, Pageable pageable) {
        Page<Trip> trips = tripRepository.searchTripsByKeyword(conversationId, keyword, pageable);
        return trips.map(this::mapToTripResponseDto);
    }

    @Override
    @Transactional(readOnly = true)
    public List<TripResponseDto> getTripsByStatus(UUID conversationId, String status) {
        TripStatusEnum tripStatus = TripStatusEnum.fromValue(status);
        List<Trip> trips = tripRepository.findByConversationIdAndStatus(conversationId, tripStatus);
        return trips.stream()
                .map(this::mapToTripResponseDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<TripCalendarDto> getTripsByUserForCalendar(UUID userId, Instant startDate, Instant endDate) {
        log.info("Getting calendar trips for user: {} between {} and {}", userId, startDate, endDate);
        
        // Get all trips for this user (via conversation membership)
        List<Trip> trips = tripRepository.findTripsByUserId(userId);
        
        List<TripCalendarDto> calendarTrips = new ArrayList<>();
        
        for (Trip trip : trips) {
            // Only include trips that overlap with the requested date range
            boolean tripOverlapsRange = !trip.getEndDate().isBefore(startDate) && !trip.getStartDate().isAfter(endDate);
            
            if (tripOverlapsRange) {
                // Count schedules within the date range
                List<TripSchedule> schedules = tripScheduleRepository.findByTripIdAndScheduleDateBetween(
                    trip.getTripId(), 
                    startDate, 
                    endDate
                );
                
                // Create ONE calendar entry per trip (not per schedule date)
                TripCalendarDto calendarDto = TripCalendarDto.builder()
                    .tripId(trip.getTripId())
                    .tripName(trip.getTripName())
                    .destination(trip.getDestination())
                    .scheduleDate(trip.getStartDate()) // Show on start date
                    .startDate(trip.getStartDate())
                    .endDate(trip.getEndDate())
                    .status(trip.getStatus().getValue())
                    .conversationId(trip.getConversation().getConversationId())
                    .conversationName(trip.getConversation().getConversationName())
                    .conversationAvatar(trip.getConversation().getConversationAvatar())
                    .schedulesOnDate((long) schedules.size())
                    .build();
                
                calendarTrips.add(calendarDto);
            }
        }
        
        return calendarTrips;
    }

    // ==================== TRIP SCHEDULE OPERATIONS ====================

    @Override
    @Transactional
    public TripScheduleResponseDto createSchedule(User user, TripScheduleDto scheduleDto) {
        log.info("Creating schedule for trip: {} by user: {}", scheduleDto.getTripId(), user.getUserId());

        Trip trip = tripRepository.findById(scheduleDto.getTripId())
                .orElseThrow(() -> new ResourceNotFoundException("Trip not found with id: " + scheduleDto.getTripId()));

        // Verify user is member of conversation
        verifyUserIsMemberOfConversation(user.getUserId(), trip.getConversation().getConversationId());

        // Parse activity type
        ActivityTypeEnum activityType = null;
        if (scheduleDto.getActivityType() != null) {
            try {
                activityType = ActivityTypeEnum.fromValue(scheduleDto.getActivityType());
            } catch (IllegalArgumentException e) {
                log.warn("Invalid activity type: {}", scheduleDto.getActivityType());
            }
        }

        TripSchedule schedule = TripSchedule.builder()
                .trip(trip)
                .title(scheduleDto.getTitle())
                .description(scheduleDto.getDescription())
                .location(scheduleDto.getLocation())
                .scheduleDate(scheduleDto.getScheduleDate())
                .startTime(scheduleDto.getStartTime())
                .endTime(scheduleDto.getEndTime())
                .activityType(activityType)
                .estimatedCost(scheduleDto.getEstimatedCost())
                .notes(scheduleDto.getNotes())
                .orderIndex(scheduleDto.getOrderIndex())
                .createdBy(user)
                .build();

        TripSchedule savedSchedule = tripScheduleRepository.save(schedule);
        log.info("Schedule created successfully with id: {}", savedSchedule.getTripScheduleId());

        return mapToScheduleResponseDto(savedSchedule);
    }

    @Override
    @Transactional
    public TripScheduleResponseDto updateSchedule(UUID scheduleId, User user, TripScheduleDto scheduleDto) {
        log.info("Updating schedule: {} by user: {}", scheduleId, user.getUserId());

        TripSchedule schedule = tripScheduleRepository.findById(scheduleId)
                .orElseThrow(() -> new ResourceNotFoundException("Schedule not found with id: " + scheduleId));

        // Verify user is member of conversation
        verifyUserIsMemberOfConversation(user.getUserId(), schedule.getTrip().getConversation().getConversationId());

        // Update fields
        if (scheduleDto.getTitle() != null) {
            schedule.setTitle(scheduleDto.getTitle());
        }
        if (scheduleDto.getDescription() != null) {
            schedule.setDescription(scheduleDto.getDescription());
        }
        if (scheduleDto.getLocation() != null) {
            schedule.setLocation(scheduleDto.getLocation());
        }
        if (scheduleDto.getScheduleDate() != null) {
            schedule.setScheduleDate(scheduleDto.getScheduleDate());
        }
        if (scheduleDto.getStartTime() != null) {
            schedule.setStartTime(scheduleDto.getStartTime());
        }
        if (scheduleDto.getEndTime() != null) {
            schedule.setEndTime(scheduleDto.getEndTime());
        }
        if (scheduleDto.getActivityType() != null) {
            try {
                schedule.setActivityType(ActivityTypeEnum.fromValue(scheduleDto.getActivityType()));
            } catch (IllegalArgumentException e) {
                log.warn("Invalid activity type: {}", scheduleDto.getActivityType());
            }
        }
        if (scheduleDto.getEstimatedCost() != null) {
            schedule.setEstimatedCost(scheduleDto.getEstimatedCost());
        }
        if (scheduleDto.getNotes() != null) {
            schedule.setNotes(scheduleDto.getNotes());
        }
        if (scheduleDto.getOrderIndex() != null) {
            schedule.setOrderIndex(scheduleDto.getOrderIndex());
        }

        TripSchedule updatedSchedule = tripScheduleRepository.save(schedule);
        return mapToScheduleResponseDto(updatedSchedule);
    }

    @Override
    @Transactional(readOnly = true)
    public TripScheduleResponseDto getScheduleById(UUID scheduleId) {
        TripSchedule schedule = tripScheduleRepository.findById(scheduleId)
                .orElseThrow(() -> new ResourceNotFoundException("Schedule not found with id: " + scheduleId));

        return mapToScheduleResponseDto(schedule);
    }

    @Override
    @Transactional
    public void deleteSchedule(UUID scheduleId, User user) {
        log.info("Deleting schedule: {} by user: {}", scheduleId, user.getUserId());

        TripSchedule schedule = tripScheduleRepository.findById(scheduleId)
                .orElseThrow(() -> new ResourceNotFoundException("Schedule not found with id: " + scheduleId));

        // Verify user is member of conversation
        verifyUserIsMemberOfConversation(user.getUserId(), schedule.getTrip().getConversation().getConversationId());

        tripScheduleRepository.delete(schedule);
    }

    @Override
    @Transactional(readOnly = true)
    public List<TripScheduleResponseDto> getSchedulesByTripId(UUID tripId) {
        List<TripSchedule> schedules = tripScheduleRepository.findByTripId(tripId);
        return schedules.stream()
                .map(this::mapToScheduleResponseDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<TripScheduleResponseDto> getSchedulesByDate(UUID tripId, Instant scheduleDate) {
        List<TripSchedule> schedules = tripScheduleRepository.findByTripIdAndScheduleDate(tripId, scheduleDate);
        return schedules.stream()
                .map(this::mapToScheduleResponseDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<TripScheduleResponseDto> getSchedulesByActivityType(UUID tripId, String activityType) {
        ActivityTypeEnum type = ActivityTypeEnum.fromValue(activityType);
        List<TripSchedule> schedules = tripScheduleRepository.findByTripIdAndActivityType(tripId, type);
        return schedules.stream()
                .map(this::mapToScheduleResponseDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<TripScheduleResponseDto> getSchedulesByDateRange(UUID tripId, Instant startDate, Instant endDate) {
        List<TripSchedule> schedules = tripScheduleRepository.findByTripIdAndDateRange(tripId, startDate, endDate);
        return schedules.stream()
                .map(this::mapToScheduleResponseDto)
                .collect(Collectors.toList());
    }

    // ==================== HELPER METHODS ====================

    private void verifyUserIsMemberOfConversation(UUID userId, UUID conversationId) {
        boolean isMember = conversationMemberRepository.existsByConversationConversationIdAndUserUserId(conversationId, userId);
        if (!isMember) {
            throw new AccessDeniedException("User is not a member of this conversation");
        }
    }

    private TripResponseDto mapToTripResponseDto(Trip trip) {
        Long scheduleCount = tripScheduleRepository.countByTripId(trip.getTripId());

        // Build conversation info
        ConversationInfoDto conversationInfo = ConversationInfoDto.builder()
                .conversationId(trip.getConversation().getConversationId())
                .conversationName(trip.getConversation().getConversationName())
                .conversationAvatar(trip.getConversation().getConversationAvatar())
                .build();

        return TripResponseDto.builder()
                .tripId(trip.getTripId())
                .conversation(conversationInfo)
                .tripName(trip.getTripName())
                .tripDescription(trip.getTripDescription())
                .coverImageUrl(trip.getCoverImageUrl())
                .destination(trip.getDestination())
                .startDate(trip.getStartDate())
                .endDate(trip.getEndDate())
                .budget(trip.getBudget())
                .status(trip.getStatus().getValue())
                .createdBy(trip.getCreatedBy().getUserId())
                .createdByName(trip.getCreatedBy().getUserProfile().getFullName())
                .createdAt(trip.getCreatedAt())
                .updatedAt(trip.getUpdatedAt())
                .scheduleCount(scheduleCount)
                .build();
    }

    private TripScheduleResponseDto mapToScheduleResponseDto(TripSchedule schedule) {
        return TripScheduleResponseDto.builder()
                .tripScheduleId(schedule.getTripScheduleId())
                .tripId(schedule.getTrip().getTripId())
                .title(schedule.getTitle())
                .description(schedule.getDescription())
                .location(schedule.getLocation())
                .scheduleDate(schedule.getScheduleDate())
                .startTime(schedule.getStartTime())
                .endTime(schedule.getEndTime())
                .activityType(schedule.getActivityType() != null ? schedule.getActivityType().getValue() : null)
                .estimatedCost(schedule.getEstimatedCost())
                .notes(schedule.getNotes())
                .orderIndex(schedule.getOrderIndex())
                .createdBy(schedule.getCreatedBy().getUserId())
                .createdByName(schedule.getCreatedBy().getUserProfile().getFullName())
                .createdAt(schedule.getCreatedAt())
                .updatedAt(schedule.getUpdatedAt())
                .build();
    }
}
