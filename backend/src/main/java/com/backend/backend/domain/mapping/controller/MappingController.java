package com.backend.backend.domain.mapping.controller;
import com.backend.backend.domain.mapping.dto.AddMappingItemRequest;
import com.backend.backend.domain.mapping.dto.AddMappingItemResponse;
import com.backend.backend.domain.mapping.dto.ApiResponse;
import com.backend.backend.domain.mapping.dto.ApplyMappingSetResponse;
import com.backend.backend.domain.mapping.dto.MappingCreateRequest;
import com.backend.backend.domain.mapping.dto.MappingCreateResponse;
import com.backend.backend.domain.mapping.dto.MappingDetailResponse;
import com.backend.backend.domain.mapping.dto.*;
import com.backend.backend.domain.mapping.entity.Mapping;
import com.backend.backend.domain.mapping.entity.MappingItem;
import com.backend.backend.domain.mapping.service.ActionService;
import com.backend.backend.domain.mapping.service.GestureService;
import com.backend.backend.domain.mapping.service.MappingItemService;
import com.backend.backend.domain.mapping.service.MappingService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/v1/mappings")
@RequiredArgsConstructor
public class MappingController {

    private final MappingService mappingService;
    private final MappingItemService mappingItemService;
    private final GestureService gestureService;
    private final ActionService actionService;


    @GetMapping("/list")
    public ResponseEntity<ApiResponse<List<MappingListResponse>>> getMappingList(
            HttpServletRequest request) {

        List<MappingListResponse> mappingList = mappingService.userMappingList(request);

        return ResponseEntity.ok(ApiResponse.<List<MappingListResponse>>builder()
                .isSuccess(true)
                .code("COMMON200")
                .message("매핑셋 목록 조회 성공")
                .data(mappingList)
                .build());
    }

    @PatchMapping("/meta/actions")
    public ResponseEntity<String> changeMapping(@RequestBody MappingItemUpdateRequest request){
        int mappingListResponses = mappingItemService.changeMappingItem(request);
        return ResponseEntity.status(HttpStatus.ACCEPTED).body("수정 성공");
    }

    @GetMapping("/meta/actions/{mappingId}")
    public ResponseEntity<List<ActionResponse>> useAction(@PathVariable Long mappingId){
        List<ActionResponse> notUseAction = actionService.findNotUseAction(mappingId.intValue());
        return ResponseEntity.status(HttpStatus.ACCEPTED)
                .body(notUseAction);
    }

    @GetMapping("/meta/actions/{mappingId}/fix")
    public ResponseEntity<List<ActionFixResponse>> getAllActionsWithGesture(@PathVariable Long mappingId){
        List<ActionFixResponse> actionList = actionService.findAllActionsWithGesture(mappingId.intValue());
        return ResponseEntity.status(HttpStatus.ACCEPTED)
                .body(actionList);
    }

    @GetMapping("/active/gestures/{mappingId}")
    public ResponseEntity<List<GestureResponse>> useGesture(@PathVariable Long mappingId){
        List<GestureResponse> notUseGesture = gestureService.findNotUseGesture(mappingId.intValue());
        return ResponseEntity.status(HttpStatus.ACCEPTED)
                .body(notUseGesture);
    }

    @GetMapping("/meta/gestures/{mappingId}")
    public ResponseEntity<List<GestureResponse>> getNotUsedGestures(@PathVariable Long mappingId){
        List<GestureResponse> notUseGesture = gestureService.findNotUseGesture(mappingId.intValue());
        return ResponseEntity.status(HttpStatus.ACCEPTED)
                .body(notUseGesture);
    }

    @GetMapping("/meta/gestures/{mappingId}/fix/{actionId}")
    public ResponseEntity<List<GestureFixResponse>> getAvailableGesturesForAction(
            @PathVariable Long mappingId,
            @PathVariable Long actionId){
        List<GestureFixResponse> gestureList = gestureService.findAvailableGesturesForAction(mappingId.intValue(), actionId);
        return ResponseEntity.status(HttpStatus.ACCEPTED)
                .body(gestureList);
    }

