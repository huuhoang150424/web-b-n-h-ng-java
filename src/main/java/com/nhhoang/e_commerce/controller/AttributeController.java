package com.nhhoang.e_commerce.controller;

import com.nhhoang.e_commerce.dto.Enum.Role;
import com.nhhoang.e_commerce.dto.requests.CreateAttributeRequest;
import com.nhhoang.e_commerce.dto.requests.UpdateAttributeRequest;
import com.nhhoang.e_commerce.dto.response.AttributeResponse;
import com.nhhoang.e_commerce.entity.Attributes;
import com.nhhoang.e_commerce.entity.User;
import com.nhhoang.e_commerce.service.AttributeService;
import com.nhhoang.e_commerce.utils.Api.ErrorResponse;
import com.nhhoang.e_commerce.utils.Api.SuccessResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/attribute")
public class AttributeController {

    @Autowired
    private AttributeService attributeService;

    @PostMapping("/createAttribute")
    public ResponseEntity<?> createAttribute(@Valid @RequestBody CreateAttributeRequest request,
                                             BindingResult bindingResult,
                                             HttpServletRequest httpRequest) {
        try {
            User currentUser = (User) httpRequest.getAttribute("user");
            if (currentUser == null) {
                return ResponseEntity.status(403).body(new ErrorResponse("Bạn cần đăng nhập"));
            }
            if (!currentUser.getRole().equals(Role.ADMIN)) {
                return ResponseEntity.status(403).body(new ErrorResponse("Chỉ ADMIN mới có quyền truy cập"));
            }

            if (bindingResult.hasErrors()) {
                Map<String, String> errors = new HashMap<>();
                bindingResult.getFieldErrors().forEach(error ->
                        errors.put(error.getField(), error.getDefaultMessage()));
                return ResponseEntity.badRequest().body(new ErrorResponse("Tạo thư mục mới thất bại", errors));
            }

            attributeService.createAttribute(request);

            Map<String, Object> result = new HashMap<>();
            result.put("message", "Tạo thuộc tính mới thành công");

            return ResponseEntity.status(201).body(new SuccessResponse("Thành công", result));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(new ErrorResponse("Thuộc tính này đã tồn tại"));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(new ErrorResponse("Lỗi server: " + e.getMessage()));
        }
    }

    @PutMapping("/updateAttribute/{id}")
    public ResponseEntity<?> updateAttribute(@PathVariable String id,
                                             @Valid @RequestBody UpdateAttributeRequest request,
                                             BindingResult bindingResult,
                                             HttpServletRequest httpRequest) {
        try {
            User currentUser = (User) httpRequest.getAttribute("user");
            if (currentUser == null) {
                return ResponseEntity.status(403).body(new ErrorResponse("Bạn cần đăng nhập"));
            }
            if (!currentUser.getRole().equals(Role.ADMIN)) {
                return ResponseEntity.status(403).body(new ErrorResponse("Chỉ ADMIN mới có quyền truy cập"));
            }

            if (bindingResult.hasErrors()) {
                Map<String, String> errors = new HashMap<>();
                bindingResult.getFieldErrors().forEach(error ->
                        errors.put(error.getField(), error.getDefaultMessage()));
                return ResponseEntity.badRequest().body(new ErrorResponse("Cập nhật thuộc tính thất bại", errors));
            }

            attributeService.updateAttribute(id, request);

            Map<String, Object> result = new HashMap<>();
            result.put("message", "Cập nhật thuộc tính thành công");

            return ResponseEntity.status(201).body(new SuccessResponse("Thành công", result));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(404).body(new ErrorResponse("Thuộc tính này không tồn tại"));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(new ErrorResponse("Lỗi server: " + e.getMessage()));
        }
    }

    @GetMapping("/getAllAttribute")
    public ResponseEntity<?> getAllAttributes(@RequestParam(required = false) Integer page,
                                              @RequestParam(required = false) Integer size,
                                              HttpServletRequest httpRequest) {
        try {
            User currentUser = (User) httpRequest.getAttribute("user");
            if (currentUser == null) {
                return ResponseEntity.status(403).body(new ErrorResponse("Bạn cần đăng nhập"));
            }
            if (!currentUser.getRole().equals(Role.ADMIN)) {
                return ResponseEntity.status(403).body(new ErrorResponse("Chỉ ADMIN mới có quyền truy cập"));
            }

            Map<String, Object> result = new HashMap<>();
            List<AttributeResponse> attributeData;

            if (page == null && size == null) {
                attributeData = attributeService.getAllAttributes().stream()
                        .map(this::mapToAttributeResponse)
                        .collect(Collectors.toList());

                result.put("totalItems", attributeData.size());
                result.put("currentPage", 1);
                result.put("totalPages", 1);
                result.put("pageSize", attributeData.size());
                result.put("data", attributeData);
            } else {
                int pageNum = page != null ? page - 1 : 0;
                int pageSize = size != null ? size : 10;
                if (pageSize > 100) pageSize = 100;

                Page<Attributes> attributePage = attributeService.getAllAttributesPaginated(pageNum, pageSize);
                attributeData = attributePage.getContent().stream()
                        .map(this::mapToAttributeResponse)
                        .collect(Collectors.toList());

                result.put("totalItems", attributePage.getTotalElements());
                result.put("currentPage", attributePage.getNumber() + 1); // Chuyển về 1-based
                result.put("totalPages", attributePage.getTotalPages());
                result.put("pageSize", attributePage.getSize());
                result.put("data", attributeData);
            }

            return ResponseEntity.ok(new SuccessResponse("Thành công", result));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(new ErrorResponse("Lỗi server: " + e.getMessage()));
        }
    }

    private AttributeResponse mapToAttributeResponse(Attributes attribute) {
        AttributeResponse response = new AttributeResponse();
        response.setId(attribute.getId());
        response.setAttributeName(attribute.getAttributeName());
        return response;
    }



    @DeleteMapping("/deleteAttribute/{id}")
    public ResponseEntity<?> deleteAttribute(@PathVariable String id, HttpServletRequest httpRequest) {
        try {
            User currentUser = (User) httpRequest.getAttribute("user");
            if (currentUser == null) {
                return ResponseEntity.status(403).body(new ErrorResponse("Bạn cần đăng nhập"));
            }
            if (!currentUser.getRole().equals(Role.ADMIN)) {
                return ResponseEntity.status(403).body(new ErrorResponse("Chỉ ADMIN mới có quyền truy cập"));
            }

            attributeService.deleteAttribute(id);

            Map<String, Object> result = new HashMap<>();
            result.put("message", "Xóa thuộc tính thành công");

            return ResponseEntity.ok(new SuccessResponse("Thành công", result));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(404).body(new ErrorResponse("Thuộc tính không tồn tại"));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(new ErrorResponse("Lỗi server: " + e.getMessage()));
        }
    }
}