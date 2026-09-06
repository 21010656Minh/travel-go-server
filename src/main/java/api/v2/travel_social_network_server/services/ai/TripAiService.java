package api.v2.travel_social_network_server.services.ai;

import api.v2.travel_social_network_server.dto.ai.AiSuggestRequestDto;
import api.v2.travel_social_network_server.dto.ai.AiSuggestResponseDto;
import api.v2.travel_social_network_server.dto.ai.ApplyAiSuggestionsDto;
import api.v2.travel_social_network_server.entities.Trip;
import api.v2.travel_social_network_server.entities.TripSchedule;
import api.v2.travel_social_network_server.entities.User;
import api.v2.travel_social_network_server.enums.AiRequestType;
import api.v2.travel_social_network_server.repositories.TripRepository;
import api.v2.travel_social_network_server.repositories.TripScheduleRepository;
import api.v2.travel_social_network_server.utilities.enums.ActivityTypeEnum;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.genai.Client;
import com.google.genai.types.GenerateContentConfig;
import com.google.genai.types.GenerateContentResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class TripAiService {

    private final TripRepository tripRepository;
    private final TripScheduleRepository tripScheduleRepository;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Value("${google.studio.api.key}")
    private String apiKey;

    @Value("${google.studio.model}")
    private String model;


    public AiSuggestResponseDto generateSuggestions(UUID tripId, String userPrompt, AiRequestType type) {
        Trip trip = tripRepository.findById(tripId)
                .orElseThrow(() -> new RuntimeException("Trip not found"));

        // Build request DTO from trip entity
        AiSuggestRequestDto requestDto = buildRequestFromTrip(trip, userPrompt, type);

        // Generate AI suggestions
        return callGeminiApi(requestDto);
    }


    private AiSuggestRequestDto buildRequestFromTrip(Trip trip, String userPrompt, AiRequestType type) {
        long daysBetween = ChronoUnit.DAYS.between(
                trip.getStartDate(),
                trip.getEndDate()
        ) + 1; // Include both start and end day

        return AiSuggestRequestDto.builder()
                .type(type)
                .prompt(userPrompt)
                .destination(trip.getDestination())
                .startDate(trip.getStartDate().toString())
                .endDate(trip.getEndDate().toString())
                .numberOfDays((int) daysBetween)
                .budget(trip.getBudget() != null ? trip.getBudget().doubleValue() : null)
                .groupType(determineGroupType(trip))
                .interests(null) // Can be extended later
                .build();
    }

    private String determineGroupType(Trip trip) {
        // Logic to determine group type based on conversation members count
        // For now, return default
        return "FRIENDS";
    }


    private AiSuggestResponseDto callGeminiApi(AiSuggestRequestDto request) {
        try {
            String systemPrompt = buildSystemPrompt(request);
            
            log.info("=== GEMINI API CALL ===");
            log.info("API Key: {}", apiKey != null ? "SET (" + apiKey.length() + " chars)" : "NULL");
            log.info("Model: {}", model);
            log.info("Destination: {}", request.getDestination());
            log.info("Prompt length: {} chars", systemPrompt.length());
            
            // Initialize Gemini client with API key
            Client client = new Client.Builder()
                    .apiKey(apiKey)
                    .build();
            
            log.info("Client built successfully, making API call...");
            
            // Generate content with default config (use empty config instead of null)
            GenerateContentConfig config = GenerateContentConfig.builder().build();
            GenerateContentResponse response = client.models.generateContent(
                    model,
                    systemPrompt,
                    config
            );
            
            String generatedText = response.text();
            log.info("AI Response received: {} characters", generatedText.length());
            log.debug("Full AI Response: {}", generatedText);
            
            return parseGeminiResponse(generatedText, request);
            
        } catch (Exception e) {
            log.error("=== GEMINI API ERROR ===");
            log.error("Error calling Gemini API: ", e);
            throw new RuntimeException("AI service error: " + e.getMessage());
        }
    }

    private String buildSystemPrompt(AiSuggestRequestDto request) {
        StringBuilder prompt = new StringBuilder();
        
        // Check if this is a generate request or normal chat
        if (AiRequestType.GENERATE.equals(request.getType())) {
            // Generate structured itinerary prompt
            prompt.append("Bạn là một chuyên gia tư vấn du lịch Việt Nam. Hãy tạo lịch trình chi tiết dựa trên thông tin sau:\n\n");
            prompt.append("THÔNG TIN CHUYẾN ĐI:\n");
            prompt.append("- Điểm đến: ").append(request.getDestination()).append("\n");
            prompt.append("- Số ngày: ").append(request.getNumberOfDays()).append(" ngày\n");
            
            if (request.getBudget() != null && request.getBudget() > 0) {
                prompt.append("- Ngân sách: ").append(String.format("%,.0f VNĐ", request.getBudget())).append("\n");
                prompt.append("- Ngân sách trung bình mỗi ngày: ").append(String.format("%,.0f VNĐ", request.getBudget() / request.getNumberOfDays())).append("\n");
            }
            
            prompt.append("- Loại nhóm: ").append(request.getGroupType()).append("\n\n");
            
            prompt.append("YÊU CẦU CỦA NGƯỜI DÙNG:\n");
            prompt.append(request.getPrompt()).append("\n\n");
            
            prompt.append("HÃY TẠO LỊCH TRÌNH VỚI CẤU TRÚC JSON SAU (chỉ trả về JSON, không có text khác):\n");
            prompt.append("{\n");
            prompt.append("  \"message\": \"Mô tả ngắn gọn về lịch trình\",\n");
            prompt.append("  \"schedules\": [\n");
            prompt.append("    {\n");
            prompt.append("      \"day\": 1,\n");
            prompt.append("      \"title\": \"Chủ đề ngày 1\",\n");
            prompt.append("      \"activities\": [\n");
            prompt.append("        {\n");
            prompt.append("          \"time\": \"08:00\",\n");
            prompt.append("          \"title\": \"Tên hoạt động\",\n");
            prompt.append("          \"description\": \"Mô tả chi tiết\",\n");
            prompt.append("          \"type\": \"VISIT\",\n");
            prompt.append("          \"location\": \"Địa điểm cụ thể\",\n");
            prompt.append("          \"estimatedCost\": 100000\n");
            prompt.append("        }\n");
            prompt.append("      ]\n");
            prompt.append("    }\n");
            prompt.append("  ]\n");
            prompt.append("}\n\n");
            
            prompt.append("LƯU Ý:\n");
            prompt.append("- type phải là một trong: VISIT, MEAL, TRANSPORT, ACCOMMODATION\n");
            prompt.append("- time theo format HH:mm (24h)\n");
            prompt.append("- estimatedCost tính bằng VNĐ\n");
            
            if (request.getBudget() != null && request.getBudget() > 0) {
                prompt.append("- Tổng chi phí các hoạt động KHÔNG vượt quá ngân sách: ").append(String.format("%,.0f VNĐ", request.getBudget())).append("\n");
                prompt.append("- Ưu tiên các địa điểm và hoạt động phù hợp với ngân sách\n");
            }
            
            prompt.append("- Lịch trình phải thực tế, có thể thực hiện được\n");
            prompt.append("- Các hoạt động phải sắp xếp hợp lý theo thời gian\n");
            prompt.append("- Gợi ý địa điểm cụ thể, nổi tiếng tại ").append(request.getDestination()).append("\n");
        } else {
            // Normal chat mode - just respond to user's question
            prompt.append("Bạn là một trợ lý AI tư vấn du lịch thân thiện và hữu ích. ");
            prompt.append("Bạn đang tư vấn cho một chuyến đi tới ").append(request.getDestination());
            
            if (request.getNumberOfDays() != null && request.getNumberOfDays() > 0) {
                prompt.append(" trong ").append(request.getNumberOfDays()).append(" ngày");
            }
            
            if (request.getBudget() != null && request.getBudget() > 0) {
                prompt.append(" với ngân sách ").append(String.format("%,.0f VNĐ", request.getBudget()));
            }
            
            prompt.append(".\n\n");
            prompt.append("Người dùng hỏi: ").append(request.getPrompt()).append("\n\n");
            prompt.append("Hãy trả lời câu hỏi một cách tự nhiên, thân thiện và hữu ích. ");
            prompt.append("Chỉ trả lời văn bản thuần túy, KHÔNG trả về JSON hay cấu trúc dữ liệu.");
        }

        return prompt.toString();
    }


    private AiSuggestResponseDto parseGeminiResponse(String generatedText, AiSuggestRequestDto request) {
        try {
            log.info("Parsing AI Response: {}", generatedText);

            // Check if this is a generate request or normal chat
            if (AiRequestType.GENERATE.equals(request.getType())) {
                // Extract JSON from response (may have markdown code blocks)
                String jsonText = extractJson(generatedText);

                // Parse to response DTO
                return objectMapper.readValue(jsonText, AiSuggestResponseDto.class);
            } else {
                // Normal chat - return plain text response
                return AiSuggestResponseDto.builder()
                        .message(generatedText)
                        .schedules(null) // No schedules for normal chat
                        .build();
            }

        } catch (Exception e) {
            log.error("Error parsing Gemini response: ", e);
            // Return fallback response
            return createFallbackResponse(request);
        }
    }

    private String extractJson(String text) {
        // Remove markdown code blocks
        text = text.replaceAll("```json\\s*", "").replaceAll("```\\s*", "").trim();
        
        // Find first { and last }
        int start = text.indexOf('{');
        int end = text.lastIndexOf('}');
        
        if (start >= 0 && end > start) {
            return text.substring(start, end + 1);
        }
        
        return text;
    }

    private AiSuggestResponseDto createFallbackResponse(AiSuggestRequestDto request) {
        List<AiSuggestResponseDto.DayScheduleDto> schedules = new ArrayList<>();
        
        AiSuggestResponseDto.DayScheduleDto day1 = AiSuggestResponseDto.DayScheduleDto.builder()
                .day(1)
                .title("Khám phá " + request.getDestination())
                .activities(List.of(
                        AiSuggestResponseDto.ActivityDto.builder()
                                .time("09:00")
                                .title("Check-in khách sạn")
                                .description("Nhận phòng và chuẩn bị cho hành trình")
                                .type("ACCOMMODATION")
                                .location(request.getDestination())
                                .estimatedCost(500000.0)
                                .build(),
                        AiSuggestResponseDto.ActivityDto.builder()
                                .time("12:00")
                                .title("Ăn trưa đặc sản địa phương")
                                .description("Thưởng thức món ăn truyền thống")
                                .type("MEAL")
                                .location(request.getDestination())
                                .estimatedCost(150000.0)
                                .build()
                ))
                .build();
        
        schedules.add(day1);

        return AiSuggestResponseDto.builder()
                .message("Đây là lịch trình gợi ý cho chuyến đi " + request.getDestination() + " của bạn")
                .schedules(schedules)
                .build();
    }

    /**
     * Apply AI suggestions to create TripSchedules
     */
    public List<TripSchedule> applyAiSuggestions(UUID tripId, ApplyAiSuggestionsDto dto, User currentUser) {
        Trip trip = tripRepository.findById(tripId)
                .orElseThrow(() -> new RuntimeException("Trip not found"));

        List<TripSchedule> createdSchedules = new ArrayList<>();
        LocalDate tripStartDate = LocalDate.from(trip.getStartDate());

        int orderIndex = 0;
        for (ApplyAiSuggestionsDto.DayScheduleDto daySchedule : dto.getSchedules()) {
            // Calculate the date for this day (day 1 = start date, day 2 = start date + 1, etc.)
            LocalDate scheduleDate = tripStartDate.plusDays(daySchedule.getDay() - 1);

            for (ApplyAiSuggestionsDto.ActivityDto activity : daySchedule.getActivities()) {
                TripSchedule schedule = TripSchedule.builder()
                        .trip(trip)
                        .title(activity.getTitle())
                        .description(activity.getDescription())
                        .location(activity.getLocation())
                        .scheduleDate(scheduleDate.atStartOfDay(ZoneId.systemDefault()).toInstant())
                        .startTime(parseTime(activity.getTime(), scheduleDate))
                        .activityType(parseActivityType(activity.getType()))
                        .estimatedCost(activity.getEstimatedCost() != null ? 
                                BigDecimal.valueOf(activity.getEstimatedCost()) : null)
                        .orderIndex(orderIndex++)
                        .createdBy(currentUser)
                        .build();

                createdSchedules.add(schedule);
            }
        }

        return tripScheduleRepository.saveAll(createdSchedules);
    }

    private Instant parseTime(String timeStr, LocalDate date) {
        try {
            if (timeStr == null || timeStr.isEmpty()) {
                return date.atStartOfDay(ZoneId.systemDefault()).toInstant();
            }
            LocalTime time = LocalTime.parse(timeStr, DateTimeFormatter.ofPattern("HH:mm"));
            return date.atTime(time).atZone(ZoneId.systemDefault()).toInstant();
        } catch (Exception e) {
            log.warn("Failed to parse time: {}", timeStr);
            return date.atStartOfDay(ZoneId.systemDefault()).toInstant();
        }
    }

    private ActivityTypeEnum parseActivityType(String type) {
        try {
            return ActivityTypeEnum.valueOf(type.toUpperCase());
        } catch (Exception e) {
            log.warn("Unknown activity type: {}, defaulting to OTHER", type);
            return ActivityTypeEnum.OTHER;
        }
    }
}