    @DeleteMapping("/{mappingId}")
    public ResponseEntity<Map<String, Object>> deleteMapping(@PathVariable Long mappingId , HttpServletRequest request) {

        Long detail = mappingService.deleteMapping(request,mappingId);

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("code", "S200-004");
        response.put("message", "매핑셋이 성공적으로 삭제되었습니다.");
        response.put("status", 200);
        response.put("data", Map.of("mappingId", mappingId));
        response.put("timestamp", OffsetDateTime.now().format(DateTimeFormatter.ISO_OFFSET_DATE_TIME));

        return ResponseEntity.ok(response);
    }


    @PostMapping("/create")
    public ResponseEntity<MappingCreateResponse> createMapping(
            HttpServletRequest userRequest,
            @RequestBody MappingCreateRequest request) {

        Mapping mapping = mappingService.createMapping(userRequest, request);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(MappingCreateResponse.success(mapping.getId()));
    }


    @GetMapping("/{mappingId}")
    public ResponseEntity<ApiResponse<MappingDetailResponse>> getMappingDetail(
            @PathVariable Long mappingId) {

        MappingDetailResponse detail = mappingService.getMappingDetail(mappingId);

        return ResponseEntity.ok(ApiResponse.<MappingDetailResponse>builder()
                .isSuccess(true)
                .code("COMMON200")
                .message("매핑셋 상세 조회 성공")
                .data(detail)
                .build());
    }

    @PostMapping("/{mappingId}/add-item")
    public ResponseEntity<AddMappingItemResponse> addMapping(
            @PathVariable Long mappingId,
            @RequestBody AddMappingItemRequest request) {

        MappingItem item = mappingService.addMappingItem(mappingId, request);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(AddMappingItemResponse.success(item.getId()));
    }


    @GetMapping("/active")
    public ResponseEntity<List<MappingListResponse>> useMapping(HttpServletRequest request){
        List<MappingListResponse> mappingListResponses = mappingService.userMappingList(request);
        return ResponseEntity.status(HttpStatus.ACCEPTED)
                .body(mappingListResponses);
    }



    @PostMapping("/{presetId}/apply")
    public ResponseEntity<ApiResponse<ApplyMappingSetResponse>> applyMappingSet(
            @PathVariable Long presetId) {

        ApplyMappingSetResponse response = mappingService.applyMappingSet(presetId);

        return ResponseEntity.ok(ApiResponse.<ApplyMappingSetResponse>builder()
                .isSuccess(true)
                .code("COMMON200")
                .message("대표 매핑셋 적용 성공")
                .data(response)
                .build());
    }



    @PatchMapping("/{mappingId}/change-name")
    public ResponseEntity<ApiResponse<ChangeMappingSetNameResponse>> changeMappingSetName(
            HttpServletRequest request,
            @PathVariable Long mappingId,
            @RequestBody ChangeMappingSetNameRequest nameRequest) {

        ChangeMappingSetNameResponse response = mappingService.changeMappingSetName(request, mappingId, nameRequest);

        return ResponseEntity.ok(ApiResponse.<ChangeMappingSetNameResponse>builder()
                .isSuccess(true)
                .code("COMMON200")
                .message("매핑셋 이름 변경 성공")
                .data(response)
                .build());
    }

    @DeleteMapping("/{mappingId}/delete-item/{mappingItemId}")
    public ResponseEntity<DeleteMappingItemResponse> deleteMappingItem(
            @PathVariable Long mappingId,
            @PathVariable Long mappingItemId) {

        Long deletedItemId = mappingService.deleteMappingItem(mappingId, mappingItemId);

        return ResponseEntity.ok(DeleteMappingItemResponse.success(deletedItemId));
    }

}