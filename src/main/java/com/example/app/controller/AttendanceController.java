package com.example.app.controller;

import com.example.app.entity.Attendance;
import com.example.app.service.AttendanceService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/attendance")
@Tag(name = "Attendance", description = "Member check-in / check-out tracking")
public class AttendanceController {

  @Autowired private AttendanceService attendanceService;

  @PostMapping("/member/{memberId}/check-in")
  @Operation(summary = "Check in a member")
  public ResponseEntity<Attendance> checkIn(@PathVariable Long memberId) {
    return ResponseEntity.ok(attendanceService.checkIn(memberId));
  }

  @PostMapping("/member/{memberId}/check-out")
  @Operation(summary = "Check out a member")
  public ResponseEntity<Attendance> checkOut(@PathVariable Long memberId) {
    return ResponseEntity.ok(attendanceService.checkOut(memberId));
  }

  @GetMapping("/today")
  @Operation(summary = "Get today's attendance (paginated)")
  public ResponseEntity<Page<Attendance>> today(@PageableDefault(size = 20) Pageable pageable) {
    return ResponseEntity.ok(attendanceService.today(pageable));
  }

  @GetMapping("/member/{memberId}")
  @Operation(summary = "Get a member's attendance history (paginated)")
  public ResponseEntity<Page<Attendance>> history(
      @PathVariable Long memberId, @PageableDefault(size = 20) Pageable pageable) {
    return ResponseEntity.ok(attendanceService.history(memberId, pageable));
  }
}
